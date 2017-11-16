package app.utilities;

import app.models.Node;

import java.util.List;

public class PathMather {

    public static Double getTotalDistanceOfPath(List<Node> path) {
        Double totalDistance = 0.0;
        for (int i = 0; i < path.size(); i++) {
            Double x1 = path.get(i).getxCoord();
            Double x2 = null;
            Double y1 = path.get(i).getyCoord();
            Double y2 = null;
            // last node makes edge with first node
            if (i == path.size() - 1) {
                x2 = path.get(0).getxCoord();
                y2 = path.get(0).getyCoord();
            } else {
                x2 = path.get(i + 1).getxCoord();
                y2 = path.get(i + 1).getyCoord();
            }
            totalDistance += calculateDistance(x1, x2, y1, y2);
        }
        return totalDistance;
    }

    public static Double calculateDistance(Double x1, Double x2, Double y1, Double y2) {
        return Math.sqrt(Math.pow((x1-x2), 2) + Math.pow((y1-y2), 2));
    }
}
