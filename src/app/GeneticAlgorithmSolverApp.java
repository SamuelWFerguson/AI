package app;

import app.models.Edge;
import app.models.Node;
import app.models.TspFile;
import app.models.LifeForm;
import app.utilities.GraphGenerator;
import app.utilities.PathMather;
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

public class GeneticAlgorithmSolverApp extends Application {
	
	//===============================================
    // Set up variables for experiment
    //===============================================
	public static final Integer NUMBER_OF_CITIES = 80;
    public static final Integer MAX_POPULATION_SIZE = 200;
    public static final Integer EXPERIMENT_LENGTH = 1500;
    public static final Integer MUTATIONS_OF_1000 = 150;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
    	
    	//===============================================
        // Set up JavaFX
        //===============================================
    	
        // set path to our fxml file
        Parent root = FXMLLoader.load(getClass().getResource("GeneticAlgorithmSolverApp.fxml"));

        // define size when creating new scene
        Scene scene = new Scene(root, 1500, 1000);

        // start JavaFx application
        primaryStage.setTitle("Tsp Application");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        //===============================================
        // Randomly generate each city
        //===============================================

        // generate coordinates of all nodes
        List<Double[]> generatedCoordinates = GraphGenerator.generateGraph(NUMBER_OF_CITIES);
        
        // populate list of nodes
        List<Node> nodes = new ArrayList<>();
        
        // create nodes from all coordinates and add thme to list
        for (Double[] coordinates : generatedCoordinates) {
            nodes.add(new Node(coordinates[0], coordinates[1], coordinates[2]));
        }
        
        //===============================================
        // Set up variables for genetic algorithm
        //===============================================

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
        
        // record time of experiment start
        long startingTime = System.nanoTime();

        //===============================================
        // Fill initial population using greedy algorithm
        //===============================================
        
        // fill population using a simple greedy algorithm
        for (int initialNodeIndex = 0; initialNodeIndex < NUMBER_OF_CITIES - 1; initialNodeIndex++) {
            // begin to build path
            List<Node> unvisitedNodes = new ArrayList<Node>(nodes);
            ObservableList<Node> path = FXCollections.observableArrayList();

            // visit the starting node
            Node startingNode = unvisitedNodes.get(initialNodeIndex);
            path.add(startingNode);
            unvisitedNodes.remove(startingNode);
            Node nextNode = startingNode;

            while (unvisitedNodes.size() != 0) {
                nextNode = getNearestNode(unvisitedNodes, nextNode);
                // add our nextNode into the path using greedy pick
                simpleGreedyPick(path, nextNode);
                // remove the node we added to path
                unvisitedNodes.remove(nextNode);
            }

            // add path to population
            population.add(new LifeForm(path, PathMather.getTotalDistanceOfPath(path)));
        }

        //===============================================
        // Begin cycles
        //===============================================
        
        for (int cycleCount = 0; cycleCount < EXPERIMENT_LENGTH; cycleCount++) {
        	
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

            // Calculate average path distance for population
            popAvgDistance = popAvgDistance / population.size();
            
            // add data points to each chart
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
            
            // reset variable used to ensure we do not kill the best lifeform
            Double pathDistanceToBeat = null;
            
            // kill off lifeforms until population is half the maximum size
            while (population.size() > MAX_POPULATION_SIZE / 2) {
            	
                // pick a random index anywhere in the population
                int indexNum = ThreadLocalRandom.current().nextInt(0, population.size());
                LifeForm target = population.get(indexNum);
                
                // set the path cost to beat if it hasn't already been set
                if (pathDistanceToBeat == null) {
                    pathDistanceToBeat = target.getTotalDistance();
                    continue;
                }
                
                // If our target is longer than or equal to our pathDistanceToBeat, kill the target.
                if (target.getTotalDistance() >= pathDistanceToBeat) {
                    population.remove(target);
                }
                
                // set new path to beat
                pathDistanceToBeat = target.getTotalDistance();
            }

            //-----------------------------------------------
            // Breed
            //-----------------------------------------------
            
            // create an empty list of babies which we will be adding onto 
            List<LifeForm> babies = new ArrayList<>();
            
            while (population.size() + babies.size() < MAX_POPULATION_SIZE) {
            	
            	// assign lifeforms 1 and 2 as parents
                LifeForm parentX = population.get(0);
                LifeForm parentY = population.get(1);
                
                // find better parents if possible
                for (LifeForm possibleParent : population) {
                	
                	// no need to check against current parent
                    if (possibleParent == parentX || possibleParent == parentY) {
                        continue;
                    }
                    
                    // assign new parent x and move on
                    if (possibleParent.getTotalDistance() < parentX.getTotalDistance()) {
                        parentX = possibleParent;
                        continue;
                    }
                    
                    // assign new parent y and move on
                    if (possibleParent.getTotalDistance() < parentY.getTotalDistance()) {
                        parentY = possibleParent;
                        continue;
                    }
                }
                
                // breed parents
                LifeForm baby = breedLifeForms(parentX, parentY);
                // add new baby to list of babies
                babies.add(baby);
            }
            
            // add all new babies into the population
            population.addAll(babies);

            //-----------------------------------------------
            // Mutate
            //-----------------------------------------------
            // for every single member of the population, give a chance to mutate
            Double newPopulationBestDistance = null;
            for (int i = 0; i < population.size(); i++) {
            	
            	// get target lifeform of this iteration
                LifeForm lifeForm = population.get(i);
                
                // do not mutate the best lifeform of the new population
                if (newPopulationBestDistance == null || PathMather.getTotalDistanceOfPath(lifeForm.getPath()) < newPopulationBestDistance ) {
                	
                	newPopulationBestDistance = PathMather.getTotalDistanceOfPath(lifeForm.getPath());
                	continue;
                }
                
                // generate a random number to determine if we have a mutation or not
                Integer random = ThreadLocalRandom.current().nextInt(1,1001);
                
                // if this number is less than our set mutations per 1000 lifeforms, mutate
                if (random < MUTATIONS_OF_1000) {
                	
                	LifeForm mutatedLifeForm;
                	
                	// randomly choose a mutation
                	Integer whichMutation = ThreadLocalRandom.current().nextInt(3);
                	
                	switch(whichMutation) {
                	
                	case 0:
                		mutatedLifeForm = mutate2WaySwap(lifeForm);
                		break;
                		
                	case 1:
                		mutatedLifeForm = mutate3WayMix(lifeForm);
                		break;
                		
                	case 2:
                		mutatedLifeForm = mutateIntersections(lifeForm);
                		break;
                		
                	default:
                		mutatedLifeForm = lifeForm;
                	}
                	
                	// add mutated lifeform back into the population
                    population.set(i, mutatedLifeForm);
                }
            }

            cycleCount++;
        }
        
        //===============================================
        // Display experiment results in application
        //===============================================
        
        // get total time taken by experiment
        Long totalTime = System.nanoTime() - startingTime;
        // format to amount in milliseconds as a string
        String totalTimeFormatted = String.format("%.0f", totalTime * Math.pow((double) 10, -6));


        // draw best path overall
        Controller.drawPath(expBestLifeForm.getPath(), Color.BLACK);

        // graph avg, best, and worst path's over time
        Integer lowerBound = Math.toIntExact(Math.round(expBestLifeForm.getTotalDistance() - 10));
        Integer upperBound = Math.toIntExact(Math.round(expWorstLifeForm.getTotalDistance() + 10));
        Controller.graphLineChart(lowerBound, upperBound, avgChartDataPoints, bestChartDataPoints, worstChartDataPoints);

        // list properties for Genetic Algorithm
        List<String> propertyList = new ArrayList<>();
        propertyList.add("Time taken to run experiment: " + totalTimeFormatted + " milliseconds");
        propertyList.add("Number of cycles: " + EXPERIMENT_LENGTH);
        propertyList.add("Mutation Rate: approximately " + MUTATIONS_OF_1000 + " mutations every thousand life forms");
        propertyList.add("Population Size: " + MAX_POPULATION_SIZE);
        propertyList.add("Number of cities: " + NUMBER_OF_CITIES);
        propertyList.add("Shortest path: " + expBestLifeForm.getPath());
        propertyList.add("Shortest path Cost: " + expBestLifeForm.getTotalDistance());

        // determine experiment average
        Double expAverage = 0.0;
        for (Double avg : expAverages) {
            expAverage += avg;
        }
        expAverage = expAverage / expAverages.size();
        propertyList.add("Experiment Average: " + expAverage);

        // determine experiment standard deviation
        Double expStdDeviation = 0.0;
        for (Double stdDev : expStdDeviations) {
            expStdDeviation += stdDev;
        }
        expStdDeviation = expStdDeviation / expAverages.size();
        propertyList.add("Experiment Std Dev: " + expStdDeviation);

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
    
    /**
     * randomly pick 2 lines in given lifeform's path to check for intersection
     * if an intersection is found, resolve it
     * 
     * @param target is the lifeform to mutate
     * @return the mutated target
     */
    private LifeForm mutateIntersections(LifeForm target) {
    	
    	List<Node> path = new ArrayList<>(target.getPath());
    	
    	// if path is not long enough for two paths to intersection it cannot have intersections
    	if (path.size() < 4) {
    		return target;
    	}
    	
    	// randomly pick the start of first line: line a
        Integer a1 = ThreadLocalRandom.current().nextInt(0, path.size());
        
        // find a2, the node after a1
        Integer a2 = a1 + 1;
        if (a2 >= path.size()) {
            a2 = 0;
        }
        
        // randomly pick the start of second line: line b
        Integer b1 = ThreadLocalRandom.current().nextInt(0, path.size());
        
        // find b2, the node after b1
        Integer b2 = b1 + 1;
        if (b2 >= path.size()) {
            b2 = 0;
        }
        
        // ensure that line b is not going to be the same line, or a line which shares a node with line a
        while (b1 == a1 || b1 == a2 || b2 == a1 || b2 == a2) {
        	// randomly pick new b1
        	b1 = ThreadLocalRandom.current().nextInt(0, path.size());
        	
        	// find new b2, the node that is connected to our new b1
        	b2 = b1 + 1;
            if (b2 >= path.size()) {
                b2 = 0;
            }
        }
        
        Node nodeA1 = path.get(a1);
        Node nodeA2 = path.get(a2);
        Node nodeB1 = path.get(b1);
        Node nodeB2 = path.get(b2);
        
    	// if there is an intersection: resolve it
        if (PathMather.doLinesIntersect(nodeA1, nodeA2, nodeB1, nodeB2)) {
        	
        	// resolve intersection
        	path.set(a1, nodeB1);
        	path.set(b1, nodeA1);
        	path.set(a2, nodeB2);
        	path.set(b2, nodeA2);
        }
    	
    	return target;
    }

    /**
     * randomly swap two nodes of a lifeform
     * 
     * @param target is the lifeform to mutate
     * @return the mutated target
     */
    private LifeForm mutate2WaySwap(LifeForm target) {
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

    /**
     * randomly mix up three nodes of a lifeform
     * 
     * @param target is the lifeform to mutate
     * @return the mutated target
     */
    private LifeForm mutate3WayMix(LifeForm target) {
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

    private Node getNearestNode(List<Node> nodes, Node previousNode) {
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
}
