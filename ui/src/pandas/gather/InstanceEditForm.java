package pandas.gather;

import jakarta.validation.constraints.NotBlank;

public record InstanceEditForm(Boolean isDisplayed, @NotBlank String tepUrl) {
    public static InstanceEditForm of(Instance instance) {
        return new InstanceEditForm(instance.getIsDisplayed(), instance.getTepUrl());
    }

    public void applyTo(Instance instance) {
        instance.setIsDisplayed(isDisplayed);
        instance.setTepUrl(tepUrl);
    }
}
