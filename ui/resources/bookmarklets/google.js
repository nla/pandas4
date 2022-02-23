document.querySelectorAll('.pandas-div').forEach(div => div.parentNode.removeChild(div));
Array.from(document.querySelectorAll('#search a > h3')).forEach(h3 => {
    let pandasUrl = "https://pandas.nla.gov.au/admin";
    let link = h3.parentNode;
    fetch(pandasUrl + "/titles/check?url=" + encodeURIComponent(link.href))
        .then(r => r.json())
        .then(titles => {
            let div = document.createElement('div');
            if (titles.length === 0) {
                let a = document.createElement("a");
                a.innerText = "[New Title]";
                a.href = pandasUrl + "/titles/new?url=" + encodeURIComponent(link.href);
                div.appendChild(a);
            } else {
                titles.forEach(title => {
                    let a = document.createElement("a");
                    a.innerText = "nla.arc-" + title.pi;
                    a.title = title.name;
                    a.href = pandasUrl + "/titles/" + title.id;
                    div.appendChild(a);
                    div.appendChild(document.createElement("br"));
                })
            }
            let div2 = document.createElement('div');
            div2.classList.add('pandas-div');
            div2.innerHTML = '';
            div2.appendChild(div);
            let container = link.parentNode;
            container.insertBefore(div2, container.childNodes[0].nextSibling);
        });
})
