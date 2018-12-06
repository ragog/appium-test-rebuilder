import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppiumLogParser {

    private final String REGEX_UDID = "\\b[0-9A-F]{8}\\b-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-\\b[0-9A-F]{12}\\b";
    private final String MARKER_REQUEST = "-->";
    private final String MARKER_RESPONSE = "<--";
    private final String RESPONSE_200 = "200";

    public ArrayList<String> extractCommands(File logFile) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(logFile));
        ArrayList<String> commands = new ArrayList<>();

        String st;
        while ((st = br.readLine()) != null) {

            if (st.contains(MARKER_REQUEST)) {

                if (!isGetSessionCommand(st)) { // Filters out getsession TODO find better way

                    String command = st.substring(st.lastIndexOf('/') + 1);
                    String requestType = getRequestType(st);
                    System.out.println(st);

                    commands.add(consumeCommand(command, requestType, br));

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
                    Pattern pattern = Pattern.compile(REGEX_UDID);
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

        if (command.equals("value")) {
            if (requestType.equals("POST")) {
                String elementId = "";
                String nextLine = br.readLine();
                String clippedNextLine = nextLine.replaceAll(".*] ", "");
                while (!nextLine.contains(MARKER_RESPONSE)) {
                    nextLine = br.readLine();
                }
                Pattern pattern = Pattern.compile(REGEX_UDID);
                Matcher matcher = pattern.matcher(nextLine);

                if (matcher.find()) {
                    elementId = matcher.group(0);
                }

                return CommandBuilder.buildSendKeys(elementId, clippedNextLine);
            }

            // TODO GET
        }

        if (command.equals("context")) {
            if (requestType.equals("GET")) {
                return CommandBuilder.buildGetContext();
            }
            if (requestType.equals("POST")) {
                return CommandBuilder.buildSetContext();
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
            while (!nextLine.contains(MARKER_RESPONSE)) {
                nextLine = br.readLine();
            }
            Pattern pattern = Pattern.compile(REGEX_UDID);
            Matcher matcher = pattern.matcher(nextLine);
            String elementId = "";
            if (matcher.find()) {
                elementId = matcher.group(0);
            }

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

        return "UnknownCommandPlaceholder";
    }
}
