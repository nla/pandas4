package pandas.core;

import pandas.agency.Agency;

import javax.validation.constraints.NotBlank;

import static info.freelibrary.util.StringUtils.trimToNull;

public record UserEditForm(
        @NotBlank String userid,
        boolean active,
        @NotBlank String nameGiven,
        @NotBlank String nameFamily,
        @NotBlank String email,
        String phone,
        String mobilePhone,
        Agency agency,
        String roleType) {

    public static UserEditForm of(Individual user) {
        return new UserEditForm(user.getUserid(), user.isActive(), user.getNameGiven(), user.getNameFamily(), user.getEmail(),
                user.getPhone(), user.getMobilePhone(), user.getAgency(),
                user.getRole() == null ? null : user.getRole().getType());
    }

    public void applyTo(Individual user) {
        user.setUserid(trimToNull(userid));
        user.setActive(active);
        user.setNameGiven(trimToNull(nameGiven));
        user.setNameFamily(trimToNull(nameFamily));
        user.setEmail(trimToNull(email));
        user.setPhone(trimToNull(phone));

        if (agency != null || roleType != null) {
            Role role = user.getRole();
            if (role == null) {
                role = new Role();
                user.setRole(role);
            }
            if (agency != null) {
                role.setOrganisation(agency.getOrganisation());
            }
            if (roleType != null) {
                role.setType(roleType);
                role.setTitle(Role.titles.get(roleType));
            }
        }
    }
}
