import java.io.*;

public class Singleton{
        private static final Singleton inst= new Singleton();
    static File file = new File("Antlog1.txt");
    static FileWriter fw ;
    static BufferedWriter bw ;
    static PrintWriter out;
        private Singleton() {
            super();
        }

        public static void initialize(){
            try {

                fw = new FileWriter(file);
                bw = new BufferedWriter(fw);
                out = new PrintWriter(bw);

            } catch(IOException io){
                io.printStackTrace();
            }
        }
        public static void close(){
            try {

                out.close();
                bw.close();
                fw.close();

            } catch(IOException io){
                io.printStackTrace();
            }
        }
        public synchronized void writeToFile(String str) {

                out.append(str + "\n");


        }


        public static Singleton getInstance() {
            return inst;
        }
}
