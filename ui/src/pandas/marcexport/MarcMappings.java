package pandas.marcexport;

import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import static pandas.marcexport.Format.MONO;
import static pandas.marcexport.PublisherType.GOVERNMENT;
import static pandas.marcexport.PublisherType.PERSONAL;

public class MarcMappings {
    public static Record nlaCatalogue(Title title, Date today) {
        MarcWriter marc = new MarcWriter();

        //
        // Leader
        //

        Leader leader = marc.getRecord().getLeader();
        leader.setRecordStatus('n'); // New
        leader.setTypeOfRecord('a'); // Language material
        leader.setImplDefined1(new char[] {
                title.getFormat().leaderCode(), // 07 - Bibliographic level = monograph
                ' '  // 08 - Type of control = No specified type
        });
        leader.setCharCodingScheme('a'); // UCS/Unicode
        leader.setImplDefined2(new char[] {
                ' ', // 17 - Encoding Level = full
                'i', // 18 - Description cataloguing form = ISBD punctuation included
                ' '  // 19 - Multipart resource record level = not set
        });

        //
        // Control fields
        //

        // 006 - Fixed-Length Data Elements-Additional Material Characteristics
        if (title.getPublisherType() == GOVERNMENT) {
            marc.addControlField("006", "m     o  d f      ");
        } else {
            marc.addControlField("006", "m     o  d        ");
        }

        // 007 - Physical Description Fixed Field-General Information
        marc.addControlField("007", "cr mn ---unn|n");

        // 008 - Fixed-Length Data Elements-General Information
        marc.addControlField("008", formatDate("yyMMdd", today) + // 00 - Date entered on file
                (title.getFormat() == MONO ? "s" :"c") + // 06 - Type of date/Publication status
                "20uu9999at      o    " +
                (title.getPublisherType() == GOVERNMENT ? "f" : " ") + // 28 - Government publication
                "000 0 eng  ");

        //
        // Data fields
        //

        // 040 - Cataloguing source
        marc.addField("040", ' ', ' ',
                "a", "ANL",
                "b", "eng",
                "e", "rda");

        // 042 - Authentication code
        marc.addField("042", ' ', ' ',
                "a", "anuc");

        // 043 - Geographic area code
        marc.addField("043", ' ', ' ',
                "a", "u-at---");

        // 100 - Creator
        boolean hasCreator;
        if (title.getPublisherType() == PERSONAL) {
            marc.addField("100", '1', ' ',
                    "a", reverseCommaName(title.getPublisherName()) + ",",
                    "e", "author.");
            hasCreator = true;
        } else {
            hasCreator = false;
        }

        // 245 - Title Statement
        marc.addField("245", hasCreator ? '1' : '0', nonfilingCharactersIndicator(title.getName()),
                "a", title.getName() + ".");

        // 264 - Production, Publication, Distribution, Manufacture, and Copyright Notice
        if (title.getPublisherName() != null) {
            if (title.getFormat() == MONO) {
                marc.addField("264", ' ', '1',
                        "a", "[Australia]:",
                        "b", title.getPublisherName() + ",",
                        "c", ".");
            } else {
                marc.addField("264", ' ', '1',
                        "a", "[Australia]:",
                        "b", title.getPublisherName());
            }
        }

        // 300 - Physical Description
        marc.addField("300", ' ', ' ',
                "a", "1 online resource");


        // 336 - Content Type
        marc.addField("336", ' ', ' ',
                "a", "two-dimensional moving image",
                "2", "rdacontent");

        marc.addField("336", ' ', ' ',
                "a", "text",
                "2", "rdacontent");

        // 337 - Media Type
        marc.addField("337", ' ', ' ',
                "a", "computer",
                "2", "rdamedia");

        // 338 - Carrier Type
        marc.addField("338", ' ', ' ',
                "a", "online resource",
                "2", "rdacarrier");

        // 347 - Digital File Characteristics
        if (title.getFormat() == MONO) {
            marc.addField("347", ' ', ' ',
                    "a", "text file",
                    "b", "PDF",
                    "2", "rda");
        }

        // 500 - General Note
        if (title.getFormat() == MONO) {
            marc.addField("500", ' ', ' ',
                    "a", "Title from title page (viewed on " + formatDate("d MMMM yyyy", title.getEarliestInstanceDate()) + ")");
        } else {
            marc.addField("500", ' ', ' ',
                    "a", "Title from title screen (viewed on " + formatDate("d MMMM yyyy", title.getEarliestInstanceDate()) + ")");
        }

        // 538 - System Details Note
        marc.addField("538", ' ', ' ',
                "a", "Mode of access: Available Online. Address as at " +
                        formatDate("dd/MM/yyyy", new Date()) + ":",
                "u", title.getTitleUrl());

        // 583 - Action Note
        marc.addField("583", ' ', ' ',
                "a", "Selected for archiving",
                "5", "ANL");

        // 653 - Index Term-Uncontrolled
        marc.addField("653", ' ', ' ',
                "a", "Australian");

        // 710 - Added Entry-Corporate Name
        if (title.getPublisherType() != null) {
            switch (title.getPublisherType()) {
                case OTHER:
                case ORGANISATION:
                case COMMERCIAL:
                    marc.addField("710", '2', ' ',
                            "a", title.getPublisherName() + ",",
                            "e", "issuing body.");
                    break;

                case GOVERNMENT:
                case EDUCATION: {
                    String[] parts = title.getPublisherName().split("\\. *", 2);
                    if (parts.length == 1) {
                        marc.addField("710", '2', ' ',
                                "a", parts[0] + ",",
                                "e", "issuing body.");
                    } else {
                        marc.addField("710", '1', ' ',
                                "a", parts[0] + ".",
                                "b", parts[1] + ",",
                                "e", "issuing body.");
                    }
                    break;
                }
            }
        }

        // 830 - Series Added Entry-Uniform Title
        marc.addField("830", ' ', '0',
                "a", "PANDORA electronic collection.");

        // 852 - Location
        marc.addField("852", '8', ' ',
                "b", "ELECAUS",
                "h", "Internet");

        // 856 - Electronic Location and Access
        if (title.getTitleUrl() != null &&
                !title.getTitleUrl().isEmpty() &&
                !title.getTitleUrl().equals("http://")) {
            marc.addField("856", '4', '0',
                    "z", "Publisher site",
                    "u", title.getTitleUrl());
        }

        marc.addField("856", '4', '1',
                "z", "Archived at ANL",
                "u", "http://nla.gov.au/nla.arc-" + title.getId());

        return marc.getRecord();
    }

    /**
     * Given the name "Jane Elizabeth Doe" returns "Doe, Jane Elizabeth".
     */
    private static String reverseCommaName(String name) {
        String names[] = name.split(" ");
        if (names.length > 1) {
            String lastName = names[names.length - 1];
            String otherNames = String.join(" ", Arrays.copyOfRange(names, 0, names.length - 1));
            return lastName + ", " + otherNames;
        } else {
            return name;
        }
    }

    /**
     * Given a title like "The National Library of Australia", returns the length of the nonfiling character
     * prefix (i.e. "a " = 2, "an "=3, "the "=4).
     */
    private static char nonfilingCharactersIndicator(String title) {
        String lowerCaseTitle = title.toLowerCase();
        for (String prefix : NONFILING_PREFIXES) {
            if (lowerCaseTitle.startsWith(prefix)) {
                return Character.forDigit(prefix.length(), 10);
            }
        }
        return '0';
    }

    private static final String[] NONFILING_PREFIXES = {"a ", "an ", "the "};

    public static String formatDate(String format, Date date) {
        return new SimpleDateFormat(format).format(date);
    }


}
