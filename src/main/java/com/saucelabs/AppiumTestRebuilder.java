package com.saucelabs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class AppiumTestRebuilder {

    public static void main(String args[]) throws IOException {

        boolean optionPrintToFile;

        // Target logfile
//        File logFile = new File("log-wiki.txt");
//        File logFile = new File("log.txt");
        File logFile = new File("log-calc.txt");

        HashMap<String, Boolean> options = new HashMap<>();
        options.put("printRequests", true);
//        options.put("printToFile", false); // TODO

        AppiumLogParser logParser = new AppiumLogParser(options);
        ArrayList<String> commands = logParser.extractCommands(logFile);

        System.out.println("\n# DISCLAIMER: the following is not an exact copy of the test script used to generate the target log, rather an approximation of it. Please keep that in mind.");
        System.out.println("\n---------------------- Generated Test Body --------------------\n");

        for (String command : commands) {
            System.out.println(command);
        }

    }

}

