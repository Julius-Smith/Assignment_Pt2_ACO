import java.text.DecimalFormat;
import java.util.List;
import java.util.Vector;

public class Ant {
    private final ProblemInstance data;
    private final AntColony antColony;
    private double objectiveValue = 0.0;
    private int[] tour;
    private List<Car> cars;
    private Vector<Integer> notJetVisited = null;

    public Ant(ProblemInstance data, AntColony antColony) {
        this.data = data;
        this.antColony = antColony;
    }

    public double getObjectiveValue() {
        if (objectiveValue == 0.0) {
            int count = Configuration.INSTANCE.countCities;
            for (int i = 0; i < count - 1; i++) {
                objectiveValue += data.getDistance(tour[i], tour[i + 1]);

            }
            objectiveValue += data.getDistance(tour[count - 1], tour[0]);
        }

        return objectiveValue;
    }

    public void newRound() {
        objectiveValue = 0.0;
        tour = new int[Configuration.INSTANCE.countCities];
        notJetVisited = new Vector<>();

        for (int i = 1; i <= Configuration.INSTANCE.countCities; i++) {
            notJetVisited.addElement(i);
        }
    }

    public void layPheromone() {
        double pheromone = Configuration.INSTANCE.decayFactor / objectiveValue;
        int count = Configuration.INSTANCE.countCities;

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- Ant.layPheromone()");
            Configuration.INSTANCE.logEngine.write("decay factor   : " + Configuration.INSTANCE.decayFactor);
            Configuration.INSTANCE.logEngine.write("objectiveValue : " + objectiveValue);
            Configuration.INSTANCE.logEngine.write("pheromone      : " + pheromone);
        }

        for (int i = 0; i < count - 1; i++) {
            antColony.addPheromone(tour[i], tour[i + 1], pheromone);
            antColony.addPheromone(tour[i + 1], tour[i], pheromone);
        }

        antColony.addPheromone(tour[count - 1], tour[0], pheromone);
        antColony.addPheromone(tour[0], tour[count - 1], pheromone);

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("---");
        }
    }

    public void lookForWay() {
        DecimalFormat decimalFormat = new DecimalFormat("#0.000000000000000");

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- Ant.lookForWay");
        }

        int numberOfCities = Configuration.INSTANCE.countCities;
        int randomIndexOfTownToStart = (int) (numberOfCities * Configuration.INSTANCE.randomGenerator.nextDouble() + 1);

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("numberOfCities           : " + numberOfCities);
            Configuration.INSTANCE.logEngine.write("randomIndexOfTownToStart : " + randomIndexOfTownToStart);
        }

        tour[0] = randomIndexOfTownToStart;
        notJetVisited.removeElement(randomIndexOfTownToStart);

        for (int i = 1; i < numberOfCities; i++) {
            double sum = 0.0;

            if (Configuration.INSTANCE.isDebug) {
                Configuration.INSTANCE.logEngine.write("i : " + i + " - notJetVisited : " + notJetVisited);
            }

            for (int j = 0; j < notJetVisited.size(); j++) {
                int position = notJetVisited.elementAt(j);
                sum += antColony.getPheromone(tour[i - 1], position) / data.getDistance(tour[i - 1], position);
            }

            double selectionProbability = 0.0;
            double randomNumber = Configuration.INSTANCE.randomGenerator.nextDouble();

            if (Configuration.INSTANCE.isDebug) {
                Configuration.INSTANCE.logEngine.write("i : " + i + " - sum : " + decimalFormat.format(sum) +
                        " - randomNumber : " + decimalFormat.format(randomNumber));
                Configuration.INSTANCE.logEngine.write("-");
            }

            for (int j = 0; j < notJetVisited.size(); j++) {
                int position = notJetVisited.elementAt(j);

                selectionProbability += antColony.getPheromone(tour[i - 1], position) /
                        data.getDistance(tour[i - 1], position) /
                        sum;

                if (Configuration.INSTANCE.isDebug)
                    if (position < 10) {
                        Configuration.INSTANCE.logEngine.write("position : 0" + position +
                                " - selectionProbability : " + decimalFormat.format(selectionProbability));
                    } else {
                        Configuration.INSTANCE.logEngine.write("position : " + position +
                                " - selectionProbability : " + decimalFormat.format(selectionProbability));
                    }

                if (randomNumber < selectionProbability) {
                    randomIndexOfTownToStart = position;
                    break;
                }
            }

            if (Configuration.INSTANCE.isDebug) {
                Configuration.INSTANCE.logEngine.write("randomIndexOfTownToStart : " + randomIndexOfTownToStart);
            }

            tour[i] = randomIndexOfTownToStart;
            notJetVisited.removeElement(randomIndexOfTownToStart);

            if (Configuration.INSTANCE.isDebug) {
                Configuration.INSTANCE.logEngine.write("-");
            }
        }

        getObjectiveValue();

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("---");
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int numberOfCities = Configuration.INSTANCE.countCities;

        stringBuilder.append("tour : ");

        for (int i = 0; i < numberOfCities; i++) {
            stringBuilder.append(tour[i]).append(" ");
        }

        stringBuilder.append("\n");
        stringBuilder.append("objectiveValue : ").append(objectiveValue);

        return stringBuilder.toString();
    }
}