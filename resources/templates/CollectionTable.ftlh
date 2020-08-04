[#include "_layout.ftlh"]
[#include "_collection.ftlh"]

[@page "Collections"]
    <link rel="stylesheet" href="webjars/datatables/1.10.21/css/dataTables.bootstrap4.min.css">
    <table id="datatable">
        <thead>
        <tr>
            <th>Name</th>
            <th>Titles</th>
        </tr>
        </thead>
    </table>

    <script src="webjars/jquery/1.11.1/jquery.min.js" defer></script>
    <script src="webjars/bootstrap/4.5.0/js/bootstrap.min.js" defer></script>
    <script src="webjars/webshim/1.15.8/dev/polyfiller.js" defer></script>
    <script src="webjars/datatables/1.10.21/js/jquery.dataTables.min.js" defer></script>
    <script src="webjars/datatables/1.10.21/js/dataTables.bootstrap4.min.js" defer></script>
    <script src="${requestContext.getContextUrl('CollectionTable.js')}" defer></script>
[/@page]