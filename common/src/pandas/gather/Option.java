package pandas.gather;

import javax.persistence.*;

@Entity
@Table(name = "COMMAND_LINE_OPT")
public class Option {
    private Long accessLevel;
    private Long id;
    private String defaultValue;
    private String displayName;
    private String explanation;
    private Integer hideArgument;
    private Integer hideOption;
    private Integer active;
    private Integer isArgumentQuoted;
    private Integer mandatory;
    private String longOption;
    private String optionDescription;
    private String optionPrefix;
    private String optionSeparator;
    private String shortOption;
    private String uiElement;
    private OptionGroup group;

    @Basic
    @Column(name = "ACCESS_LEVEL", nullable = true, precision = 0)
    public Long getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Long accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Id
    @Column(name = "COMMAND_LINE_OPTION_ID", nullable = false, precision = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "DEFAULT_VALUE", nullable = true, length = 128)
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Basic
    @Column(name = "DISPLAY_NAME", nullable = true, length = 256)
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Basic
    @Column(name = "EXPLANATION", nullable = true, length = 2000)
    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    @Basic
    @Column(name = "HIDE_ARGUMENT", nullable = true, precision = 0)
    public Integer getHideArgument() {
        return hideArgument;
    }

    public void setHideArgument(Integer hideArgument) {
        this.hideArgument = hideArgument;
    }

    @Basic
    @Column(name = "HIDE_OPTION", nullable = true, precision = 0)
    public Integer getHideOption() {
        return hideOption;
    }

    public void setHideOption(Integer hideOption) {
        this.hideOption = hideOption;
    }

    @Basic
    @Column(name = "IS_ACTIVE", nullable = true, precision = 0)
    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    @Basic
    @Column(name = "IS_ARGUMENT_QUOTED", nullable = true, precision = 0)
    public Integer getIsArgumentQuoted() {
        return isArgumentQuoted;
    }

    public void setIsArgumentQuoted(Integer argumentQuoted) {
        this.isArgumentQuoted = argumentQuoted;
    }

    @Basic
    @Column(name = "IS_MANDATORY", nullable = true, precision = 0)
    public Integer getMandatory() {
        return mandatory;
    }

    public void setMandatory(Integer mandatory) {
        this.mandatory = mandatory;
    }

    @Basic
    @Column(name = "LONG_OPTION", nullable = true, length = 64)
    public String getLongOption() {
        return longOption;
    }

    public void setLongOption(String longOption) {
        this.longOption = longOption;
    }

    @Basic
    @Column(name = "OPTION_DESCRIPTION", nullable = true, length = 256)
    public String getOptionDescription() {
        return optionDescription;
    }

    public void setOptionDescription(String optionDescription) {
        this.optionDescription = optionDescription;
    }

    @Basic
    @Column(name = "OPTION_PREFIX", nullable = true, length = 16)
    public String getOptionPrefix() {
        return optionPrefix;
    }

    public void setOptionPrefix(String optionPrefix) {
        this.optionPrefix = optionPrefix;
    }

    @Basic
    @Column(name = "OPTION_SEPARATOR", nullable = true, length = 16)
    public String getOptionSeparator() {
        return optionSeparator;
    }

    public void setOptionSeparator(String optionSeparator) {
        this.optionSeparator = optionSeparator;
    }

    @Basic
    @Column(name = "SHORT_OPTION", nullable = true, length = 16)
    public String getShortOption() {
        return shortOption;
    }

    public void setShortOption(String shortOption) {
        this.shortOption = shortOption;
    }

    @Basic
    @Column(name = "UI_ELEMENT", nullable = true, length = 64)
    public String getUiElement() {
        return uiElement;
    }

    public void setUiElement(String uiElement) {
        this.uiElement = uiElement;
    }

    @ManyToOne
    @JoinColumn(name = "OPTION_GROUP_ID", referencedColumnName = "OPTION_GROUP_ID")
    public OptionGroup getGroup() {
        return group;
    }

    public void setGroup(OptionGroup group) {
        this.group = group;
    }
}
