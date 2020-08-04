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
        <a class="badge badge-light" href="titles?subject=${subject.id?c}">${subject.titleCount}</a>
        [#if subject.collectionCount > 0]<a class="badge badge-info" href="collections?subject=${subject.id?c}">${subject.collectionCount}</a>[/#if]
    </li>
[/#macro]