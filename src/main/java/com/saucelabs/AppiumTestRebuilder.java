package com.saucelabs;

import com.beust.jcommander.JCommander;
import com.saucelabs.util.Strings;
import com.sun.org.apache.xpath.internal.operations.Bool;

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

    // Options to modify output
    private static boolean optionPrintToFile;
    private static boolean optionPrintRequests;

    public static void main(String args[]) throws IOException {

        // Parse command line arguments
        Arguments arguments = new Arguments();

        JCommander.newBuilder()
                .addObject(arguments)
                .build()
                .parse(args);

        // Target logs to generate script from
        logPath = arguments.logPath;
        File logFile = new File(logPath);

        boolean envPrintToFile = Boolean.parseBoolean(System.getProperty("optionPrintToFile"));
        boolean envPrintRequests = Boolean.parseBoolean(System.getProperty("optionPrintRequests"));

        optionPrintToFile = !envPrintToFile ? arguments.printToFile : envPrintToFile;
        optionPrintRequests = !envPrintRequests ? arguments.printRequests : envPrintRequests;

        HashMap<String, Boolean> options = new HashMap<>();
        options.put("printRequests", optionPrintRequests);

        AppiumLogParser logParser = new AppiumLogParser(options);
        logParser.parseLog(logFile);

        commands = logParser.getCommands();
        requests = logParser.getRequests();

        // Actual output
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
        Path file = Paths.get("test-rebuilt-from-" + logPath);
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

