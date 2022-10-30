import java.io.ObjectInputFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Ant extends Thread {
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

    public void initializeCars(){
        this.cars = new ArrayList<>();
        for(int i = 0; i < Configuration.INSTANCE.vehicleQuantity; i++){
            cars.add(new Car());
            cars.get(i).initializeRoute(new ArrayList<>());
            cars.get(i).setRoute(0,0);
        }
    }


    public double getObjectiveValue() {
        int penalty = 0;
        if (objectiveValue == 0.0) {
            int count = Configuration.INSTANCE.countCities;
            int currentDemand;

            for (Car car : cars) {
                int tempVehicleCapacity = Configuration.INSTANCE.vehicleCapacity;
                for (int i = 0; i < car.getRoute().size() - 1; i++) {
                    int position = car.getRoute().get(i);
                    int position2 = car.getRoute().get(i + 1);
                    objectiveValue += Configuration.INSTANCE.distanceMatrix.get(position).get(position2);


                    //if exceeding capacity, go to depo and back again
                    currentDemand = (int) Configuration.INSTANCE.cities.get(i).getDemand();
                    while (currentDemand > 0) {
                        if (tempVehicleCapacity - currentDemand < 0) {
                            currentDemand -= tempVehicleCapacity;
                            objectiveValue += Configuration.INSTANCE.distanceMatrix.get(car.getRoute().get(i)).get(0);
                            objectiveValue += Configuration.INSTANCE.distanceMatrix.get(0).get(car.getRoute().get(i));
                            tempVehicleCapacity = Configuration.INSTANCE.vehicleCapacity;
                        } else {
                            tempVehicleCapacity -= currentDemand;
                            currentDemand = 0;
                        }
                    }

                    //Punish for time window
                    //setting time of car to start of window of first customer
                    int tempCustomerIndex = car.getRoute().get(i+1);
                    City tempCustomer = Configuration.cities.get(tempCustomerIndex);
                    int tempReadyTime = (int) tempCustomer.getReadyTime();
                    int tempDueTime = (int) tempCustomer.getDueTime();
                    //if car is early, it will wait for ready time
                    if (car.getCurrentTime() < tempReadyTime) {
                        car.setTime(tempReadyTime);
                    }
                    //if car is within window, no penalty
                    if (car.getCurrentTime() >= tempReadyTime && car.getCurrentTime() <= tempDueTime) {
                        penalty += 0; //car.getCurrentTime() - tempReadyTime;
                        //else, penalty is time outside of window
                    } else {
                        penalty += car.getCurrentTime() - tempDueTime;
                    }
                    //update time by service time (i.e., 10)
                    car.updateTime();
                }
                car.setTime(0);
                //add distance to depo at end
                int position = car.getRoute().size() - 1;
                int position2 = car.getRoute().get(0);
                objectiveValue += Configuration.INSTANCE.distanceMatrix.get(position).get(position2);
            }
        }
        return ((objectiveValue) +  ((100)*penalty));
    }

    public void newRound() {
        objectiveValue = 0.0;
        //tour = new int[Configuration.INSTANCE.countCities];
        notJetVisited = new Vector<>();

        for (int i = 1; i <= Configuration.INSTANCE.countCities; i++) {
            notJetVisited.addElement(i);
        }
    }

    public void layPheromone() {
        //double pheromone = Configuration.INSTANCE.decayFactor / objectiveValue;
        double pheromone = 1 / objectiveValue;
        int count = Configuration.INSTANCE.countCities;

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- Ant.layPheromone()");
            Configuration.INSTANCE.logEngine.write("decay factor   : " + Configuration.INSTANCE.decayFactor);
            Configuration.INSTANCE.logEngine.write("objectiveValue : " + objectiveValue);
            Configuration.INSTANCE.logEngine.write("pheromone      : " + pheromone);
        }

        for(Car car : cars) {
            List<Integer> tempRoute = car.getRoute();
            for (int i = 0; i < car.getRoute().size() - 1; i++) {
                antColony.addPheromone(tempRoute.get(i), tempRoute.get(i+1), pheromone);
                antColony.addPheromone(tempRoute.get(i+1), tempRoute.get(i), pheromone);
            }

            antColony.addPheromone(tempRoute.get(tempRoute.size()-1), tempRoute.get(0), pheromone);
            antColony.addPheromone(tempRoute.get(0), tempRoute.get(tempRoute.size()-1), pheromone);
        }
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("---");
        }
    }

    public synchronized void run() {

        newRound();
        DecimalFormat decimalFormat = new DecimalFormat("#0.000000000000000");

        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("--- Ant.lookForWay");
        }

        int numberOfCities = Configuration.INSTANCE.countCities;
        //int randomIndexOfTownToStart = (int) (numberOfCities * Configuration.INSTANCE.randomGenerator.nextDouble() + 1);
        int randomIndexOfTownToStart = (int) (numberOfCities * Configuration.INSTANCE.randomGenerator.nextDouble());
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("numberOfCities           : " + numberOfCities);
            //Configuration.INSTANCE.logEngine.write("randomIndexOfTownToStart : " + randomIndexOfTownToStart);
        }



        //depo set as first postition
        initializeCars();

        for(Car car : cars) {
            for (int i = 1; i <= numberOfCities/Configuration.INSTANCE.vehicleQuantity; i++) {
                double sum = 0.0;

                if (Configuration.INSTANCE.isDebug) {
                    Configuration.INSTANCE.logEngine.write("i : " + i + " - notJetVisited : " + notJetVisited);
                }

                for (int j = 0; j < notJetVisited.size(); j++) {
                    int position = notJetVisited.elementAt(j);
                    int position2 = car.getRoute().get(i-1);
                    double tempDistance = Configuration.INSTANCE.distanceMatrix.get(position2).get(position);
                    sum += Math.pow(antColony.getPheromone(position2, position),Configuration.INSTANCE.alphaValue)
                            / Math.pow(tempDistance,Configuration.INSTANCE.betaValue);
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
                    int position2 = car.getRoute().get(i-1);

                    selectionProbability += Math.pow(antColony.getPheromone(position2, position),Configuration.INSTANCE.alphaValue) /
                            Math.pow(Configuration.INSTANCE.distanceMatrix.get(position2).get(position),Configuration.INSTANCE.betaValue)/
                            sum;

                    if (Configuration.INSTANCE.isDebug)
                        if (position < 10) {
                            Configuration.INSTANCE.logEngine.write("position : 0" + position +
                                    " - selectionProbability : " + decimalFormat.format(selectionProbability));
                        } else {
                            Configuration.INSTANCE.logEngine.write("position : " + position +
                                    " - selectionProbability : " + decimalFormat.format(selectionProbability));
                        }

                    if(Double.isNaN(selectionProbability)){
                        selectionProbability =1;
                    }
                    if (randomNumber < selectionProbability) {
                        randomIndexOfTownToStart = position;
                        break;
                    }
                }

                if (Configuration.INSTANCE.isDebug) {
                    Configuration.INSTANCE.logEngine.write("randomIndexOfTownToStart : " + randomIndexOfTownToStart);
                }

                //tour[i] = randomIndexOfTownToStart;
                car.getRoute().add(i,randomIndexOfTownToStart);
                notJetVisited.removeElement(randomIndexOfTownToStart);

                if (Configuration.INSTANCE.isDebug) {
                    Configuration.INSTANCE.logEngine.write("-");
                }
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
        int vehicleID = 0;

        for (Car car : cars) {
            vehicleID++;
            stringBuilder.append("vehicle #" + vehicleID + " | route = [ " + Configuration.INSTANCE.cities.get(0).getName() + " -> ");

            for (int i = 1; i < car.getRoute().size(); i++) {
                if (i == car.getRoute().size() - 1) {
                    stringBuilder.append(Configuration.INSTANCE.cities.get(car.getRoute().get(i)).getName() + " -> depot]");
                } else {
                    stringBuilder.append(Configuration.INSTANCE.cities.get(car.getRoute().get(i)).getName() + " -> ");
                }
            }

            stringBuilder.append("\n");
        }

        //time window check
        boolean check = true;
        List<Integer> out = new ArrayList<>();
        for(Car car : cars){
            int listSize = car.getRoute().size() - 1;
            for (int i = 1; i <= listSize; i++) {
                int tempCustomerIndex = car.getRoute().get(i);
                City tempCustomer = Configuration.cities.get(tempCustomerIndex);
                int tempReadyTime = (int)tempCustomer.getReadyTime();
                int tempDueTime = (int)tempCustomer.getDueTime();
                if(car.getCurrentTime() < tempReadyTime){
                    car.setTime(tempReadyTime);
                }
                if(!(car.getCurrentTime() >= tempReadyTime && car.getCurrentTime()<= tempDueTime)) {
                    check = false;
                    out.add(tempCustomerIndex);
                }
                car.updateTime();
            }
        }

        if(check == true){
            stringBuilder.append("\n");
            stringBuilder.append("Time Window PASSED");

        }  else{
            stringBuilder.append("\n");
            stringBuilder.append("Time Window FAILED");
            stringBuilder.append(out.toString());

        }
        stringBuilder.append("\n");
        stringBuilder.append("objectiveValue : ").append(objectiveValue);

        return stringBuilder.toString();
    }
}