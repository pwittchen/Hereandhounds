package com.futureprocessing.hereandhounds.imagerecognition.recognizeim;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.futureprocessing.hereandhounds.activity.EyeViewActivity;

import java.io.ByteArrayOutputStream;

import pl.itraff.TestApi.ItraffApi.ItraffApi;

public class RecognizeImHelper {

    private EyeViewActivity activity;

    public RecognizeImHelper(EyeViewActivity eyeViewActivity) {
        activity = eyeViewActivity;
    }

    public void takePicture() {
        boolean clientIdAndApiKeyAreFilledIn = RecognizeImConstants.CLIENT_API_KEY != null && RecognizeImConstants.CLIENT_API_KEY.length() > 0 && RecognizeImConstants.CLIENT_API_ID != null && RecognizeImConstants.CLIENT_API_ID > 0;
        if (clientIdAndApiKeyAreFilledIn) {
            activity.getCameraView().getCamera().takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Log.d(activity.LOG_TAG, "takePicture()");
                    camera.startPreview();
                    if (ItraffApi.isOnline(activity.getApplicationContext())) {
                        activity.getDialogHelper().showWaitDialog();
                        ItraffApi api = new ItraffApi(RecognizeImConstants.CLIENT_API_ID, RecognizeImConstants.CLIENT_API_KEY, activity.LOG_TAG, true);
                        api.setMode(ItraffApi.MODE_SINGLE);
                        api.sendPhoto(bytes, itraffApiHandler, false);
                    } else {
                        Toast.makeText(activity.getApplicationContext(), "Not connected", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Toast.makeText(activity.getApplicationContext(), "Fill in Your Client Id and API Key", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("HandlerLeak")
    public Handler itraffApiHandler = new Handler() {
        // callback from api
        @Override
        public void handleMessage(Message msg) {
            activity.getDialogHelper().dismissWaitDialog();
            Bundle data = msg.getData();
            if (data != null) {
                Integer status = data.getInt(ItraffApi.STATUS, -1);
                String response = data.getString(ItraffApi.RESPONSE);
                if (status == 0) { // status ok
                    Toast.makeText(activity.getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                } else if (status == -1) { // application error (for example timeout)
                    Toast.makeText(activity.getApplicationContext(), "API error", Toast.LENGTH_LONG).show();
                } else { // error from api
                    Toast.makeText(activity.getApplicationContext(), "Error: " + response, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    public void onActivityResultCalled(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {
                    Bitmap image = (Bitmap) bundle.get("data");
                    if (image != null) {
                        if (ItraffApi.isOnline(activity.getApplicationContext())) {
                            activity.getDialogHelper().showWaitDialog();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext());
                            ItraffApi api = new ItraffApi(RecognizeImConstants.CLIENT_API_ID, RecognizeImConstants.CLIENT_API_KEY, activity.LOG_TAG, true);
                            if (prefs.getString("mode", "single").equals("multi")) {
                                api.setMode(ItraffApi.MODE_MULTI);
                            } else {
                                api.setMode(ItraffApi.MODE_SINGLE);
                            }
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] pictureData = stream.toByteArray();
                            api.sendPhoto(pictureData, itraffApiHandler, prefs.getBoolean("allResults", true));
                        } else {
                            Toast.makeText(activity.getApplicationContext(), "Not connected", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } else if (resultCode == RecognizeImConstants.RESULT_BMP_DAMAGED) {
            Log.d(activity.LOG_TAG, "RESULT_BMP_DAMAGED");
        }
    }

}
