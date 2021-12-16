package pandas.agency;

import org.springframework.web.multipart.MultipartFile;
import pandas.core.Organisation;

import javax.validation.constraints.NotBlank;
import java.io.IOException;

import static info.freelibrary.util.StringUtils.trimToNull;

public record AgencyEditForm(@NotBlank String name,
                             @NotBlank String alias,
                             String url,
                             String externalEmail,
                             String localReferencePrefix,
                             String localDatabasePrefix,
                             MultipartFile logo,
                             boolean removeLogo) {

    public static AgencyEditForm of(Agency agency) {
        Organisation organisation = agency.getOrganisation();
        return new AgencyEditForm(agency.getName(),
                organisation.getAlias(),
                organisation.getUrl(),
                agency.getExternalEmail(),
                agency.getLocalReferencePrefix(),
                agency.getLocalDatabasePrefix(),
                null, false);
    }

    public void applyTo(Agency agency) throws IOException {
        Organisation organisation = agency.getOrganisation();
        organisation.setAlias(trimToNull(alias));
        organisation.setName(trimToNull(name));
        organisation.setUrl(trimToNull(url));
        agency.setExternalEmail(trimToNull(externalEmail));
        agency.setLocalReferencePrefix(trimToNull(localReferencePrefix));
        agency.setLocalDatabasePrefix(trimToNull(localDatabasePrefix));
        if (logo != null && !logo.isEmpty()) {
            agency.setLogo(logo.getBytes());
        } else if (removeLogo) {
            agency.setLogo(null);
        }
    }
}
