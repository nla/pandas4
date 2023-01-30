const subjectsSlimSelect = new SlimSelect({
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
            .then(results => callback(results.map(collection => ({
                value: collection.id,
                text: collection.fullName,
                data: {
                    subjects: collection.inheritedSubjects.map(subject => typeof subject === 'number' ? subject : subject.id),
                }
            }))))
            .catch(error => callback(false));
    },
});

document.getElementById('collections').addEventListener('change', function (event) {
    // if there's no subjects selected populate the subjects with the ones from the collection
    if (subjectsSlimSelect.selected().length === 0) {
        let collectionOption = event.target.options[event.target.selectedIndex];
        if (collectionOption && collectionOption.dataset.subjects) {
            subjectsSlimSelect.set(collectionOption.dataset.subjects.split(","));
        }
    }
});

let seedUrlsField = document.getElementById("seedUrls");

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
            let publisherType = document.getElementById("publisherType");
            if (info.value === "new") {
                document.getElementById("publisherId").value = "";
                document.getElementById("publisherName").value = info.text;
                publisherType.required = true;
                document.getElementById("newPublisherFields").style.display = "inherit";

                const urlString = getPrimarySeedUrl();
                if (urlString) {
                    const host = new URL(urlString).host;
                    const publisherTypeOptions = publisherType.options;

                    // try to preselect an appropriate publisher type based on the domain suffix
                    if (publisherType.selectedIndex === 0) {
                        outer:
                            for (let i = 0; i < publisherTypeOptions.length; i++) {
                                const option = publisherTypeOptions[i];
                                if (option.dataset.domainsuffixes) {
                                    for (const suffix of option.dataset.domainsuffixes.split(/ +/)) {
                                        if (host.endsWith(suffix)) {
                                            publisherType.selectedIndex = i;
                                            break outer;
                                        }
                                    }
                                }
                            }
                    }
                }
            } else {
                document.getElementById("publisherId").value = info.value === undefined ? "" : info.value;
                document.getElementById("publisherName").value = "";
                publisherType.selectedIndex = 0;
                publisherType.required = false;
                document.getElementById("newPublisherFields").style.display = "none";
            }
        }
    });
}

// Hide ABN field for personal publisher type
function handlePublisherTypeChange() {
    let publisherAbnLabel = document.getElementById("publisherAbn").parentElement;
    if (document.getElementById("publisherType").selectedOptions[0].text === "Personal") {
        publisherAbnLabel.style.display = "none";
    } else {
        publisherAbnLabel.style.display = "";
    }
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

function getSeedUrls() {
    return document.getElementById("seedUrls").value.split(/\s+/);
}

function getPrimarySeedUrl() {
    return getSeedUrls()[0];
}

function setPrimarySeedUrl(url) {
    let urls = getSeedUrls();
    if (url === urls[0]) return;
    urls[0] = url;
    seedUrlsField.value = urls.join("\n");
}

function normalizeSeedUrls() {
    let urls = [];
    let changedAnything = false;
    for (let url of getSeedUrls()) {
        let originalUrl = url;
        if (url === "") continue;
        if (!url.match(/^[a-z]+:\/\//)) {
            url = "http://" + url;
        }
        url = url.replace(/^https?:\/\/(nitter\.net|nitter\.mstdn\.social|mobile\.twitter\.com|twitter\.com)\//, "https://nitter.archive.org.au/");
        urls.push(url);
        if (url !== originalUrl) {
            changedAnything = true;
        }
    }
    if (changedAnything) {
        seedUrlsField.value = urls.join("\n");
    }
}

function seedUrlsChanged () {
    let fetchAlert = document.getElementById("fetchAlert");
    fetchAlert.innerHTML = '';
    fetchAlert.style.display = 'none'
    document.getElementById("duplicateAlert").innerHTML = '';
    document.getElementById("duplicateAlert").style.display = 'none'

    normalizeSeedUrls();

    /* ensure all pages on this website can't be selected for nitter */
    if (getPrimarySeedUrl().startsWith("https://nitter.archive.org.au/")) {
        if (!document.querySelectorAll("input[type=radio][name=scope]")[2].checked) {
            document.querySelectorAll("input[type=radio][name=scope]")[1].checked = true;
        }
        document.querySelectorAll("input[type=radio][name=scope]")[0].disabled = true;
    } else {
        document.querySelectorAll("input[type=radio][name=scope]")[1].disabled = false;
    }

    fetch(titleCheckEndpoint + "?url=" + encodeURIComponent(getPrimarySeedUrl()))
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
                    encodeURIComponent(getPrimarySeedUrl()), "_blank"));
            }
        });

    fetch(pageinfoEndpoint + "?url=" + encodeURIComponent(getPrimarySeedUrl()))
        .catch(reason => console.log(reason))
        .then(response => response.json())
        .then(info => {
            function normalize(url) {
                return url.replace(/^https?:\/\//, "").replace(/^www\./, "").replace(/\/+$/, "");
            }

            if (info.location && normalize(getPrimarySeedUrl()).localeCompare(normalize(info.location), 'en', { sensitivity: 'base' }) === 0) {
                setPrimarySeedUrl(info.location);
                seedUrlsChanged();
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

function checkSurts() {
    let seeds = document.getElementById('seedUrls').value;
    const surtsHint = document.getElementById('surtsHint');
    let selectedGatherMethod = document.getElementById('gatherMethod').selectedOptions;
    if (!seeds || selectedGatherMethod.length === 0 || selectedGatherMethod[0].text !== 'Heritrix') {
        surtsHint.innerHTML = '';
        return;
    }
    surtsHint.innerHTML = '...';
    const formData = new FormData();
    formData.append('_csrf', document.querySelector("input[name='_csrf']").value);
    formData.append('seedUrls', seeds);
    fetch(titleCheckSurtsEndpoint, {method: 'POST', body: formData}).then(response => {
        if (!response.ok) {
            throw new Error("HTTP error " + response.status);
        }
        return response.text()
    }).then(text => surtsHint.innerText = 'Heritrix SURTs:\n' + text + '\n')
        .catch(error => console.error("SURT check failed: " + error));
}

// Only show the gather filters field if the HTTrack gather method is selected.
function showOrHideFilters() {
    let selectedGatherMethod = document.getElementById('gatherMethod').selectedOptions;
    let httrackMethodIsSelected = selectedGatherMethod.length !== 0 && selectedGatherMethod[0].text === 'HTTrack';
    document.getElementById('filters').parentElement.style.display = httrackMethodIsSelected ? 'inherit' : 'none';
}

// When a newline is entered in the URL field, expand it to multiple lines
function autoExpandSeedUrlField() {
    if (seedUrlsField.rows === 1 && seedUrlsField.value.includes('\n')) {
        seedUrlsField.rows = 5;
        document.getElementById('urlPlusButton').style.display = 'none';
        document.getElementById('seedUrlsLabelText').innerText += "s";
    }
}
document.getElementById('seedUrls').addEventListener('input', autoExpandSeedUrlField);
autoExpandSeedUrlField();

// When the URL field's "+" button is clicked, expand it
document.getElementById('urlPlusButton').addEventListener('click', function() {
    let seedUrlsTextArea = document.getElementById("seedUrls");
    if (!seedUrlsTextArea.value.endsWith("\n")) {
        seedUrlsTextArea.value += "\n";
    }
    autoExpandSeedUrlField();
    seedUrlsTextArea.focus();
});

// Setup event listeners

seedUrlsField.addEventListener("change", function() {
    seedUrlsChanged();
    checkSurts();
});
document.getElementById('seedUrls').addEventListener("change", checkSurts);
document.getElementById('gatherMethod').addEventListener("change", function () {
    showOrHideFilters();
    checkSurts();
});
nameField.addEventListener("change", nameChanged);
document.getElementById("publisherType").addEventListener("change", handlePublisherTypeChange);


// Fire initial change events

if (seedUrlsField.value !== "") {
    seedUrlsChanged();
}
showOrHideFilters();
checkSurts();
if (nameField.value) nameChanged();
handlePublisherTypeChange();

// we keep the form disabled until the page is fully loaded
// otherwise it could be submitted with partial values which results in data loss
window.loaded = true;