[#include "layout.ftlh"]
[@page "${(category.categoryId??)?then('Edit', 'New')} ${category.type}"]
    <form method=post class=form>
        <div class="form-group">
            <label for="name">Name:</label>
            <input class="form-control" id="name" name="name" value="${category.name!''}" required>
        </div>
        <div class="form-group">
            <label for="description">Description:</label>
            <textarea class="form-control" id="description" name="description" rows="3">${category.description!''}</textarea>
        </div>

        <div>
            <div class="pull-right">
                <button type="submit" class="btn btn-primary">Save</button>
                <a href="/categories/${(category.categoryId!parentId!0)?c}" class="btn btn-default">Cancel</a>
            </div>
            [#if category.categoryId??]
                <button type="submit" formaction="subjects/${category.categoryId?c}/delete" class="btn btn-danger">Delete</button>
            [/#if]
        </div>
    </form>
[/@page]