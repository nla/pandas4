package pandas.admin.render;

import java.util.Date;
import java.util.List;

public class Render {
    private String url;
    private Date renderDate;
    private String title;
    private List<RenderImage> images;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRenderDate(Date renderDate) {
        this.renderDate = renderDate;
    }

    public Date getRenderDate() {
        return renderDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public List<RenderImage> getImages() {
        return images;
    }

    public void setImages(List<RenderImage> images) {
        this.images = images;
    }
}
