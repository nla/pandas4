{
    document.querySelectorAll('.pandas-div').forEach(div => div.parentNode.removeChild(div));
    let selector;
    if (location.host === 'www.bing.com') {
        selector = '#b_results > li cite';
    } else if (location.host === 'www.google.com') {
        selector = '#search a > h3';
    } else {
        selector = 'a[href]';
    }
    Array.from(document.querySelectorAll(selector)).forEach(link => {
        let pandasUrl = "https://pandas.nla.gov.au/admin";
        let url;
        if (location.host === 'www.bing.com') {
            url = link.innerText;
        } else if (location.host === 'www.google.com') {
            link = link.parentNode;
            url = link.href;
        } else {
            url = link.href;
            if (link.host === location.host) {
                return;
            }
        }
        fetch(pandasUrl + "/titles/check?url=" + encodeURIComponent(url))
            .then(r => r.json())
            .then(titles => {
                let div = document.createElement('span');
                if (titles.length === 0) {
                    let a = document.createElement("a");
                    a.innerText = "[New Title]";
                    if (link.host.endsWith(".au")) {
                        a.innerText += " (AU)";
                    }
                    a.href = pandasUrl + "/titles/new?url=" + encodeURIComponent(url);
                    div.appendChild(a);
                } else {
                    titles.forEach(title => {
                        let a = document.createElement("a");
                        a.innerText = "[nla.arc-" + title.pi + '] ' + title.name;
                        a.href = pandasUrl + "/titles/" + title.id;
                        div.appendChild(a);
                        div.appendChild(document.createElement("br"));
                    })
                }
                let div2 = document.createElement('span');
                div2.style.display = 'block';
                div.style.display = 'block';
                div2.classList.add('pandas-div');
                div2.appendChild(div);
                let container = link.parentNode;
                if (location.host === 'www.bing.com') {
                    console.log(container.insertBefore(div2, null));
                } else if (location.host === 'www.google.com') {
                    container.insertBefore(div2, container.childNodes[0].nextSibling);
                } else {
                    container.insertBefore(div2, link.nextSibling);
                }
            });
    })
}