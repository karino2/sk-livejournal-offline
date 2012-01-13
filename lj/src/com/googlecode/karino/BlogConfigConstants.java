package com.googlecode.karino;

import java.util.LinkedList;

/**
 * This class offers enumerations to be used to indicate which type of interface
 * class is used in posting entries to given type of blog.
 * 
 * This can be an overgeneralization (to hide the type of the blog) and lead to
 * some stupidity in the BlogInteface. (If the operations of the different
 * publishing systems are not really semantically compliant with each other.)
 * 
 * We need to be ready to change the approach if the current one leads to a lot
 * of glue code.
 * 
 * @author juha
 * 
 */

public class BlogConfigConstants {
	public enum BlogInterfaceType {
		BLOGGER, ATOM, RSS, BLOGGER_DEBUG, METAWEBLOG, LIVEJOURNAL, UNKNOWN
	}

	/**
	 * Use this in the user agent or ID string to communicate with the Blog
	 * servers.
	 */
	private final static int UNKNOWN_CONFIG_TYPE = 0xFFFF;
	public final static String APPNAME = "joker.iki.fi-Mobilogger-1";
	public final static char FIELD_DELIMITER = '|';

	public static String typeConstantTitle(BlogInterfaceType type) {
		String res = null;
		switch (type) {
		case BLOGGER:
			res = "Blogger / API";
			break;
		case ATOM:
			res = "ATOM 1.0 compliant";
			break;
		case RSS:
			res = "RSS 2.0 compliant";
			break;
		case BLOGGER_DEBUG:
			res = "Blogger / HTTPS";
			break;
		case METAWEBLOG:
			res = "MetaWeblog compliant";
			break;
		case LIVEJOURNAL:
			res = "LiveJournal API";
			break;
		default:
			res = "Incompatible";
			break;
		}
		return res;
	}

	/**
	 * Returns all possible blog interface types' titles in an LinkedList of
	 * Strings.
	 * 
	 * @return
	 */

	public static LinkedList<String> typeConstantTitles() {
		BlogInterfaceType[] arr = BlogInterfaceType.values();
		LinkedList res = new LinkedList();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] != BlogInterfaceType.UNKNOWN) {
				res.add(typeConstantTitle(arr[i]));
			}
		}
		return res;
	}

	public static String typeConstantDesc(BlogInterfaceType type) {
		String base = "You need to check your blog service provider for "
				+ "information which API they support. Blogger API is "
				+ "supported by BlogSpot and many other online blogging "
				+ "services.";
		switch (type) {
		case BLOGGER:
			return "Blog uses Blogger API (Google Data API). " + base;
			// break;
		case ATOM:
			return "Blog uses ATOM compliant publishing API. " + base;
			// break;
		case RSS:
			return "Blog uses RSS compliant protocol. " + base;
			// break;
		case METAWEBLOG:
			return "Blog uses MetaWeblog API. " + base;
			// break;
		case LIVEJOURNAL:
			return "Blog is a LiveJournal subscription blog." + base;
		default:
			return "Unknown protocol. " + base;
		}
	}

	/**
	 * The integer returned by this method always maps to the type enum
	 * 
	 * @param type
	 * @return
	 */

	public static int getInterfaceNumberByType(BlogInterfaceType type) {
		switch (type) {
		case BLOGGER:
			return 1;
			// break;
		case ATOM:
			return 2;
			// break;
		case RSS:
			return 3;
			// break;
		case BLOGGER_DEBUG:
			return 4;
		case METAWEBLOG:
			return 5;
		case LIVEJOURNAL:
			return 6;
		default:
			return UNKNOWN_CONFIG_TYPE;
		}
	}

	public static BlogInterfaceType getInterfaceTypeByNumber(int configNum) {
		switch (configNum) {
		case 1:
			return BlogInterfaceType.BLOGGER;
		case 2:
			return BlogInterfaceType.ATOM;
		case 3:
			return BlogInterfaceType.RSS;
		case 4:
			return BlogInterfaceType.BLOGGER_DEBUG;
		case 5:
			return BlogInterfaceType.METAWEBLOG;
		case 6:
			return BlogInterfaceType.LIVEJOURNAL;
		case UNKNOWN_CONFIG_TYPE:
		default:
			return BlogInterfaceType.LIVEJOURNAL;
		}
	}

}