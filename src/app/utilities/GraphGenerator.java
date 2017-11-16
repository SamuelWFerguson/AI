package app.utilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class GraphGenerator {

    public static ArrayList<Double[]> generateGraph(Integer amountOfNodes) {
        ArrayList<Double[]> nodes = new ArrayList<>();

        while (nodes.size() < amountOfNodes) {
            Double[] node = new Double[3];
            node[0] = (double) (nodes.size() + 1);
            node[1] = ThreadLocalRandom.current().nextDouble(100);
            node[2] = ThreadLocalRandom.current().nextDouble(100);
            nodes.add(node);
        }

        return nodes;
    }
}
