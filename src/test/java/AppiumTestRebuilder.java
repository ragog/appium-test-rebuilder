import java.io.*;
import java.util.ArrayList;

public class AppiumTestRebuilder {

    public static void main(String args[]) throws IOException {

        File file = new File("log.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        ArrayList<String> commands = new ArrayList<String>();

        String st;
        while ((st = br.readLine()) != null) {
            if (st.contains("-->")) {
                String requestType = st.substring(st.indexOf('>')+1, st.indexOf('/'));
                String command = st.substring(st.lastIndexOf('/') + 1);
                System.out.println(st);
                if (command.equals("element")) {
                    String nextLine = br.readLine();
                    String clippedNextLine = nextLine.replaceAll(".*] ", "");
                    commands.add(CommandBuilder.buildFindElement(clippedNextLine));
                }
                if (command.equals("context")) {
                    if (requestType.equals("GET")) {
                        commands.add(CommandBuilder.buildFindElement(CommandBuilder.buildGetContext()));
                    }
                    if (requestType.equals("POST")) {
                        commands.add(CommandBuilder.buildFindElement(CommandBuilder.buildSetContext()));
                    }
                }
                if (command.equals("alert_text")) {
                    if (requestType.equals("GET")) {
                        commands.add(CommandBuilder.buildGetAlertText());
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

