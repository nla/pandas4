package pandas.admin.marcexport;

import java.util.Date;

public class TitleSummaryRow {
    long pi;
    String name;
    String format;
    String owner;
    Date instanceDate;
    Date archivedDate;
    private int agencyId;

    public Date getArchivedDate() {
        return archivedDate;
    }

    public void setArchivedDate(Date archivedDate) {
        this.archivedDate = archivedDate;
    }

    public long getPi() {
        return pi;
    }

    public void setPi(long pi) {
        this.pi = pi;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Date getInstanceDate() {
        return instanceDate;
    }

    public void setInstanceDate(Date instanceDate) {
        this.instanceDate = instanceDate;
    }

    public int getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(int agencyId) {
        this.agencyId = agencyId;
    }
}
