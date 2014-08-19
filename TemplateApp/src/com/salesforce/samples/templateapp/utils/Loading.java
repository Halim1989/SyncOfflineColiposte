package com.salesforce.samples.templateapp.utils;

import android.app.Activity;
import android.app.ProgressDialog;

public class Loading {

	public static ProgressDialog progressBar;

	public static void show(Activity activity) {
		final ProgressDialog progressDialog = new ProgressDialog(activity);
		progressDialog.setCancelable(false);
		progressBar = progressDialog;
		progressBar.show();
	}

	public static void hide() {
		progressBar.hide();
	}
}
