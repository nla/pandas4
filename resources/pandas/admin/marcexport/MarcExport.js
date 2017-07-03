$.webshims.polyfill("forms forms-ext");
$.webshims.activeLang('en-AU');

function mostRecentSunday() {
    var d = new Date();
    return new Date(d.setDate(d.getDate() - d.getDay()));
}

function startOfPreviousWeek() {
    var d = mostRecentSunday();
    return new Date(d.setDate(d.getDate() - 7));
}

function endOfPreviousWeek() {
    var d = mostRecentSunday();
    return new Date(d.setDate(d.getDate() - 1));
}

$(function () {

    $('[type="date"].start-date').prop('defaultValue', function () {
        return startOfPreviousWeek().toJSON().split('T')[0];
    });

    $('[type="date"].end-date').prop('defaultValue', function () {
        return endOfPreviousWeek().toJSON().split('T')[0];
    });

    $('[type="date"].max-today').prop('max', function () {
        return new Date().toJSON().split('T')[0];
    });

    var grid = $('#grid')
        .on('xhr.dt', function (e, settings, json, xhr) {
            $("#grid_length > label:nth-child(1)").contents().last()[0].textContent = " of " + json.data.length + " entries";
        })
        .DataTable({
            "ajax": {
                "url": "admin/marcexport/titles.json",
                "data": function (d) {
                    return $.extend({}, d, {
                        "startDate": $(".start-date").val(),
                        "endDate": $(".end-date").val(),
                        "includeIntegrating": $("#includeIntegrating").prop("checked"),
                        "includeMono": $("#includeMono").prop("checked"),
                        "includeSerial": $("#includeSerial").prop("checked"),
                        "includeCataloguingNotRequired": $("#includeCataloguingNotRequired").prop("checked"),
                        "includeCollectionMembers": $("#includeCollectionMembers").prop("checked")
                    });
                }
            },
            "columns": [
                {
                    data: null,
                    render: function (data, type, row) {
                        if (type === 'display') {
                            return '<input type="checkbox" class="editor-active row-checkbox" name="selected[]" value="' + row.pi + '" checked>';
                        }
                        return data;
                    },
                    className: "dt-body-center",
                    orderable: false,
                },
                {"data": "pi"},
                {"data": "name"},
                {"data": "format"},
                {"data": "owner"},
                {"data": "instanceDate"},
                {"data": "archivedDate"},
            ],
            "order": [[3, "asc"], [5, "desc"]],
            processing: true
        });

    $(".reload-grid").change(function () {
        grid.ajax.reload();
    });

    $("#selectAll").change(function () {
        $(grid.cells().nodes()).find('.row-checkbox').prop('checked', $(this).is(':checked'))
    });

    $("#dateRangeForm").submit(function () {
        var ids = '';

        $(grid.cells().nodes()).find('.row-checkbox:checked').each(function (i, checkbox) {
            ids += checkbox.value + "\n";
        });

        this.ids.value = ids;
    });
});