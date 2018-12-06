import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AppiumTestRebuilder {

    public static void main(String args[]) throws IOException {

        // Target logfile
        File logFile = new File("log-wiki.txt");

        AppiumLogParser logParser = new AppiumLogParser();
        ArrayList<String> commands = logParser.extractCommands(logFile);

        System.out.println("\n---------------------- Generated Test Body --------------------\n");

        for (String command : commands) {
            System.out.println(command);
        }

    }

}

