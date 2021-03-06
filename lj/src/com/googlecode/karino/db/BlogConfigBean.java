package com.googlecode.karino.db;

/**
 * This JavaBean is used as a transfer object to carry blog entries to SQLite3
 * DB and back, and between the blogging interface and app logic.
 * 
 * @author juha
 * 
 */

public class BlogConfigBean {
	private String blogname;
	private String rssfeedurl;
	private String lastwritten;
	private int lastentry;
	private CharSequence postconfig;
	private String username;
	private String password;
	private int postmethod;
	private int id; // autogenerated in DB!

	public int getPostmethod() {
		return postmethod;
	}

	public void setPostmethod(int postmethod) {
		this.postmethod = postmethod;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getBlogname() {
		return blogname;
	}

	public void setBlogname(String blogname) {
		this.blogname = blogname;
	}

	public String getRssfeedurl() {
		return rssfeedurl;
	}

	public void setRssfeedurl(String rssfeedurl) {
		this.rssfeedurl = rssfeedurl;
	}

	public String getLastwritten() {
		return lastwritten;
	}

	public void setLastwritten(String lastwritten) {
		this.lastwritten = lastwritten;
	}

	public int getLastentry() {
		return lastentry;
	}

	public void setLastentry(int lastentry) {
		this.lastentry = lastentry;
	}

	public CharSequence getPostConfig() {
		return postconfig;
	}

	public void setPostConfig(CharSequence postconfig) {
		this.postconfig = postconfig;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String toString() {
		if (blogname != null) {
			return "[" + id + "]-" + blogname;
		} else {
			return "[?]-" + blogname;
		}
	}
}