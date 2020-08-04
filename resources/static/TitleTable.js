$('#datatable').DataTable({
    'ajax': {
        'contentType': 'application/json',
        'url': 'titles/datatable',
        'type': 'POST',
        'data': function (d) {
            return JSON.stringify(d);
        }
    },

    serverSide: true,
    pageLength: 25,
    columns: [
        {
            data: 'name',
            render: function(data, type, row, meta) {
                return $("<a>", {href: "titles/" + row.id, text: data})[0].outerHTML;
            }
        },
    ]
});