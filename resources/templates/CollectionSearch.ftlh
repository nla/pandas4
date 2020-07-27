[#include "layout.ftlh"]
[@page "Search Collections"]
    <form method=get class=form>
        <input class=form-control name="q" value="${q!''}">
    </form>
    <p></p>
    <ul>
        [#list results as result]
            <li><a href="/collections/${result.categoryId?c}">${result.fullName}</a></li>
        [/#list]
    </ul>
[/@page]