console.log("kagi.js loaded");
document.body.style.border = "5px solid blue";

function annotateResults() {
    let resultTitleElements = [];
    let query = "";
    for (let el of document.querySelectorAll(".__sri-title")) {
        let link = el.querySelector(".__sri_title_link");
        if (!link) continue;
        let url = link.href.replace(/^(https?:\/\/[^/]+\/).*$/, "$1");
        resultTitleElements.push(el);
        query += "url=" + encodeURIComponent(url) + "&";
    }
    fetch("https://pandas.nla.gov.au/admin/titles/check", {
        method: "POST",
        body: query,
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        }
    }).then(r => r.json())
        .then(response => {
            for (let i = 0; i < response.length; i++) {
                let titles = response[i];
                let el = resultTitleElements[i];
                el.parentNode.querySelectorAll(".pandas-div").forEach(div => div.parentNode.removeChild(div));

                let div = document.createElement("div");
                div.classList.add("pandas-div");
                el.parentNode.insertBefore(div, el.nextSibling);

                if (titles.length === 0) {
                    let link = el.querySelector(".__sri_title_link");
                    let a = document.createElement("a");
                    a.innerText = "[New Title]";
                    a.href = "https://pandas.nla.gov.au/admin/titles/new?url=" + encodeURIComponent(link.href);
                    div.appendChild(a);
                } else {
                    titles.forEach(title => {
                        let a = document.createElement("a");
                        a.innerText = "[nla.arc-" + title.pi + '] ' + title.name;
                        a.href = "https://pandas.nla.gov.au/admin/titles/" + title.id;
                        div.appendChild(a);
                        div.appendChild(document.createElement("br"));
                    })
                }
            }
    });
}

let observer = new MutationObserver(function() {
    console.log("results changed");
    annotateResults();
    // observer.observe(document.querySelector("#page1"), {childList: true});
});
observer.observe(document.querySelector(".results-box"), {childList: true});
observer.observe(document.querySelector("#page1"), {childList: true});

annotateResults();