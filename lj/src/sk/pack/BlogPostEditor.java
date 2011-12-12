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
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class BlogPostEditor extends Activity {
	EditText bSubject, bBody;
	private static BlogEntryBean b = null;
	private ProgressDialog publishProgress = null;
	private final String MSG_KEY = "value";
	private SpannableBufferHelper helper = null;
	int publishStatus = 0;
	private BlogDBAdapter mDbHelper;
	String login = " ", password = " ";


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editor);
		helper = new SpannableBufferHelper();
		
		bBody = (EditText) findViewById(sk.pack.R.id.edit_et_post);
		bSubject = (EditText) findViewById(sk.pack.R.id.edit_et_subject);
		b = new BlogEntryBean();
		mDbHelper = new BlogDBAdapter(this);
		try {
			mDbHelper.open();
		} catch (Exception e) {
		}
		readAccount();
		long draftId = getIntent().getLongExtra("DraftID", -1);
		if(draftId != -1)
			loadDraft(draftId);
		
		((Button)findViewById(R.id.edit_bt_bold)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onFormatClicked(R.id.edit_bt_bold);				
				
			}});
		
		((Button)findViewById(R.id.edit_bt_italic)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onFormatClicked(R.id.edit_bt_italic);				
			}});
		((Button)findViewById(R.id.edit_bt_normal)).setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				onFormatClicked(R.id.edit_bt_normal);				
			}});
	}
	void onFormatClicked(int id)
	{
		if(!bBody.hasFocus()) {
			AlertUtil.showAlert(this, "Not available",
							"Only the blog text can have styling information, sorry.");
			return;
		}
		
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
		
		int typeface = Typeface.NORMAL;
		switch(id)
		{
		case R.id.edit_bt_bold:
			typeface = Typeface.BOLD;
			break;
		case R.id.edit_bt_italic:
			typeface = Typeface.ITALIC;
			break;
		case R.id.edit_bt_normal:
			typeface = Typeface.NORMAL;
			break;
		}

		
		if (selectionStart == selectionEnd) {
			text.setSpan(new StyleSpan(typeface), selectionStart,
					selectionEnd, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			bBody.setText(helper.XHTMLToSpannable(helper
					.SpannableToXHTML(text)));
			bBody.setSelection(selectionEnd);

		}else {
			text.setSpan(new StyleSpan(typeface), selectionStart,
					selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			bBody.setText(helper.XHTMLToSpannable(helper
					.SpannableToXHTML(text)));
			bBody.setSelection(selectionEnd);
			
		}
	}
	
	void showMessage(String message) {
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.blog_post, menu);    	
    	return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch(id)
		{
		case R.id.item_drafts:
	    	Intent intent = new Intent(this, ListDraftActivity.class);
	    	startActivity(intent);
	    	return true;
		case R.id.item_new:
			clearEntry();
			return true;
		case R.id.item_post:
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
			return true;
		case R.id.item_save:
			b.setBlogEntry(helper.SpannableToXHTML(bBody.getText()));
			b.setTitle(bSubject.getText().toString());
			mDbHelper.saveDraft(b);
			finish();
			return true;
		}
		return false;
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

	void readAccount() {
		Account account = mDbHelper.fetchAccount();
		login = account.login;
		password = account.password;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("BLOG_SUBJECT", bSubject.getText().toString());
		outState.putString("BLOG_BODY", helper.SpannableToXHTML(bBody.getText()));
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		bSubject.setText(savedInstanceState.getString("BLOG_SUBJECT"));
		Spannable text = helper.XHTMLToSpannable(savedInstanceState.getString("BLOG_BODY"));
		bBody.setText(text);
		
	}

	void clearEntry() {
		bSubject.setText("");
		bBody.setText("");
		b = new BlogEntryBean();
	}
}
