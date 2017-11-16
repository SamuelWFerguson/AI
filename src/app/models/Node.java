package app.models;

public class Node {
    private Integer orderNumber;
    private Double xCoord;
    private Double yCoord;

    public Node(Integer orderNumber, Double xCoord, Double yCoord) {
        this.orderNumber = orderNumber;
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    public Node(Double orderNumber, Double xCoord, Double yCoord) {
        String orderNumberAsString = orderNumber.toString();
        orderNumberAsString = orderNumberAsString.substring(0, orderNumberAsString.length() - 2);
        this.orderNumber = Integer.parseInt(orderNumberAsString);
        this.xCoord = xCoord;
        this.yCoord = yCoord;
    }

    public String toString() {
        return orderNumber.toString();
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public Double getxCoord() {
        return xCoord;
    }

    public void setxCoord(Double xCoord) {
        this.xCoord = xCoord;
    }

    public Double getyCoord() {
        return yCoord;
    }

    public void setyCoord(Double yCoord) {
        this.yCoord = yCoord;
    }
}
