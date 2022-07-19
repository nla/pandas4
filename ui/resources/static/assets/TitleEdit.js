
new SlimSelect({
    select: '#subjects',
    hideSelectedOption: true,
    searchFilter: function (option, search) {
        return option.data['fullname'].toLowerCase().indexOf(search.toLowerCase()) !== -1;
    }
});
new SlimSelect({
    select: '#collections',
    hideSelectedOption: true,
    searchFilter: function (option, search) {
        return true; // leave filtering to the backend
    },
    ajax: function (search, callback) {
        if (!search) return callback(false);
        fetch(collectionsEndpoint + "?q=" + encodeURIComponent(search) + "&size=100")
            .then(response => response.json())
            .then(results => callback(results.map(collection => ({value: collection.id, text: collection.fullName}))))
            .catch(error => callback(false));
    },
});
let titleUrlField = document.getElementById("titleUrl");

function escapeHtml(text) {
    var element = document.createElement('p');
    element.appendChild(document.createTextNode(text));
    return element.innerHTML;
}

let publisherSelect = null;
if (document.getElementById("publisher")) {
    publisherSelect = new SlimSelect({
        select: '#publisher',
        allowDeselect: true,
        hideSelectedOption: true,
        addable: function (value) {
            return {
                text: value,
                value: "new" // I'd prefer to use null/empty-string but slim-js then overrides it with the text
            };
        },
        searchFilter: function (option, search) {
            return true; // leave filtering to the backend
        },
        ajax: function (search, callback) {
            if (!search) {
                callback(false);
                return;
            }
            fetch(publisherJsonEndpoint + "?q=" + encodeURIComponent(search) + "&size=100")
                .then(response => response.json())
                .then(results => {
                    callback(results.map(publisher => {
                        return {
                            value: publisher.id,
                            text: publisher.name,
                            innerHTML: ('<span class=publisher-select-item>' + escapeHtml(publisher.name) +
                                ' <a class=title-count href=' + publishersEndpoint + '/' + publisher.id + ' target=_blank>'
                                + publisher.titleCount + '</a></span>')
                        }
                    }));
                }).catch(error => {
                console.log("Publisher search failed: ", error);
                callback(false)
            });
        },
        // By default slim-select clears the search when the drop-down is closed. This means if the user focuses
        // another field we lose their search or any pre-populated value. So as a workaround stash a copy of the
        // search text before closing and restore it after closing.
        beforeClose: function() {
            this.pandasSavedSearch = this.slim.search.input.value;
        },
        afterClose: function() {
            this.slim.search.input.value = this.pandasSavedSearch;
            delete this.pandasSavedSearch;
        },
        onChange: function (info) {
            if (info.value === "new") {
                document.getElementById("publisherId").value = "";
                document.getElementById("publisherName").value = info.text;
                document.getElementById("publisherType").selectedIndex = 0;
                document.getElementById("publisherType").required = true;
                document.getElementById("newPublisherFields").style.display = "inherit";

                const urlString = titleUrlField.value;
                if (urlString) {
                    const host = new URL(urlString).host;
                    const publisherTypeOptions = document.getElementById("publisherType").options;

                    // try to preselect an appropriate publisher type based on the domain suffix
                    outer:
                        for (let i = 0; i < publisherTypeOptions.length; i++) {
                            const option = publisherTypeOptions[i];
                            if (option.dataset.domainsuffixes) {
                                for (const suffix of option.dataset.domainsuffixes.split(/ +/)) {
                                    if (host.endsWith(suffix)) {
                                        document.getElementById("publisherType").selectedIndex = i;
                                        break outer;
                                    }
                                }
                            }
                        }
                }
            } else {
                document.getElementById("publisherId").value = info.value === undefined ? "" : info.value;
                document.getElementById("publisherName").value = "";
                document.getElementById("publisherType").selectedIndex = 0;
                document.getElementById("publisherType").required = false;
                document.getElementById("newPublisherFields").style.display = "none";
            }
        }
    });
}

function createLink(text, href, target, className) {
    let link = document.createElement("a");
    link.href = href;
    link.innerText = text;
    if (target) link.target = target;
    if (className) link.classList.add(className);
    return link;
}

let nameField = document.getElementById("name");

function nameChanged() {
    if (publisherSelect && !publisherSelect.selected()) {
        let name = nameField.value;
        name = name.replace(/\s*:.*/, '');
        publisherSelect.search(name);
    }

    fetch(titleCheckNameEndpoint + "?name=" + encodeURIComponent(nameField.value))
        .then(response => response.json())
        .then(titles => {
            titles = titles.filter(title => title.id !== thisTitleId);

            let warningDiv = document.getElementById("duplicateNameAlert");
            warningDiv.innerHTML = '';

            if (titles.length === 0) {
                warningDiv.style.display = 'none';
                return;
            }

            warningDiv.style.display = 'block';
            warningDiv.append("We have some other records with a similar name: ");
            const list = document.createElement("ul");
            warningDiv.append(list);

            for (let title of titles) {
                let urlMatch = title.titleUrl.match(/(https?:\/\/)?(www\.)?([^/]*)(\?.*)?/)
                let host = urlMatch && urlMatch[3];

                let item = document.createElement("li");
                item.append(createLink(title.name, titlesEndpoint + "/" + title.id, "_blank"));
                if (host) item.append(' ', createLink('(' + host + ')', title.titleUrl, "_blank", "live-url"));

                list.append(item);
            }

            if (titles.length >= 4) {
                warningDiv.append(createLink("More...", titlesEndpoint + "?q=" + encodeURIComponent(nameField.value), "_blank"));
            }
        });
}
nameField.addEventListener("change", nameChanged);
if (nameField.value) nameChanged();

function titleUrlChanged () {
    let fetchAlert = document.getElementById("fetchAlert");
    fetchAlert.innerHTML = '';
    fetchAlert.style.display = 'none'
    document.getElementById("duplicateAlert").innerHTML = '';
    document.getElementById("duplicateAlert").style.display = 'none'

    if (!titleUrlField.value.match(/^[a-z]+:\/\//)) {
        titleUrlField.value = "http://" + titleUrlField.value;
    }
    titleUrlField.value = titleUrlField.value.replace(/^https?:\/\/(nitter\.net|nitter\.mstdn\.social|mobile\.twitter\.com|twitter\.com)\//, "https://nitter.archive.org.au/");
    /* ensure all pages on this website can't be selected for nitter */
    if (titleUrlField.value.startsWith("https://nitter.archive.org.au/")) {
        if (!document.querySelectorAll("input[type=radio][name=scope]")[2].checked) {
            document.querySelectorAll("input[type=radio][name=scope]")[1].checked = true;
        }
        document.querySelectorAll("input[type=radio][name=scope]")[0].disabled = true;
    } else {
        document.querySelectorAll("input[type=radio][name=scope]")[1].disabled = false;
    }

    fetch(titleCheckEndpoint + "?url=" + encodeURIComponent(titleUrlField.value))
        .then(response => response.json())
        .then(titles => {
            titles = titles.filter(title => title.id !== thisTitleId);
            if (titles.length === 0) return;
            let warningDiv = document.getElementById("duplicateAlert");
            warningDiv.innerHTML = '';
            warningDiv.style.display = 'block';
            warningDiv.append("We have some other records for this website: ");

            const list = document.createElement("ul");
            warningDiv.append(list);
            for (let title of titles) {
                let item = document.createElement("li");
                item.appendChild(createLink(title.name,titlesEndpoint + "/" + title.id, "_blank"));
                list.appendChild(item);
            }

            if (titles.length >= 4) {
                warningDiv.appendChild(createLink("More...", titlesEndpoint + "?q=" +
                    encodeURIComponent(titleUrlField.value), "_blank"));
            }
        });

    fetch(pageinfoEndpoint + "?url=" + encodeURIComponent(titleUrlField.value))
        .catch(reason => console.log(reason))
        .then(response => response.json())
        .then(info => {
            function normalize(url) {
                return url.replace(/^https?:\/\//, "").replace(/^www\./, "").replace(/\/+$/, "");
            }

            if (info.location && normalize(titleUrlField.value).localeCompare(normalize(info.location), 'en', { sensitivity: 'base' }) === 0) {
                titleUrlField.value = info.location;
                titleUrlChanged();
                return;
            }

            let alertMessage = null;
            if (info.status === -1) {
                alertMessage = `This website does not exist (DNS lookup failed).`;
            }else if (info.status === 403) {
                alertMessage = `This website may be blocking us (${info.status} ${info.reason}).`;
            } else if (info.status === 404) {
                alertMessage = `This web page does not exist (${info.status} ${info.reason}). Please check the address is correct.`;
            } else if (info.status >= 400) {
                alertMessage = `This web page returned an error (${info.status} ${info.reason}).`;
            } else if (info.location) {
                alertMessage = `This web address redirects to ${info.location}`;
            }

            if (alertMessage) {
                fetchAlert.innerText = alertMessage;
                fetchAlert.style.display = 'block';
            } else {
                fetchAlert.innerText = '';
                fetchAlert.style.display = 'nonew';
            }

            let nameTextbox = nameField;
            if (!nameTextbox.value && info.title && info.status < 300) {
                nameTextbox.value = info.title.replace(/^Home\s*[-:|]\s*/, '');
                if (document.activeElement === nameTextbox) {
                    nameTextbox.select();
                }
                nameChanged();
            }
        });
}
titleUrlField.addEventListener("change", titleUrlChanged);
if (titleUrlField.value !== "") {
    titleUrlChanged();
}

// we keep the form disabled until the page is fully loaded
// otherwise it could be submitted with partial values which results in data loss
window.loaded = true;