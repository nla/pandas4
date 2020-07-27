[#include "layout.ftlh"]
[@page title="${category.name}" header=false]
    <header>
        <ol>
            [#list breadcrumbs as breadcrumb]
                [#if breadcrumb?is_last]
                    <li class="active">${breadcrumb.name}</li>
                [#else]
                    <li><a href="${breadcrumb.href}">${breadcrumb.name}</a></li>
                [/#if]
            [/#list]
        </ol>
        <nav>
            [#if category.categoryId != 0]
                <a href="/collections/${category.categoryId?c}/edit" class="btn btn-primary">Edit</a>
            [/#if]
        </nav>
    </header>

    <style>
        .subcategories {
            list-style-type: none;
            padding-left: 0;
            column-count: 4;
            column-gap: 1rem;
        }
        .subcategories > li { break-inside: avoid-column; }
        .subcategory-subject { font-weight: bold; }
        .subcategory-altlink { font-style: italic; }
    </style>

    <p>${category.description!""}</p>

    <section>
        <header>
            <h4>Subcollections</h4>
            <nav>
                [#if category.type == "Subject"]
                    <a href="/collections/new?type=Subject&parentId=${category.categoryId?c}" class="btn btn-outline-secondary">New Subject</a>
                [/#if]
                [#if category.categoryId != 0]
                    <a href="/collections/new?type=Collection&parentId=${category.categoryId?c}" class="btn btn-outline-secondary">New Collection</a>
                [/#if]
            </nav>
        </header>

        <ul class="subcategories">
            [#list category.subcategories as child]
                <li class="subcategory-${child.type?lower_case} [#if (child.parentCategory!category) != category]subcategory-altlink[/#if]"><a href="/collections/${child.categoryId?c}">${child.name}</a></li>
            [/#list]
        </ul>
    </section>

    [#if category.type == "Collection"]
        <section>
            <header>
                <h4>Sites</h4>
                <nav>
                    <a class="btn btn-outline-secondary" href="#">Add Title</a>
                    <a class="btn btn-outline-secondary" href="#">Add URL</a>
                </nav>
            </header>

            <table>
                [#list category.sites as site]
                    <tr>
                        <td><input type=checkbox></td>
                        <td [#if !site.visible]style="color:#999"[/#if]>${site.name}</td>
                    </tr>
                [/#list]
            </table>
        </section>
    [/#if]
[/@page]