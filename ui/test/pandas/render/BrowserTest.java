package pandas.render;

import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;

public class BrowserTest {
    @Test
    public void test() throws IOException {
        try (Browser browser = new Browser();
             Browser.Tab tab = browser.createTab()) {
            tab.navigate("http://localhost/");
        } catch (IOException e) {
            // ignore test if browser isn't installed
            if (e.getMessage().contains("execute")) {
                Assume.assumeNoException(e);
            }
        }
    }

}