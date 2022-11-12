import java.util.ArrayList;
import java.util.List;

public class AntColony {
    private final double[][] pheromoneMatrix;
    private final Ant[] antArray;
    private  List<Thread> threads;
    public AntColony() {
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- AntColony()");
        }

        int count = Configuration.INSTANCE.countCities;
        pheromoneMatrix = new double[count+1][count+1];

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                pheromoneMatrix[i][j] = Configuration.INSTANCE.startPheromoneValue;
            }
        }

        antArray = new Ant[Configuration.INSTANCE.numberOfAnts];
        threads = new ArrayList<>();

        for (int i = 0; i < Configuration.INSTANCE.numberOfAnts; i++) {
            antArray[i] = new Ant(Configuration.INSTANCE.data, this, i);
        }

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("---");
        }
    }

    public void addPheromone(int from, int to, double pheromoneValue) {
        pheromoneMatrix[from][to] += pheromoneValue;
    }

    public double getPheromone(int from, int to) {
        return pheromoneMatrix[from][to];
    }

    public void doDecay() {
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- AntColony.doDecay()");
        }

        int count = Configuration.INSTANCE.countCities;
        for (int i = 0; i < count+1; i++) {
            for (int j = 0; j < count+1; j++) {
                pheromoneMatrix[i][j] *= (1.0 - Configuration.INSTANCE.decayFactor);
            }
        }

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("---");
        }
    }

    private Ant getBestAnt() {
        int indexOfAntWithBestObjectiveValue = 0;
        double objectiveValue = Double.MAX_VALUE;

        for (int i = 0; i < Configuration.INSTANCE.numberOfAnts; i++) {
            double currentObjectiveValue = antArray[i].getObjectiveValue();
            if (currentObjectiveValue < objectiveValue) {
                objectiveValue = currentObjectiveValue;
                indexOfAntWithBestObjectiveValue = i;
            }
        }

        return antArray[indexOfAntWithBestObjectiveValue];
    }

    //return String for PT
    public String solve() throws InterruptedException{
        int iteration = 0;


        while (iteration < Configuration.INSTANCE.numberOfIterations) {
            Configuration.INSTANCE.logEngine.write("*** iteration - " + iteration);

            Singleton.getInstance().writeToFile("Iteration: " + iteration + "\n");
            //printPheromoneMatrix();

            iteration++;
            threads = new ArrayList<>();

            for (int i = 0; i < Configuration.INSTANCE.numberOfAnts; i++) {

                threads.add(new Thread(antArray[i]));
                threads.get(i).start();

            }

            for (int i = 0; i < Configuration.INSTANCE.numberOfAnts; i++) {
                threads.get(i).join();
            }

            doDecay();

            for(Ant ant : antArray){
                ant.layPheromone();
            }
            //getBestAnt().layPheromone();

            //Singleton.getInstance().writeToFile(printPheromoneMatrix());

            System.out.println(getBestAnt().toString());

            Configuration.INSTANCE.logEngine.write("***");
//            for (int i = 0; i < Configuration.INSTANCE.numberOfAnts; i++) {
//                antArray[i] = new Ant(Configuration.INSTANCE.data, this);
//            }
        }
        return Double.toString(getBestAnt().getObjectiveValue());
    }

    public String printPheromoneMatrix() {
        StringBuilder str = new StringBuilder();
//        if (Configuration.INSTANCE.isDebug) {
//            Configuration.INSTANCE.logEngine.write("--- AntColony.printPheromoneMatrix()");
//        }

        int n = pheromoneMatrix.length;
        for (double[] matrix : pheromoneMatrix) {
            for (int j = 0; j < n; j++) {
                str.append(Configuration.INSTANCE.decimalFormat.format(matrix[j]) + " ");
            }
            str.append("\n");
        }

        str.append("---");

        return str.toString();
    }

    public String toString() {
        return getBestAnt().toString();
    }
}