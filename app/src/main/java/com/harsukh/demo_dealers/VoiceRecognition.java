package com.harsukh.demo_dealers;

import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by harsukh on 6/9/17.
 */

public class VoiceRecognition implements RecognitionListener {
    private static final String TAG = "VoiceRec";
    private static final CharSequence initString = "Okay On Star";
    private LinkedList<ISpeechObserver> speechObservers;

    public VoiceRecognition() {
        Log.d(TAG, "listening");
        speechObservers = new LinkedList<>();
    }


    @Override
    public void onReadyForSpeech(Bundle bundle) {

    }

    @Override
    public void onBeginningOfSpeech() {


    }

    @Override
    public void onRmsChanged(float v) {


    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        if (speechObservers.size() > 0) {
            for (ISpeechObserver speechObserver : speechObservers) {
                speechObserver.endOfSpeech();
            }
        }
    }

    @Override
    public void onError(int i) {
        if (speechObservers.size() > 0) {
            for (ISpeechObserver speechObserver : speechObservers) {
                speechObserver.restart();
            }
        }
    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        for (String command : matches) {
            System.out.println(command);
        }
        if (matches != null && speechObservers.size() > 0) {
            for (ISpeechObserver speechObserver : speechObservers) {
                speechObserver.setText(matches);
            }
        }
    }


    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {
        //dosomething here
    }

    public void addObserver(ISpeechObserver speechObserver) {
        speechObservers.add(speechObserver);
    }
}
