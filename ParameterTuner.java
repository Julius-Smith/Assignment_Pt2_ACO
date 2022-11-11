import java.io.*;

public class ParameterTuner {

    public static void main(String args[]) {

        try {
            File file = new File("Parametertuning.txt");
            FileWriter fw = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw);

            double best = 100000;
            double bestAntsNum = 0;
            double bestDecay = 0;
            double bestAlpha = 0;
            double bestBeta = 0;
            //pop
            for (int i = 20; i <= 100; i += 20) {
                Configuration.INSTANCE.numberOfAnts = i;
                //truncation
                for (double decay = 0.1; decay <= 0.9; decay += 0.1) {
                    Configuration.INSTANCE.decayFactor = decay;
                    //mutation
                    for (double alpha = 0.5; alpha <= 2.5; alpha += 0.5) {
                        Configuration.INSTANCE.alphaValue = alpha;
                        //crossover
                        for (double beta = 2; beta <= 4; beta += 0.5) {
                            Configuration.INSTANCE.betaValue = beta;
                            System.out.println(Configuration.INSTANCE.numberOfAnts);
                            System.out.println(Configuration.INSTANCE.decayFactor);
                            System.out.println(Configuration.INSTANCE.alphaValue);
                            System.out.println(Configuration.INSTANCE.betaValue);

                            String Dist = ApplicationPT.go();
                            String output = "Ants: " + Integer.toString(i) + "\nDecay: " + Double.toString(decay) +
                                    "\nAlphaV: " + Double.toString(alpha) + "\nBetaV: " + Double.toString(beta)
                                    + "\nObjectiveV:  " + Dist + "\n";
                            out.println(output);
                            if (Double.parseDouble(Dist) <= best) {
                                best = Double.parseDouble(Dist);
                                bestAntsNum = i;
                                bestDecay = decay;
                                bestAlpha = alpha;
                                bestBeta = beta;
                            }
                        }
                    }
                }
            }

            out.println("Best Distance: " + best);
            out.println("AN: " + bestAntsNum);
            out.println("D: " + bestDecay);
            out.println("A: " + bestAlpha);
            out.println("B: " + bestBeta);
            out.close();
            bw.close();
            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}