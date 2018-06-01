[#-- @ftlvariable name="preset" type="pandas.admin.gather.FilterPreset" --]
[#include "../core/layout.ftl"]
[@page (preset.id??)?string("Edit Filter Preset", "New Filter Preset")]

<form method="post" class="form">
    <div class="form-group">
        <label for="name">Name:</label>
        <input class="form-control" id="name" name="name" value="${preset.name!""}" required>
    </div>
    <div class="form-group">
        <label for="filters">Filters:</label>
        <textarea class="form-control" id="filters" name="filters" rows="15">${preset.filters!""?replace(" ", "\n")}</textarea>
    </div>

    <div>
        [#if preset.id??]
            <div class="pull-right">
                <button type="submit" formaction="gather/filterpresets/${preset.id?c}/update" class="btn btn-primary">Save</button>
                <a href="gather/filterpresets" class="btn btn-default">Cancel</a>
            </div>
            <button type="submit" formaction="gather/filterpresets/${preset.id?c}/delete" class="btn btn-danger">Delete</button>
        [#else]
            <div class="pull-right">
                <button type="submit" formaction="gather/filterpresets/create" class="btn btn-primary">Save</button>
                <a href="gather/filterpresets" class="btn btn-default">Cancel</a>
            </div>
        [/#if]
    </div>
</form>


[/@page]