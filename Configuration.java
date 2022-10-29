import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public enum Configuration {
    INSTANCE;

    // common
    public final String userDirectory = System.getProperty("user.dir");
    //file path
    public final Path path = Paths.get("instance.txt");
    public static final Map<Integer, City> cities = new TreeMap<>();

    public List<List<Double>> distanceMatrix;
    public void initDistanceMatrix() {
        distanceMatrix = Utility.calculateDistanceMatrix(cities);
    }
    public int countCities = 0;
    public final String fileSeparator = System.getProperty("file.separator");
    public final String dataDirectory = userDirectory + fileSeparator + "data" + fileSeparator;
    public final String logDirectory = userDirectory + fileSeparator + "log" + fileSeparator;
    public final LogEngine logEngine = new LogEngine(logDirectory + "debug.log");
    public final boolean isDebug = true;
    public final DecimalFormat decimalFormat = new DecimalFormat("#0.000000000000000");
    public final MersenneTwister randomGenerator = new MersenneTwister(System.currentTimeMillis());
    // ant colony optimization
    public final double decayFactor = 0.3;
    public final double startPheromoneValue = 0.00005;
    public final int numberOfAnts = 10;
    public final int numberOfIterations = 250;
    public ProblemInstance data;
}