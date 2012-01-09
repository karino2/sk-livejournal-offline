package com.googlecode.karino.db;

/**
 * This JavaBean is used to carry blog settings in and out of the DB.
 * 
 * @author juha
 * 
 */

public class SettingBean {
	private String settingname;
	private String settingtitle;
	private String settingvalue;
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSettingname() {
		return settingname;
	}

	public void setSettingname(String settingname) {
		this.settingname = settingname;
	}

	public String getSettingtitle() {
		return settingtitle;
	}

	public void setSettingtitle(String settingtitle) {
		this.settingtitle = settingtitle;
	}

	public String getSettingvalue() {
		return settingvalue;
	}

	public void setSettingvalue(String settingvalue) {
		this.settingvalue = settingvalue;
	}

	public String toString() {
		return "name: " + settingname + " title: " + settingtitle + " value: "
				+ settingvalue + " id: " + id;
	}

}