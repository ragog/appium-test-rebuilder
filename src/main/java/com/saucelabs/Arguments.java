package com.saucelabs;

import com.beust.jcommander.Parameter;

public class Arguments {

    @Parameter(description = "Target logfile to be used to generate the script")
    public String logPath;

    @Parameter(names = { "-f", "--file" }, description = "Print to file instead of sout")
    public boolean printToFile;

    @Parameter(names = { "-r", "--requests" }, description = "Print WebDriver requests as well as test script" )
    public  boolean printRequests;

}
