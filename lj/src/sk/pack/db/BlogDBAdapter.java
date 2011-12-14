package sk.pack.db;

import java.util.Date;

import sk.pack.ProfileManager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlogDBAdapter {
	public static final String KEY_LOGIN = "login";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_ROWID = "_id";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private static final String DATABASE_CREATE = "create table blogconfig (_id integer primary key autoincrement, "
			+ "login text not null, password text not null);";
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "blogconfig";
	private static final String DRAFTS_TABLE = "drafts";
	private static final int DATABASE_VERSION = 2;
	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL(DATABASE_CREATE);
			db.execSQL("create table drafts "
			+"(_id integer primary key autoincrement"
			+ ", subject text"
			+ ", body text"
			+ ", date integer"
			+");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS blogconfig");
			db.execSQL("DROP TABLE IF EXISTS " + DRAFTS_TABLE);
			onCreate(db);
		}
	}

	public BlogDBAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public BlogDBAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbHelper.close();
	}

	public long createConfig(String login, String password) {
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_LOGIN, login);
		initialValues.put(KEY_PASSWORD, password);
		return mDb.insert(DATABASE_TABLE, null, initialValues);
	}

	public boolean deleteConfig(long rowId) {

		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}

	/**
	 * Return a Cursor over the list of all notes in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllConfigs() {

		return mDb.query(DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LOGIN,
				KEY_PASSWORD }, null, null, null, null, null);
	}

	/**
	 * Return a Cursor positioned at the note that matches the given rowId
	 * 
	 * @param rowId
	 *            id of note to retrieve
	 * @return Cursor positioned to matching note, if found
	 * @throws SQLException
	 *             if note could not be found/retrieved
	 */
	public Cursor fetchConfig(long rowId) throws SQLException {

		Cursor mCursor =

		mDb.query(true, DATABASE_TABLE, new String[] { KEY_ROWID, KEY_LOGIN,
				KEY_PASSWORD }, KEY_ROWID + "=" + rowId, null, null, null,
				null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
	
	public class Account
	{
		public String login;
		public String password;
	}
	
	public Account fetchAccount()
	{
		Account ret = new Account();
		Cursor cursor = fetchConfig(ProfileManager.CONFIG_ROW);
		int iq = cursor.getColumnIndexOrThrow(BlogDBAdapter.KEY_LOGIN);
		ret.login = cursor.getString(iq);
		ret.password = cursor.getString(cursor
				.getColumnIndexOrThrow(BlogDBAdapter.KEY_PASSWORD));
		return ret;
	}

	public boolean updateConfig(long rowId, String login, String password) {
		ContentValues args = new ContentValues();
		args.put(KEY_LOGIN, login);
		args.put(KEY_PASSWORD, password);

		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
	}

	public Cursor fetchDraftSubjects() {
		return mDb.query(DRAFTS_TABLE, new String[]{"_id", "date", "subject" },
				null, null, null, null, "date desc");
	}
	
	public Cursor fetchDrafts() {
		return mDb.query(DRAFTS_TABLE, new String[]{"_id", "date", "subject", "body" },
				null, null, null, null, "date asc");
	}
	
	public BlogEntryBean fetchDraft(long id)
	{
		Cursor cursor = mDb.query(DRAFTS_TABLE, new String[]{"_id", "date", "subject", "body" },
				"_id = ?", new String[]{ String.valueOf(id) }, null, null, null);
		cursor.moveToFirst();
		BlogEntryBean ent = draftFromCurrent(cursor);
		cursor.close();
		return ent;
	}

	public BlogEntryBean draftFromCurrent(Cursor cursor) {
		BlogEntryBean ent = new BlogEntryBean();
		ent.setDraft(true);
		ent.setCreated(new Date(cursor.getLong(1)));
		ent.setTitle(cursor.getString(2));
		ent.setBlogEntry(cursor.getString(3));
		ent.setId(cursor.getLong(0));
		return ent;
	}

	public void saveDraft(BlogEntryBean b) {
		ContentValues values = new ContentValues();
		values.put("subject", b.getTitle());
		values.put("body", b.getBlogEntry().toString());
		if(b.getId() == -1)
		{
			values.put("date", (new Date()).getTime());
			mDb.insert(DRAFTS_TABLE, null, values);
		}
		else
		{
			mDb.update(DRAFTS_TABLE, values, "_id=?", new String[]{ String.valueOf(b.getId()) });
		}
	}

	public void deleteDraft(long id) {
		mDb.delete(DRAFTS_TABLE, "_id=?", new String[]{ String.valueOf(id) });
	}

}
