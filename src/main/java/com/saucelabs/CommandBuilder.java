package com.saucelabs;

import com.saucelabs.util.Strings;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommandBuilder {

    private static ArrayList<Element> elementList = new ArrayList<>();
    private static ArrayList<ArrayList<Element>> elementsList = new ArrayList<>();

    public static String buildFindElement(String rawCommandString, String elementId) {

        JSONObject elementJSON = new JSONObject(rawCommandString);
        String strategy = (String)elementJSON.get("using");
        String value = (String)elementJSON.get("value");

        if (!elementList.isEmpty()) {

            for (Element element : elementList) {
                if (element.getId().equals(elementId)) {
                    return element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\");";
                }
            }

            String elementName = "element" + elementList.size();
            Element element = new Element(elementId, elementName, strategy, value);
            elementList.add(element);

            if (strategy.equals("class name")) {
                strategy = "className";
            }
            if (strategy.equals("css selector")) {
                strategy = "cssSelector";
            }

            return "MobileElement " + element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\"));";

        }

        String elementName = "element"+elementList.size();
        for (Element element : elementList) {
            if (element.getId().equals(elementId)) {
                elementName = element.getName();
            }
        }

        // TODO duplicated code

        Element element = new Element(elementId, elementName, strategy, value);
        elementList.add(element);

        if (strategy.equals("class name")) {
            strategy = "className";
        }
        if (strategy.equals("css selector")) {
            strategy = "cssSelector";
        }

        return "MobileElement " + element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\"));";

    }

    public static String buildFindElements(String rawCommandString, List<String> elementIds) {
        ArrayList<Element> list = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(rawCommandString);
        String strategy = (String)jsonObject.get("using");
        String value = (String)jsonObject.get("value");

        String elementName = "element" + elementList.size();

        for (String elementId: elementIds) {
            for (Element element : elementList) {
                if (element.getId().equals(elementId)) {
                    elementName = element.getName();
                }
            }

             list.add(new Element(elementId, elementName, strategy, value));

        }

        elementsList.add(list);

        return "ArrayList<MobileElement> list" + elementsList.size() + " = driver.findElements(By." + strategy + "(\"" + value + "\"));"; // TODO not finished - get references to elements in list

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

    public static String buildSendKeys(String elementId, String rawCommandString){

        JSONObject elementJSON = new JSONObject(rawCommandString);
        JSONArray values = (JSONArray)elementJSON.get("value");
        String value = (String)values.get(0);

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

    public static String buildInitSession(String rawCommandString) {
        JSONObject elementJSON = new JSONObject(rawCommandString);
        JSONObject innerJSONElement = elementJSON.getJSONObject("desiredCapabilities");
        String platform = (String)innerJSONElement.get("platformName");
        String command = "";

        Map<String, Object> jsonMap = innerJSONElement.toMap();
        String capabilities = "DesiredCapabilities desiredCapabilities = new DesiredCapabilities();\n";

        for (String key : jsonMap.keySet()) {
            String value = jsonMap.get(key).toString();
            capabilities += "desiredCapabilities.setCapability(\"" + key + "\", \"" + value +"\");\n";
        }

        if (platform.equalsIgnoreCase("android")) {
            command = capabilities + Strings.DRIVER_INIT_ANDROID;
        } else if (platform.equalsIgnoreCase("ios")) {
            command = capabilities + Strings.DRIVER_INIT_IOS;
        }

        return command;
    }

    public static String buildTimeouts(String rawCommandString) {
        JSONObject elementJSON = new JSONObject(rawCommandString);
        int ms = (int)elementJSON.get("ms");
        return "driver.manage().timeouts().implicitlyWait("+ ms +", TimeUnit.MILLISECONDS);";
    }

    public static String buildTouchActionPerform(String rawCommandString) {
        JSONObject jsonObject = new JSONObject(rawCommandString);
        JSONArray jsonArray = jsonObject.getJSONArray("actions");
        JSONObject action = (JSONObject)jsonArray.get(0);
        String actionField = (String)action.get("action");
        if (!actionField.equals("tap")) {
            return ""; // TODO handle all actions in chain + support all types of action
        }
        JSONObject optionsField = (JSONObject)action.get("options");
        int coordinateX = (int)optionsField.get("x");
        int coordinateY = (int)optionsField.get("y");

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