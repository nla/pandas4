[#macro navli href]
<li [#if requestContext.requestUri?starts_with(requestContext.contextPath + href)]class="active"[/#if]>
    <a href="${href}">[#nested /]</a>
</li>
[/#macro]

[#macro page title header=true]
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <base href="[#if requestContext.contextPath == ""]/[#else]${requestContext.contextPath}/[/#if]">
    <title>${title} - PANDAS Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <link rel="stylesheet" href="pandas-admin.css">
</head>
<body>

<header>
    <a href="/">PANDAS Admin</a>
    <form action="/collections/search"><input name="q" placeholder="Search collections"></form>
</header>

<main>
    <nav>
        <ul>
            <li>Tools</li>
            [@navli href="collections"]Collections[/@navli]
            [@navli href="marcexport"]MARC Export[/@navli]
            <li>Gather</li>
            [@navli href="gather/filterpresets"]Filter Presets[/@navli]
        </ul>
    </nav>
    <article>
        [#if header]<h1>${title}</h1>[/#if]
        [#nested/]
    </article>
</main>

</body>
</html>
[/#macro]

