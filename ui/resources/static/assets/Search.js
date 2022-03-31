// omit empty/default fields from the query string for cleaner URLs
function omitEmptyFields() {
    document.querySelectorAll('.facets input[type=date], .facets input[type=search]').forEach(field => {
        if (!field.value) {
            field.name = "";
        }
    });
    document.querySelectorAll('select.auto-submit').forEach(field => {
        if (field.value === field.dataset.default) {
            field.name = "";
        }
    });
}

document.getElementById("sf").onsubmit = omitEmptyFields;

// clear all
document.getElementById("filter-clear-button").onclick = function () {
    document.querySelectorAll('.facets input[type=checkbox]').forEach(cb => cb.checked = false);
    document.querySelectorAll('.facets input[type=date], .facets input[type=search]').forEach(field => field.value = "");
    omitEmptyFields();
    this.form.submit();
}

// auto-submit when checkboxes are clicked
document.querySelectorAll(".facets input[type='checkbox'], .auto-submit").forEach(field =>
    field.onchange = function () {
        omitEmptyFields();
        this.form.submit();
    });