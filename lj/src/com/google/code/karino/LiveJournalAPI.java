package com.google.code.karino;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcLiteHttpTransportFactory;

import com.google.code.karino.util.AlertUtil;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import com.google.code.karino.R;

/**
 * LiveJournal works with a similar type of XML-RPC interface as does the
 * metaweblog protocol. Essentially, the createpost message is different. The
 * method is blogger.newPost rather than metaweblog.newpost.
 * 
 * @author juha
 * 
 */

public class LiveJournalAPI implements BlogInterface {

	private final String TAG = "LiveJournalAPI";
	private final String LJ_APPKEY = "0123456789ABCDEF";
	private final String MSG_KEY = "value";
	private final String STATUS_KEY = "status";
	private final String STATUS_RESPONSE_NULL = "1";
	private final String STATUS_ZERO_BLOGS = "2";
	private final String STATUS_OK = "3";
	private final String STATUS_BAD_LOGIN = "4";
	private final String RESPONSE_NAMES_KEY = "response_names";
	private final String RESPONSE_IDS_KEY = "response_ids";
	private Activity parentRef = null;
	private ProgressDialog fetchProgress = null;
	private String[] blogIds = null;
	private int fetchStatus = 0;
	private int currentySelectedBlog = -1;
	private String configBlogID = null;
	private Message statusMsg;

	// the handler for the UI callback from the publish thread
	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle content = msg.getData();
			String progressId = content.getString(MSG_KEY);
			if (progressId != null) {
				if (progressId.equals("1")) {
					fetchProgress.setMessage((CharSequence) parentRef
							.getString(R.string.prepare));
					fetchStatus = 1;
				} else if (progressId.equals("2")) {
					fetchProgress.setMessage((CharSequence) parentRef
							.getString(R.string.authenticating));
					fetchStatus = 2;
				} else if (progressId.equals("3")) {
					fetchProgress.setMessage((CharSequence) parentRef
							.getString(R.string.contacting_server));
					fetchStatus = 3;
				} else if (progressId.equals("4")) {
					fetchProgress.setMessage((CharSequence) parentRef
							.getString(R.string.extracting_response));
					fetchStatus = 4;
				} else if (progressId.equals("5")) {
					fetchProgress.setMessage((CharSequence) parentRef
							.getString(R.string.done));
					fetchStatus = 5;
				}
				String status = content.getString(STATUS_KEY);
				if (status != null && fetchStatus == 5) {
					if (status.equals(STATUS_RESPONSE_NULL)) {
						fetchStatus = 6;
					} else if (status.equals(STATUS_ZERO_BLOGS)) {
						fetchStatus = 7;
					} else if (status.equals(STATUS_OK)) {
						// keep fetch status as 5 to indicate success to
						// showFetchStatus method.
						fetchProgress.setMessage("Displaying blog names...");
						String names = content.getString(RESPONSE_NAMES_KEY);
						String ids = content.getString(RESPONSE_IDS_KEY);
						String[] namearr = names.split("\\|");
						String[] idsarr = ids.split("\\|");
						for (int i = 0; i < namearr.length; i++) {
						}
						if (namearr == null || namearr.length < 1) {
							fetchStatus = 8;
						} else if (idsarr == null || idsarr.length < 1) {
							fetchStatus = 9;
						} else {
							// extract the response from thread and feed to
							blogIds = idsarr;
						}
					} else if (status.equals(STATUS_BAD_LOGIN)) {
						fetchStatus = 10;
					}
				}
				// Continue from here by handling the responses from the
				// metafeed fetch thread.
			} else {
				// panic!
			}
		}
	};

	final Runnable mFetchResults = new Runnable() {
		public void run() {
			showFetchStatus();
		}
	};

	public void setInstanceConfig(CharSequence config) {
		// sanity checking whether this is an url...
		this.configBlogID = config.toString();
	}

	public String getAuthId(String username, String password) {
		// since livejournal api sends the credentials as part of the
		// createpost call, this just catenates the username and password with
		// || as field separator. This is then send to the createPost by caller
		// which again parses it.
		String res = username + "||" + password;
		return res;
	}

	void getUserBlogs(Activity a) {
		parentRef = a;
		if (parentRef == null) {
			throw new IllegalStateException("Cannot continue with null parent!");
		}
		String username = null;
		String password = null;

		EditText usernameView = (EditText) parentRef
				.findViewById(R.id.ed_et_login);
		if (usernameView.getText() == null
				|| usernameView.getText().length() < 1) {
			AlertUtil
					.showAlert(parentRef, "Username needed",
							"You need to give your LiveJournal username in order to continue!");
			return;
		} else {
			username = usernameView.getText().toString();
		}
		usernameView = null;
		EditText passwordView = (EditText) parentRef
				.findViewById(R.id.ed_et_password);
		if (passwordView.getText() == null
				|| passwordView.getText().length() < 1) {
			AlertUtil
					.showAlert(parentRef, "Password needed",
							"You need to give your LiveJournal       password in order to continue!");
			return;
		} else {
			password = passwordView.getText().toString();
		}

		final String fetchUsername = username;
		final String fetchPassword = password;
		fetchProgress = ProgressDialog.show(parentRef, "Fetching your blogs",
				"Starting to fetch blogs...");
		Thread fetchLJBlogs = new Thread() {
			public void run() {
				Bundle status = new Bundle();
				statusMsg = mHandler.obtainMessage();
				status.putString(MSG_KEY, "1");
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
				String localPostUrl = getPostUrl(null);
				statusMsg = mHandler.obtainMessage();
				status.putString(MSG_KEY, "2");
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				try {
					config.setServerURL(new URL(localPostUrl));
				} catch (MalformedURLException e) {
				}
				statusMsg = mHandler.obtainMessage();
				status.putString(MSG_KEY, "3");
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				XmlRpcClient client = new XmlRpcClient();
				Object[] params = new Object[] { LJ_APPKEY, fetchUsername,
						fetchPassword };
				client.setTransportFactory(new XmlRpcLiteHttpTransportFactory(
						client));
				client.setConfig(config);
				Object resobject = null;
				statusMsg = mHandler.obtainMessage();
				status.putString(MSG_KEY, "4");
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				try {
					resobject = client.execute("blogger.getUsersBlogs", params);
				} catch (XmlRpcException e) {
					statusMsg = mHandler.obtainMessage();
					status.putString(STATUS_KEY, STATUS_RESPONSE_NULL);
					status.putString(MSG_KEY, "5");
					statusMsg.setData(status);
					mHandler.sendMessage(statusMsg);
					mHandler.post(mFetchResults);
					return;
				} catch (ClassCastException ce) {
					// seems that we get null here if the credentials are wrong,
					// so let's assume that.
					if (resobject == null) {
						statusMsg = mHandler.obtainMessage();
						status.putString(STATUS_KEY, STATUS_BAD_LOGIN);
						status.putString(MSG_KEY, "5");
						statusMsg.setData(status);
						mHandler.sendMessage(statusMsg);
						mHandler.post(mFetchResults);
						return;
					}
				}
				statusMsg = mHandler.obtainMessage();
				status.putString(MSG_KEY, "5");
				statusMsg.setData(status);
				try {
					mHandler.sendMessage(statusMsg);
				} catch (Exception e) {
				}
				mHandler.post(mFetchResults);
			}
		}; // end Thread
		fetchLJBlogs.start();
		fetchProgress.setMessage("Started to fetch your blogs...");
	}

	public boolean createPost(Activity parent, String authToken,
			String postUrl, String titleType, String title, String contentType,
			String content, String authorName, String authorEmail,
			boolean isDraft) {
		String username = "unassigned";
		String password = "unassigned";
		if (authToken.matches("(.*)\\|\\|(.*)")) {
			try {
				String[] splt = authToken.split("\\|\\|");

				username = splt[0];
				password = splt[1];
			} catch (Exception e) {

			}
		}
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		// check the config var
		String localPostUrl = getPostUrl(authToken);
		try {
			config.setServerURL(new URL(localPostUrl));
		} catch (MalformedURLException e) {
		}
		// The blog id number, as String
		String blogIdNum = this.configBlogID;
		Boolean publish = new Boolean(true);
		if (isDraft) {
			publish = new Boolean(false);
		} else {
			publish = new Boolean(true);
		}
		// LJ just wants a string body with title on the first row.
		String postBody = "<title>" + title + "</title>\n" + content.toString();
		XmlRpcClient client = new XmlRpcClient();
		Object[] params = new Object[] { LJ_APPKEY, "" + blogIdNum, username,
				password, postBody, publish };
		client.setTransportFactory(new XmlRpcLiteHttpTransportFactory(client));
		client.setConfig(config);
		String postID = null;
		try {
			postID = (String) client.execute("blogger.newPost", params);
		} catch (XmlRpcException e) {
		}
		if ((postID == null) || postID.equals("")) {
			return false;
		} else if (postID.matches("" + this.configBlogID + "\\:(\\d+)")) {
			return true;
		} else {
			return true;
		}
	}

	private void showFetchStatus() {
		fetchProgress.dismiss();
		if (fetchStatus == 5) {
			AlertUtil.showAlert(parentRef,
					parentRef.getString(R.string.status), parentRef
							.getString(R.string.verify_ok));
		} else if (fetchStatus == 6) {
			AlertUtil.showAlert(parentRef,
					parentRef.getString(R.string.status), parentRef
							.getString(R.string.null_answer));
		} else if (fetchStatus == 7) {
			AlertUtil.showAlert(parentRef,
					parentRef.getString(R.string.status),
					"Can't find any blogs for this user.");
		} else if (fetchStatus == 8) {
			AlertUtil.showAlert(parentRef,
					parentRef.getString(R.string.status),
					"Can't extract blog names from respose.");
		} else if (fetchStatus == 9) {
			AlertUtil.showAlert(parentRef,
					parentRef.getString(R.string.status),
					"Can't extract blog ids from response.");
		} else if (fetchStatus == 10) {
			AlertUtil.showAlert(parentRef,
					parentRef.getString(R.string.status), (String) parentRef
							.getString(R.string.code_4));
		} else {
			AlertUtil.showAlert(parentRef,
					parentRef.getString(R.string.status),
					"Fetch of blogs failed! (Code " + fetchStatus + ")");
		}
	}

	public String getPostUrl(String authToken) {
		return "http://www.livejournal.com/interface/blogger/";
	}

	public CharSequence getConfigEditorData() {
		// here, we return the id of the selected blog config
		return blogIds[currentySelectedBlog];
	}

}