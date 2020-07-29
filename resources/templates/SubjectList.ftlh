[#include "_layout.ftlh"]
[#include "_collection.ftlh"]

[@page title="Subjects" header=false]
    <header>
        <h1>Subjects</h1>
        <nav><a href="subjects/new" class="btn btn-primary">New Subject</a></nav>
    </header>
    <ul class="subcategories" style="column-count: 3">
    [#list subjects as subject]
        [@subjectItem subject/]
        <ul style="list-style-type: none">
            [#list subject.children as child]
                [@subjectItem child/]
            [/#list]
        </ul>
    [/#list]
    </ul>
[/@page]