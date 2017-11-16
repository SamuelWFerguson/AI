package app.utilities;

import app.models.ExperimentResults;
import app.models.Node;

import java.util.ArrayList;
import java.util.List;

public class WisdomPathBuilder {

    public static ArrayList<Node> buildWisdomPath(List<Node> nodes, List<ExperimentResults> experts) {
        ArrayList<Node> wisdomPath = new ArrayList<>();
        ArrayList<Integer> popularityList = new ArrayList<>();

        // start at one of the experts starting places
        wisdomPath.add(experts.get(0).getLongestPath().get(0));

        // for each node, determine which next node experts travel to the most
        while (wisdomPath.size() != nodes.size()) {

            //find the node we are working with
            Node currentNode = wisdomPath.get(wisdomPath.size() - 1);

            //===============================================
            // Determine popularity
            //===============================================
            // start with a list full of zeros for every city, representing how many experts chose them next
            popularityList.clear();
            for (int j = 0; j < nodes.size(); j++) {
                popularityList.add(0);
            }

            for (ExperimentResults expert : experts) {
                List<Node> expertPath = expert.getLongestPath();

                // get node coming after our current node for this expert
                Integer nextNodeIndex = expertPath.indexOf(currentNode) + 1;
                // wrap the next node index around if it reaches 100
                if (nextNodeIndex == nodes.size()) nextNodeIndex = 0;
                Node nextNode = expertPath.get(nextNodeIndex);
                Integer nextNodeOrderNumber = nextNode.getOrderNumber();

                // increase the count for this node
                Integer count = popularityList.get(nextNodeOrderNumber - 1);
                count++;
                popularityList.set(nextNodeOrderNumber - 1, count);
            }

            //===============================================
            // Find the most popular node
            //===============================================
            List<Node> mostPopularNodes = getMostPopularNodes(popularityList, nodes, wisdomPath);

            // add the most popular city to our path
            if (mostPopularNodes.size() == 1) {
                wisdomPath.add(mostPopularNodes.get(0));
            } else {
                Node closestNode = null;
                Double closestDistance = null;
                for (Node node : mostPopularNodes) {
                    if (wisdomPath.contains(node)) {
                        continue;
                    }

                    if (closestNode == null) {
                        closestNode = node;
                        closestDistance = PathMather.calculateDistance(currentNode.getxCoord(), node.getxCoord(), currentNode.getyCoord(), node.getyCoord());
                        continue;
                    }

                    Double nodeDistance = PathMather.calculateDistance(currentNode.getxCoord(), node.getxCoord(), currentNode.getyCoord(), node.getyCoord());
                    if (nodeDistance < closestDistance) {
                        closestNode = node;
                        closestDistance = nodeDistance;
                    }
                }
                if (closestNode != null) {
                    wisdomPath.add(closestNode);
                }
            }
        }

        // check for repeated nodes
        ArrayList<Node> checkedNodes = new ArrayList<>();
        for (Node node : wisdomPath) {
            if (checkedNodes.contains(node)) {
                System.out.println("Wisdom path contains repeated node: " + node);
            }
            checkedNodes.add(node);
        }

        return wisdomPath;
    }

    private static ArrayList<Node> getMostPopularNodes(List<Integer> popularityList, List<Node> nodes, List<Node> wisdomPath) {
        ArrayList<Node> popularNodes = new ArrayList<>();
        ArrayList<Node> acceptablePopularNodes = new ArrayList<>();

        // find the highest popularity number
        Integer highestNumber = 0;
        for (Integer popularityNumber : popularityList) {
            if (popularityNumber > highestNumber) {
                highestNumber = popularityNumber;
            }
        }

        Boolean foundNodes = false;
        while (!foundNodes) {
            // add all nodes of this popularity to the list
            for (int i = 0; i < popularityList.size(); i++ ) {
                Integer popularityNumber = popularityList.get(i);
                if (popularityNumber.equals(highestNumber)) {
                    Integer orderNumber = i + 1;
                    Node popularNode = getNodeByOrderNumber(nodes, orderNumber);
                    popularNodes.add(popularNode);
                }
            }
            // remove any nodes already in wisdom path
            for (Node node : popularNodes) {
                if (!wisdomPath.contains(node)) {
                    acceptablePopularNodes.add(node);
                }
            }

            if (acceptablePopularNodes.size() > 0) {
                foundNodes = true;
            } else {
                highestNumber--;
            }
        }
        return popularNodes;
    }

    private static Node getNodeByOrderNumber(List<Node> nodes, Integer orderNum) {
        for (Node node : nodes) {
            if (node.getOrderNumber().equals(orderNum)) {
                return node;
            }
        }
        return null;
    }
}
