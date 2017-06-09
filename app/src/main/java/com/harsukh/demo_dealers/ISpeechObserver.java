package com.harsukh.demo_dealers;

import java.util.ArrayList;

/**
 * Created by harsukh on 6/9/17.
 */

public interface ISpeechObserver {

    void setText(ArrayList<String> matches);

    void endOfSpeech();

    void restart();
}
