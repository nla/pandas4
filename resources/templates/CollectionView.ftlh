[#include "_layout.ftlh"]
[#include "_collection.ftlh"]

[#macro collectionLink collection]
    [#list collection.breadcrumbs as breadcrumb]
        <a class="category-${breadcrumb.type?lower_case}"
           href="collections/${breadcrumb.categoryId?c}">${breadcrumb.name}</a>
        [#if !breadcrumb?is_last] / [/#if]
    [/#list]
[/#macro]

[@page title="${category.name}" header=false]
    <style>
        .subcategories {
            list-style-type: none;
            padding-left: 0;
            column-count: 4;
            column-gap: 1rem;
        }
        .subcategories > li {
            break-inside: avoid-column;
            overflow: hidden;
            padding-left: 1.5em;
            text-indent:-1.5em;
        }
        .category-subject { font-weight: bold; }
        .category-altlink { font-style: italic; }
        .category-hidden a { color: #999; }
    </style>

    <header>
        <h1>${category.name}</h1>
        <nav>
            [#if category.categoryId != 0]
                <a href="collections/${category.categoryId?c}/edit" class="btn btn-primary">Edit</a>
            [/#if]
        </nav>
    </header>


    <p>${(category.descriptionSanitized!"")?no_esc}</p>

    [#if category.categoryId != 0]
        <section>
            <header>
                <h4>Parents</h4>
                <nav>
                    <a href="collections/${category.categoryId?c}/add-parents" class="btn btn-outline-secondary">Add</a>
                </nav>
            </header>
            <ul>
                [#list category.parents as parent]
                    <li>
                        [@collectionLink parent/]
                    </li>
                [/#list]
            </ul>
        </section>
    [/#if]

    <section>
        <header>
            <h4>Children</h4>
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
                <li class="category-${child.type?lower_case} [#if !child.displayed]category-hidden[/#if] [#if (child.parentCategory!category) != category]category-altlink[/#if]"><a href="collections/${child.categoryId?c}">${child.name}</a></li>
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