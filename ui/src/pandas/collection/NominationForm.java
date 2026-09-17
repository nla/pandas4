package pandas.collection;

import jakarta.validation.constraints.NotBlank;

public class NominationForm {
    @NotBlank(message = "Enter a URL")
    private String seedUrl;
    private String name;
    private String context;

    public String getSeedUrl() {
        return seedUrl;
    }

    public void setSeedUrl(String seedUrl) {
        this.seedUrl = seedUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
