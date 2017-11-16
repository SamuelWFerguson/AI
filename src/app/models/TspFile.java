package app.models;

import java.util.ArrayList;
import java.util.List;

public class TspFile {

    private String name;
    private String type;
    private List<String> comments;
    private Integer dimension;
    private String edgeWeightType;
    private List<Double[]> coordinates;

    public TspFile() {
        comments = new ArrayList<>();
        coordinates = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getComments() {
        return comments;
    }

    public void addComment(String comment) {
        this.comments.add(comment);
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public String getEdgeWeightType() {
        return edgeWeightType;
    }

    public void setEdgeWeightType(String edgeWeightType) {
        this.edgeWeightType = edgeWeightType;
    }

    public List<Double[]> getCoordinates() {
        return coordinates;
    }

    public void addCoordinates(Double[] coordinate) {
        coordinates.add(coordinate);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
