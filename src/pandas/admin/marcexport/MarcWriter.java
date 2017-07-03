package pandas.admin.marcexport;

import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;

public class MarcWriter {
    private static final MarcFactory MARC_FACTORY = MarcFactory.newInstance();
    private Record record = MARC_FACTORY.newRecord();

    public void addField(String tag, char ind1, char ind2, String... subfieldCodesAndData) {
        record.addVariableField(MARC_FACTORY.newDataField(tag, ind1, ind2, subfieldCodesAndData));
    }

    public void addControlField(String tag, String data) {
        record.addVariableField(MARC_FACTORY.newControlField(tag, data));
    }

    public Record getRecord() {
        return record;
    }

    public String toString() {
        return record.toString();
    }
}
