[#include "layout.ftlh"]
[@page "${category.name}"]
    <style>
        .subcategory-subject { font-weight: bold; }
        .subcategory-altlink { font-style: italic; }
    </style>

    <p>${category.description!""}</p>

    <nav class="actions">
        <a href="/categories/${category.categoryId?c}/edit" class="btn btn-default">Edit</a>
    </nav>

    <h4>Subcollections</h4>
    <ul style="column-count: 4; column-gap: 20px" class="subcategories">
        [#list category.subcategories as child]
            <li class="subcategory-${child.type?lower_case} [#if (child.parentCategory!category) != category]subcategory-altlink[/#if]"><a href="/categories/${child.categoryId?c}">${child.name}</a></li>
        [/#list]
    </ul>

    <nav class="actions">
        [#if category.type == "Subject"]
            <a href="/categories/new?type=Subject&parentId=${category.categoryId?c}" class="btn btn-default">New Subject</a>
        [/#if]
        [#if category.categoryId != 0]
            <a href="/categories/new?type=Collection&parentId=${category.categoryId?c}" class="btn btn-default">New Subcollection</a>
        [/#if]
    </nav>

    [#if category.type == "Collection"]
        <h4>Sites</h4>

        <table>
            [#list category.sites as site]
                <tr>
                    <td><input type=checkbox></td>
                    <td [#if !site.visible]style="color:#999"[/#if]>${site.name}</td>
                </tr>
            [/#list]
        </table>

        <nav class="actions">
            <a class="btn btn-default">Add Title</a>
            <a class="btn btn-default">Add URL</a>
        </nav>
    [/#if]
[/@page]