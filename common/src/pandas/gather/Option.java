package pandas.gather;

import jakarta.persistence.*;

@Entity
@Table(name = "COMMAND_LINE_OPT")
public class Option {
    public static long GATHER_FILTERS_ID = 4;

    @Id
    @Column(name = "COMMAND_LINE_OPTION_ID", nullable = false, precision = 0)
    private Long id;

    @Basic
    @Column(name = "ACCESS_LEVEL", nullable = true, precision = 0)
    private Long accessLevel;

    @Basic
    @Column(name = "DEFAULT_VALUE", nullable = true, length = 128)
    private String defaultValue;

    @Basic
    @Column(name = "DISPLAY_NAME", nullable = true, length = 256)
    private String displayName;

    @Basic
    @Column(name = "EXPLANATION", nullable = true, length = 2000)
    private String explanation;

    @Basic
    @Column(name = "HIDE_ARGUMENT", nullable = true, precision = 0)
    private Integer hideArgument;

    @Basic
    @Column(name = "HIDE_OPTION", nullable = true, precision = 0)
    private Integer hideOption;

    @Basic
    @Column(name = "IS_ACTIVE", nullable = true, precision = 0)
    private Integer active;

    @Basic
    @Column(name = "IS_ARGUMENT_QUOTED", nullable = true, precision = 0)
    private Integer isArgumentQuoted;

    @Basic
    @Column(name = "IS_MANDATORY", nullable = true, precision = 0)
    private Integer mandatory;

    @Basic
    @Column(name = "LONG_OPTION", nullable = true, length = 64)
    private String longOption;

    @Basic
    @Column(name = "OPTION_DESCRIPTION", nullable = true, length = 256)
    private String optionDescription;

    @Basic
    @Column(name = "OPTION_PREFIX", nullable = true, length = 16)
    private String optionPrefix;

    @Basic
    @Column(name = "OPTION_SEPARATOR", nullable = true, length = 16)
    private String optionSeparator;

    @Basic
    @Column(name = "SHORT_OPTION", nullable = true, length = 16)
    private String shortOption;

    @Basic
    @Column(name = "UI_ELEMENT", nullable = true, length = 64)
    private String uiElement;

    @ManyToOne
    @JoinColumn(name = "OPTION_GROUP_ID", referencedColumnName = "OPTION_GROUP_ID")
    private OptionGroup group;

    public Long getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(Long accessLevel) {
        this.accessLevel = accessLevel;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Integer getHideArgument() {
        return hideArgument;
    }

    public void setHideArgument(Integer hideArgument) {
        this.hideArgument = hideArgument;
    }

    public Integer getHideOption() {
        return hideOption;
    }

    public void setHideOption(Integer hideOption) {
        this.hideOption = hideOption;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Integer getIsArgumentQuoted() {
        return isArgumentQuoted;
    }

    public void setIsArgumentQuoted(Integer argumentQuoted) {
        this.isArgumentQuoted = argumentQuoted;
    }

    public Integer getMandatory() {
        return mandatory;
    }

    public void setMandatory(Integer mandatory) {
        this.mandatory = mandatory;
    }

    public String getLongOption() {
        return longOption;
    }

    public void setLongOption(String longOption) {
        this.longOption = longOption;
    }

    public String getOptionDescription() {
        return optionDescription;
    }

    public void setOptionDescription(String optionDescription) {
        this.optionDescription = optionDescription;
    }

    public String getOptionPrefix() {
        return optionPrefix;
    }

    public void setOptionPrefix(String optionPrefix) {
        this.optionPrefix = optionPrefix;
    }

    public String getOptionSeparator() {
        return optionSeparator;
    }

    public void setOptionSeparator(String optionSeparator) {
        this.optionSeparator = optionSeparator;
    }

    public String getShortOption() {
        return shortOption;
    }

    public void setShortOption(String shortOption) {
        this.shortOption = shortOption;
    }

    public String getUiElement() {
        return uiElement;
    }

    public void setUiElement(String uiElement) {
        this.uiElement = uiElement;
    }

    public OptionGroup getGroup() {
        return group;
    }

    public void setGroup(OptionGroup group) {
        this.group = group;
    }

    public boolean isGatherFilters() {
        return id.equals(GATHER_FILTERS_ID);
    }
}
