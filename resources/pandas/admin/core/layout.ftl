[#macro navli href]
<li [#if request.pathInfo()[1..]?starts_with(href)]class="active"[/#if]>
    <a href="${href}">[#nested /]</a>
</li>
[/#macro]

[#macro page title]
<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <base href="${request.contextPath()!"/"}">
    <title>${title} - PANDAS Admin</title>
    <link rel="stylesheet" href="webjars/bootstrap/3.3.4/css/bootstrap.min.css">
    <link rel="stylesheet" href="pandas-admin.css">
</head>
<body>
<header>
    <h1><span class="pandas-logo">PANDAS</span> Admin</h1>
</header>

<div class="wrapper">
    <nav class="sidebar">
        <ul class="nav nav-pills nav-stacked">
            <li class="nav-header">Tools</li>
            [@navli href="admin/marcexport"]MARC Export[/@navli]
            <li class="nav-header">Gather</li>
            [@navli href="admin/gather/filterpresets"]Filter Presets[/@navli]
        </ul>
    </nav>
    <main class="content">
        <h2>${title}</h2>
        [#nested/]
    </main>
</div>

</body>
</html>
[/#macro]

