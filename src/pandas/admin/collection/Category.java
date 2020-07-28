package pandas.admin.collection;

import java.util.*;

public interface Category {
    Long getCategoryId();

    String getName();

    void setName(String name);

    String getThumbnailUrl();

    void setThumbnailUrl(String thumbnailUrl);

    String getDescription();

    void setDescription(String description);

    List<Category> getSubcategories();

    Category getParentCategory();

    void setParentCategory(Category parent);

    List<Title> getTitles();

    String getType();

    String getFullName();
}
