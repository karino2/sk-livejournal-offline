package com.google.code.karino;

import android.app.Activity;
import android.content.Context;
import android.widget.TableLayout;

/**
 * All of the different fi.iki.joker.mobilogger.blogs.* classes which are
 * responsible of communicating with the blogging site should implement this.
 * Abstracts away the details of the underlying blog implementation and
 * communication method to the caller of the methods.
 * 
 * It's not really know (to me at least) whether we can hide the different APIs
 * behind one interface like this. If the operations of different APIs are too
 * far away from each other semantically (for example, google api authenticates
 * first and then you use auth token to post entries and in cortrast MetaWeblog
 * authenticates each individual request), this may lead to a lot of confusing
 * glue just to keep the interface happy. If this becomes too much of a problem,
 * then the approach needs to change. (And the change will affect all the blog
 * interface implementations, of course.)
 * 
 * @author juha
 * 
 */

public interface BlogInterface {

	/**
	 * This method is used to get the authentication/session id which can be
	 * used to post entries to blogs and perform other operations which require
	 * authentication.
	 * 
	 * @param username
	 * @param password
	 * @return
	 */

	public String getAuthId(String username, String password);

	/**
	 * Used to create new posting to selected blog.
	 * 
	 * @param parent
	 *            Reference to calling activity
	 * @param authToken
	 * @param postUrl
	 * @param titleType
	 * @param title
	 * @param contentType
	 * @param content
	 * @param authorName
	 * @param authorEmail
	 * @param isDraft
	 * @return
	 */

	public boolean createPost(Activity parent, String authToken,
			String postUrl, String titleType, String title, String contentType,
			String content, String authorName, String authorEmail,
			boolean isDraft);

	/**
	 * Returns the url that can be used to manage blog entries
	 * 
	 * @param authToken
	 * @return
	 */

	public String getPostUrl(String authToken);

	/**
	 * This method is used by the config editor to read the config. You are
	 * expected to provide the data in a CharSequence where that makes sense the
	 * the blog posting method. TODO: that method does not exist yet, create it
	 * and substitute the createpost, update post etc to use the config.
	 */

	public CharSequence getConfigEditorData();

	/**
	 * You can use this method to repopulate the API with the saved config
	 * editor data. Typically it's used for blog interfaces which first need
	 * some instance-specific configuration which is to be saved, and then
	 * reloaded at the time of posting.
	 * 
	 * CONTRACT: The getConfigEditorData should return the CharSequence that is
	 * understood by the setInstaceConfig. API is required to save the config to
	 * it's state as long as it's alive.
	 * 
	 */

	public void setInstanceConfig(CharSequence config);

}