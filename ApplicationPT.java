public class ApplicationPT {


    public static String go() throws InterruptedException {

        Configuration.INSTANCE.logEngine.write("--- starting");

        Configuration.INSTANCE.data = new ProblemInstance();
        Configuration.INSTANCE.data.readData();
        AntColony antColony = new AntColony();
        return antColony.solve();

        //Configuration.INSTANCE.logEngine.write(antColony.toString());

        //Configuration.INSTANCE.logEngine.close();
    }
}
