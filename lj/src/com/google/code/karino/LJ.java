package com.google.code.karino;

import com.google.code.karino.db.BlogDBAdapter;

import com.google.code.karino.R;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LJ extends Activity {
	private Button postButton, editButton, draftsButton;
	private BlogDBAdapter mDbHelper;
	private String login = " ", password = " ";
	private boolean isBlogConfigOk = false;
	
	@Override
	protected void onResume() {
		super.onResume();
		loadAccountInfo();
		postButton.setEnabled(isBlogConfigOk);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		mDbHelper = new BlogDBAdapter(this);
		try {
			mDbHelper.open();
		} catch (Exception e) {
		}
		postButton = (Button) findViewById(R.id.main_bt_post);
		loadAccountInfo();
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
		menu.add(0, Menu.FIRST, Menu.NONE, com.google.code.karino.R.string.about);
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

	private void loadAccountInfo() {
		try {
			isBlogConfigOk = false;
			BlogDBAdapter.Account account = mDbHelper.fetchAccount();
			login = account.login;
			password = account.password;
			int i = login.length();
			int j = password.length();
			if (!((i < 1) || (j < 1))) {
				isBlogConfigOk = true;
			}
		} catch (Exception e) {
		}
	}

}