[#include "_layout.ftlh"]

[@page "Thumbnail Editor"]
    [#list images as image]
        <img src="${image}" style="border: 1px solid #999">
    [/#list]
[/@page]