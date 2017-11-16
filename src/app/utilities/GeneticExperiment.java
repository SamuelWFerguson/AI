package app.utilities;

import app.models.Edge;
import app.models.ExperimentResults;
import app.models.LifeForm;
import app.models.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticExperiment {

    private static int maxPopulationSize;
    private static int mutationsOf1000;

    public static ExperimentResults runExperimentForLongestPath(List<Node> nodes, int maxPopSize, int expLength, int mutationRate) {
        ExperimentResults results = new ExperimentResults();

        //===============================================
        // Set up variables for genetic algorithm
        //===============================================
        maxPopulationSize = maxPopSize;
        mutationsOf1000 = mutationRate;
        Integer experimentLength = expLength;
        Integer cycleCount = 0;

        // current population
        List<LifeForm> population = new ArrayList<>();
        Double popAvgDistance;
        Double popMaxDistance;
        Double popMinDistance;

        // overall experiment statistics
        List<Double> expAverages = new ArrayList<>();
        List<Double> expStdDeviations = new ArrayList<>();
        ArrayList<Integer> avgChartDataPoints = new ArrayList<>();
        ArrayList<Integer> longestChartDataPoints = new ArrayList<>();
        LifeForm expLongestLifeForm = null;
        List<Node> expLongestPath = new ArrayList<>();

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
            population.add(new LifeForm(path, PathMather.getTotalDistanceOfPath(path)));
        }

        //===============================================
        // Begin experiment cycles
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
                    lifeForm.setTotalDistance(PathMather.getTotalDistanceOfPath(lifeForm.getPath()));
                }
                // track populations longest distance
                if (popMaxDistance == null || lifeForm.getTotalDistance() > popMaxDistance) {
                    popMaxDistance = lifeForm.getTotalDistance();
                }
                if (popMinDistance == null || lifeForm.getTotalDistance() < popMinDistance) {
                    popMinDistance = lifeForm.getTotalDistance();
                    // keep track of longest distance life form in experiment
                    if (expLongestLifeForm == null || popMaxDistance > expLongestLifeForm.getTotalDistance()) {
                        expLongestLifeForm = lifeForm;
                        expLongestPath = lifeForm.getPath();

                    }
                }

                popAvgDistance += lifeForm.getTotalDistance();
            }

            // Population specific data
            popAvgDistance = popAvgDistance / population.size();
            if (expAverages.size() > 1) {
                expStdDeviations.add(Math.abs(popAvgDistance - expAverages.get(expAverages.size() - 1)));
            }
            expAverages.add(popAvgDistance);
            avgChartDataPoints.add(Math.toIntExact(Math.round(popAvgDistance)));
            longestChartDataPoints.add(Math.toIntExact(Math.round(popMaxDistance)));

            //-----------------------------------------------
            // Kill
            //-----------------------------------------------
            killShortestPaths(population);

            //-----------------------------------------------
            // Breed
            //-----------------------------------------------
            breedLongestPaths(population);

            //-----------------------------------------------
            // Mutate
            //-----------------------------------------------
            mutatePaths(population);

            cycleCount++;
        }

        //-----------------------------------------------
        // Calculate some experiment statistics
        //-----------------------------------------------
        Double expAverage = 0.0;
        for (Double avg : expAverages) {
            expAverage += avg;
        }
        expAverage = expAverage / expAverages.size();

        Double expStdDeviation = 0.0;
        for (Double stdDev : expStdDeviations) {
            expStdDeviation += stdDev;
        }
        expStdDeviation = expStdDeviation / expAverages.size();

        //-----------------------------------------------
        // Populate experiment results object
        //-----------------------------------------------
        results.setAvgChartDataPoints(avgChartDataPoints);
        results.setLongestChartDataPoints(longestChartDataPoints);
        results.setExpAverages(expAverages);
        results.setExpAverage(expAverage);
        results.setExpStdDeviations(expStdDeviations);
        results.setExpStdDeviation(expStdDeviation);
        results.setLongestPath(expLongestPath);
        results.setExpLongestLifeForm(expLongestLifeForm);
        results.setLongestPathCost((int) Math.round(PathMather.getTotalDistanceOfPath(expLongestPath)));

        return results;
    }

    public static ExperimentResults runExperiment(List<Node> nodes, int maxPopSize, int expLength, int mutationRate) {
        ExperimentResults results = new ExperimentResults();

        //===============================================
        // Set up variables for genetic algorithm
        //===============================================
        maxPopulationSize = maxPopSize;
        mutationsOf1000 = mutationRate;
        Integer experimentLength = expLength;
        Integer cycleCount = 0;

        // current population
        List<LifeForm> population = new ArrayList<>();
        Double popAvgDistance;
        Double popMaxDistance;
        Double popMinDistance;

        // overall experiment statistics
        List<Double> expAverages = new ArrayList<>();
        List<Double> expStdDeviations = new ArrayList<>();
        ArrayList<Integer> avgChartDataPoints = new ArrayList<>();
        ArrayList<Integer> worstChartDataPoints = new ArrayList<>();
        ArrayList<Integer> bestChartDataPoints = new ArrayList<>();
        LifeForm expBestLifeForm = null;
        List<Node> expBestPath = new ArrayList<>();
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
            population.add(new LifeForm(path, PathMather.getTotalDistanceOfPath(path)));
        }

        //===============================================
        // Begin experiment cycles
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
                    lifeForm.setTotalDistance(PathMather.getTotalDistanceOfPath(lifeForm.getPath()));
                }
                // track populations minimum distance
                if (popMinDistance == null || lifeForm.getTotalDistance() < popMinDistance) {
                    popMinDistance = lifeForm.getTotalDistance();
                    // keep track of best overall life form in experiment
                    if (expBestLifeForm == null || popMinDistance < expBestLifeForm.getTotalDistance()) {
                        expBestLifeForm = lifeForm;
                        expBestPath = lifeForm.getPath();

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

            // Population specific data
            popAvgDistance = popAvgDistance / population.size();
            if (expAverages.size() > 1) {
                expStdDeviations.add(Math.abs(popAvgDistance - expAverages.get(expAverages.size() - 1)));
            }
            expAverages.add(popAvgDistance);
            avgChartDataPoints.add(Math.toIntExact(Math.round(popAvgDistance)));
            bestChartDataPoints.add(Math.toIntExact(Math.round(popMinDistance)));
            worstChartDataPoints.add(Math.toIntExact(Math.round(popMaxDistance)));

            //-----------------------------------------------
            // Kill
            //-----------------------------------------------
            killLongestPaths(population);

            //-----------------------------------------------
            // Breed
            //-----------------------------------------------
            breedShortestPaths(population);

            //-----------------------------------------------
            // Mutate
            //-----------------------------------------------
            mutatePaths(population);

            cycleCount++;
        }

        //-----------------------------------------------
        // Calculate some experiment statistics
        //-----------------------------------------------
        Double expAverage = 0.0;
        for (Double avg : expAverages) {
            expAverage += avg;
        }
        expAverage = expAverage / expAverages.size();

        Double expStdDeviation = 0.0;
        for (Double stdDev : expStdDeviations) {
            expStdDeviation += stdDev;
        }
        expStdDeviation = expStdDeviation / expAverages.size();

        //-----------------------------------------------
        // Populate experiment results object
        //-----------------------------------------------
        results.setAvgChartDataPoints(avgChartDataPoints);
        results.setShortestChartDataPoints(bestChartDataPoints);
        results.setLongestChartDataPoints(worstChartDataPoints);
        results.setExpAverages(expAverages);
        results.setExpAverage(expAverage);
        results.setExpStdDeviations(expStdDeviations);
        results.setExpStdDeviation(expStdDeviation);
        results.setExpShortestLifeForm(expBestLifeForm);
        results.setShortestPath(expBestPath);
        results.setExpLongestLifeForm(expWorstLifeForm);
        results.setShortestPathCost((int) Math.round(PathMather.getTotalDistanceOfPath(expBestPath)));

        return results;
    }

    private static void killLongestPaths(List<LifeForm> population) {
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
            if (target.getTotalDistance() >= pathCostToBeat) {
                population.remove(target);
            }
            pathCostToBeat = target.getTotalDistance();
        }
    }

    private static void killShortestPaths(List<LifeForm> population) {
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
    }

    private static void  breedShortestPaths(List<LifeForm> population) {
        List<LifeForm> babies = new ArrayList<>();
        while (population.size() + babies.size() < maxPopulationSize) {
            LifeForm parentX = population.get(0);
            LifeForm parentY = population.get(1);
            // find best possible parents
            for (LifeForm possibleParent : population) {
                if (possibleParent == parentX || possibleParent == parentY) {
                    continue;
                }
                if (possibleParent.getTotalDistance() < parentX.getTotalDistance()) {
                    parentX = possibleParent;
                }
                if (possibleParent.getTotalDistance() < parentY.getTotalDistance()) {
                    parentY = possibleParent;
                }
            }
            LifeForm baby = breedLifeForms(parentX, parentY);
            babies.add(baby);
        }
        population.addAll(babies);
    }

    private static void breedLongestPaths(List<LifeForm> population) {
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
    }

    private static void mutatePaths(List<LifeForm> population) {
        for (int i = 0; i < population.size(); i++) {
            LifeForm lifeForm = population.get(i);
            Integer random = ThreadLocalRandom.current().nextInt(1,1001);
            if (random < mutationsOf1000) {
                LifeForm mutatedLifeForm = mutateLifeForm3WayMix(lifeForm);
                population.set(i, mutatedLifeForm);
            }
        }
    }

    private static LifeForm breedLifeForms(LifeForm parentX, LifeForm parentY) {
        List<Node> pathX = new ArrayList<>(parentX.getPath());
        List<Node> pathY = new ArrayList<>(parentY.getPath());

        LifeForm baby = new LifeForm();
        List<Node> babyPath = new ArrayList<>();

        Integer numberOfNodesFromX = 2;
        Integer randomIndex = ThreadLocalRandom.current().nextInt(0, pathX.size());

        // add nodes from parent x first
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
        // complete remaining path using parent y
        babyPath.addAll(pathY);
        baby.setPath(babyPath);
        return baby;
    }

    private static LifeForm mutateLifeFormSwap2(LifeForm target) {
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

    private static LifeForm mutateLifeForm3WayMix(LifeForm target) {
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

    private static Node getNextNode(List<Node> nodes, Node previousNode) {
        Node nearestNode = null;
        Double nearestDistance = null;
        for (Node node : nodes) {
            Double distance = PathMather.calculateDistance(previousNode.getxCoord(), node.getxCoord(), previousNode.getyCoord(), node.getyCoord());
            if (nearestDistance == null || distance < nearestDistance) {
                nearestDistance = distance;
                nearestNode = node;
            }
        }
        return nearestNode;
    }

    private static void simpleGreedyPick(ObservableList<Node> path, Node nextNode) {
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
}
