package pandas.collection;

import pandas.agency.Agency;

/**
 * Basic details about a Title. Used as an optimisation to reduce the amount of db queries we have to do when
 * displaying lists of titles.
 */
public class TitleBrief {
    private Long id;
    private Long pi;
    private String name;
    private Agency agency;

    public TitleBrief(Long id, Long pi, String name, Agency agency) {
        this.id = id;
        this.pi = pi;
        this.name = name;
        this.agency = agency;
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
}
