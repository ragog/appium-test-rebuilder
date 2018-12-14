package com.saucelabs.util;

public class Strings {
    public final static String DISCLAIMER = "\n# DISCLAIMER: the following is not an exact copy of the test script used to generate the target log, rather an approximation of it. Please keep that in mind.";
    public final static String SEPARATOR_BODY = "\n---------------------- Generated Test Body --------------------\n";
    public final static String DRIVER_INIT_ANDROID = "AndroidDriver<MobileElement> driver = new AndroidDriver<MobileElement>(url, desiredCapabilities);";
    public final static String DRIVER_INIT_IOS = "IOSDriver driver<MobileElement> = new IOSDriver<MobileElement>(url, desiredCapabilities);";
    public final static String PLACEHOLDER_UNKNOWN = "UnknownCommandPlaceholder";
    public final static String PLACEHOLDER_UNIMPLEMENTED = "UnimplementedCommandPlaceholder";
}
