import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public enum Configuration {
    INSTANCE;
    //file path
    public final Path path = Paths.get("instance.txt");
    public static final Map<Integer, City> cities = new TreeMap<>();
    public final int vehicleCapacity = 200;
    public final int vehicleQuantity = 20;
    public List<List<Double>> distanceMatrix;
    public void initDistanceMatrix() {
        distanceMatrix = Utility.calculateDistanceMatrix(cities);
    }
    public int countCities = 0;
    public final LogEngine logEngine = new LogEngine( "debug.log");
    public final boolean isDebug = false;
    public final DecimalFormat decimalFormat = new DecimalFormat("#0.000000000000000");
    public final MersenneTwister randomGenerator = new MersenneTwister(System.currentTimeMillis());
    // ant colony optimization
    public final double startPheromoneValue = 0.000005;//0.00005;
    public final int numberOfAnts = 100;
    public final int numberOfIterations = 1000;
    public ProblemInstance data;
    public final double alphaValue = 1;
    public final double betaValue = 2;
    public final double decayFactor = 0.5;
    //PT


}