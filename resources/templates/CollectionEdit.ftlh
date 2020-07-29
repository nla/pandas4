[#include "_layout.ftlh"]
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

        <div class="form-group">
            Parents:
            <table>
                [#list category.parents as parent]
                    <tr>
                        <td><a href="collections/${parent.categoryId?c}">${parent.fullName}</a></td>
                        <td></td>
                    </tr>
                [/#list]
            </table>
        </div>

        <div>
            <button type="submit" class="btn btn-primary">Save</button>
            <a href="collections/${(category.categoryId!parentId!0)?c}" class="btn btn-secondary">Cancel</a>
            [#if category.categoryId??]
                <button type="submit" formaction="collections/${category.categoryId?c}/delete" class="btn btn-danger float-right">Delete</button>
            [/#if]
        </div>
    </form>
[/@page]