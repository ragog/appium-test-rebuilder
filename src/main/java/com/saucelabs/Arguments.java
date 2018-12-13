package com.saucelabs;

import com.beust.jcommander.Parameter;

public class Arguments {

    @Parameter(description = "Target logfile to be used to generate the script")
    public String logPath;

}
