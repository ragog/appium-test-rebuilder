package com.saucelabs;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppiumLogParser {

    private final String REGEX_ELEMENT_UDID_IOS = "\\b[0-9A-F]{8}\\b-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-\\b[0-9A-F]{12}\\b";
    private final String REGEX_SESSION_UDID = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
    private final String MARKER_REQUEST = "-->";
    private final String MARKER_RESPONSE = "<--";
    private final String RESPONSE_200 = "200";
    private final String MARKER_FIND_ELEMENT_RESULT = "driver.findElement() result:"; // TODO get smarter please

    private boolean optionPrintRequests;
    private String platform;

    private ArrayList<String> commands = new ArrayList<>();
    private ArrayList<String> requests = new ArrayList<>();

    public AppiumLogParser(HashMap<String, Boolean> options){
        optionPrintRequests = options.get("printRequests");
    }

    public ArrayList<String> parseLog(File logFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(logFile));

        String st;
        while ((st = br.readLine()) != null) {

            if (st.contains(MARKER_REQUEST)) {

                String requestType = getRequestType(st);

                if (!isGetSessionCommand(st) || requestType.equals("DELETE")) { // Filters out session calls apart form delete.. TODO find better way

                    String command = st.substring(st.lastIndexOf('/') + 1);

                    if (optionPrintRequests) {
                        requests.add(st);
                    }

                    String convertedCommand = consumeCommand(command, requestType, br);
                    if (!convertedCommand.isEmpty()) {
                        commands.add(convertedCommand);
                    }
                }
            }
        }

        return commands;

    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public ArrayList<String> getRequests() {
        return requests;
    }

    public String consumeCommand(String command, String requestType, BufferedReader br) throws IOException {

        if (command.equals("element")) {
            String nextLine = br.readLine();
            String clippedNextLine = nextLine.replaceAll(".*] ", "");

            String elementId = platform.equalsIgnoreCase("android") ? elementFindAndroid(br) : elementFindIOS(br);

            return CommandBuilder.buildFindElement(clippedNextLine, elementId);
        }

        if (command.equals("elements")) {
            List<String> elementIds;
            String nextLine = br.readLine();
            String clippedNextLine = nextLine.replaceAll(".*] ", "");

            elementIds = platform.equalsIgnoreCase("android") ? elementsFindAndroid(br) : elementsFindIOS(br);

            return CommandBuilder.buildFindElements(clippedNextLine, elementIds);
        }

        if (command.equals("value")) {
            if (requestType.equals("POST")) {
                String nextLine = br.readLine();
                String clippedNextLine = nextLine.replaceAll(".*] ", "");
                String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);

                return CommandBuilder.buildSendKeys(elementId, clippedNextLine);
            }

            // TODO GET
        }

        if (command.equals("keys")) { // TODO distinguish between sendkeys and setvalue
            if (requestType.equals("POST")) {
                String nextLine = br.readLine();
                String clippedNextLine = nextLine.replaceAll(".*] ", "");
                String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
                return CommandBuilder.buildSendKeys(elementId, clippedNextLine);
            }

        }

        if (command.equals("context")) {
            if (requestType.equals("POST")) {
                return CommandBuilder.buildSetContext();
            }
            if (requestType.equals("GET")) {
                return ""; // ignore
            }
        }
        if (command.equals("alert_text")) {
            if (requestType.equals("GET")) {
                return CommandBuilder.buildGetAlertText();
            }
            if (requestType.equals("POST")) {
                return CommandBuilder.buildSetAlertText();
            }
        }
        if (command.equals("accept_alert")) {
            if (requestType.equals("POST")) {
                return CommandBuilder.buildAcceptAlert();
            }
        }
        if (command.equals("dismiss_alert")) {
            if (requestType.equals("POST")) {
                return CommandBuilder.buildDismissAlert();
            }
        }
        if (command.equals("click")) {
            String nextLine = br.readLine();
            String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
            return CommandBuilder.buildClickElement(elementId);
        }
        if (command.equals("screenshot")) {
            if (requestType.equals("GET")) {
                return CommandBuilder.buildGetScreenshot();
            }
        }
        if (command.equals("timeouts")) {
            if (requestType.equals("POST")) {
                String nextLine = br.readLine();
                String clippedNextLine = nextLine.replaceAll(".*] ", "");
                return CommandBuilder.buildTimeouts(clippedNextLine);
            }
        }
        if (command.equals("session")) {
            String nextLine = br.readLine();
            String clippedNextLine = nextLine.replaceAll(".*] ", "");
            JSONObject elementJSON = new JSONObject(clippedNextLine);
            JSONObject innerJSONElement = elementJSON.getJSONObject("desiredCapabilities");
            platform = (String)innerJSONElement.get("platformName");
            return CommandBuilder.buildInitSession(clippedNextLine);
        }
        if (command.equals("displayed")) {
            String nextLine = br.readLine();
            String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
            return CommandBuilder.buildIsDisplayed(elementId);
        }
        if (command.equals("name")) {
            String nextLine = br.readLine();
            String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
            return CommandBuilder.buildAttributeName(elementId);
        }
        if (command.equals("location")) {
            String nextLine = br.readLine();
            String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
            return CommandBuilder.buildLocation(elementId);
        }
        if (command.equals("size")) {
            String nextLine = br.readLine();
            String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
            return CommandBuilder.buildSize(elementId);
        }
        if (command.equals("clear")) {
            String nextLine = br.readLine();
            String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
            return CommandBuilder.buildClear(elementId);
        }
        if (command.equals("text")) {
            if (requestType.equals("GET")) {
                String nextLine = br.readLine();
                String elementId = platform.equalsIgnoreCase("android") ? fetchElementIdFromResponseAndroid(br, nextLine) : fetchElementIdFromResponseIOS(br);
                return CommandBuilder.buildGetText(elementId);
            }
        }
        if (command.equals("perform")) {
            String nextLine = br.readLine();
            String clippedNextLine = nextLine.replaceAll(".*] ", "");

            return CommandBuilder.buildTouchActionPerform(clippedNextLine);
        }
        if (command.matches(REGEX_SESSION_UDID)) {
            if (requestType.equals("DELETE")) {
                return CommandBuilder.buildDeleteSessionCommand();
            }
        }
        return "UnknownCommandPlaceholder";
    }

    private boolean isGetSessionCommand(String st) {
        return (st.chars().filter(ch -> ch == '/').count() == 4);
    }

    private String getRequestType(String st) {
        return st.substring(st.indexOf('>')+2, st.indexOf('/')-1); // TODO less hacky bitte
    }

    private String fetchElementIdFromResponseIOS(BufferedReader br) throws IOException {

        String nextLine = br.readLine();
        while (!nextLine.contains(MARKER_RESPONSE)) {
            nextLine = br.readLine();
        }

        Pattern pattern = Pattern.compile(REGEX_ELEMENT_UDID_IOS);
        Matcher matcher = pattern.matcher(nextLine);
        String elementId = "";
        if (matcher.find()) {
            elementId = matcher.group(0);
        }
        return elementId;
    }

    private String fetchElementIdFromResponseAndroid(BufferedReader br, String currentLine) throws IOException {

        String elementId = "";
        String clippedLine = currentLine.substring(currentLine.indexOf('{'));

        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(clippedLine);
            elementId = (String)jsonObject.get("id");
        } catch (JSONException obj) {
            String nextLine = br.readLine();
            elementId = nextLine.substring(nextLine.indexOf('"')+1, nextLine.indexOf(',')-1); // TODO hacky...
        }

        return elementId;

    }

    private String elementFindIOS(BufferedReader br) throws IOException {
        String elementId = "";
        String nextLine = br.readLine();

        while (!nextLine.contains(MARKER_RESPONSE)) {
            if (nextLine.contains(RESPONSE_200)) {
                Pattern pattern = Pattern.compile(REGEX_ELEMENT_UDID_IOS);
                Matcher matcher = pattern.matcher(nextLine);
                if (matcher.find())
                {
                    elementId = matcher.group(0);
                }
            }
            nextLine = br.readLine();
        }
        return elementId;
    }

    private ArrayList<String> elementsFindIOS(BufferedReader br) throws IOException {
        ArrayList<String> elementIds = new ArrayList<>();

        String nextLine = br.readLine();
        while (!nextLine.contains(MARKER_RESPONSE)) {
            if (nextLine.contains(RESPONSE_200)) {
                Pattern pattern = Pattern.compile(REGEX_ELEMENT_UDID_IOS);
                Matcher matcher = pattern.matcher(nextLine);
                while (matcher.find()) {
                    elementIds.add(matcher.group(0));
                }
            }
            nextLine = br.readLine();
        }
        return elementIds;
    }

    private String elementFindAndroid(BufferedReader br) throws IOException {

        String elementId = "";
        String nextLine = br.readLine();

        while (!nextLine.contains(MARKER_RESPONSE)) {
            if (nextLine.contains(MARKER_FIND_ELEMENT_RESULT)) {
                String clippedLine = nextLine.substring(nextLine.indexOf('{'));

                JSONObject jsonObject = new JSONObject(clippedLine);

                return (String)jsonObject.get("ELEMENT");
            }
            nextLine = br.readLine();
        }
        return elementId;
    }

    private ArrayList<String> elementsFindAndroid(BufferedReader br) throws IOException { // TODO test for /elements
        ArrayList<String> elementIds = new ArrayList<>();

        String nextLine = br.readLine();
        while (!nextLine.contains(MARKER_RESPONSE)) {
            if (nextLine.contains(MARKER_FIND_ELEMENT_RESULT)) {
                String clippedLine = nextLine.substring(nextLine.indexOf('{'));

                JSONObject jsonObject = new JSONObject(clippedLine);

                elementIds.add((String)jsonObject.get("ELEMENT")); // TODO wrong, fix
            }
            nextLine = br.readLine();
        }
        return elementIds;
    }

}
