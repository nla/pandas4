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

    <section class="search-results">
        <div style="color: #999; margin-top: 0.5rem">
        Found <strong>${results.getTotalElements()}</strong> matching titles
        </div>

        <ol start="${results.pageable.offset + 1}">
        [#list results.toList() as title]
            <li>
                <header>
                    <strong><a href="titles/${title.id?c}">${title.name}</a></strong>
                    <nav>
                    [#if title.titleUrl??]
                        <a class="live-url" href="${title.titleUrl}">${title.titleUrl?replace("^https?://(www\\.)?", "", "r")?replace("^([^/]+)/$", "$1", "r")?truncate(80, "…")}</a>
                    [/#if]
                    [#if title.pi??]
                        <a href="https://webarchive.nla.gov.au/tep/${title.pi?c}">TEP</a>
                    [/#if]
                    [#if title.titleUrl??]
                        <a href="https://webarchive.nla.gov.au/awa/*/${title.titleUrl}">CAL</a>
                    [/#if]
                    </nav>
                </header>
                <div>
                    <ul class="subjects">
                    [#list title.subjects as subject]
                        <li><a href="subjects/${subject.id}">${subject.name}</a>
                    [/#list]
                    </ul>
                    [#if title.publisher??]
                        &mdash; ${(title.publisher.organisation.name)!""}
                    [/#if]
                </div>
                <footer>
                    <span class="regdate">${title.regDate.toLocalDate()}</span> ·
                    <span class="status">${title.status.name}</span> ·
                    <span class="owner">${(title.owner.userid)!""}</span>
                    <span class="agency">(${title.agency.organisation.alias!""})</span>
                </footer>
            </li>
        [/#list]
        </ol>
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
    </section>
[/@page]