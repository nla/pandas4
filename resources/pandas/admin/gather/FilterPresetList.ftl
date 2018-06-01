[#-- @ftlvariable name="presets" type="java.util.Collection<pandas.admin.gather.FilterPreset>" --]
[#include "../core/layout.ftl"]
[@page "Gather Filter Presets"]
<ul>
    [#list presets as preset]
        <li><a href="gather/filterpresets/${preset.id}">${preset.name}</a></li>
    [/#list]
</ul>

<a href="gather/filterpresets/new" class="btn btn-primary">New Filter Preset</a>

[/@page]