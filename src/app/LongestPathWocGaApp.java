package app;

import app.models.*;
import app.utilities.*;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LongestPathWocGaApp extends Application {

    private static String FILE_PATH = "/home/sam/Documents/CECS545/Project 5/Random11.tsp";


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // set path to our fxml file
        Parent root = FXMLLoader.load(getClass().getResource("LongestPathWocGaApp.fxml"));

        // define size when creating new scene
        Scene scene = new Scene(root, 1500, 1000);

        // start JavaFx application
        primaryStage.setTitle("Tsp Application");
        primaryStage.setScene(scene);
        primaryStage.show();

        // read file info
        //File file = new File(FILE_PATH);
        //TspFile tspFile = TspFileParser.parseTspFile(file);

        // populate list of nodes
        List<Node> nodes = new ArrayList<>();
        /*if (tspFile != null && tspFile.getCoordinates() != null) {
            for (Double[] coordinates : tspFile.getCoordinates()) {
                nodes.add(new Node(coordinates[0], coordinates[1], coordinates[2]));
            }
        }*/
        for (Double[] coordinates : GraphGenerator.generateGraph(45)) {
            nodes.add(new Node(coordinates[0], coordinates[1], coordinates[2]));
        }

        //===============================================
        // Set up variables for genetic algorithm
        //===============================================
        Integer maxPopulationSize = 100;
        Integer experimentLength = 1500;
        Integer mutationsOf1000 = 250;
        Integer numberOfExperts = 100;

        // Run genetic experiment, get the results
        ArrayList<ExperimentResults> experts = new ArrayList<>();

        // Run GA experiments to generate experts
        for (int i = 0; i < numberOfExperts; i++) {
            experts.add(GeneticExperiment.runExperimentForLongestPath(nodes, maxPopulationSize, experimentLength, mutationsOf1000));
        }

        // Aggregate opinions into a wisdom of the crowds solution
        List<Node> wisdomPath = WisdomPathBuilder.buildWisdomPath(nodes, experts);

        //===============================================
        // Graph data
        //===============================================
        ExperimentResults longestExpert = experts.get(0);
        Double averageExpert = 0.0;
        for (ExperimentResults expert : experts) {
            int expertPathCost = expert.getLongestPathCost();
            if (expertPathCost > longestExpert.getLongestPathCost()) {
                longestExpert = expert;
            }
            averageExpert += expertPathCost;
        }
        averageExpert = averageExpert/experts.size();
        Double wisdomPathCost = PathMather.getTotalDistanceOfPath(wisdomPath);
        Double longestExpertPathCost = PathMather.getTotalDistanceOfPath(longestExpert.getLongestPath());

        Controller.graphPathToBarChart(Math.toIntExact(Math.round(longestExpertPathCost)), "Longest expert path");
        Controller.graphPathToBarChart(Math.toIntExact(Math.round(averageExpert)), "Average expert path");
        Controller.graphPathToBarChart(Math.toIntExact(Math.round(wisdomPathCost)), "Wisdom path");

        // drawing
        Controller.drawPath(wisdomPath, Color.BLACK);

        // list properties for GA

        List<String> propertyList = new ArrayList<>();
        propertyList.add("Wisdom Path: " + wisdomPath);
        propertyList.add("Wisdom Path Size: " + wisdomPath.size());
        propertyList.add("Cost of Wisdom Path: " + wisdomPathCost);
        propertyList.add("Cost of longest expert path: " + longestExpertPathCost);
        propertyList.add("Length of Experiment: " + experimentLength + " cycles");
        propertyList.add("Mutation Rate: approximately " + mutationsOf1000 + " mutations every thousand life forms");
        propertyList.add("Population Size: " + maxPopulationSize);

        System.out.println("Wisdom cost: " + wisdomPathCost);
        System.out.println("Longest expert cost: " + longestExpert.getLongestPathCost());
        System.out.println("Average expert cost: " + averageExpert);


        Controller.setPropertyList(propertyList);
    }
}
