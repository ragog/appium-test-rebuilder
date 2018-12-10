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

            if (strategy.equals("class name")) { // one-off to take care of the selector TODO check for other selectors too
                strategy = "className";
            }

            return "MobileElement " + element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\"));";

        }

        String elementName = "element"+elementList.size();;
        for (Element element : elementList) {
            if (element.getId().equals(elementId)) {
                elementName = element.getName();
            }
        }

        Element element = new Element(elementId, elementName, strategy, value);
        elementList.add(element);

        if (strategy.equals("class name")) {
            strategy = "className";
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

    public static String buildClickElement(String id) {

        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(id)) {
                elementName = element.getName();
            }
        }

        if (!elementName.isEmpty()) {
            return elementName + ".click();";
        }

        int elementIndex = -1;

        for (ArrayList<Element> list : elementsList) {
            for (int i = 0; i<list.size(); i++) {
                if (list.get(i).getId().equals(id)) {
                    elementIndex = i;
                }
            }
        }

        return "list" + elementsList.size() + "[" + elementIndex + "].click();";

    }

    public static String buildSendKeys(String id, String rawCommandString){

        JSONObject elementJSON = new JSONObject(rawCommandString);
        JSONArray values = (JSONArray)elementJSON.get("value");
        String value = (String)values.get(0);

        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(id)) {
                elementName = element.getName();
            }
        }
        return elementName + ".sendKeys(\"" + value + "\");";
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
        String capabilities = "DesiredCapabilities desiredCapabilities = new DesiredCapabilities()\n";

        for (String key : jsonMap.keySet()) {
            String value = jsonMap.get(key).toString();
            capabilities += "desiredCapabilities.setCapability(\"" + key + "\", \"" + value +"\");\n";
        }

        if (platform.equalsIgnoreCase("android")) {
            command = capabilities + "AndroidDriver driver = new AndroidDriver(url, desiredCapabilities)"; // TODO URL
        } else if (platform.equalsIgnoreCase("ios")) {
            command = capabilities + "IOSDriver driver = new IOSDriver(url, desiredCapabilities)"; // TODO URL
        }

        return command;
    }

    public static String buildTimeouts(String rawCommandString) {
        JSONObject elementJSON = new JSONObject(rawCommandString);
        int ms = (int)elementJSON.get("ms");
        return "driver.manage().timeouts().implicitlyWait("+ ms +", TimeUnit.MILLISECONDS)";
    }

    public static String buildIsDisplayed(String id) {
        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(id)) {
                elementName = element.getName();
            }
        }
        return elementName + ".isDisplayed();";
    }

    public static String buildAttributeName(String id) {
        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(id)) {
                elementName = element.getName();
            }
        }
        return elementName + ".getAttribute(\"content-desc\");";
    }

    public static String buildDeleteSessionCommand() {
        return "driver.quit()";
    }

    public static String buildLocation(String id) {
        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(id)) {
                elementName = element.getName();
            }
        }
        return elementName + ".getLocation()";
    }

    public static String buildSize(String id) {
        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(id)) {
                elementName = element.getName();
            }
        }
        return elementName + ".getSize()";
    }

}