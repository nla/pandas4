package pandas.admin.gather;

import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.List;

public interface FilterPresetDAO {
    @SqlQuery("SELECT gather_filter_preset_id id, filter_name name, filter_preset filters FROM gather_filter_preset")
    @MapResultAsBean
    List<FilterPreset> listFilterPresets();

    @SqlQuery("SELECT gather_filter_preset_id id, filter_name name, filter_preset filters FROM gather_filter_preset WHERE gather_filter_preset_id = :id")
    @MapResultAsBean
    FilterPreset findFilterPreset(@Bind("id") int id);

    @SqlUpdate("UPDATE gather_filter_preset SET filter_name = :name, filter_preset = :filters WHERE gather_filter_preset_id = :id")
    int updateFilterPreset(@BindBean FilterPreset preset);

    @SqlUpdate("INSERT INTO gather_filter_preset (gather_filter_preset_id, filter_name, filter_preset) VALUES (gather_filter_preset_seq.NEXTVAL, :name, :filters)")
    void insertFilterPreset(@BindBean FilterPreset preset);

    @SqlUpdate("DELETE FROM gather_filter_preset WHERE gather_filter_preset_id = :id")
    int deleteFilterPreset(@Bind("id") long id);
}
