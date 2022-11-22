import java.io.ObjectInputFilter;
import java.text.DecimalFormat;
import java.util.*;

public class Ant extends Thread {
    private final ProblemInstance data;
    private final AntColony antColony;
    private double objectiveValue = 0.0;
    private double  distance = 0.0;
    private double penalty = 0;
    private int[] tour;
    private List<Car> cars;
    private Vector<Integer> notJetVisited = null;

    private int num ;

    public Ant(ProblemInstance data, AntColony antColony, int num) {
        this.data = data;
        this.antColony = antColony;
        this.num = num;
        initializeCars();
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

        if (objectiveValue == 0.0) {
            int count = Configuration.INSTANCE.countCities;
            int currentDemand;

            for (Car car : cars) {
                int tempVehicleCapacity = Configuration.INSTANCE.vehicleCapacity;
                int time = 0;
                for (int i = 0; i <=car.getRoute().size() - 1; i++) {

                        if (i == 0) {
                            objectiveValue += Configuration.INSTANCE.distanceMatrix.get(0).get(car.getRoute().get(0));
                            distance += Configuration.INSTANCE.distanceMatrix.get(0).get(car.getRoute().get(0));
                        } else {
                            objectiveValue += Configuration.INSTANCE.distanceMatrix.get(car.getRoute().get(i - 1)).get(car.getRoute().get(i));
                            distance += Configuration.INSTANCE.distanceMatrix.get(car.getRoute().get(i - 1)).get(car.getRoute().get(i));
                        }



                    //if exceeding capacity, go to depo and back again
                    currentDemand = (int) Configuration.INSTANCE.cities.get(i).getDemand();
                    while (currentDemand > 0) {
                        if (tempVehicleCapacity - currentDemand < 0) {
                            currentDemand -= tempVehicleCapacity;
                            objectiveValue += Configuration.INSTANCE.distanceMatrix.get(car.getRoute().get(i)).get(0);
                            objectiveValue += Configuration.INSTANCE.distanceMatrix.get(0).get(car.getRoute().get(i));
                            distance +=  Configuration.INSTANCE.distanceMatrix.get(car.getRoute().get(i)).get(0);
                            distance += Configuration.INSTANCE.distanceMatrix.get(0).get(car.getRoute().get(i));
                            tempVehicleCapacity = Configuration.INSTANCE.vehicleCapacity;
                        } else {
                            tempVehicleCapacity -= currentDemand;
                            currentDemand = 0;
                        }
                    }

                    //Punish for time window
                    //setting time of car to start of window of first customer
                    int tempCustomerIndex = car.getRoute().get(i);
                    City tempCustomer = Configuration.cities.get(tempCustomerIndex);
                    int tempReadyTime = (int) tempCustomer.getReadyTime();
                    int tempDueTime = (int) tempCustomer.getDueTime();
                    //if car is early, it will wait for ready time
                    if (time < tempReadyTime) {
                        time = tempReadyTime;
                    }
                    //if car is within window, no penalty
                    if (time >= tempReadyTime && time <= tempDueTime) {
                        penalty += 0; //car.getCurrentTime() - tempReadyTime;
                        //else, penalty is time outside of window
                    } else {
                        penalty += time - tempDueTime;
                    }
                    //update time by service time (i.e., 10)
                    time +=10;
                }

                //add distance to depo at end
                int position = car.getRoute().size() - 1;
                //int position2 = car.getRoute().get(0);
                objectiveValue += Configuration.INSTANCE.distanceMatrix.get(position).get(0);
                distance += Configuration.INSTANCE.distanceMatrix.get(position).get(0);
            }


            objectiveValue = objectiveValue + (1000)*penalty;
        }

        return (objectiveValue);
    }

    public void newRound() {
        objectiveValue = 0.0;
        distance = 0.0;
        penalty = 0.0;
        //tour = new int[Configuration.INSTANCE.countCities];
        notJetVisited = new Vector<>();

        for (int i = 1; i <= Configuration.INSTANCE.countCities; i++) {
            notJetVisited.addElement(i);
        }

    }

    public void layPheromone() {
        //double pheromone = Configuration.INSTANCE.decayFactor / objectiveValue;
        double pheromone = 1/ objectiveValue;
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
                //antColony.addPheromone(tempRoute.get(i+1), tempRoute.get(i), pheromone);
            }

            antColony.addPheromone(tempRoute.get(tempRoute.size()-1), tempRoute.get(0), pheromone);
            //antColony.addPheromone(tempRoute.get(0), tempRoute.get(tempRoute.size()-1), pheromone);
        }
        if (Configuration.INSTANCE.isDebug) {
            Configuration.INSTANCE.logEngine.write("---");
        }
    }

    public synchronized void run() {

        newRound();
        DecimalFormat decimalFormat = new DecimalFormat("#0.000000000000000");

        Singleton.getInstance().writeToFile("Agent: " + this.num + " --- " + LogEngine.getCurrentDate() +
                "\n--- Ant Searching" +"\n");

        int numberOfCities = Configuration.INSTANCE.countCities;

        int randomIndexOfTownToStart = (int) (numberOfCities * Configuration.INSTANCE.randomGenerator.nextDouble());

        //depo set as first postition
        initializeCars();

        for(Car car : cars) {
            int time = 0;
            for (int i = 1; i <= numberOfCities/Configuration.INSTANCE.vehicleQuantity; i++) {
                double sum = 0.0;

                for (int j = 0; j < notJetVisited.size(); j++) {
                    int position = notJetVisited.elementAt(j);
                    int position2 = car.getRoute().get(i-1);
                    double tempDistance = Configuration.INSTANCE.distanceMatrix.get(position2).get(position);
                    //making distance include delta time
                    double penalty  = 0;
                    City tempCustomer = Configuration.cities.get(position);

                    int tempDueTime = (int) tempCustomer.getDueTime();

                    //if car is early, it will wait for ready time
                    //if car is within window, no penalty
                    if (time > tempDueTime) {
                        penalty += time - tempDueTime;
                    }

                    double cost = 0.1*tempDistance + (1000)*(penalty);
                    sum += Math.pow(antColony.getPheromone(position2, position),Configuration.INSTANCE.alphaValue)
                            / Math.pow(cost,Configuration.INSTANCE.betaValue);
                }

                double selectionProbability = 0.0;
                double randomNumber = Configuration.INSTANCE.randomGenerator.nextDouble();


                for (int j = 0; j < notJetVisited.size(); j++) {
                    int position = notJetVisited.elementAt(j);
                    int position2 = car.getRoute().get(i-1);
                    double tempDistance = Configuration.INSTANCE.distanceMatrix.get(position2).get(position);
                    //making distance include delta time
                    double penalty  = 0;
                    City tempCustomer = Configuration.cities.get(position);
                    int tempReadyTime = (int) tempCustomer.getReadyTime();
                    int tempDueTime = (int) tempCustomer.getDueTime();
                    //if car is early, it will wait for ready time

                    //if car is within window, no penalty
                    if (time > tempDueTime) {
                        penalty += time - tempDueTime;
                    }

                    double cost = 0.1*tempDistance + (1000)*(penalty);

                    selectionProbability += Math.pow(antColony.getPheromone(position2, position),Configuration.INSTANCE.alphaValue) /
                            Math.pow(cost,Configuration.INSTANCE.betaValue)/
                            sum;

                    if(Double.isNaN(selectionProbability)){
                        selectionProbability =1;
                    }
                    if (randomNumber < selectionProbability) {
                        randomIndexOfTownToStart = position;
                        if(time <tempReadyTime){
                            time =tempReadyTime;
                        }
                        time+=10;
                        break;
                    }
                }


                car.getRoute().add(i,randomIndexOfTownToStart);
                notJetVisited.removeElement(randomIndexOfTownToStart);
                Collections.shuffle(notJetVisited);

            }

        }

        getObjectiveValue();

        Singleton.getInstance().writeToFile("Agent: " + this.num + " --- " + LogEngine.getCurrentDate() +
                "\nDone Searching\n");

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
        List<Integer> cities = new ArrayList<>();

        for(int i = 2; i <= 101; i++){
            cities.add(i);
        }
        List<Integer> route = new ArrayList<>();

        for(Car car : cars){
            int time = 0;
            int listSize = car.getRoute().size() - 1;
            for (int i = 1; i <= listSize; i++) {
                int tempCustomerIndex = car.getRoute().get(i);


                    route.add(tempCustomerIndex);


                City tempCustomer = Configuration.cities.get(tempCustomerIndex);
                int tempReadyTime = (int)tempCustomer.getReadyTime();
                int tempDueTime = (int)tempCustomer.getDueTime();
                if(time < tempReadyTime){
                    time =tempReadyTime;
                }
                if(!(time>= tempReadyTime && time<= tempDueTime)) {
                    check = false;
                    out.add(tempCustomerIndex);
                }
                time +=10;
            }
//            car.setTime(0);
        }

        if(check == true){
            stringBuilder.append("\n");
            stringBuilder.append("Time Window PASSED");

        }  else{
            stringBuilder.append("\n");
            stringBuilder.append("Time Window FAILED");
            stringBuilder.append(out.toString());

        }

        Collections.sort(route);


        stringBuilder.append("\n");
        stringBuilder.append("objectiveValue : ").append(objectiveValue);
        stringBuilder.append("\n");
        stringBuilder.append("Distance: ").append(distance);
        //stringBuilder.append("Cities to visit: ").append(cities);

        return stringBuilder.toString();
    }
}