package com.google.code.karino;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.code.karino.db.BlogDBAdapter;

import android.app.Activity;
import android.database.Cursor;
import android.util.Log;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ProfileManager extends Activity {
	public static final int CONFIG_ROW = 1;
	public static final int POST_ROW = 3;
	public static final int TEMP_CONFIG_ROW = 2;
	private EditText mLogin;
	private EditText mPassword;
	private final String TAG = "BlogConfigEditor";
	private Button button;
	private BlogDBAdapter mDbHelper;
	private String oldLogin;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editdialog);
		mDbHelper = new BlogDBAdapter(this);
		try {
			mDbHelper.open();
		} catch (Exception e) {
		}
		mLogin = (EditText) findViewById(R.id.ed_et_login);
		mPassword = (EditText) findViewById(R.id.ed_et_password);
		button = (Button) findViewById(R.id.ed_bt_confirm);
		TextView tv;
		tv = (TextView) findViewById(R.id.ed_tv_login);
		tv.setText(R.string.login);
		tv = (TextView) findViewById(R.id.ed_tv_password);
		tv.setText(R.string.password);
		button.setText(R.string.save);

		// populateFields();
		oldLogin = mLogin.getText().toString();
		button.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				saveState();
				setResult(RESULT_OK);
				finish();
			}
		});
		button = (Button) findViewById(R.id.ed_bt_fetch);
		button.setText(R.string.verify);
		final LiveJournalAPI lj = new LiveJournalAPI();
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				lj.getUserBlogs(ProfileManager.this);
			}
		});

	}

	private void populateFields() {

		try {
			Cursor bc = mDbHelper.fetchConfig(TEMP_CONFIG_ROW);
			startManagingCursor(bc);
			String str = bc.getString(bc
					.getColumnIndexOrThrow(BlogDBAdapter.KEY_LOGIN));
			mLogin.setText(str);
			if (oldLogin == null) {
				oldLogin = str;
			}
			mPassword.setText(bc.getString(bc
					.getColumnIndexOrThrow(BlogDBAdapter.KEY_PASSWORD)));
		} catch (Exception e) {
			oldLogin = "";
		}
	}

	protected void onResume() {
		super.onResume();
		populateFields();
	}

	protected void onPause() {
		super.onPause();
		String login = mLogin.getText().toString();
		String password = mPassword.getText().toString();
		if (!mDbHelper.updateConfig(TEMP_CONFIG_ROW, login, password)) {
			mDbHelper.createConfig(login, password);
		}

	}

	private void saveState() {
		String login = mLogin.getText().toString();
		if (!oldLogin.equals(login)) {
			if (!mDbHelper.updateConfig(POST_ROW, " ", " ")) {
				mDbHelper.createConfig(" ", " ");
			}
		}
		String password = mPassword.getText().toString();
		if (!mDbHelper.updateConfig(CONFIG_ROW, login, password)) {
			mDbHelper.createConfig(login, password);
		}
	}
}