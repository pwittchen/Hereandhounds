package com.futureprocessing.hereandhounds.ui;

import android.app.Activity;
import android.app.ProgressDialog;

public class DialogHelper {

    private Activity activity;
    private ProgressDialog waitDialog;

    public DialogHelper(Activity activity, ProgressDialog waitDialog) {
        this.activity = activity;
        this.waitDialog = waitDialog;
    }

    public void showWaitDialog() {
        if (waitDialog != null) {
            if (!waitDialog.isShowing()) {
                waitDialog.show();
            }
        } else {
            waitDialog = new ProgressDialog(activity);
            waitDialog.setMessage("Loading...");
            waitDialog.show();
        }
    }

    public void dismissWaitDialog() {
        try {
            if (waitDialog != null && waitDialog.isShowing()) {
                waitDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
