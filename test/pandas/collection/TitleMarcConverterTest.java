package pandas.collection;

import org.junit.Test;
import pandas.core.Organisation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

public class TitleMarcConverterTest {
    @Test
    public void test() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

        Organisation org = new Organisation();
        org.setName("Cricket Australia");
        org.setUrl("http://www.cricket.com.au/");

        PublisherType government = new PublisherType();
        government.setName("Government");

        Format format = new Format();
        format.setName("Serial");

        Publisher publisher = new Publisher();
        publisher.setOrganisation(org);
        publisher.setType(government);

        Instant firstInstance = dateFormat.parse("2016-04-27T09:19:51").toInstant();
        Title title = new Title() {
            @Override
            public Instant getFirstInstanceDate() {
                return firstInstance;
            }
        };
        title.setId(65591L);
        title.setPi(65591L);
        title.setName("cricket.com.au");
        title.setTitleUrl("http://www.cricket.com.au/");
        title.setRegDate(dateFormat.parse("2016-04-27T09:19:51").toInstant());
        title.setFormat(format);
        title.setPublisher(publisher);

        Date today = dateFormat.parse("2017-03-08T08:50:27");

        assertEquals("LEADER 00000nas a2200000 i 4500\n" +
                        "006 m     o  d f      \n" +
                        "007 cr mn ---unn|n\n" +
                        "008 170308c20uu9999at      o    f000 0 eng  \n" +
                        "040   $aANL$beng$erda\n" +
                        "042   $aanuc\n" +
                        "043   $au-at---\n" +
                        "245 00$acricket.com.au.\n" +
                        "264  1$a[Australia]:$bCricket Australia\n" +
                        "300   $a1 online resource\n" +
                        "336   $atwo-dimensional moving image$2rdacontent\n" +
                        "336   $atext$2rdacontent\n" +
                        "337   $acomputer$2rdamedia\n" +
                        "338   $aonline resource$2rdacarrier\n" +
                        "500   $aTitle from title screen (viewed on 27 April 2016)\n" +
                        "538   $aMode of access: Available Online. Address as at 08/03/2017:$uhttp://www.cricket.com.au/\n" +
                        "583   $aSelected for archiving$5ANL\n" +
                        "653   $aAustralian\n" +
                        "710 2 $aCricket Australia,$eissuing body.\n" +
                        "830  0$aPANDORA electronic collection.\n" +
                        "852 8 $bELECAUS$hInternet\n" +
                        "856 40$zPublisher site$uhttp://www.cricket.com.au/\n" +
                        "856 41$zArchived at ANL$uhttp://nla.gov.au/nla.arc-65591\n",
                TitleMarcConverter.convert(title, today).toString());
    }
}