package com.saucelabs;

import com.beust.jcommander.JCommander;
import com.saucelabs.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class AppiumTestRebuilder {

    private static String logPath;
    private static ArrayList<String> commands;
    private static ArrayList<String> requests;
    private static boolean optionPrintToFile = true;
    private static boolean optionPrintRequests = true;

    public static void main(String args[]) throws IOException {

        Arguments arguments = new Arguments();

        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        logPath = arguments.logPath;

        File logFile = new File(logPath);

        HashMap<String, Boolean> options = new HashMap<>();
        options.put("printRequests", true);

        AppiumLogParser logParser = new AppiumLogParser(options);
        logParser.parseLog(logFile);

        commands = logParser.getCommands();
        requests = logParser.getRequests();

        if (optionPrintToFile) {
            printToFile();
        } else {
            System.out.println(Strings.DISCLAIMER);
            System.out.println(Strings.SEPARATOR_BODY);
            for (String command : commands) {
                System.out.println(command);
            }
        }

    }

    public static void printToFile() throws IOException {
        Path file = Paths.get("test-rebuilder-output.txt");
        if (optionPrintRequests) {
            requests.add(Strings.DISCLAIMER);
            requests.add(Strings.SEPARATOR_BODY);
            requests.addAll(commands);
            Files.write(file, requests, Charset.forName("UTF-8"));
        } else {
            Files.write(file, commands, Charset.forName("UTF-8"));
        }
    }

}

