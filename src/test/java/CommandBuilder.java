import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class CommandBuilder {

    private static ArrayList<Element> elementList = new ArrayList<>();

    public static String buildFindElement(String rawCommandString, String elementId) {

        JSONObject elementJSON = new JSONObject(rawCommandString);
        String strategy = (String)elementJSON.get("using");
        String value = (String)elementJSON.get("value");

        if (!elementList.isEmpty()) {

            Element element = new Element(elementId, "element"+elementList.size(), strategy, value);
            elementList.add(element);

            if (strategy.equals("class name")) { // one-off to take care of the selector TODO check for other selectors too
                strategy = "className";
            }

            return "MobileElement " + element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\"));";

        }

        Element element = new Element(elementId, "element"+elementList.size(), strategy, value);
        elementList.add(element);

        if (strategy.equals("class name")) {
            strategy = "className";
        }

        return "MobileElement " + element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\"));";

    }

    public static String buildFindElements(String rawCommandString) {
        JSONObject jsonObj = new JSONObject(rawCommandString);
        String strategy = (String)jsonObj.get("using");
        String value = (String)jsonObj.get("value");
        // TODO any way to create element to add to elementList?

        return "driver.findElements(By." + strategy + "(\"" + value + "\"));";
    }

    public static String buildClickElement(String id) {

        String elementName = "";

        for (Element element : elementList) {
            if (element.getId().equals(id)) {
                elementName = element.getName();
            }
        }
        return elementName + ".click();";
    }

    public static String buildSendKeys(String id, String rawCommandString){

        JSONObject elementJSON = new JSONObject(rawCommandString);
        String appiumId = (String)elementJSON.get("id");
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


}
