import org.json.JSONObject;

public class CommandBuilder {

    public static String buildFindElement(String rawCommandString) {
//        {"using":"xpath","value":"//XCUIElementTypeButton[contains(@name, 'driverStatus')]"}
        JSONObject jsonObj = new JSONObject(rawCommandString);
        String strategy = (String)jsonObj.get("using");
        String value = (String)jsonObj.get("value");
        return "driver.findElement(By." + strategy + "(\"" + value + "\");";
    }

    public static String buildFindElements(String rawCommandString) {
        JSONObject jsonObj = new JSONObject(rawCommandString);
        String strategy = (String)jsonObj.get("using");
        String value = (String)jsonObj.get("value");
        return "driver.findElements(By." + strategy + "(\"" + value + "\");";
    }

    public static String buildClickElement(String rawCommandString) {
        return "someElement.click();";
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
