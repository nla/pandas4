package pandas.admin.render;

import org.junit.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class BrowserTest {
    @Test
    public void test() throws IOException {
        try (Browser browser = new Browser();
             Browser.Tab tab = browser.createTab()) {
            tab.navigate("http://localhost/");

        }
    }

}