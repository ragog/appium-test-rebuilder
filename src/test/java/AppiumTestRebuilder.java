import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppiumTestRebuilder {

    public static void main(String args[]) throws IOException {

        File file = new File("log.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        ArrayList<String> commands = new ArrayList<String>();

        String st;
        while ((st = br.readLine()) != null) {
            if (st.contains("-->")) {
                String requestType = st.substring(st.indexOf('>')+1, st.indexOf('/'));
                if (!(st.chars().filter(ch -> ch == '/').count() == 4)) { // Filters out getsession TODO find better way
                    String command = st.substring(st.lastIndexOf('/') + 1);
                    System.out.println(st);
                    if (command.equals("element")) {
                        String elementId = "";
                        String nextLine = br.readLine();
                        String clippedNextLine = nextLine.replaceAll(".*] ", "");
                        while (!nextLine.contains("<--")) {
                            if (nextLine.contains("200")) {
                                Pattern pattern = Pattern.compile("\\b[0-9A-F]{8}\\b-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-\\b[0-9A-F]{12}\\b");
                                Matcher matcher = pattern.matcher(nextLine);
                                if (matcher.find())
                                {
                                    elementId = matcher.group(0);
                                }
                            }
                            nextLine = br.readLine();
                        }
                        String finalCommand = CommandBuilder.buildFindElement(clippedNextLine, elementId);
                        if (finalCommand!=null) {
                            commands.add(finalCommand);
                        }
                    }
                    if (command.equals("context")) {
                        if (requestType.equals("GET")) {
                            commands.add(CommandBuilder.buildGetContext());
                        }
                        if (requestType.equals("POST")) {
                            commands.add(CommandBuilder.buildSetContext());
                        }
                    }
                    if (command.equals("alert_text")) {
                        if (requestType.equals("GET")) {
                            commands.add(CommandBuilder.buildGetAlertText());
                        }
                        if (requestType.equals("POST")) {
                            commands.add(CommandBuilder.buildSetAlertText());
                        }
                    }
                    if (command.equals("accept_alert")) {
                        if (requestType.equals("POST")) {
                            commands.add(CommandBuilder.buildAcceptAlert());
                        }
                    }
                    if (command.equals("dismiss_alert")) {
                        if (requestType.equals("POST")) {
                            commands.add(CommandBuilder.buildDismissAlert());
                        }
                    }
                    if (command.equals("click")) {
                        String nextLine = br.readLine();
                        while (!nextLine.contains("<--")) {
                            nextLine = br.readLine();
                        }
                        System.out.println(" "+nextLine);
                        Pattern pattern = Pattern.compile("\\b[0-9A-F]{8}\\b-[0-9A-F]{4}-[0-9A-F]{4}-[0-9A-F]{4}-\\b[0-9A-F]{12}\\b");
                        Matcher matcher = pattern.matcher(nextLine);
                        String elementId = "";
                        if (matcher.find())
                        {
                            elementId = matcher.group(0);
                        }

                        commands.add(CommandBuilder.buildClickElement(elementId));
                    }
                }
            }
        }

        System.out.println("--------------------");
        for (String command : commands) {
            System.out.println(command);
        }

    }

}

