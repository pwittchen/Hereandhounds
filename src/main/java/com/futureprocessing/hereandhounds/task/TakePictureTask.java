package com.futureprocessing.hereandhounds.task;

import android.os.AsyncTask;

import com.futureprocessing.hereandhounds.imagerecognition.recognizeim.RecognizeImHelper;

public class TakePictureTask extends AsyncTask<String, String, String> {

    private RecognizeImHelper recognizeImHelper;

    public TakePictureTask(RecognizeImHelper recognizeImHelper) {
        this.recognizeImHelper = recognizeImHelper;
    }

    @Override
    protected String doInBackground(String... params) {
        recognizeImHelper.takePicture();
        return null;
    }
}

