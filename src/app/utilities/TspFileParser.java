package app.utilities;

import app.models.TspFile;

import java.io.File;
import java.util.Scanner;

public class TspFileParser {
    static String NAME = "NAME: ";
    static String TYPE = "TYPE: ";
    static String COMMENT = "COMMENT: ";
    static String DIMENSION = "DIMENSION: ";
    static String EDGE_WEIGHT_TYPE = "EDGE_WEIGHT_TYPE: ";

    public static TspFile parseTspFile(File file) {
        try {
            TspFile tspFile = new TspFile();
            Scanner scanner = new Scanner(file);
            // boolean to determine if we are in the coordinate section
            Boolean isCoordinateSection = false;
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (isCoordinateSection) {
                    String[] coordinatesAsString = line.split(" ");
                    if (coordinatesAsString.length == 3) {
                        Double[] coordinates = new Double[3];
                        // storing the 'city number'
                        coordinates[0] = Double.parseDouble(coordinatesAsString[0]);
                        // storing x
                        coordinates[1] = Double.parseDouble(coordinatesAsString[1]);
                        // storing y
                        coordinates[2] = Double.parseDouble(coordinatesAsString[2]);
                        tspFile.addCoordinates(coordinates);
                    } else {
                        // input file is formatted incorrectly- somebody has clearly sabotaged our input files.
                        throw new Exception("Somebody's poisoned the water hole!");
                    }
                }
                if (line.equals("NODE_COORD_SECTION")) {
                    isCoordinateSection = true;
                    continue;
                }
                if (line.startsWith(NAME)) {
                    String name = line.split(NAME)[1];
                    tspFile.setName(name);
                    continue;
                }
                if (line.startsWith(TYPE)) {
                    String type = line.split(TYPE)[1];
                    tspFile.setType(type);
                    continue;
                }
                if (line.startsWith(COMMENT)) {
                    String comment = line.split(COMMENT)[1];
                    tspFile.addComment(comment);
                    continue;
                }
                if (line.startsWith(DIMENSION)) {
                    int dimension = Integer.parseInt(line.split(DIMENSION)[1]);
                    tspFile.setDimension(dimension);
                    continue;
                }
                if (line.startsWith(EDGE_WEIGHT_TYPE)) {
                    String edgeWeightType = line.split(EDGE_WEIGHT_TYPE)[1];
                    tspFile.setEdgeWeightType(edgeWeightType);
                }
            }
            return tspFile;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
