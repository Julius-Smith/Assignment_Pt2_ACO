public class AntColony {
    private final double[][] pheromoneMatrix;
    private final Ant[] antArray;

    public AntColony() {
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- AntColony()");
        }

        int count = Configuration.INSTANCE.countCities;
        pheromoneMatrix = new double[count][count];

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                pheromoneMatrix[i][j] = Configuration.INSTANCE.startPheromoneValue;
            }
        }

        antArray = new Ant[Configuration.INSTANCE.numberOfAnts];

        for (int i = 0; i < Configuration.INSTANCE.numberOfAnts; i++) {
            antArray[i] = new Ant(Configuration.INSTANCE.data, this);
        }

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("---");
        }
    }

    public void addPheromone(int from, int to, double pheromoneValue) {
        pheromoneMatrix[from - 1][to - 1] += pheromoneValue;
    }

    public double getPheromone(int from, int to) {
        return pheromoneMatrix[from - 1][to - 1];
    }

    public void doDecay() {
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- AntColony.doDecay()");
        }

        int count = Configuration.INSTANCE.countCities;
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
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

    public void solve() {
        int iteration = 0;

        while (iteration < Configuration.INSTANCE.numberOfIterations) {
            Configuration.INSTANCE.logEngine.write("*** iteration - " + iteration);

            printPheromoneMatrix();

            iteration++;

            for (int i = 0; i < Configuration.INSTANCE.numberOfAnts; i++) {
                antArray[i].newRound();
                antArray[i].lookForWay();
            }

            doDecay();
            //TO-DO: update pheremone using all them ants.
            getBestAnt().layPheromone();

            printPheromoneMatrix();

            Configuration.INSTANCE.logEngine.write("***");
        }
    }

    public void printPheromoneMatrix() {
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- AntColony.printPheromoneMatrix()");
        }

        int n = pheromoneMatrix.length;
        for (double[] matrix : pheromoneMatrix) {
            for (int j = 0; j < n; j++) {
                System.out.print(Configuration.INSTANCE.decimalFormat.format(matrix[j]) + " ");
            }
            System.out.println();
        }

        System.out.println("---");
    }

    public String toString() {
        return getBestAnt().toString();
    }
}