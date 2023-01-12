package pandas.api;

import pandas.delivery.DeliveryUrls;

public class Util {
	static boolean isHttrackCrawl(String gatherMethod) {
        return gatherMethod == null || gatherMethod.equalsIgnoreCase("HTTrack") || gatherMethod.equalsIgnoreCase("Upload");
    }

	static String buildLink(String gatherMethod, String tepUrl, String gatheredUrl) {
		if (isHttrackCrawl(gatherMethod)) {
			return DeliveryUrls.applyRewriteRules(tepUrl, isHttrackCrawl(gatherMethod));
		}

		if (tepUrl.startsWith("http://") || tepUrl.startsWith("https://")) {
			return tepUrl;
		}

		return gatheredUrl;
	}
}
