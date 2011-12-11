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
	private Button postButton, editButton, exitButton;
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
		postButton.setText(R.string.createpost);
		postButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				readDB();
				if (isBlogConfigOk) {
					Intent intent = new Intent(LJ.this, BlogPostEditor.class);
					intent.setAction(Intent.ACTION_INSERT);
					LJ.this.startActivity(intent);
				} else {
					AlertUtil.showAlert(LJ.this,
							getString(R.string.blog_not_configured),
							getString(R.string.conf_first));
				}
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
		exitButton = (Button) findViewById(R.id.main_bt_exit);
		exitButton.setText(R.string.exit);
		exitButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
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