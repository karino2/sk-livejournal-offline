package com.google.code.karino.db;

import java.util.Date;

/**
 * This bean is used as a transfer object to save and load blog entries from the
 * DB and move the data between app logic and blogging interface.
 * 
 * @author juha
 * 
 */

public class BlogEntryBean {

	private CharSequence blogEntry;
	private String title;
	private Date created;
	private long id = -1;
	private int publishedIn;
	private boolean isDraft;

	public CharSequence getBlogEntry() {
		return blogEntry;
	}	

	public void setBlogEntry(CharSequence blogEntry) {
		this.blogEntry = blogEntry;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public int getPublishedIn() {
		return publishedIn;
	}

	public void setPublishedIn(int publishedIn) {
		this.publishedIn = publishedIn;
	}

	public boolean isDraft() {
		return isDraft;
	}

	public boolean getDraft() {
		return isDraft();
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}

	/*
	 * !!!!!!!!!!!!!!!!!!!!!!! public String toString() { SimpleDateFormat df =
	 * new SimpleDateFormat(DBConstants.DB_DATE_FORMAT); return
	 * ""+getTitle()+" - "+df.format(created); }
	 */
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

}