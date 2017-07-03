package pandas.admin.marcexport;

import org.junit.Test;

import java.util.Date;

import static org.junit.Assert.assertEquals;

public class MarcMappingsTest {
    @Test
    public void nlaCatalogue() throws Exception {
        Title title = new Title();
        title.setId(65591);
        title.setName("cricket.com.au");
        title.setTitleUrl("http://www.cricket.com.au/");
        title.setRegisteredDate(new Date(1461716391087L));
        title.setPublisherName("Cricket Australia");
        title.setPublisherUrl("http://www.cricket.com.au/");
        title.setPublisherTypeId(1);
        title.setFormatId(1);
        title.setEarliestInstanceDate(new Date(1461716391087L));

        Date today = new Date(1488930627000L);

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
                        "538   $aMode of access: Available Online. Address as at " + MarcMappings.formatDate("dd/MM/yyyy", new Date()) + ":$uhttp://www.cricket.com.au/\n" +
                        "583   $aSelected for archiving$5ANL\n" +
                        "653   $aAustralian\n" +
                        "710 2 $aCricket Australia,$eissuing body.\n" +
                        "830  0$aPANDORA electronic collection.\n" +
                        "852 8 $bELECAUS$hInternet\n" +
                        "856 40$zPublisher site$uhttp://www.cricket.com.au/\n" +
                        "856 41$zArchived at ANL$uhttp://nla.gov.au/nla.arc-65591\n",
                MarcMappings.nlaCatalogue(title, today).toString());
    }
}