package pandas.admin.render;

public class RenderImage {
    private RenderImageType type;
    private String src;
    private String dataUrl;

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public RenderImageType getType() {
        return type;
    }

    public void setType(RenderImageType type) {
        this.type = type;
    }
}
