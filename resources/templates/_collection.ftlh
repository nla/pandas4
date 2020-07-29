[#macro collectionLink collection]
    [#list collection.breadcrumbs as breadcrumb]
        <a class="category-${breadcrumb.type?lower_case}"
           href="collections/${breadcrumb.categoryId?c}">${breadcrumb.name}</a>
        [#if !breadcrumb?is_last] / [/#if]
    [/#list]
[/#macro]