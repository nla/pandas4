package pandas.delivery;

import pandas.util.DateFormats;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeliveryUrls {
    // eg /parchive/2000/olympics/O2000-Oct-01/www.olympics.com/eng/index.html
    // eg /parchive/2000/paralympic.org/O2000-Oct-20/www.paralympic.org.au/team2000/index.html
    private static Pattern OLYMPICS_SPECIAL_CASE = Pattern.compile("^/parchive/2000/(?:olympics|paralympic.org)/");
    private static Pattern PARCHIVE_REGEX = Pattern.compile("^/parchive/(.*)$");
    private static Pattern NPH_REGEX = Pattern.compile(".*/nph-arch(/\\d{4})?/[a-zA-Z]\\d{4}-[a-zA-Z]{3}-\\d{1,2}/(.*)$");

	private static final Pattern REPLAY_URL_PATTERN = Pattern.compile("https://[^/]+/awa/([0-9]{14})/.*");
	private static final Pattern PANDORA_URL_PATTERN = Pattern.compile(".*/pandora\\.nla\\.gov\\.au/pan/[0-9]+/([0-9]{8}(?:-[0-9]{4})?)/.*");
	private static final DateTimeFormatter PAN_DATE = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm", Locale.US).withZone(ZoneId.of("Australia/Sydney"));

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

	public static Instant extractDateFromUrl(String url) {
		try {
			Matcher m;
			if ((m = REPLAY_URL_PATTERN.matcher(url)).matches()) {
				return DateFormats.ARC_DATE.parse(m.group(1), Instant::from);
			} else if ((m = PANDORA_URL_PATTERN.matcher(url)).matches()) {
				String string = m.group(1);
				if (!string.contains("-")) {
					string += "-0000";
				}
				return PAN_DATE.parse(string, Instant::from);
			}
			return null;
		} catch (DateTimeParseException e) {
			return null;
		}
	}
}
