package pandas.gather;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "OPTION_ARGUMENT")
public class OptionArgument {
    private static final String BLANK_ARGUMENT = "<blank>";

    @Id
    @Column(name = "OPTION_ARGUMENT_ID", nullable = false, precision = 0)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "OPTION_ARGUMENT_SEQ")
    @SequenceGenerator(name = "OPTION_ARGUMENT_SEQ", sequenceName = "OPTION_ARGUMENT_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "COMMAND_LINE_OPTION_ID", referencedColumnName = "COMMAND_LINE_OPTION_ID")
    private Option option;

    @Basic
    @Column(name = "ARGUMENT", nullable = true, length = 4000)
    private String argument;
    @Basic
    @Column(name = "ARGUMENT_DESCRIPTION", nullable = true, length = 512)
    private String description;

    public String getArgument() {
        return argument;
    }

    public void setArgument(String argument) {
        this.argument = argument;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OptionArgument that = (OptionArgument) o;
        return Objects.equals(argument, that.argument) && Objects.equals(description, that.description) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(argument, description, id);
    }

    public Option getOption() {
        return option;
    }

    public void setOption(Option option) {
        this.option = option;
    }

    public void toCommandLine(StringBuilder sb) {
        if (option == null) return;

        boolean showArgument = option.getHideArgument() == null || option.getHideArgument() != 1;
        boolean hideNonZeroArgument = option.getHideArgument() != null && option.getHideArgument() == 2;
        boolean showOption = option.getHideOption() == null || option.getHideOption() == 0;
        boolean quoteArgument = option.getIsArgumentQuoted() != null && option.getIsArgumentQuoted() > 0;
        boolean innerQuoteArgument = option.getIsArgumentQuoted() != null && option.getIsArgumentQuoted() == 2;

        String argValue = getArgument();
        if (argValue != null) {
            if (argValue.equals(BLANK_ARGUMENT)) {
                return; // argument has been intentionally set to be blank by user, overriding any defaults
            } else if (argValue.equals("0") && option.getUiElement() != null && option.getUiElement().equals("checkbox") && !showArgument) {
                // We need to check to see if a checkbox option should be shown.
                // If the option is a checkbox and the hideArgument flag is 1 and the argument is 0 (ie. FALSE or OFF)
                // then we should not show the option (or the arg obviously)
                // This is the only option/uielement that behaves in this way
                // eg. OFF: <blank> ON: -j
                return;
            } else if(showArgument && hideNonZeroArgument && !argValue.equals("0")) {
                // This setting had to be added to deal with really inconsistent gather options like -%P that
                // expect "-%P0" when disabled but just "-%P" not "-%P1" when enabled. So this setting hides the
                // argument (but keeps the option) if the argument is non-zero.
                // eg. OFF: -%P0 ON: -%P (see comment above hideArgument() for details)
                showArgument = false;
            }
        } else if (showArgument) {
            // The argument should be shown, but it is null, so skip this option.
            return;
        }

        // write out the option prefix (minus-sign). eg. '-'
        if (option.getOptionPrefix() != null) {
            sb.append(option.getOptionPrefix());
        }

        // write out the option, if there is one. eg. '-f'
        if (option.getShortOption() != null && showOption) {
            sb.append(option.getShortOption());
        }

        // write out the argument, if there is one and quote if necessary. eg. '-f"hello"'
        if (argValue != null && showArgument) {
            if (quoteArgument) sb.append('"');
            if (innerQuoteArgument) {
                sb.append(argValue.replaceAll(" ","\" \""));
            } else {
                sb.append(argValue);
            }
            if (quoteArgument) sb.append('"');
        }

        // write out a space seperator.
        sb.append(" ");
    }
}
