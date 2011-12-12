package sk.pack;

import sk.pack.db.BlogDBAdapter;
import sk.pack.db.BlogEntryBean;
import sk.pack.db.SpannableBufferHelper;
import sk.pack.db.BlogDBAdapter.Account;
import sk.pack.util.AlertUtil;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//changes in readDB

public class BlogPostEditor extends Activity {
	// private boolean flag=false;
	private static final String TAG = "BlogPostEditor";
	EditText bSubject, bBody;
	private static Button button;
	private static BlogEntryBean b = null;
	private ProgressDialog publishProgress = null;
	private final String MSG_KEY = "value";
	private SpannableBufferHelper helper = null;
	private static final int GROUP_BASIC = 0;
	private static final int GROUP_EMBED = 2;
	int publishStatus = 0;
	private BlogDBAdapter mDbHelper;
	String login = " ", password = " ";

	protected Dialog onCreateDialog(int id) {
		return new AlertDialog.Builder(BlogPostEditor.this).setIcon(
				R.drawable.alert_dialog_icon).setTitle(
				R.string.alert_dialog_two_buttons_title).setPositiveButton(
				R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						clearEntry();
						/* User clicked OK so do some stuff */
					}
				}).setNegativeButton(R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						/* User clicked Cancel so do some stuff */
					}
				}).create();
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		helper = new SpannableBufferHelper();
		
		button = (Button) findViewById(sk.pack.R.id.edit_bt_post);
		bBody = (EditText) findViewById(sk.pack.R.id.edit_et_post);
		bSubject = (EditText) findViewById(sk.pack.R.id.edit_et_subject);
		b = new BlogEntryBean();
		mDbHelper = new BlogDBAdapter(this);
		try {
			mDbHelper.open();
		} catch (Exception e) {
		}
		long draftId = getIntent().getLongExtra("DraftID", -1);
		if(draftId == -1)
			readDB();
		else
		{
			readAccount();
			loadDraft(draftId);
		}
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				b.setTitle(bSubject.getText().toString());
				CharSequence cs = (CharSequence) bBody.getText().toString();
				b.setBlogEntry(cs);
				if (!cs.equals("")) {
					post();
				} else {
					AlertUtil.showAlert(BlogPostEditor.this,
							getString(R.string.unable_to_publish),
							getString(R.string.fill_the_post_field));
				}
			}
		});
		button = (Button) findViewById(sk.pack.R.id.edit_bt_clear);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				/**/
				showDialog(0);
			}
		});
		((Button)findViewById(sk.pack.R.id.edit_bt_save)).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				b.setBlogEntry(helper.SpannableToXHTML(bBody.getText()));
				b.setTitle(bSubject.getText().toString());
				mDbHelper.saveDraft(b);
				finish();
			}});
	}
	
	void showMessage(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		// Create first level
		menu.add(1, 3, Menu.NONE, "bold");
		menu.add(GROUP_BASIC, 4, Menu.NONE, "italic");
		menu.add(GROUP_BASIC, 5, Menu.NONE, "normal");
		menu.add(Menu.NONE, R.id.item_drafts,Menu.NONE, R.string.drafts);
		/*
		 * SubMenu embed = menu.addSubMenu(GROUP_EMBED, 6, Menu.NONE, "embed");
		 * embed.add(GROUP_EMBED, 7, Menu.NONE, "link"); embed.add(GROUP_EMBED,
		 * 8, Menu.NONE, "html");
		 */
		// Create submenu based on the available blogs configured
		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if(id == R.id.item_drafts)
		{
	    	Intent intent = new Intent(this, ListDraftActivity.class);
	    	startActivity(intent);
	    	/*
	    	intent.setAction(Intent.ACTION_PICK);
	    	startActivityForResult(intent, REQUEST_PICK_DRAFTS);			
	    	*/
	    	// finish();
			return true;
		}
		int group = item.getGroupId();
		helper.debugWriteBuffer("Buffer before changes:", bBody.getText());
		Spannable text = bBody.getText();
		int selectionStart = Selection.getSelectionStart(text);
		int selectionEnd = Selection.getSelectionEnd(text);
		// if user has selected from end to beginning, flip
		// values, we are interested about the selection, not
		// direction
		if (selectionStart > selectionEnd) {
			int temp = selectionEnd;
			selectionEnd = selectionStart;
			selectionStart = temp;
		}
		if (group == GROUP_BASIC) {
			if ((id != Menu.FIRST) && !bBody.hasFocus()) {
				AlertUtil
						.showAlert(this, "Not available",
								"Only the blog text can have styling information, sorry.");
				return super.onOptionsItemSelected(item);
			}
			if (selectionStart == selectionEnd) {
				switch (id) {
				case 3:
					// Log.d(TAG, "case3eq");
					text.setSpan(new StyleSpan(Typeface.BOLD), selectionStart,
							selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					bBody.setText(helper.XHTMLToSpannable(helper
							.SpannableToXHTML(text)));
					bBody.setSelection(selectionEnd);
					break;
				case 4:
					// Log.d(TAG, "case4eq");
					text.setSpan(new StyleSpan(Typeface.ITALIC),
							selectionStart, selectionEnd,
							Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					bBody.setText(helper.XHTMLToSpannable(helper
							.SpannableToXHTML(text)));
					bBody.setSelection(selectionEnd);
					break;
				case 5:
					// Log.d(TAG, "case5eq");
					text.setSpan(new StyleSpan(Typeface.NORMAL),
							selectionStart, selectionEnd,
							Spannable.SPAN_INCLUSIVE_INCLUSIVE);
					bBody.setText(helper.XHTMLToSpannable(helper
							.SpannableToXHTML(text)));
					bBody.setSelection(selectionEnd);

				}
			} else {
				switch (id) {
				case 3:
					// Log.d(TAG, "case3!");
					text.setSpan(new StyleSpan(Typeface.BOLD), selectionStart,
							selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					bBody.setText(helper.XHTMLToSpannable(helper
							.SpannableToXHTML(text)));
					bBody.setSelection(selectionEnd);
					break;
				case 4:
					// Log.d(TAG, "case3!");
					text.setSpan(new StyleSpan(Typeface.ITALIC),
							selectionStart, selectionEnd,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					bBody.setText(helper.XHTMLToSpannable(helper
							.SpannableToXHTML(text)));
					bBody.setSelection(selectionEnd);
					break;
				case 5:
					// Log.d(TAG, "case3!");
					text.setSpan(new StyleSpan(Typeface.NORMAL),
							selectionStart, selectionEnd,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					// helper.removeSpans()
					bBody.setText(helper.XHTMLToSpannable(helper
							.SpannableToXHTML(text)));
					bBody.setSelection(selectionEnd);
					break;
				}
			}
		} else if (group == GROUP_EMBED) {
			if ((id > 6 && id < 12) && !bBody.hasFocus()) {
				AlertUtil.showAlert(this, "Not available",
						"Only the blog text can have embedded objects, sorry.");
				return super.onOptionsItemSelected(item);
			}
			if (id == 7) { // LINK
				/*
				 * currentUrlSpan = new URLSpan(""); currentSpanStart =
				 * selectionStart; currentSpanEnd = selectionEnd;
				 * getTextInputFromDialog();
				 */
			} else if (id == 8) { // HTML
				if (selectionStart == selectionEnd) {
					/*
					 * currentSpanStart = selectionStart; currentSpanEnd =
					 * selectionStart; getHtmlInputFromDialog();
					 */
				} else {
					AlertUtil
							.showAlert(this, "Invalid selection",
									"When embedding HTML markup, don't select a range.");
					return super.onOptionsItemSelected(item);
				}
			} else if (id == 10) { // Image
				/*
				 * if (selectionStart == selectionEnd) { currentSpanStart =
				 * selectionStart; currentSpanEnd = selectionStart;
				 * getImageSelectionFromActivity(); } else {
				 * AlertUtil.showAlert(this, "Invalid selection",
				 * "When embedding an image, don't select a range."); return
				 * super.onOptionsItemSelected(item); }
				 */
			}

		}

		helper.debugWriteBuffer("Buffer after changes:", bBody.getText());
		// Log.d(TAG, "As XHTML:" + helper.SpannableToXHTML(bBody.getText()));
		return super.onOptionsItemSelected(item);
	}
	
	private void loadDraft(long id) {
		b = mDbHelper.fetchDraft(id);
		bSubject.setText(b.getTitle());
		Spannable text = helper.XHTMLToSpannable(b.getBlogEntry());
		bBody.setText(text);
	}

	final Runnable mPublishResults = new Runnable() {
		public void run() {
			showPublishedStatus();
		}
	};

	private void showPublishedStatus() {
		publishProgress.dismiss();
		if (publishStatus == 5) {
			AlertUtil.showAlert(this, getString(R.string.publish_status),
					getString(R.string.publish_ok));
			if(b.getId() != -1)
			{
				// also saved in draft. delete it.
				mDbHelper.deleteDraft(b.getId());
			}
			clearEntry();
			// activity should finish after ok.
		} else {
			String str = "";
			if (publishStatus == 4)
				str = '\n' + getString(R.string.code_4);
			AlertUtil.showAlert(this, getString(R.string.publish_status),
					getString(R.string.publish_failed) + " (Code "
							+ publishStatus + ")" + str);
		}
	}

	private void post() {
		final Activity thread_parent = this;
		publishProgress = ProgressDialog.show(this,
				getString(R.string.publishing_blog_entry),
				getString(R.string.starting_to_publish_blog_entry));
		Thread publish = new Thread() {
			public void run() {
				Bundle status = new Bundle();
				mHandler.getLooper().prepare();
				Message statusMsg = mHandler.obtainMessage();
				publishStatus = 0;
				status.putString(MSG_KEY, "1");
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				boolean publishOk = false;
				BlogInterface blogapi = BlogInterfaceFactory.getLiveJournalApi();

				status.putString(MSG_KEY, "2");
				statusMsg = mHandler.obtainMessage();
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				String auth_id = blogapi.getAuthId(login, password);
				publishStatus = 2;
				if (auth_id != null) {
					status.putString(MSG_KEY, "3");
					statusMsg = mHandler.obtainMessage();
					statusMsg.setData(status);
					mHandler.sendMessage(statusMsg);
					String postUri = blogapi.getPostUrl(auth_id);
					SpannableBufferHelper helper = new SpannableBufferHelper();
					status.putString(MSG_KEY, "4");
					statusMsg = mHandler.obtainMessage();
					statusMsg.setData(status);
					mHandler.sendMessage(statusMsg);
					Spannable text = bBody.getText();
					String str = helper.SpannableToXHTML(text);
					publishOk = blogapi.createPost(thread_parent, auth_id,
							postUri, null, b.getTitle(), null, str, login,
							login, b.isDraft());
					/*
					 * status.putString(MSG_KEY, "3"); statusMsg =
					 * mHandler.obtainMessage(); statusMsg.setData(status);
					 * mHandler.sendMessage(statusMsg); String postUri =
					 * blogapi.getPostUrl(auth_id); SpannableBufferHelper helper
					 * = new SpannableBufferHelper(); status.putString(MSG_KEY,
					 * "4"); statusMsg = mHandler.obtainMessage();
					 * statusMsg.setData(status);
					 * mHandler.sendMessage(statusMsg); CharSequence cs =
					 * b.getBlogEntry(); EditText et = new
					 * EditText(thread_parent); et.setText(cs); Spannable spa =
					 * et.getText(); spa.setSpan(cs, 0, 1, 1);//011 String
					 * spannableToXHTML = null; try { spannableToXHTML =
					 * helper.SpannableToXHTML(spa); } catch (Exception e) { }
					 * 
					 * publishOk = blogapi.createPost(thread_parent, auth_id,
					 * postUri, null, b.getTitle(), null, spannableToXHTML,
					 * login, login, b.isDraft());
					 */
				} else {
					publishStatus = 3;
				}
				status.putString(MSG_KEY, "5");
				statusMsg = mHandler.obtainMessage();
				statusMsg.setData(status);
				mHandler.sendMessage(statusMsg);
				if (publishOk) {
					publishStatus = 5;
				} else {
					publishStatus = 4;
				}
				mHandler.post(mPublishResults);
			}
		};
		publish.start();
		publishProgress
				.setMessage((CharSequence) getString(R.string.started_publishing));
	}

	final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Bundle content = msg.getData();
			String progressId = content.getString(MSG_KEY);
			if (progressId != null) {
				if (progressId.equals("1")) {
					publishProgress
							.setMessage((CharSequence) getString(R.string.preparing_blog_config));
				} else if (progressId.equals("2")) {
					publishProgress
							.setMessage((CharSequence) getString(R.string.authenticating));
				} else if (progressId.equals("3")) {
					publishProgress
							.setMessage((CharSequence) getString(R.string.contacting_server));
				} else if (progressId.equals("4")) {
					publishProgress
							.setMessage((CharSequence) getString(R.string.creating_new_entry));
				} else if (progressId.equals("5")) {
					publishProgress
							.setMessage((CharSequence) getString(R.string.done));
				}
			} else {
				// panic!
			}
		}
	};

	public void readDB() {

		try {
			readAccount();
		} catch (Exception e) {
		}
		try {
			Cursor bc = mDbHelper.fetchConfig(ProfileManager.POST_ROW);
			startManagingCursor(bc);
			int i = bc.getColumnIndexOrThrow(BlogDBAdapter.KEY_LOGIN);
			String str = bc.getString(i);
			if (!str.equals(" "))
				bSubject.setText(str);
			str = bc.getString(bc
					.getColumnIndexOrThrow(BlogDBAdapter.KEY_PASSWORD));
			if (!str.equals(" ")) {
				Spannable text = helper.XHTMLToSpannable(str);
				bBody.setText(text);
			}
		} catch (Exception e) {
		}
	}

	void readAccount() {
		Account account = mDbHelper.fetchAccount();
		login = account.login;
		password = account.password;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!mDbHelper.updateConfig(ProfileManager.POST_ROW, bSubject.getText()
				.toString(), helper.SpannableToXHTML(bBody.getText()))) {
			mDbHelper.createConfig(bSubject.getText().toString(), bBody
					.getText().toString());
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		// if not, onResume is called after returning from ListDraftActivity, etc.
		if(b.getId() == -1)
			readDB();
	}

	void clearEntry() {
		bSubject.setText("");
		bBody.setText("");
		b = new BlogEntryBean();
	}
}
