[#include "_layout.ftlh"]
[#include "_collection.ftlh"]

[@page "Add Parents"]
    <form method=get class=form>
        <label>
            <input class=form-control name="q" value="${q!''}">
        </label>
        <button type=submit class="btn btn-primary">Search</button>
    </form>
    <p></p>
    <form method=post>
        [#list results as result]
            <label style="display: block"><input type=checkbox name=id value="${result.categoryId?c}">
                [@collectionLink result /]</label>
        [/#list]
        <nav>
            [#if q??]
                <button type="submit" class="btn btn-primary">Add Selected</button>
            [/#if]
            <a href="collections/${category.categoryId?c}" class="btn btn-secondary">Cancel</a>
        </nav>
    </form>
[/@page]