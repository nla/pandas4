export {};
import SlimSelect from "slim-select";

declare var titleCheckEndpoint: string;
declare var titleCheckNameEndpoint: string;
declare var titleCheckSurtsEndpoint: string;
declare var pageinfoEndpoint: string;
declare var publisherJsonEndpoint: string;
declare var publishersEndpoint: string;
declare var subjectsSuggestEndpoint: string;
declare var titlesEndpoint: string;
declare var titlesBasicSearchEndpoint: string;
declare var collectionsEndpoint: string;
declare var thisTitleId: number;

const sitePresets = [
    {
        "urlRegex": "https://www\\.threads\\.net/@.*",
        "collections": [{id: 21206, name: "Threads accounts"}],
        "subjects": [323],
        "scope": 2,
        "gatherMethod": 21
    }
];

for (let id of ['#continuesTitles', '#continuedByTitles']) {
    new SlimSelect({
        select: id,
        settings: {
            hideSelected: true,
            placeholderText: ''
        },
        events: {
            searchFilter: function (option, search) {
                return true; // leave filtering to the backend
            },
            search: function (search, currentData) {
                return new Promise((resolve, reject) => {
                    if (!search) return reject('No input')
                    let queryString = "q=" + encodeURIComponent(search);
                    if (thisTitleId) queryString += "&notTitle=" + thisTitleId;
                    fetch(titlesBasicSearchEndpoint + "?" + queryString)
                        .then(response => response.ok ? response.json() : Promise.reject(response))
                        .then(results => resolve(results.map(title => ({
                            value: title.id,
                            text: title.name + " [" + title.humanId + "]",
                        }))))
                        .catch(reject);
                });
            }
        }
    });
}

const subjectsSlimSelect = new SlimSelect({
    select: '#subjects',
    settings: {
        hideSelected: true,
        placeholderText: ''
    },
    events: {
        searchFilter: function (option, search) {
            return option.data['fullname'].toLowerCase().indexOf(search.toLowerCase()) !== -1;
        }
    }
});

const collectionsSlimSelect = new SlimSelect({
    select: '#collections',
    settings: {
        hideSelected: true,
        placeholderText: ''
    },
    events: {
        searchFilter: function (option, search) {
            return true; // leave filtering to the backend
        },
        search: function (search, currentData) {
            return new Promise((resolve, reject) => {
                if (!search) return reject();
                fetch(collectionsEndpoint + "?q=" + encodeURIComponent(search) + "&size=100")
                    .then(response => response.ok ? response.json() : Promise.reject(response))
                    .then(results => resolve(results.map(collection => ({
                        value: collection.id,
                        text: collection.fullName,
                        data: {
                            subjects: collection.inheritedSubjects.map(subject => typeof subject === 'number' ? subject : subject.id),
                        }
                    }))))
                    .catch(reject);
            });
        }
    }
});

document.getElementById('collections').addEventListener('change', function (event) {
    // if there's no subjects selected populate the subjects with the ones from the collection
    if (subjectsSlimSelect.getSelected().length === 0) {
        let target = event.target as HTMLSelectElement;
        let collectionOption = target.options[target.selectedIndex];
        if (collectionOption && collectionOption.dataset.subjects) {
            subjectsSlimSelect.setSelected(collectionOption.dataset.subjects.split(","));
        }
    }
});

let seedUrlsTextArea = document.getElementById("seedUrls") as HTMLTextAreaElement;

function escapeHtml(text) {
    var element = document.createElement('p');
    element.appendChild(document.createTextNode(text));
    return element.innerHTML;
}

let publisherSlimSelect = null as SlimSelect;

// fetch the contact people for the selected publisher
function refreshPublisherContactPeople(publisherId) {
    console.log("refreshPublisherContactPeople(" + publisherId + ")");
    let select = document.getElementById("titlePermission.contactPerson") as HTMLSelectElement;
    if (!select) {
        console.warn("Couldn't find titlePermission.contactPerson select");
        return;
    }

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
    let publisherState = {
        savedSearch: ""
    };
    publisherSlimSelect = new SlimSelect({
        select: '#publisher',
        settings: {
            hideSelected: true,
            allowDeselect: true,
            placeholderText: ''
        },
        events: {
            addable: function (value) {
                return {
                    text: value,
                    value: "new" // I'd prefer to use null/empty-string but slim-js then overrides it with the text
                };
            },
            searchFilter: function (option, search) {
                return true; // leave filtering to the backend
            },
            search: function (search, currentData) {
                if (search) publisherState.savedSearch = search;
                return new Promise((resolve, reject) => {
                    if (!search) return reject('No input');
                    fetch(publisherJsonEndpoint + "?q=" + encodeURIComponent(search) + "&size=100")
                        .then(response => response.ok ? response.json() : Promise.reject(response))
                        .then(results => {
                            resolve(results.map(publisher => {
                                return {
                                    value: publisher.id,
                                    text: publisher.name,
                                    innerHTML: ('<span class=publisher-select-item>' + escapeHtml(publisher.name) +
                                        ' <a class=title-count href=' + publishersEndpoint + '/' + publisher.id + ' target=_blank>'
                                        + publisher.titleCount + '</a></span>')
                                }
                            }));
                        }).catch(reject);
                });
            },
            // By default, slim-select clears the search when the drop-down is closed. This means if the user focuses
            // another field we lose their search or any pre-populated value. So as a workaround stash a copy of the
            // search text before closing and restore it after closing.
            afterClose: function () {
                publisherSlimSelect.search(publisherState.savedSearch);
            },
            afterChange: function (selectedOptions) {
                let publisherTypeSelect = document.getElementById("publisherType") as HTMLSelectElement;
                let publisherIdInput = document.getElementById("publisherId") as HTMLInputElement;
                let publisherNameInput = document.getElementById("publisherName") as HTMLInputElement;
                let newPublisherFieldsDiv = document.getElementById("newPublisherFields") as HTMLDivElement;
                let selectedOption = selectedOptions && selectedOptions[0];
                if (selectedOption && selectedOption.value === "new") {
                    publisherIdInput.value = "";
                    publisherNameInput.value = selectedOption.text;
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
                    publisherIdInput.value = !selectedOption || selectedOption.value === undefined ? "" : selectedOption.value;
                    publisherNameInput.value = "";
                    publisherTypeSelect.selectedIndex = 0;
                    publisherTypeSelect.required = false;
                    newPublisherFieldsDiv.style.display = "none";
                }
                refreshPublisherContactPeople(selectedOption.value);
            }
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

function createLink(text: string, href: string, target: string, className?: string) {
    let link = document.createElement("a");
    link.href = href;
    link.innerText = text;
    if (target) link.target = target;
    if (className) link.classList.add(className);
    return link;
}

let nameInput = document.getElementById("name") as HTMLInputElement;

function nameChanged() {
    let selected = publisherSlimSelect.getSelected();
    if (publisherSlimSelect && (!selected || !selected[0])) {
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

function isValidUrl(string) {
    try {
        if (!string.match(/^https?:\/\//)) return false;
        new URL(string);
        return true;
    } catch (err) {
        return false;
    }
}

function normalizeSeedUrls() {
    let urls = [];
    let changedAnything = false;
    seedUrlsTextArea.setCustomValidity("");
    for (let url of getSeedUrls()) {
        let originalUrl = url;
        if (url === "") continue;
        if (!url.match(/^[a-z]+:\/\//)) {
            url = "http://" + url;
        }
        if (!isValidUrl(url)) {
            seedUrlsTextArea.setCustomValidity("Invalid URL: " + url);
        }
        urls.push(url);
        if (url !== originalUrl) {
            changedAnything = true;
        }
    }
    seedUrlsTextArea.reportValidity();
    if (changedAnything) {
        seedUrlsTextArea.value = urls.join("\n");
    }
}

function seedUrlsChanged() {
    let fetchAlert = document.getElementById("fetchAlert");
    fetchAlert.innerHTML = '';
    fetchAlert.style.display = 'none'
    document.getElementById("duplicateAlert").innerHTML = '';
    document.getElementById("duplicateAlert").style.display = 'none';
    document.getElementById("suggestedSubjects").parentElement.style.display = 'none';

    normalizeSeedUrls();

    let primarySeedUrl = getPrimarySeedUrl();
    applySitePresets(primarySeedUrl);

    fetch(titleCheckEndpoint + "?url=" + encodeURIComponent(primarySeedUrl))
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
                    encodeURIComponent(primarySeedUrl), "_blank"));
            }
        });

    fetch(subjectsSuggestEndpoint + "?url=" + encodeURIComponent(primarySeedUrl))
        .then(response => response.ok ? response.json() : Promise.reject(response))
        .catch(reason => console.log(reason))
        .then(subjectNames => {
            let span = document.getElementById("suggestedSubjects");
            span.innerHTML = '';

            for (const subject of subjectNames) {
                if (span.childElementCount > 0) {
                    span.append(", ");
                }
                let a = document.createElement("a");
                a.innerText = subject;
                a.onclick = function() {
                    for (const option of subjectsSlimSelect.getData()) {
                        if (option['text'] === subject) {
                            const selected = subjectsSlimSelect.getSelected();
                            selected.push(option.value);
                            subjectsSlimSelect.setSelected(selected);
                            return false;
                        }
                    }
                    return false;
                };
                span.append(a);
            }

            span.parentElement.style.display = 'inherit';
        });

    fetch(pageinfoEndpoint + "?url=" + encodeURIComponent(primarySeedUrl))
        .then(response => response.ok ? response.json() : Promise.reject(response))
        .catch(reason => console.log(reason))
        .then(info => {
            function normalize(url) {
                return url.replace(/^https?:\/\//, "").replace(/^www\./, "").replace(/\/+$/, "");
            }

            if (info.location && normalize(primarySeedUrl).localeCompare(normalize(info.location), 'en', {sensitivity: 'base'}) === 0) {
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

function selectValues(slimSelect: SlimSelect, valuesToSelect: string[]) {
    let values = slimSelect.getSelected();
    for (let value of valuesToSelect) {
        if (!values.includes(value)) {
            values.push(value);
        }
    }
    slimSelect.setSelected(values);
}

function applySitePresets(url : string) {
    for (let preset of sitePresets) {
        if (!url.match(preset.urlRegex)) continue;
        console.log("Applying preset: " + JSON.stringify(preset));

        if (preset.scope) {
            let scopeRadio = document.querySelector("input[type=radio][name=scope][value='" + preset.scope + "']") as HTMLInputElement;
            if (scopeRadio) {
                scopeRadio.checked = true;
            }
        }

        if (preset.gatherMethod) {
            let gatherMethodSelect = document.getElementById('gatherMethod') as HTMLSelectElement;
            gatherMethodSelect.value = preset.gatherMethod.toString();
        }

        if (preset.subjects && preset.subjects.length !== 0) {
            let valuesToSelect = preset.subjects.map(subject => subject.toString());
            selectValues(subjectsSlimSelect, valuesToSelect);
        }

        if (preset.collections && preset.collections.length !== 0) {
            for (let collection of preset.collections) {
                collectionsSlimSelect.addOption({
                    text: collection.name,
                    value: collection.id.toString(),
                });
            }
            let valuesToSelect = preset.collections.map(collection => collection.id.toString());
            selectValues(collectionsSlimSelect, valuesToSelect);
        }
    }
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
document.querySelectorAll("button.btn-primary[type=submit][disabled]")
    .forEach(submitButton => (submitButton as HTMLButtonElement).disabled = false);