package app.models;

import java.util.ArrayList;
import java.util.List;

public class LifeForm {
    private List<Node> path;
    private Double totalDistance;

    public LifeForm() {
        path = new ArrayList<>();
    }

    public LifeForm(List<Node> path, Double distance) {
        this.path = path;
        this.totalDistance = distance;
    }

    public List<Node> getPath() {
        return path;
    }

    public void setPath(List<Node> route) {
        this.path = route;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }
}
