package com.googlecode.karino.util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.R;

public class AlertUtil {

	public static void showAlert(Context con, String title, String message) {
		Dialog dlg = new AlertDialog.Builder(con).setIcon(
				R.drawable.ic_dialog_alert).setTitle(title).setPositiveButton(
				"ok", null).setMessage(message).create();
		dlg.show();
	}

	public static void showAlert(Context con, String title, String message,
			CharSequence positiveButtontxt,
			DialogInterface.OnClickListener positiveListener,
			CharSequence negativeButtontxt,
			DialogInterface.OnClickListener negativeListener) {
		Dialog dlg = new AlertDialog.Builder(con).setIcon(
				R.drawable.ic_dialog_alert).setTitle(title).setNegativeButton(
				negativeButtontxt, negativeListener).setPositiveButton(
				positiveButtontxt, positiveListener).setMessage(message)
				.create();
		dlg.show();
	}

}