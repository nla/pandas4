let issueTable = document.getElementById("issueTable");
let draggingRow = null;
let placeholderRow = document.createElement('tr');
let td = document.createElement('td');
td.colSpan = 3;
placeholderRow.append(td);

issueTable.addEventListener("click", function (event) {
    let button = event.target.closest('button');
    if (!button) return;
    if (button.classList.contains('add-issue-button')) {
        event.preventDefault();
        let newIssue = document.getElementById('issueTemplate').content.cloneNode(true);
        let nameField = newIssue.querySelector('input[name=name]');
        button.closest('tr').after(newIssue);
        nameField.focus();
    } else if (button.classList.contains('add-issue-group-button')) {
        event.preventDefault();
        let newGroup = document.getElementById('issueGroupTemplate').content.cloneNode(true);
        let nameField = newGroup.querySelector('input[name=name]');
        button.closest('tr').after(newGroup);
        nameField.focus();
    } else if (button.classList.contains('delete-issue-button')) {
        event.preventDefault();
        button.closest('tr').remove();
    }
});

issueTable.addEventListener("dragstart", function (event) {
    if (event.target.classList.contains("issue") || event.target.classList.contains("issue-group")) {
        event.dataTransfer.setData('text/x-pandas-issue', 'true');
        event.dataTransfer.effectAllowed = 'move';
        draggingRow = event.target;
        placeholderRow.style.height = draggingRow.clientHeight + 'px';
        event.target.after(placeholderRow);
        window.requestAnimationFrame(() => draggingRow.style.display = "none");
    }
});

issueTable.addEventListener('dragover', function (event) {
    let tr = event.target.closest('tr');
    if (tr && (tr.classList.contains("issue") || tr.classList.contains("issue-group"))) {
        event.preventDefault();
        if (tr.rowIndex + 1 === placeholderRow.rowIndex) {
            tr.before(placeholderRow);
        } else {
            tr.after(placeholderRow);
        }
    }
});

issueTable.addEventListener('dragend', function () {
    if (draggingRow !== null) {
        draggingRow.style.display = "";
        placeholderRow.after(draggingRow);
        placeholderRow.remove();
        draggingRow = null;
    }
});

issueTable.addEventListener('mouseenter', function (event) {
    if (event.target.classList.contains("drag-handle")) {
        event.target.closest('tr').draggable = true;
    }
}, true);

issueTable.addEventListener('mouseleave', function (event) {
    if (event.target.classList.contains("drag-handle")) {
        event.target.closest('tr').draggable = false;
    }
}, true);