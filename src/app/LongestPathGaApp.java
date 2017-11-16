package app;

import app.models.Edge;
import app.models.Node;
import app.models.TspFile;
import app.models.LifeForm;
import app.utilities.TspFileParser;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class LongestPathGaApp extends Application {

    private static String FILE_PATH = "/home/sam/Documents/CECS545/Project 5/Random222.tsp";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // set path to our fxml file
        Parent root = FXMLLoader.load(getClass().getResource("LongestPathGaApp.fxml"));

        // define size when creating new scene
        Scene scene = new Scene(root, 1500, 1000);

        // start JavaFx application
        primaryStage.setTitle("Tsp Application");
        primaryStage.setScene(scene);
        primaryStage.show();

        // read file info
        File file = new File(FILE_PATH);
        TspFile tspFile = TspFileParser.parseTspFile(file);

        // populate list of nodes
        List<Node> nodes = new ArrayList<>();
        if (tspFile != null && tspFile.getCoordinates() != null) {
            for (Double[] coordinates : tspFile.getCoordinates()) {
                nodes.add(new Node(coordinates[0], coordinates[1], coordinates[2]));
            }
        }

        //===============================================
        // Set up variables for genetic algorithm
        //===============================================
        Integer maxPopulationSize = 200;
        Integer experimentLength = 1000;
        Integer cycleCount = 0;
        Integer mutationsOf1000 = 150;

        // current population
        List<LifeForm> population = new ArrayList<>();
        Double popAvgDistance = null;
        Double popMaxDistance = null;
        Double popMinDistance = null;

        // overall experiment statistics
        List<Double> expAverages = new ArrayList<>();
        List<Double> expStdDeviations = new ArrayList<>();
        ArrayList<Integer> avgChartDataPoints = new ArrayList<>();
        ArrayList<Integer> worstChartDataPoints = new ArrayList<>();
        ArrayList<Integer> bestChartDataPoints = new ArrayList<>();
        LifeForm expBestLifeForm = null;
        LifeForm expWorstLifeForm = null;

        //===============================================
        // Get initial population using greedy algorithm
        //===============================================
        if (maxPopulationSize > nodes.size()) {
            maxPopulationSize = nodes.size();
        }
        for (int initialNodeIndex = 0; initialNodeIndex < maxPopulationSize; initialNodeIndex++) {
            // begin to build path
            List<Node> unvisitedNodes = new ArrayList<>(nodes);
            ObservableList<Node> path = FXCollections.observableArrayList();

            // add the starting node
            Node startingNode = unvisitedNodes.get(initialNodeIndex);
            path.add(startingNode);
            unvisitedNodes.remove(startingNode);
            Node nextNode = startingNode;

            while (unvisitedNodes.size() != 0) {
                nextNode = getNextNode(unvisitedNodes, nextNode);
                // add our nextNode into the path using greedy pick
                simpleGreedyPick(path, nextNode);
                // remove the node we added to path
                unvisitedNodes.remove(nextNode);
            }

            // add path to population
            population.add(new LifeForm(path, getTotalDistanceOfPath(path)));
        }

        //===============================================
        // Begin cycles
        //===============================================
        while (cycleCount < experimentLength) {
            //-----------------------------------------------
            // Calculate statistics
            //-----------------------------------------------
            popMaxDistance = null;
            popMinDistance = null;
            popAvgDistance = 0.0;

            for (LifeForm lifeForm : population) {
                // calculate total distance for all new life forms
                if (lifeForm.getTotalDistance() == null) {
                    lifeForm.setTotalDistance(getTotalDistanceOfPath(lifeForm.getPath()));
                }
                // track populations minimum distance
                if (popMinDistance == null || lifeForm.getTotalDistance() < popMinDistance) {
                    popMinDistance = lifeForm.getTotalDistance();
                    // keep track of best overall life form in experiment
                    if (expBestLifeForm == null || popMinDistance < expBestLifeForm.getTotalDistance()) {
                        expBestLifeForm = lifeForm;
                    }
                }
                // track populations maximum distance
                if (popMaxDistance == null || lifeForm.getTotalDistance() > popMaxDistance) {
                    popMaxDistance = lifeForm.getTotalDistance();
                    // keep track of worst overall life form in experiment
                    if (expWorstLifeForm == null || popMaxDistance > expWorstLifeForm.getTotalDistance()) {
                        expWorstLifeForm = lifeForm;
                    }
                }
                popAvgDistance += lifeForm.getTotalDistance();
            }

            // graphing data
            popAvgDistance = popAvgDistance / population.size();
            avgChartDataPoints.add(Math.toIntExact(Math.round(popAvgDistance)));
            bestChartDataPoints.add(Math.toIntExact(Math.round(popMinDistance)));
            worstChartDataPoints.add(Math.toIntExact(Math.round(popMaxDistance)));
            if (expAverages.size() > 1) {
                expStdDeviations.add(Math.abs(popAvgDistance - expAverages.get(expAverages.size() - 1)));
            }
            expAverages.add(popAvgDistance);

            //-----------------------------------------------
            // Kill
            //-----------------------------------------------
            Double pathCostToBeat = null;
            while (population.size() > maxPopulationSize / 2) {
                // find who of the population will be receiving my judgement
                int indexNum = ThreadLocalRandom.current().nextInt(0, population.size());
                LifeForm target = population.get(indexNum);
                // set the path cost to beat if it hasn't already been set
                if (pathCostToBeat == null) {
                    pathCostToBeat = target.getTotalDistance();
                    continue;
                }
                // check if our target deserves to live
                if (target.getTotalDistance() <= pathCostToBeat) {
                    population.remove(target);
                }
                pathCostToBeat = target.getTotalDistance();
            }

            //-----------------------------------------------
            // Breed
            //-----------------------------------------------
            List<LifeForm> babies = new ArrayList<>();
            while (population.size() + babies.size() < maxPopulationSize) {
                LifeForm parentX = population.get(0);
                LifeForm parentY = population.get(1);
                // find best possible parents
                for (LifeForm possibleParent : population) {
                    if (possibleParent == parentX || possibleParent == parentY) {
                        continue;
                    }
                    if (possibleParent.getTotalDistance() > parentX.getTotalDistance()) {
                        parentX = possibleParent;
                    }
                    if (possibleParent.getTotalDistance() > parentY.getTotalDistance()) {
                        parentY = possibleParent;
                    }
                }
                LifeForm baby = breedLifeForms(parentX, parentY);
                babies.add(baby);
            }
            population.addAll(babies);

            //-----------------------------------------------
            // Mutate
            //-----------------------------------------------
            for (int i = 0; i < population.size(); i++) {
                LifeForm lifeForm = population.get(i);
                Integer random = ThreadLocalRandom.current().nextInt(1,1001);
                if (random < mutationsOf1000) {
                    LifeForm mutatedLifeForm = mutateLifeForm3WayMix(lifeForm);
                    population.set(i, mutatedLifeForm);
                }
            }

            cycleCount++;
        }


        // draw best path overall
        Controller.drawPath(expWorstLifeForm.getPath(), Color.BLACK);

        // graph avg, best, and worst path's over time
        Integer lowerBound = Math.toIntExact(Math.round(expBestLifeForm.getTotalDistance() - 10));
        Integer upperBound = Math.toIntExact(Math.round(expWorstLifeForm.getTotalDistance() + 10));
        Controller.graphLineChart(lowerBound, upperBound, avgChartDataPoints, bestChartDataPoints, worstChartDataPoints);

        // list properties for GA
        List<String> propertyList = new ArrayList<>();
        propertyList.add("Length of Experiment: " + experimentLength + " cycles");
        propertyList.add("Mutation Rate: approximately " + mutationsOf1000 + " mutations every thousand life forms");
        propertyList.add("Population Size: " + maxPopulationSize);
        propertyList.add("Longest path: " + expWorstLifeForm.getPath());
        propertyList.add("Longest path Cost: " + expWorstLifeForm.getTotalDistance());

        Double expAverage = 0.0;
        for (Double avg : expAverages) {
            expAverage += avg;
        }
        expAverage = expAverage / expAverages.size();
        propertyList.add("Experiment Average: " + expAverage);

        Double expStdDeviation = 0.0;
        for (Double stdDev : expStdDeviations) {
            expStdDeviation += stdDev;
        }
        expStdDeviation = expStdDeviation / expAverages.size();
        propertyList.add("Experiment Std Dev: " + expStdDeviation);

        System.out.println(expWorstLifeForm.getPath());
        Controller.setPropertyList(propertyList);
    }

    private LifeForm breedLifeForms(LifeForm parentX, LifeForm parentY) {
        List<Node> pathX = new ArrayList<>(parentX.getPath());
        List<Node> pathY = new ArrayList<>(parentY.getPath());

        LifeForm baby = new LifeForm();
        List<Node> babyPath = new ArrayList<>();

        Integer numberOfNodesFromX = 2;
        Integer randomIndex = ThreadLocalRandom.current().nextInt(0, pathX.size());

        for (int i = 0; i < numberOfNodesFromX; i++) {
            if (randomIndex >= pathX.size()) {
                randomIndex = 0;
            }
            Node node = pathX.get(randomIndex);
            // add node to baby
            babyPath.add(node);
            // remove node from parentY to avoid overlap
            pathY.remove(node);
            randomIndex++;
        }
        // add rest of path from parent y
        babyPath.addAll(pathY);
        baby.setPath(babyPath);
        return baby;
    }

    private LifeForm mutateLifeFormSwap2(LifeForm target) {
        List<Node> path = new ArrayList<>(target.getPath());
        Integer indexA = ThreadLocalRandom.current().nextInt(0, path.size());
        Integer indexB = indexA + 1;
        if (indexB >= path.size()) {
            indexB = 0;
        }

        Node nodeA = path.get(indexA);
        Node nodeB = path.get(indexB);

        //swap A and B
        path.set(indexA, nodeB);
        path.set(indexB, nodeA);

        LifeForm mutation = new LifeForm();
        mutation.setPath(path);

        return mutation;
    }

    private LifeForm mutateLifeForm3WayMix(LifeForm target) {
        List<Node> path = new ArrayList<>(target.getPath());
        Integer index1 = ThreadLocalRandom.current().nextInt(0, path.size());
        Integer index2 = index1 + 1;
        if (index2 >= path.size()) {
            index2 = 0;
        }
        Integer index3 = index2 + 1;
        if (index3 >= path.size()) {
            index3 = 0;
        }

        List<Node> nodesForSwap = new ArrayList<>();
        Node node1 = path.get(index1);
        Node node2 = path.get(index2);
        Node node3 = path.get(index3);
        nodesForSwap.add(node1);
        nodesForSwap.add(node2);
        nodesForSwap.add(node3);

        // assign random node to spot number 1;
        Integer randomNodeIndex = ThreadLocalRandom.current().nextInt(0, nodesForSwap.size());
        Node randomNode = nodesForSwap.get(randomNodeIndex);
        path.set(index1, randomNode);
        nodesForSwap.remove(randomNode);
        // to spot number 2
        randomNodeIndex = ThreadLocalRandom.current().nextInt(0, nodesForSwap.size());
        randomNode = nodesForSwap.get(randomNodeIndex);
        path.set(index2, randomNode);
        nodesForSwap.remove(randomNode);
        // to spot number 3
        randomNodeIndex = ThreadLocalRandom.current().nextInt(0, nodesForSwap.size());
        randomNode = nodesForSwap.get(randomNodeIndex);
        path.set(index3, randomNode);
        nodesForSwap.remove(randomNode);

        LifeForm mutation = new LifeForm();
        mutation.setPath(path);
        return mutation;
    }

    private Node getNextNode(List<Node> nodes, Node previousNode) {
        Node nearestNode = null;
        Double nearestDistance = null;
        for (Node node : nodes) {
            Double distance = calculateDistance(previousNode.getxCoord(), node.getxCoord(), previousNode.getyCoord(), node.getyCoord());
            if (nearestDistance == null || distance < nearestDistance) {
                nearestDistance = distance;
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    private void simpleGreedyPick(ObservableList<Node> path, Node nextNode) {
        // skip finding an edge when none exist
        if (path.size() == 1 || path.size() == 0) {
            path.add(nextNode);
            return;
        }

        // find all edges
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            // last node makes edge with first node
            if (i == path.size() - 1) {
                Edge edge = new Edge(path.get(i), path.get(0));
                edges.add(edge);
            } else {
                Edge edge = new Edge(path.get(i), path.get(i + 1));
                edges.add(edge);
            }
        }

        // find the closest edge
        Edge closestEdge = null;
        Double closestEdgeDistance = null;
        for (Edge edge :  edges) {
            Double edgeDistance = edge.getDistanceToEdge(nextNode);
            if (closestEdgeDistance == null || edgeDistance < closestEdgeDistance) {
                closestEdgeDistance = edgeDistance;
                closestEdge = edge;
            }
        }

        // insert nextNode into the path after n1 of the closest edge
        if (closestEdge != null) {
            Node n1 = closestEdge.getN1();
            Integer n1Index = path.indexOf(n1);
            path.add(n1Index, nextNode);
        }
    }

    private ArrayList<Node> buildRandomPath(ArrayList<Node> nodes) {
        return null;
    }

    private static Double getTotalDistanceOfPath(List<Node> path) {
        Double totalDistance = 0.0;
        for (int i = 0; i < path.size(); i++) {
            Double x1 = path.get(i).getxCoord();
            Double x2 = null;
            Double y1 = path.get(i).getyCoord();
            Double y2 = null;
            // last node makes edge with first node
            if (i == path.size() - 1) {
                x2 = path.get(0).getxCoord();
                y2 = path.get(0).getyCoord();
            } else {
                x2 = path.get(i + 1).getxCoord();
                y2 = path.get(i + 1).getyCoord();
            }
            totalDistance += calculateDistance(x1, x2, y1, y2);
        }
        return totalDistance;
    }

    private static Double calculateDistance(Double x1, Double x2, Double y1, Double y2) {
        return Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
    }
}
