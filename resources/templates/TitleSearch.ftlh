[#include "_layout.ftlh"]
[#include "_collection.ftlh"]

[@page "Titles"]
    <form><input name="q" value="${q!""}" placeholder="Keywords"> <input type=submit value=Search>
        <details>
            <summary>Advanced</summary>
            <label for=subjectFilter>Subject:</label>
            <select id=subjectFilter name=subject multiple>
                [#list allSubjects as subject]
                    <option value="${subject.id}"[#if selectedSubjectIds?seq_contains(subject.id)] selected[/#if]>${subject.name}</option>
                [/#list]
            </select>
            </label>
        </details>
    </form>
    <div style="color: #999; margin-top: 0.5rem">
    Found <strong>${results.getTotalElements()}</strong> matching titles
    </div>

    [#list results.toList() as title]
        <div style="margin: 1rem 0">
            <div>
                <strong><a href="titles/${title.id?c}">${title.name}</a></strong>
                [#if title.titleUrl??]
                    <a style="color: darkgreen; font-size: 60%" href="${title.titleUrl}">${title.titleUrl?replace("^https?://(www\\.)?", "", "r")?replace("^([^/]+)/$", "$1", "r")?truncate(80, "…")}</a>
                [/#if]
            </div>
            <div style="font-size: 80%">
                [#list title.subjects as subject]
                    <a href="subjects/${subject.id}" style="color:teal">${subject.name}</a> [#if !subject?is_last]·[/#if]
                [/#list]
                [#if title.publisher??]
                    &mdash; ${(title.publisher.organisation.name)!""}
                [/#if]

            </div>
            <div style="color: #888; font-size: 80%">
                <strong>${title.regDate.toLocalDate()}</strong> ·
                <span style="font-variant: small-caps">${title.status.name}</span> ·
                ${(title.owner.userid)!""}
                (${title.agency.organisation.alias!""})
            </div>
        </div>
    [/#list]
    <nav aria-label="Page navigation example">
        <ul class="pagination justify-content-center">
            [#if results.hasPrevious()]
                <li class="page-item">
                    <a class="page-link"
                       href="${uriBuilder.cloneBuilder().queryParam("page", results.previousPageable().getPageNumber()).toUriString()}"
                       tabindex="-1">Previous</a>
                </li>
            [#else]
                <li class="page-item disabled"><a class="page-link">Previous</a></li>
            [/#if]
            [#if results.hasNext()]
                <li class="page-item">
                    <a class="page-link"
                       href="${uriBuilder.cloneBuilder().queryParam("page", results.nextPageable().getPageNumber()).toUriString()}"
                       tabindex="-1">Next</a>
                </li>
            [#else]
                <li class="page-item disabled"><a class="page-link">Next</a></li>
            [/#if]
        </ul>
    </nav>
[/@page]