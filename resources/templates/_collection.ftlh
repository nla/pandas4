[#macro collectionLink collection]
    [#list collection.breadcrumbs as breadcrumb]
        <a class="category-${breadcrumb.type?lower_case}"
           href="collections/${breadcrumb.categoryId?c}">${breadcrumb.name}</a>
        [#if !breadcrumb?is_last] / [/#if]
    [/#list]
[/#macro]

[#macro subjectItem subject]
    <li>
        <a href="subjects/${subject.id?c}">${subject.name}</a>
        <span class="badge badge-light">${subject.titleCount}</span>
        [#if subject.collectionCount > 0]<a class="badge badge-info" href="collections/search?subjectId=${subject.id?c}">${subject.collectionCount}</a>[/#if]
    </li>
[/#macro]