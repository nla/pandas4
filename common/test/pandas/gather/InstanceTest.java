package pandas.gather;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstanceTest {

    @Test
    public void tepUrlToAbsolute() {
        assertEquals("http://example.com/", Instance.tepUrlToAbsolute("http://example.com/"));
        assertEquals("http://www.olympics.com/eng/index.html", Instance.tepUrlToAbsolute("/parchive/2000/olympics/O2000-Oct-01/www.olympics.com/eng/index.html"));
        assertEquals("http://www.awm.gov.au/journal/j33/index.htm", Instance.tepUrlToAbsolute("/parchive/2001/S2001-Mar-5/www.awm.gov.au/journal/j33/index.htm"));
        assertEquals("http://www.ozemail.com.au/~marcusr/aren/", Instance.tepUrlToAbsolute("/nph-arch/O1998-Apr-30/http://www.ozemail.com.au/~marcusr/aren/"));
        assertEquals("http://pandora.nla.gov.au/pan/10283/20020113-0000/www.usq.edu.au/faculty/business/departments/hrm/HRMJournal/JMP-Articles/Penny%20Clark.pdf", Instance.tepUrlToAbsolute("/pan/10283/20020113-0000/www.usq.edu.au/faculty/business/departments/hrm/HRMJournal/JMP-Articles/Penny%20Clark.pdf"));
    }
}