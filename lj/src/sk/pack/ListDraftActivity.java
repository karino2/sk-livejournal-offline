package sk.pack;

import java.text.SimpleDateFormat;
import java.util.Date;

import sk.pack.db.BlogDBAdapter;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;

public class ListDraftActivity extends ListActivity {
	private BlogDBAdapter mDbHelper;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mDbHelper  = new BlogDBAdapter(this);
		mDbHelper.open();
		
		Cursor cursor = mDbHelper.fetchDrafts();
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
				Intent result = new Intent();
				result.putExtra("DraftID", id);
				setResult(RESULT_OK, result);
				finish();
			}});

		
	}
}
