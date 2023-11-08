export {};
import type { default as SlimSelect_ } from "slim-select";

declare var SlimSelect: typeof SlimSelect_;
declare var titleCheckEndpoint: string;
declare var titleCheckNameEndpoint: string;
declare var titleCheckSurtsEndpoint: string;
declare var pageinfoEndpoint: string;
declare var publisherJsonEndpoint: string;
declare var publishersEndpoint: string;
declare var titlesEndpoint: string;
declare var titlesBasicSearchEndpoint: string;
declare var collectionsEndpoint: string;
declare var thisTitleId: number;

for (let id of ['#continuesTitles', '#continuedByTitles']) {
    new SlimSelect({
        select: id,
        hideSelectedOption: true,
        searchFilter: function (option, search) {
            return true; // leave filtering to the backend
        },
        ajax: function (search, callback) {
            if (!search) return callback(false);
            fetch(titlesBasicSearchEndpoint + "?q=" + encodeURIComponent(search) + "&notTitle=" + thisTitleId)
                .then(response => response.ok ? response.json() : Promise.reject(response))
                .then(results => callback(results.map(title => ({
                    value: title.id,
                    text: title.name + " [" + title.humanId + "]",
                }))))
                .catch(error => callback(false));
        },
    });
}

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
            .then(response => response.ok ? response.json() : Promise.reject(response))
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
        let target = event.target as HTMLSelectElement;
        let collectionOption = target.options[target.selectedIndex];
        if (collectionOption && collectionOption.dataset.subjects) {
            subjectsSlimSelect.set(collectionOption.dataset.subjects.split(","));
        }
    }
});

let seedUrlsTextArea = document.getElementById("seedUrls") as HTMLTextAreaElement;

function escapeHtml(text) {
    var element = document.createElement('p');
    element.appendChild(document.createTextNode(text));
    return element.innerHTML;
}

let publisherSlimSelect = null;

// fetch the contact people for the selected publisher
function refreshPublisherContactPeople(publisherId) {
    console.log("refreshPublisherContactPeople(" + publisherId + ")");
    let select = document.getElementById("titlePermission.contactPerson") as HTMLSelectElement;

    // remove existing options with class publisher-contact-person-option
    let options = select.querySelectorAll(".publisher-contact-person-option");
    for (let option of options) {
        option.remove();
    }

    if (!publisherId || publisherId === "new") return;

    fetch(publishersEndpoint + "/" + publisherId + "/contact-people.json")
        .then(response => response.ok ? response.json() : Promise.reject(response))
        .then(contactPeople => {
            // add new options
            for (let person of contactPeople) {
                // skip if the person is already in the list (e.g. they're the previously selected contact)
                if (select.querySelector("option[value='" + person.id + "']")) continue;

                let option = document.createElement("option");
                option.text = person.nameAndFunction;
                option.value = person.id;
                option.classList.add("publisher-contact-person-option");
                select.add(option);
            }
        })
        .catch(error => console.log("Failed to fetch publisher contact people: ", error));
}

if (document.getElementById("publisher")) {
    publisherSlimSelect = new SlimSelect({
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
                .then(response => response.ok ? response.json() : Promise.reject(response))
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
        beforeClose: function () {
            this.pandasSavedSearch = this.slim.search.input.value;
        },
        afterClose: function () {
            this.slim.search.input.value = this.pandasSavedSearch;
            delete this.pandasSavedSearch;
        },
        onChange: function (info) {
            let publisherTypeSelect = document.getElementById("publisherType") as HTMLSelectElement;
            let publisherIdInput = document.getElementById("publisherId") as HTMLInputElement;
            let publisherNameInput = document.getElementById("publisherName") as HTMLInputElement;
            let newPublisherFieldsDiv = document.getElementById("newPublisherFields") as HTMLDivElement;
            if (info.value === "new") {
                publisherIdInput.value = "";
                publisherNameInput.value = info.text;
                publisherTypeSelect.required = true;
                newPublisherFieldsDiv.style.display = "inherit";

                const urlString = getPrimarySeedUrl();
                if (urlString) {
                    const host = new URL(urlString).host;
                    const publisherTypeOptions = publisherTypeSelect.options;

                    // try to preselect an appropriate publisher type based on the domain suffix
                    if (publisherTypeSelect.selectedIndex === 0) {
                        outer:
                            for (let i = 0; i < publisherTypeOptions.length; i++) {
                                const option = publisherTypeOptions[i];
                                if (option.dataset.domainsuffixes) {
                                    for (const suffix of option.dataset.domainsuffixes.split(/ +/)) {
                                        if (host.endsWith(suffix)) {
                                            publisherTypeSelect.selectedIndex = i;
                                            break outer;
                                        }
                                    }
                                }
                            }
                    }
                }
            } else {
                publisherIdInput.value = info.value === undefined ? "" : info.value;
                publisherNameInput.value = "";
                publisherTypeSelect.selectedIndex = 0;
                publisherTypeSelect.required = false;
                newPublisherFieldsDiv.style.display = "none";
            }
            refreshPublisherContactPeople(info.value);
        }
    });
    let publisherSelectElement = document.querySelector('#publisher') as HTMLSelectElement;
    refreshPublisherContactPeople(publisherSelectElement.value);
}

// Hide ABN field for personal publisher type
function handlePublisherTypeChange() {
    let publisherAbnLabel = document.getElementById("publisherAbn").parentElement;
    let publisherTypeSelect = document.getElementById("publisherType") as HTMLSelectElement;
    if (publisherTypeSelect.selectedOptions[0].text === "Personal") {
        publisherAbnLabel.style.display = "none";
    } else {
        publisherAbnLabel.style.display = "";
    }
}

function createLink(text : string, href : string, target : string, className? : string) {
    let link = document.createElement("a");
    link.href = href;
    link.innerText = text;
    if (target) link.target = target;
    if (className) link.classList.add(className);
    return link;
}

let nameInput = document.getElementById("name") as HTMLInputElement;

function nameChanged() {
    if (publisherSlimSelect && !publisherSlimSelect.selected()) {
        let name = nameInput.value;
        name = name.replace(/\s*:.*/, '');
        publisherSlimSelect.search(name);
    }

    fetch(titleCheckNameEndpoint + "?name=" + encodeURIComponent(nameInput.value))
        .then(response => response.ok ? response.json() : Promise.reject(response))
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
                warningDiv.append(createLink("More...", titlesEndpoint + "?q=" + encodeURIComponent(nameInput.value), "_blank"));
            }
        });
}

function getSeedUrls() {
    return seedUrlsTextArea.value.split(/\s+/);
}

function getPrimarySeedUrl() {
    return getSeedUrls()[0];
}

function setPrimarySeedUrl(url) {
    let urls = getSeedUrls();
    if (url === urls[0]) return;
    urls[0] = url;
    seedUrlsTextArea.value = urls.join("\n");
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
        urls.push(url);
        if (url !== originalUrl) {
            changedAnything = true;
        }
    }
    if (changedAnything) {
        seedUrlsTextArea.value = urls.join("\n");
    }
}

function seedUrlsChanged() {
    let fetchAlert = document.getElementById("fetchAlert");
    fetchAlert.innerHTML = '';
    fetchAlert.style.display = 'none'
    document.getElementById("duplicateAlert").innerHTML = '';
    document.getElementById("duplicateAlert").style.display = 'none'

    normalizeSeedUrls();

    let scopeRadios = document.querySelectorAll("input[type=radio][name=scope]") as NodeListOf<HTMLInputElement>;
    scopeRadios[1].disabled = false;

    fetch(titleCheckEndpoint + "?url=" + encodeURIComponent(getPrimarySeedUrl()))
        .then(response => response.ok ? response.json() : Promise.reject(response))
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
                item.appendChild(createLink(title.name, titlesEndpoint + "/" + title.id, "_blank"));
                list.appendChild(item);
            }

            if (titles.length >= 4) {
                warningDiv.appendChild(createLink("More...", titlesEndpoint + "?q=" +
                    encodeURIComponent(getPrimarySeedUrl()), "_blank"));
            }
        });

    fetch(pageinfoEndpoint + "?url=" + encodeURIComponent(getPrimarySeedUrl()))
        .then(response => response.ok ? response.json() : Promise.reject(response))
        .catch(reason => console.log(reason))
        .then(info => {
            function normalize(url) {
                return url.replace(/^https?:\/\//, "").replace(/^www\./, "").replace(/\/+$/, "");
            }

            if (info.location && normalize(getPrimarySeedUrl()).localeCompare(normalize(info.location), 'en', {sensitivity: 'base'}) === 0) {
                setPrimarySeedUrl(info.location);
                seedUrlsChanged();
                return;
            }

            let alertMessage = null;
            if (info.status === -1) {
                alertMessage = `This website does not exist (DNS lookup failed).`;
            } else if (info.status === 403) {
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
                fetchAlert.style.display = 'none';
            }

            let nameTextbox = nameInput;
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
    let seedUrlsTextArea = document.getElementById('seedUrls') as HTMLTextAreaElement;
    let seeds = seedUrlsTextArea.value;
    const surtsHint = document.getElementById('surtsHint');
    let gatherMethodSelect = document.getElementById('gatherMethod') as HTMLSelectElement;
    let selectedGatherMethod = gatherMethodSelect.selectedOptions;
    if (!seeds || selectedGatherMethod.length === 0 || selectedGatherMethod[0].text !== 'Heritrix') {
        surtsHint.innerHTML = '';
        return;
    }
    surtsHint.innerHTML = '...';
    const formData = new FormData();
    let csrfInput = document.querySelector("input[name='_csrf']") as HTMLInputElement;
    formData.append('_csrf', csrfInput.value);
    formData.append('seedUrls', seeds);
    fetch(titleCheckSurtsEndpoint, {method: 'POST', body: formData}).then(response => {
        if (!response.ok) {
            throw new Error("HTTP error " + response.status);
        }
        return response.text()
    }).then(text => surtsHint.innerText = 'Heritrix SURTs:\n' + text + '\n')
        .catch(error => console.error("SURT check failed: " + error));
}

function setInputHidden(id: string, hidden: boolean) {
    let input = document.getElementById(id) as HTMLInputElement;
    if (hidden) {
        input.classList.add('hidden-input');
    } else {
        input.classList.remove('hidden-input');
    }
}

// Only show the gather filters field if the HTTrack gather method is selected.
function showOrHideFilters() {
    let gatherMethodSelect = document.getElementById('gatherMethod') as HTMLSelectElement;
    let selectedGatherMethod = gatherMethodSelect.selectedOptions;
    let gatherMethodName = selectedGatherMethod.length === 0 ? '' : selectedGatherMethod[0].text;
    document.getElementById('filters').parentElement.style.display = gatherMethodName === 'HTTrack' ? 'inherit' : 'none';
    setInputHidden('ignoreRobotsTxtLabel', !['Heritrix', 'HTTrack'].includes(gatherMethodName));
}

// When a newline is entered in the URL field, expand it to multiple lines
function autoExpandSeedUrlField() {
    if (seedUrlsTextArea.rows === 1 && seedUrlsTextArea.value.includes('\n')) {
        seedUrlsTextArea.rows = 5;
        document.getElementById('urlPlusButton').style.display = 'none';
        document.getElementById('seedUrlsLabelText').innerText += "s";
    }
}

document.getElementById('seedUrls').addEventListener('input', autoExpandSeedUrlField);
autoExpandSeedUrlField();

// When the URL field's "+" button is clicked, expand it
document.getElementById('urlPlusButton').addEventListener('click', function () {
    if (!seedUrlsTextArea.value.endsWith("\n")) {
        seedUrlsTextArea.value += "\n";
    }
    autoExpandSeedUrlField();
    seedUrlsTextArea.focus();
});

// Setup event listeners

seedUrlsTextArea.addEventListener("change", function () {
    seedUrlsChanged();
    checkSurts();
});
document.getElementById('seedUrls').addEventListener("change", checkSurts);
document.getElementById('gatherMethod').addEventListener("change", function () {
    showOrHideFilters();
    checkSurts();
});
nameInput.addEventListener("change", nameChanged);
document.getElementById("publisherType").addEventListener("change", handlePublisherTypeChange);

// Hide the history details when the "Link as the next in a series" checkbox is checked
// This ensures the user can't get confused and also try to manually link it.
let continuesCheckbox = document.getElementById("continuesCheckbox") as HTMLInputElement;
if (continuesCheckbox) {
    continuesCheckbox.addEventListener("click", function () {
        const checked = continuesCheckbox.checked;
        document.getElementById("historyDetails").style.display = checked ? "none" : "block";
    });
}

// Fire initial change events

if (seedUrlsTextArea.value !== "") {
    seedUrlsChanged();
}
showOrHideFilters();
checkSurts();
if (nameInput.value) nameChanged();
handlePublisherTypeChange();

// last step: enable the form's submit buttons now that the javascript has loaded
document.querySelectorAll("button[type=submit][disabled]")
    .forEach(submitButton => (submitButton as HTMLButtonElement).disabled = false);