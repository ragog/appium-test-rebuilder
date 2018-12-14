import com.saucelabs.AppiumTestRebuilder;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class WikiTest {

    @BeforeClass
    public void setup () {
        System.setProperty("optionPrintToFile", "true");
        System.setProperty("optionPrintRequests", "true");
    }

    @Test
    public void logTestNoUnrecognizedCommand() throws IOException {
        ArrayList<String> list = new ArrayList<>(Arrays.asList("log.txt", "log-calc.txt", "log-wiki.txt", "log-kbc.txt"));
        for (String path : list) {
            AppiumTestRebuilder.main(new String[] { path });
            String content = new String(Files.readAllBytes(Paths.get("test-rebuilt-from-" + path)));
            Assert.assertFalse(content.contains("UnknownCommandPlaceholder"));
        }

    }



}
