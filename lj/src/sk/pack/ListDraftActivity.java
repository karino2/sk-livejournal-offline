package sk.pack;

import java.text.SimpleDateFormat;
import java.util.Date;

import sk.pack.db.BlogDBAdapter;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;

public class ListDraftActivity extends ListActivity {
	private BlogDBAdapter mDbHelper;
	private Cursor cursor;
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

			@Override
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

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long id) {
				Intent intent = new Intent(ListDraftActivity.this, BlogPostEditor.class);
				intent.putExtra("DraftID", id);
				startActivity(intent);
			}});

		
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
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setCancelable(true);
			final BulkDraftPostTask task = new BulkDraftPostTask(this, this, dialog, BlogInterfaceFactory.getLiveJournalApi(),
					new BulkDraftPostTask.ResultListener() {
						
						@Override
						public void notifySuccess() {
							cursor.requery();
							showMessage("post drafts done");
						}
						
						@Override
						public void notifyError(String message) {
							cursor.requery();
							showMessage(message);
						}
					});
			dialog.setOnCancelListener(new OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					task.cancel(false);
					showMessage("post cancelled");
					cursor.requery();
				}
			});
			task.execute("");
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
