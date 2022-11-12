public class Application {
    public static void main(String... args) throws InterruptedException {

        Configuration.INSTANCE.logEngine.write("--- starting");

        Configuration.INSTANCE.data = new ProblemInstance();
        Configuration.INSTANCE.data.readData();
        AntColony antColony = new AntColony();
        Singleton.initialize();
        antColony.solve();
        Singleton.close();
        Configuration.INSTANCE.logEngine.write(antColony.toString());

        Configuration.INSTANCE.logEngine.close();
    }
}