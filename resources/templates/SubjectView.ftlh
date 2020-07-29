[#include "_layout.ftlh"]
[#include "_collection.ftlh"]

[@page title="${subject.name} (Subject)" header=false]
    <ol class="breadcrumb">
        <li class=breadcrumb-item style="font-weight: bold"><a href="subjects">Subjects</a></li>
        [#list subject.subjectBreadcrumbs as breadcrumb]
            [#if breadcrumb?is_last]
                <li class="active breadcrumb-item">${breadcrumb.name}</li>
            [#else]
                <li class="breadcrumb-item"><a href="subjects/${breadcrumb.id?c}">${breadcrumb.name}</a></li>
            [/#if]
        [/#list]
    </ol>
    <header>
        <h1>${subject.name}</h1>
        <nav><a href="subjects/${subject.id?c}/edit" class="btn btn-primary">Edit</a></nav>
    </header>

    <section>
        <h5>Subcategories</h5>
        <ul class="subcategories">
            [#list subject.children as child]
                <li><a href="subjects/${child.id?c}">${child.name}</a>
            [/#list]
        </ul>
    </section>

    <section>
        <h5>Collections</h5>
        <ul class="subcategories">
            [#list subject.collections as collection]
                <li>[@collectionLink collection /]
            [/#list]
        </ul>
    </section>

    <section>
        <h5>Titles</h5>
        <ul class="subcategories">
            [#list subject.titles as title]
                <li><a href="titles/${title.id?c}">${title.name!""}</a>
            [/#list]
        </ul>
    </section>
[/@page]