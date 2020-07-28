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
                <a href="collections/${category.categoryId?c}/edit" class="btn btn-primary">Edit</a>
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
                    <a href="collections/new?type=Subject&parentId=${category.categoryId?c}" class="btn btn-outline-secondary">New Subject</a>
                [/#if]
                [#if category.categoryId != 0]
                    <a href="collections/new?type=Collection&parentId=${category.categoryId?c}" class="btn btn-outline-secondary">New Collection</a>
                [/#if]
            </nav>
        </header>

        <ul class="subcategories">
            [#list category.subcategories as child]
                <li class="subcategory-${child.type?lower_case} [#if (child.parentCategory!category) != category]subcategory-altlink[/#if]"><a href="collections/${child.categoryId?c}">${child.name}</a></li>
            [/#list]
        </ul>
    </section>

    [#if category.type == "Collection"]
        <section>
            <header>
                <h4>Titles</h4>
                <nav>
[#--                    <a class="btn btn-outline-secondary" href="#">Add Title</a>--]
[#--                    <a class="btn btn-outline-secondary" href="#">Add URL</a>--]
                </nav>
            </header>

            <table class="table">
                <thead>
                    <tr>
                        <th><input type=checkbox id=select-all></th>
                        <th>PI</th>
                        <th>Name</th>
                        <th>Registered</th>
                    </tr>
                </thead>
                <tbody>
                    [#list category.titles as title]
                        <tr>
                            <td><input type=checkbox class=row-checkbox></td>
                            <td>${title.pi?c!""}</td>
                            <td><a href="titles/${title.id?c}" [#if !title.visible]style="color:#999"[/#if]>${title.name}</a></td>
                            <td>${title.regDate.toLocalDate()}</td>
                        </tr>
                    [/#list]
                </tbody>
            </table>
            <script>
                document.getElementById("select-all").onchange = function (e) {
                    var self = this;
                    document.querySelectorAll(".row-checkbox").forEach(function (checkbox) {
                        checkbox.checked = self.checked;
                    });
                };
            </script>
        </section>
    [/#if]
[/@page]