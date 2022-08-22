package pandas.collection;

import pandas.agency.Agency;

import java.time.Instant;
import java.time.ZoneId;

/**
 * Basic details about a Title. Used as an optimisation to reduce the amount of db queries we have to do when
 * displaying lists of titles.
 */
public class TitleBrief {
    private final Instant firstArchivedDate;
    private final Instant lastArchivedDate;
    private Long id;
    private Long pi;
    private String name;
    private Agency agency;

    public TitleBrief(Long id, Long pi, String name, Agency agency) {
        this(id, pi, name, agency, null, null);
    }

    public TitleBrief(Long id, Long pi, String name, Agency agency, Instant firstArchivedDate, Instant lastArchivedDate) {
        this.id = id;
        this.pi = pi;
        this.name = name;
        this.agency = agency;
        this.firstArchivedDate = firstArchivedDate;
        this.lastArchivedDate = lastArchivedDate;
    }

    public Long getId() {
        return id;
    }

    public Long getPi() {
        return pi;
    }

    public String getName() {
        return name;
    }

    public Agency getAgency() {
        return agency;
    }

    public Instant getFirstArchivedDate() {
        return firstArchivedDate;
    }

    public Instant getLastArchivedDate() {
        return lastArchivedDate;
    }

    public String getArchivedYearRange() {
        if (firstArchivedDate == null || lastArchivedDate == null) return null;
        int startYear = firstArchivedDate.atZone(ZoneId.systemDefault()).getYear();
        int endYear = lastArchivedDate.atZone(ZoneId.systemDefault()).getYear();
        if (startYear == endYear) return String.valueOf(startYear);
        return startYear + "-" + endYear;
    }
}
