package sk.pack;

import sk.pack.db.BlogDBAdapter;
import sk.pack.util.AlertUtil;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LJ extends Activity {
	private static final String TAG = "MainActivity";
	private Button postButton, editButton, draftsButton;
	private BlogDBAdapter mDbHelper;
	private String login = " ", password = " ";
	private boolean isBlogConfigOk = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mDbHelper = new BlogDBAdapter(this);
		try {
			mDbHelper.open();
		} catch (Exception e) {
		}
		postButton = (Button) findViewById(R.id.main_bt_post);
		readDB();
		postButton.setEnabled(isBlogConfigOk);
		postButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LJ.this, BlogPostEditor.class);
				intent.setAction(Intent.ACTION_INSERT);
				LJ.this.startActivity(intent);
			}
		});

		editButton = (Button) findViewById(R.id.main_bt_ep);
		editButton.setText(R.string.profile_manager);
		editButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(LJ.this, ProfileManager.class);
				LJ.this.startActivity(i);

			}
		});
		draftsButton = (Button) findViewById(R.id.main_bt_drafts);
		draftsButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(LJ.this, ListDraftActivity.class);
				LJ.this.startActivity(i);
			}
		});
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, menu.FIRST, Menu.NONE, sk.pack.R.string.about);
		return result;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case Menu.FIRST:
			Intent i = new Intent(this, com.sadko.about.AboutActivity.class);
			LJ.this.startActivity(i);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void readDB() {
		try {
			isBlogConfigOk = false;
			Cursor bc = mDbHelper.fetchConfig(ProfileManager.CONFIG_ROW);
			startManagingCursor(bc);
			login = bc.getString(bc
					.getColumnIndexOrThrow(BlogDBAdapter.KEY_LOGIN));
			password = bc.getString(bc
					.getColumnIndexOrThrow(BlogDBAdapter.KEY_PASSWORD));
			int i = login.length();
			int j = password.length();
			if (!((i < 1) || (j < 1))) {
				isBlogConfigOk = true;
			}
		} catch (Exception e) {
		}
	}

}