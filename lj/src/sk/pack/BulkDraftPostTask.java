package sk.pack;

import sk.pack.db.BlogDBAdapter;
import sk.pack.db.BlogEntryBean;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

public class BulkDraftPostTask extends AsyncTask<String, String, String> {
	Context context;
	ProgressDialog progress;
	BlogInterface blogApi;
	Activity parent;
	String errorMessage;
	
	public interface ResultListener {
		public void notifyError(String message);
		public void notifySuccess();
	}

	ResultListener listener;
	public BulkDraftPostTask(Activity parent, Context context, ProgressDialog progress, BlogInterface blogApi, ResultListener listener)
	{
		this.parent = parent;
		this.context = context;
		this.progress = progress;
		this.blogApi = blogApi;
		this.listener = listener;
		errorMessage = null;
	}

	@Override
	protected String doInBackground(String... params) {
		BlogDBAdapter db = new BlogDBAdapter(context);
		db.open();
		try
		{
			BlogDBAdapter.Account account = db.fetchAccount();
			String auth_id = blogApi.getAuthId(account.login, account.password);
			if(auth_id == null)
			{
				errorMessage = "login fail";
				return null;
			}
			String postUrl = blogApi.getPostUrl(auth_id);
			Cursor cursor = db.fetchDrafts();
			if(cursor.moveToFirst())
			{
				do
				{
					BlogEntryBean draft = db.draftFromCurrent(cursor);
					
					boolean success = blogApi.createPost(parent, auth_id, postUrl,
							null, draft.getTitle(), null, draft.getBlogEntry().toString(), account.login, account.login, false);
					if(success)
					{
						publishProgress("post (" + draft.getTitle() + ")");
						db.deleteDraft(draft.getId());
					}
					else
					{
						errorMessage = "failed to post some drafts";
					}
				}
				while(cursor.moveToNext());
			}
			cursor.close();
		}
		finally
		{
			db.close();
		}
		return null;
	}
	
	@Override
	protected void onProgressUpdate(String... args) {
		progress.setMessage(args[0]);		
	}
	
	@Override
	protected void onPostExecute(String result) {
		progress.dismiss();
		if(errorMessage != null)
			listener.notifyError(errorMessage);
		else
			listener.notifySuccess();
	}

}
