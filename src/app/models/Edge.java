package app.models;

public class Edge {

    private Node n1;
    private Node n2;

    public Edge(Node node1, Node node2) {
        n1 = node1;
        n2 = node2;
    }

    public Node getN1() {
        return n1;
    }

    public void setN1(Node n1) {
        this.n1 = n1;
    }

    public Node getN2() {
        return n2;
    }

    public void setN2(Node n2) {
        this.n2 = n2;
    }

    public Double getDistanceToEdge(Node newNode) {
        Double d1 = calculateDistance(newNode.getxCoord(), n1.getxCoord(), newNode.getyCoord(), n1.getyCoord());
        Double d2 = calculateDistance(newNode.getxCoord(), n2.getxCoord(), newNode.getyCoord(), n2.getyCoord());
        return d1 + d2;
    }

    private static Double calculateDistance(Double x1, Double x2, Double y1, Double y2) {
        Double distance = Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
        return distance;
    }
}
