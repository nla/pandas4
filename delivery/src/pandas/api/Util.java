package pandas.api;

import java.util.regex.Pattern;

public class Util {

	// eg /parchive/2000/olympics/O2000-Oct-01/www.olympics.com/eng/index.html
	// eg /parchive/2000/paralympic.org/O2000-Oct-20/www.paralympic.org.au/team2000/index.html
	private static Pattern OLYMPICS_SPECIAL_CASE = Pattern.compile("^/parchive/2000/(?:olympics|paralympic.org)/");
	private static Pattern PARCHIVE_REGEX = Pattern.compile("^/parchive/(.*)$");
	private static Pattern NPH_REGEX = Pattern.compile(".*/nph-arch(/\\d{4})?/[a-zA-Z]\\d{4}-[a-zA-Z]{3}-\\d{1,2}/(.*)$");

	
    public static String applyRewriteRules(String url, boolean prependPandoraDomain) {
    	if (url == null || url.isBlank()) {
    		return null;
    	}
    	
    	if (url.contains("parchive")) {
    		url = OLYMPICS_SPECIAL_CASE.matcher(url).replaceFirst("/parchive/");
    		url = PARCHIVE_REGEX.matcher(url).replaceFirst("/nph-arch/$1");
    	}

    	if (url.contains("nph-arch")) {
    		url = NPH_REGEX.matcher(url).replaceFirst("$2");
    		prependPandoraDomain = false;
    	}
    	
    	if (prependPandoraDomain) {
    		url = "http://pandora.nla.gov.au" + url;
    	}
    	
    	return url;
    }

    static boolean isHttrackCrawl(String gatherMethod) {
        return gatherMethod == null || gatherMethod.equalsIgnoreCase("HTTrack") || gatherMethod.equalsIgnoreCase("Upload");
    }

	static String buildLink(String gatherMethod, String tepUrl, String gatheredUrl) {
		if (isHttrackCrawl(gatherMethod)) {
			return applyRewriteRules(tepUrl, isHttrackCrawl(gatherMethod));
		}

		if (tepUrl.startsWith("http://") || tepUrl.startsWith("https://")) {
			return tepUrl;
		}

		return gatheredUrl;
	}
}
