import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogEngine {
    private BufferedWriter bufferedWriter;

    public LogEngine(String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName, true);
            bufferedWriter = new BufferedWriter(fileWriter);
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public String getCurrentDate() {
        Date date = new Date();
        // SimpleDateFormat simpleDateFormat = new SimpleDateFormat();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.S");
        return simpleDateFormat.format(date);
    }

    public void write(String text) {
        try {
            if (Configuration.INSTANCE.isDebug) {
                System.out.println(text);
            }
            bufferedWriter.write(getCurrentDate() + " : " + text + "\n");
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void write(String className, String methodName, String attributeName, String attributeValue) {
        System.out.println("c : " + className + " * m : " + methodName + " * " + attributeName + " = " + attributeValue);
    }

    public void write(String className, String methodName, String attributeName, String attributeValue, long runtime) {
        System.out.println("c : " + className + " * m : " + methodName + " * " + attributeName + " = " + attributeValue + " * r : " + runtime + " ms");
    }

    public void close() {
        try {
            bufferedWriter.close();
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
}