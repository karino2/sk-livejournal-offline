package sk.pack;

import android.util.Log;
import sk.pack.BlogConfigConstants.BlogInterfaceType;

/**
 * Serves singleton instances of blog interfaces to callers. Again, may be an
 * overgeneralization for a mobile app but originally I would like to keep the
 * coupling as loose as possible.
 * 
 * @author juhak
 * 
 */

public class BlogInterfaceFactory {

	private static final String TAG = "BlogInterfaceFactory";

	static BlogInterface instance;

	public static BlogInterface getInstance(BlogInterfaceType type) {
		if (type == BlogConfigConstants.BlogInterfaceType.LIVEJOURNAL) {
			if (instance == null || !(instance instanceof LiveJournalAPI)) {
				instance = new LiveJournalAPI();
			}
			return instance;
		} else {
			return null;
		}
	}
	
	// I think this is enough (by karino).
	public static BlogInterface getLiveJournalApi() {
		BlogConfigConstants.BlogInterfaceType typeEnum = BlogConfigConstants
				.getInterfaceTypeByNumber(6);
		BlogInterface blogapi = null;
		blogapi = BlogInterfaceFactory.getInstance(typeEnum);
		CharSequence config = "";
		blogapi.setInstanceConfig(config);
		return blogapi;
	}


}