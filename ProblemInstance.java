import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ProblemInstance {
    /*
    private double[][] distanceMatrix = {{0, 1, 3, 2, 1}, {1, 0, 1, 1, 2},
            {3, 1, 0, 1, 3}, {2, 1, 1, 0, 1},
            {1, 2, 3, 1, 0}};
     */

    public void readData(){
        try {
            Files.lines(Configuration.INSTANCE.path).forEach(line ->
            {
                line = line.trim();//remove trailing spaces
                List<Double> coordinatesXY = new ArrayList<>();
                String[] tempStringArray = line.split(("\\s+"));
                coordinatesXY.add(Double.parseDouble(tempStringArray[1]));
                coordinatesXY.add(Double.parseDouble(tempStringArray[2]));
                City city = new City(coordinatesXY, Double.parseDouble(tempStringArray[3]), Integer.toString(Integer.parseInt(tempStringArray[0])),
                        Double.parseDouble(tempStringArray[4]), Double.parseDouble(tempStringArray[5]));
                Configuration.INSTANCE.cities.put((Integer.parseInt(tempStringArray[0])) - 1, city);
            });


            Configuration.INSTANCE.countCities = Configuration.INSTANCE.cities.size() - 1;
            Configuration.INSTANCE.initDistanceMatrix();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

    }



//    public double getDistance(int from, int to) {
//        return distanceMatrix[from - 1][to - 1];
//    }
}