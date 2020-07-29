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

    List<Category> getParents();

    Category getParentCategory();

    void setParentCategory(Category parent);

    List<Title> getTitles();

    String getType();

    String getFullName();

    List<Category> getBreadcrumbs();

    String getDescriptionSanitized();

    boolean isDisplayed();
}
