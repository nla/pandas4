package pandas.agency;

import pandas.core.Organisation;

public record AgencyEditForm(String name,
                             String alias,
                             String url,
                             String externalEmail,
                             String localReferencePrefix,
                             String localDatabasePrefix) {

    public static AgencyEditForm of(Agency agency) {
        Organisation organisation = agency.getOrganisation();
        return new AgencyEditForm(agency.getName(),
                organisation.getAlias(),
                organisation.getUrl(),
                agency.getExternalEmail(),
                agency.getLocalReferencePrefix(),
                agency.getLocalDatabasePrefix());
    }

    public void applyTo(Agency agency) {
        Organisation organisation = agency.getOrganisation();
        organisation.setName(name);
        organisation.setUrl(url);
        agency.setExternalEmail(externalEmail);
        agency.setLocalReferencePrefix(localReferencePrefix);
        agency.setLocalDatabasePrefix(localDatabasePrefix);
    }
}
