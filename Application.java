public class Application {
    public static void main(String... args) {
        Configuration.INSTANCE.logEngine.write("--- starting");

        Configuration.INSTANCE.data = new ProblemInstance();
        Configuration.INSTANCE.data.readData();
        AntColony antColony = new AntColony();
        antColony.solve();
        Configuration.INSTANCE.logEngine.write(antColony.toString());

        Configuration.INSTANCE.logEngine.close();
    }
}