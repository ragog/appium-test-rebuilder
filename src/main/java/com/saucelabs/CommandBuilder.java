package com.saucelabs;

import com.saucelabs.util.Strings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommandBuilder {

    private static ArrayList<Element> elementList = new ArrayList<>();
    private static ArrayList<ArrayList<Element>> elementsList = new ArrayList<>();

    public static String buildFindElement(String strategy, String value, String elementId) {

        String existingName = fetchElementName(elementId);
        String elementType;
        String elementName;
        if (existingName.isEmpty()) {
             elementType = "MobileElement ";
             elementName = "element" + elementList.size();
        } else {
            elementType = "";
            elementName = existingName;
        }

        Element element = new Element(elementId, elementName, strategy, value);
        elementList.add(element);

        if (strategy.equals("class name")) {
            strategy = "className";
        }
        if (strategy.equals("css selector")) {
            strategy = "cssSelector";
        }

        return String.format("%s%s = driver.findElement(By.%s(\"%s\"));",
                elementType, element.getName(), strategy, value);

    }

    public static String buildFindElements(String strategy, String value, List<String> elementIds) {
        ArrayList<Element> list = new ArrayList<>();

        String elementName = "listElement" + elementsList.size();

        for (String elementId: elementIds) {
            for (Element element : elementList) {
                if (element.getId().equals(elementId)) {
                    elementName = element.getName();
                }
            }

             list.add(new Element(elementId, elementName, strategy, value));

        }

        elementsList.add(list);

        return "ArrayList<MobileElement> list" + elementsList.size() + " = driver.findElements(By." + strategy + "(\"" + value + "\"));";

    }

    public static String buildClickElement(String elementId) {

        String elementName = fetchElementName(elementId);

        if (!elementName.isEmpty()) {
            return elementName + ".click();";
        }

        int elementIndex = -1;

        for (ArrayList<Element> list : elementsList) {
            for (int i = 0; i<list.size(); i++) {
                if (list.get(i).getId().equals(elementId)) {
                    elementIndex = i;
                }
            }
        }

        return "list" + elementsList.size() + ".get(" + elementIndex + ").click();";

    }

    public static String buildSetUrl(String url) {
        return "driver.get(\"" + url + "\");";
    }

    public static String buildGetUrl() {
        return "driver.getCurrentUrl();";
    }

    public static String buildSendKeys(String elementId, String value){

        if (elementId.isEmpty()) {
            return "// sendKeys(\""+ value + "\"); on unknown element";
        }

        String elementName = fetchElementName(elementId);

        if (!elementName.isEmpty()) {
            return elementName + ".sendKeys(\"" + value + "\");";
        }

        int elementIndex = -1;

        for (ArrayList<Element> list : elementsList) {
            for (int i = 0; i<list.size(); i++) {
                if (list.get(i).getId().equals(elementId)) {
                    elementIndex = i;
                }
            }
        }

        return "list" + elementsList.size() + ".get(" + elementIndex + ").sendKeys(\"" + value + "\");";
    }

    public static String buildGetContext() {
        return "getContextPlaceholder"; // TODO
    }

    public static String buildSetContext() {
        return "setContextPlaceholder"; // TODO
    }

    public static String buildGetAlertText() {
        return "driver.switchTo().alert().getText();";
    }

    public static String buildAcceptAlert() {
        return "driver.switchTo().alert().accept();";
    }

    public static String buildDismissAlert() {
        return "driver.switchTo().alert().dismiss();";
    }

    public static String buildSetAlertText() {
        return ""; // TODO
    }

    public static String buildGetScreenshot() {
        return "driver.getScreenshotAs(OutputType.BASE64);";
    }

    public static String buildInitSession(HashMap<String, Object> capMap, String platform) {

        String command = "";

        String capabilities = "DesiredCapabilities desiredCapabilities = new DesiredCapabilities();\n";

        for (String key : capMap.keySet()) {
            String value = capMap.get(key).toString();
            capabilities += "desiredCapabilities.setCapability(\"" + key + "\", \"" + value +"\");\n";
        }

        if (platform.equalsIgnoreCase("android")) {
            command = capabilities + Strings.DRIVER_INIT_ANDROID;
        } else if (platform.equalsIgnoreCase("ios")) {
            command = capabilities + Strings.DRIVER_INIT_IOS;
        }

        return command;
    }

    public static String buildTimeouts(long ms) {
        return "driver.manage().timeouts().implicitlyWait("+ ms +", TimeUnit.MILLISECONDS);";
    }

    public static String buildTouchActionPerform(String actionField, HashMap<String, Object> optionMap) {

        if (!actionField.equals("tap")) {
            return Strings.PLACEHOLDER_UNIMPLEMENTED;
        }

        int coordinateX = (int)optionMap.get("x");
        int coordinateY = (int)optionMap.get("y");

        return "new TouchAction(driver).tap(new PointOption().withCoordinates("+coordinateX+", "+coordinateY+"));";
    }

    public static String buildIsDisplayed(String elementId) {
        return fetchElementName(elementId) + ".isDisplayed();";
    }

    public static String buildAttributeName(String elementId) {
        return fetchElementName(elementId) + ".getAttribute(\"content-desc\");";
    }

    public static String buildDeleteSessionCommand() {
        return "driver.quit();";
    }

    public static String buildLocation(String elementId) {
        return fetchElementName(elementId) +".getLocation();";
    }

    public static String buildSize(String elementId) {
        return fetchElementName(elementId) + ".getSize();";
    }

    public static String buildClear(String elementId) {
        return fetchElementName(elementId) + ".clear();";
    }

    public static String buildGetText(String elementId) {
        return fetchElementName(elementId) + ".getText();";
    }

    public static String fetchElementName(String elementId) {
        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(elementId)) {
                elementName = element.getName();
            }
        }
        return elementName;
    }

}