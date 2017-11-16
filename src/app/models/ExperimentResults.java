package app.models;

import java.util.ArrayList;
import java.util.List;

public class ExperimentResults {
    private List<Double> expAverages;
    private Double expAverage;
    private List<Double> expStdDeviations;
    private Double expStdDeviation;
    private ArrayList<Integer> avgChartDataPoints;
    private ArrayList<Integer> longestChartDataPoints;
    private ArrayList<Integer> shortestChartDataPoints;
    private List<Node> shortestPath;
    private List<Node> longestPath;
    private Integer shortestPathCost;
    private Integer longestPathCost;
    private LifeForm expShortestLifeForm;
    private LifeForm expLongestLifeForm;

    public ExperimentResults() {
        expAverages = new ArrayList<>();
        expStdDeviations = new ArrayList<>();
        avgChartDataPoints = new ArrayList<>();
        longestChartDataPoints = new ArrayList<>();
        shortestChartDataPoints = new ArrayList<>();
    }

    public List<Double> getExpAverages() {
        return expAverages;
    }

    public void setExpAverages(List<Double> expAverages) {
        this.expAverages = expAverages;
    }

    public List<Double> getExpStdDeviations() {
        return expStdDeviations;
    }

    public void setExpStdDeviations(List<Double> expStdDeviations) {
        this.expStdDeviations = expStdDeviations;
    }

    public ArrayList<Integer> getAvgChartDataPoints() {
        return avgChartDataPoints;
    }

    public void setAvgChartDataPoints(ArrayList<Integer> avgChartDataPoints) {
        this.avgChartDataPoints = avgChartDataPoints;
    }

    public ArrayList<Integer> getLongestChartDataPoints() {
        return longestChartDataPoints;
    }

    public void setLongestChartDataPoints(ArrayList<Integer> longestChartDataPoints) {
        this.longestChartDataPoints = longestChartDataPoints;
    }

    public ArrayList<Integer> getShortestChartDataPoints() {
        return shortestChartDataPoints;
    }

    public void setShortestChartDataPoints(ArrayList<Integer> shortestChartDataPoints) {
        this.shortestChartDataPoints = shortestChartDataPoints;
    }

    public LifeForm getExpShortestLifeForm() {
        return expShortestLifeForm;
    }

    public void setExpShortestLifeForm(LifeForm expShortestLifeForm) {
        this.expShortestLifeForm = expShortestLifeForm;
    }

    public LifeForm getExpLongestLifeForm() {
        return expLongestLifeForm;
    }

    public void setExpLongestLifeForm(LifeForm expLongestLifeForm) {
        this.expLongestLifeForm = expLongestLifeForm;
    }

    public Double getExpAverage() {
        return expAverage;
    }

    public void setExpAverage(Double expAverage) {
        this.expAverage = expAverage;
    }

    public Double getExpStdDeviation() {
        return expStdDeviation;
    }

    public void setExpStdDeviation(Double expStdDeviation) {
        this.expStdDeviation = expStdDeviation;
    }

    public List<Node> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(List<Node> shortestPath) {
        this.shortestPath = shortestPath;
    }

    public Integer getShortestPathCost() {
        return shortestPathCost;
    }

    public void setShortestPathCost(Integer shortestPathCost) {
        this.shortestPathCost = shortestPathCost;
    }

    public List<Node> getLongestPath() {
        return longestPath;
    }

    public void setLongestPath(List<Node> longestPath) {
        this.longestPath = longestPath;
    }

    public Integer getLongestPathCost() {
        return longestPathCost;
    }

    public void setLongestPathCost(Integer longestPathCost) {
        this.longestPathCost = longestPathCost;
    }
}
