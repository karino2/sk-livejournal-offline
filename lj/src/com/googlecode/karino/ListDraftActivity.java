package com.googlecode.karino;

import java.text.SimpleDateFormat;
import java.util.Date;


import com.googlecode.karino.R;
import com.googlecode.karino.db.BlogDBAdapter;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class ListDraftActivity extends ListActivity {
	private BlogDBAdapter mDbHelper;
	private Cursor cursor;
	static final int POST_ALL_DIALOG_ID = 1;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper  = new BlogDBAdapter(this);
		mDbHelper.open();
		
		cursor = mDbHelper.fetchDraftSubjects();
		startManagingCursor(cursor);
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.list_draft_item,
				cursor, new String[] {"date", "subject"}, new int[] {R.id.tv_date, R.id.tv_subject});
		adapter.setViewBinder(new ViewBinder(){

			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
				if(columnIndex == 1)
				{
					TextView tv = (TextView)view;
					SimpleDateFormat  sdf = new SimpleDateFormat("yyyy/MM/dd");
					tv.setText(sdf.format(new Date(cursor.getLong(columnIndex))));
					return true;
				}
				return false;
			}});
		setListAdapter(adapter);
		
		getListView().setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				Intent intent = new Intent(ListDraftActivity.this, BlogPostEditor.class);
				intent.putExtra("DraftID", id);
				startActivity(intent);
			}});
		
		registerForContextMenu(getListView());
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(Menu.NONE, R.id.item_delete, Menu.NONE, R.string.delete);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.item_delete:
	        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	        mDbHelper.deleteDraft(info.id);
	        cursor.requery();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drafts_menu, menu);
		return super.onCreateOptionsMenu(menu);		
	}
	
    void showMessage(String message)
    {
		Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
		toast.show();    	
    }
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId())
		{
		case R.id.item_post_all:
			showDialog(POST_ALL_DIALOG_ID);
			postAll();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	protected android.app.Dialog onCreateDialog(int id)
	{
		switch(id)
		{
		case POST_ALL_DIALOG_ID:
			return postAll();
		}
		return null;
	}

	ProgressDialog postAll() {
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setTitle("Posting...");
		dialog.setCancelable(true);
		final BulkDraftPostTask task = new BulkDraftPostTask(this, this, dialog, BlogInterfaceFactory.getLiveJournalApi(),
				new BulkDraftPostTask.ResultListener() {
					
					public void notifySuccess() {
						cursor.requery();
						showMessage("post drafts done");
					}
					
					public void notifyError(String message) {
						cursor.requery();
						showMessage(message);
					}
				});
		dialog.setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				task.cancel(false);
				showMessage("post cancelled");
				cursor.requery();
			}
		});
		task.execute("");
		return dialog;
	}
}
