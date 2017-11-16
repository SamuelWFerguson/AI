package app;
import app.models.ExperimentResults;
import app.models.Node;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.*;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Controller {

    private static GraphicsContext gc;
    private static ListView propertyList;
    private static BarChart<String, Number> costChart;
    private static LineChart<Number, Number> costOverTimeChart;
    private static NumberAxis chartYAxis;

    private final static String seriesName = "Path Cost";
    private final static String bestExpert = "best expert";
    private final static String worstExpert = "worst expert";
    private final static String averageExpert = "average expert";
    private final static String wisdomPath = "wisdom path";



    @FXML
    private Canvas canvas;
    @FXML
    private BarChart<String, Number> chart;
    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private ListView properties;

    @FXML
    void initialize() {
        gc = canvas.getGraphicsContext2D();
        propertyList = properties;
        costChart = chart;
        chartYAxis = yAxis;
        costOverTimeChart = lineChart;
    }

    static void graphPathToBarChart(Integer pathCost, String pathName) {
        XYChart.Series<String, Number> pathSeries = new XYChart.Series<>();
        pathSeries.getData().add(new XYChart.Data<>(pathName, pathCost));
        costChart.getData().add(pathSeries);

    }

    static void graphPathsToBarChart(ArrayList<Integer> pathCosts) {
        XYChart.Series<String, Number> pathSeries = new XYChart.Series<>();
        pathSeries.setName(seriesName);
        for (int i = 0; i < pathCosts.size(); i++) {
            Integer pathCost = pathCosts.get(i);
            switch(i) {
                case 0:
                    pathSeries.getData().add(new XYChart.Data<>(bestExpert, pathCost));
                    break;
                case 1:
                    pathSeries.getData().add(new XYChart.Data<>(worstExpert, pathCost));
                    break;
                case 2:
                    pathSeries.getData().add(new XYChart.Data<>(averageExpert, pathCost));
                    break;
                case 3:
                    pathSeries.getData().add(new XYChart.Data<>(wisdomPath, pathCost));
                    break;
                default:
                    break;
            }

        }
        costChart.getData().add(pathSeries);
    }

    static void setBarGraphBounds(Integer lowerBound, Integer upperBound) {
        chartYAxis.setAutoRanging(false);
        chartYAxis.setLowerBound(lowerBound);
        chartYAxis.setUpperBound(upperBound);
        chartYAxis.setTickUnit(100.0);
    }

    static void graphLineChart(Integer lowerBound, Integer upperBound, ArrayList<Integer> avgData, ArrayList<Integer> bestData, ArrayList<Integer> worstData) {
        // create avg series
        XYChart.Series avgSeries = new XYChart.Series<Integer, Integer>();
        avgSeries.setName("avg cost");
        for (int i = 0; i < avgData.size(); i++) {
            avgSeries.getData().add(new XYChart.Data<>(i + 1, avgData.get(i)));
        }
        /*// create best series
        XYChart.Series bestSeries = new XYChart.Series();
        bestSeries.setName("best path cost");
        for (int i = 0; i < bestData.size(); i++) {
            bestSeries.getData().add(new XYChart.Data<Integer, Integer>(i + 1, bestData.get(i)));
        }*/

        // create longest series
        XYChart.Series worstSeries = new XYChart.Series();
        worstSeries.setName("longest path cost");
        for (int i = 0; i < worstData.size(); i++) {
            worstSeries.getData().add(new XYChart.Data<Integer, Integer>(i + 1, worstData.get(i)));
        }

        costOverTimeChart.getData().addAll(avgSeries, worstSeries);
        chartYAxis.setAutoRanging(false);
        chartYAxis.setLowerBound(lowerBound);
        chartYAxis.setUpperBound(upperBound);
        chartYAxis.setTickUnit(100.0);
    }

    static void setPropertyList(List list) {
        ObservableList oList = FXCollections.observableArrayList(list);
        propertyList.setItems(oList);
    }

    static void drawNode(Node node) {
        gc.setFill(Color.BLACK);
        Double r = 10.0;
        Double x = (node.getxCoord() * 5) - r;
        Double y = (node.getyCoord() * 5) - r;
        gc.fillOval(x,y,r * 2,r * 2);
        gc.setFill(Color.RED);
        gc.fillText(node.getOrderNumber().toString(), x, y - 5);
    }

    static void drawLine(Node n1, Node n2, Color color) {
        gc.setLineWidth(3);
        gc.setStroke(color);
        gc.strokeLine(n1.getxCoord() * 5, n1.getyCoord() * 5, n2.getxCoord() * 5, n2.getyCoord() * 5);
    }

    static void drawPath(List<Node> path, Color color) {
        for (int i = 0; i < path.size(); i++) {
            drawNode(path.get(i));
            // last node connects back to starting node
            if (i == path.size() - 1) {
                drawLine(path.get(i), path.get(0), color);
            } else {
                drawLine(path.get(i), path.get(i + 1), color);
            }

        }
    }

    static void clearCanvas() {
        gc.clearRect(0,0,500,500);
    }
}
