package pandas.gatherer.core;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pywb")
public class PywbConfig {
    private String bindAddress = "127.0.0.1";
    private int port = 10910;

    public String getBindAddress() {
        return bindAddress;
    }

    public int getPort() {
        return port;
    }

    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
