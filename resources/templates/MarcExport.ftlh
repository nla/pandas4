[#include "layout.ftlh"]

[@page "Export MARC Records"]

<link rel="stylesheet" href="webjars/datatables/1.10.21/css/dataTables.bootstrap4.min.css">
<script src="webjars/jquery/1.11.1/jquery.min.js" defer></script>
<script src="webjars/bootstrap/4.5.0/js/bootstrap.min.js" defer></script>
<script src="webjars/webshim/1.15.8/dev/polyfiller.js" defer></script>
<script src="webjars/datatables/1.10.21/js/jquery.dataTables.min.js" defer></script>
<script src="webjars/datatables/1.10.21/js/dataTables.bootstrap4.min.js" defer></script>
<script src="marcexport/MarcExport.js" defer></script>

<style>
    form textarea {
        width: 300px;
        height: 400px;
    }

    input[type=date] {
        width: 10em;
    }

    .table-filters {
        margin-left: 20px;
        margin-right: 20px;
        margin-top: 20px;
        margin-bottom: 10px;
    }
</style>

<ul class="nav nav-tabs" id="myTab" role="tablist">
    <li class="nav-item" role="presentation">
        <a class="nav-link active" id="home-tab" data-toggle="tab" href="#tab-date-range" role="tab" aria-controls="home" aria-selected="true">Date Range</a>
    </li>
    <li class="nav-item" role="presentation">
        <a class="nav-link" id="profile-tab" data-toggle="tab" href="#tab-individual-titles" role="tab" aria-controls="profile" aria-selected="false">Individual Titles</a>
    </li>
</ul>

<div class="tab-content">
    <div class="tab-pane show active" id="tab-date-range">
        <div class="table-filters">
            <div>
                First archived between <input type="date" name="startDate"
                                              class="max-today start-date reload-grid"> and
                <input type="date" class="max-today end-date reload-grid" name="endDate">
            </div>
            <div>
                <label><input id="includeIntegrating" type="checkbox" checked class="reload-grid">
                    Integrating</label>
                <label><input id="includeMono" type="checkbox" checked class="reload-grid"> Mono</label>
                <label><input id="includeSerial" type="checkbox" class="reload-grid"> Serial</label>
                <label><input id="includeCollectionMembers" type="checkbox" class="reload-grid"> In a collection</label>
                <label><input id="includeCataloguingNotRequired" type="checkbox" class="reload-grid">
                    Cataloguing not
                    required</label>
            </div>
        </div>
        <div class="well">
            <table id="grid" class="table">
                <thead>
                <tr>
                    <th><input id="selectAll" type="checkbox" checked></th>
                    <th>PI</th>
                    <th>Title</th>
                    <th>Format</th>
                    <th>Owner</th>
                    <th>First Instance</th>
                    <th>First Archived</th>
                </tr>
                </thead>
                <tbody>

                </tbody>
            </table>

        </div>
        <div>
            <form method="post" id="dateRangeForm">
                <input type="hidden" name="ids">
                <button name="format" value="text" class="btn btn-primary">Preview</button>
                <button name="format" value="marc" class="btn btn-secondary">Export</button>
            </form>
        </div>
    </div>
    <div class="tab-pane" id="tab-individual-titles">
        <form method="post">
            <div>
                <p><label for="ids">List of PANDAS title ids (PIs) to export:</label></p>
                <textarea id="ids" name="ids"></textarea>
            </div>

            <div>
                <button name="format" value="text" class="btn btn-primary">Preview</button>
                <button name="format" value="marc" class="btn btn-secondary">Export</button>
            </div>
        </form>
    </div>
</div>


[/@page]