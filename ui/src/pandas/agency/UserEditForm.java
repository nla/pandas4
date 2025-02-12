package pandas.agency;

import jakarta.validation.constraints.NotBlank;
import pandas.core.Role;

import static pandas.core.Utils.trimToNull;

public record UserEditForm(
        @NotBlank String userid,
        Boolean disabled,
        @NotBlank String nameGiven,
        @NotBlank String nameFamily,
        @NotBlank String email,
        String phone,
        String mobilePhone,
        Agency agency,
        String roleType,
        String newPassword,
        String confirmPassword,
        boolean prefersStickyFilters) {

    public static UserEditForm of(User user) {
        return new UserEditForm(user.getUserid(), !user.isActive(), user.getNameGiven(), user.getNameFamily(), user.getEmail(),
                user.getPhone(), user.getMobilePhone(), user.getAgency(),
                user.getRole() == null ? null : user.getRole().getType(), null, null,
                user.getPrefersStickyFilters());
    }

    public void applyTo(User user, boolean editingSelf) {
        user.setUserid(trimToNull(userid));
        if (disabled != null && !editingSelf) {
            user.setActive(!disabled);
        }
        user.setNameGiven(trimToNull(nameGiven));
        user.setNameFamily(trimToNull(nameFamily));
        user.setEmail(trimToNull(email));
        user.setPhone(trimToNull(phone));
        if (newPassword != null && !newPassword.isEmpty() && newPassword.equals(confirmPassword)) {
            user.setPassword(newPassword);
        }
        user.setPrefersStickyFilters(prefersStickyFilters);

        if (agency != null || roleType != null) {
            Role role = user.getRole();
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
