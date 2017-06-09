package com.harsukh.demo_dealers;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by harsukh on 6/9/17.
 */

public interface ISpeechObserver {

    void setText(Iterator<String> matches);

    void endOfSpeech();

    void restart();
}
