export {};
import type {default as SlimSelect_} from "slim-select";

declare var SlimSelect: typeof SlimSelect_;
declare var collectionsEndpoint: string;
declare var updateReasons: () => void; /* defined inline in TitleEdit.html */

new SlimSelect({
    select: '#subjectsToAdd',
    hideSelectedOption: true,
    searchFilter: function (option, search) {
        return option.data['fullname'].toLowerCase().indexOf(search.toLowerCase()) !== -1;
    }
});

new SlimSelect({
    select: '#subjectsToRemove',
    hideSelectedOption: true,
    searchFilter: function (option, search) {
        return option.data['fullname'].toLowerCase().indexOf(search.toLowerCase()) !== -1;
    }
});

new SlimSelect({
    select: '#collectionsToAdd',
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

new SlimSelect({
    select: '#collectionsToRemove',
    hideSelectedOption: true,
});

document.querySelectorAll("input[type=checkbox][id^=edit]").forEach((checkbox : HTMLInputElement) => {
    const inputId = checkbox.id.substr(4, 1).toLowerCase() + checkbox.id.substr(5);
    const input = document.getElementById(inputId) as HTMLInputElement;
    input.disabled = !checkbox.checked;
    checkbox.addEventListener("change", () => {
        input.disabled = !checkbox.checked;
        if (checkbox.checked) {
            input.focus();
        }
        if (checkbox.id === "editStatus") {
            updateReasons();
        }
    });
});

function countSelectedTitles() : number {
    return document.querySelectorAll("input[name=titles]:checked").length;
}

function updateTitleCount() {
    const count = countSelectedTitles();
    document.querySelectorAll(".titleCount").forEach(span => {
        span.textContent = count.toLocaleString();
    });
    (document.getElementById("actionSave") as HTMLButtonElement).disabled = (count === 0);
}

document.querySelectorAll("input[name=titles]").forEach(checkbox => {
    checkbox.addEventListener("change", () => {
        updateTitleCount();
    });
});

(document.getElementById("selectAllTitles") as HTMLInputElement).addEventListener("change", function () {
    document.querySelectorAll('input[name=titles]').forEach((cb : HTMLInputElement) => cb.checked = this.checked);
    updateTitleCount();
});

document.getElementById("titleBulkEditForm").addEventListener("submit", function () {
    return confirm("Are you certain you wish to bulk change " + countSelectedTitles() + " titles?\n\nThis operation cannot be undone.")
});