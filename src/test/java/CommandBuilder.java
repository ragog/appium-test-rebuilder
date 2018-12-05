import org.json.JSONObject;

import java.util.ArrayList;

public class CommandBuilder {

    private static ArrayList<Element> elementList = new ArrayList<>();

    public static String buildFindElement(String rawCommandString, String elementId) {
        JSONObject jsonObj = new JSONObject(rawCommandString);
        String strategy = (String)jsonObj.get("using");
        String value = (String)jsonObj.get("value");
        if (!elementList.isEmpty()) {
            if (!value.equals(elementList.get(elementList.size()-1).getStrategyValue())) { // skips if duplicate of previous command TODO less hacky
                Element element = new Element(elementId, strategy, value);
                elementList.add(element);
                if (strategy.equals("class name")) {
                    strategy = "className";
                }
                return "MobileElement " + element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\"));";
            }
        } else {
            Element element = new Element(elementId, strategy, value);
            elementList.add(element);
            if (strategy.equals("class name")) {
                strategy = "className";
            }
            return "MobileElement " + element.getName() + " = driver.findElement(By." + strategy + "(\"" + value + "\"));";
        }
        return null;
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
        return elementName + ".click();"; // TODO
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


}
