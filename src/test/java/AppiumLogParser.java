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

    private final String REGEX_ELEMENT_UDID = "\\b[0-9A-F]{8}\\b-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-\\b[0-9A-F]{12}\\b";
    private final String REGEX_SESSION_UDID = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b";
    private final String MARKER_REQUEST = "-->";
    private final String MARKER_RESPONSE = "<--";
    private final String RESPONSE_200 = "200";

    private boolean optionPrintRequests;

    public AppiumLogParser(HashMap<String, Boolean> options){
        optionPrintRequests = options.get("printRequests");
    }

    public ArrayList<String> extractCommands(File logFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(logFile));
        ArrayList<String> commands = new ArrayList<>();

        String st;
        while ((st = br.readLine()) != null) {

            if (st.contains(MARKER_REQUEST)) {

                String requestType = getRequestType(st);

                if (!isGetSessionCommand(st) || requestType.equals("DELETE")) { // Filters out session calls apart form delete.. TODO find better way

                    String command = st.substring(st.lastIndexOf('/') + 1);

                    if (optionPrintRequests) {
                        System.out.println(st);
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

    public boolean isGetSessionCommand(String st) {
        return (st.chars().filter(ch -> ch == '/').count() == 4);
    }

    public String getRequestType(String st) {
        return st.substring(st.indexOf('>')+2, st.indexOf('/')-1); // TODO less hacky bitte
    }

    public String consumeCommand(String command, String requestType, BufferedReader br) throws IOException {

        if (command.equals("element")) {
            String elementId = "";
            String nextLine = br.readLine();
            String clippedNextLine = nextLine.replaceAll(".*] ", "");
            while (!nextLine.contains(MARKER_RESPONSE)) {
                if (nextLine.contains(RESPONSE_200)) {
                    Pattern pattern = Pattern.compile(REGEX_ELEMENT_UDID);
                    Matcher matcher = pattern.matcher(nextLine);
                    if (matcher.find())
                    {
                        elementId = matcher.group(0);
                    }
                }
                nextLine = br.readLine();
            }
            return CommandBuilder.buildFindElement(clippedNextLine, elementId);
        }

        if (command.equals("elements")) {
            List<String> elementIds = new ArrayList<>();
            String nextLine = br.readLine();
            String clippedNextLine = nextLine.replaceAll(".*] ", "");
            while (!nextLine.contains(MARKER_RESPONSE)) {
                if (nextLine.contains(RESPONSE_200)) {
                    Pattern pattern = Pattern.compile(REGEX_ELEMENT_UDID);
                    Matcher matcher = pattern.matcher(nextLine);
                    while (matcher.find()) {
                        elementIds.add(matcher.group(0));
                    }
                }
                nextLine = br.readLine();
            }
            return CommandBuilder.buildFindElements(clippedNextLine, elementIds);
        }

        if (command.equals("value")) {
            if (requestType.equals("POST")) {
                String nextLine = br.readLine();
                String clippedNextLine = nextLine.replaceAll(".*] ", "");

                String elementId = fetchElementIdFromResponse(br);
                return CommandBuilder.buildSendKeys(elementId, clippedNextLine);
            }

            // TODO GET
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
            String elementId = fetchElementIdFromResponse(br);
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
            return CommandBuilder.buildInitSession(clippedNextLine);
        }
        if (command.equals("displayed")) {
            String elementId = fetchElementIdFromResponse(br);
            return CommandBuilder.buildIsDisplayed(elementId);
        }
        if (command.equals("name")) {
            String elementId = fetchElementIdFromResponse(br);
            return CommandBuilder.buildAttributeName(elementId);
        }
        if (command.matches(REGEX_SESSION_UDID)) {
            if (requestType.equals("DELETE")) {
                return CommandBuilder.buildDeleteSessionCommand();
            }
        }
        return "UnknownCommandPlaceholder";
    }

    private String fetchElementIdFromResponse(BufferedReader br) throws IOException {

        String nextLine = br.readLine();
        while (!nextLine.contains(MARKER_RESPONSE)) {
            nextLine = br.readLine();
        }

        Pattern pattern = Pattern.compile(REGEX_ELEMENT_UDID);
        Matcher matcher = pattern.matcher(nextLine);
        String elementId = "";
        if (matcher.find()) {
            elementId = matcher.group(0);
        }
        return elementId;
    }
}
