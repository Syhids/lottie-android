package com.airbnb.lottie.samples;

import android.app.Application;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatDelegate;

public class LottieApplication extends Application implements ILottieApplication {
  private int droppedFrames;
  private long droppedFramesStartingNs;
  private long currentFrameNs;

  @Override public void onCreate() {
    super.onCreate();
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }

  @Override public void startRecordingDroppedFrames() {
    droppedFrames = 0;
    droppedFramesStartingNs = currentFrameNs;
  }

  @Override public Pair<Integer, Long> stopRecordingDroppedFrames() {
    long duration = currentFrameNs - droppedFramesStartingNs;
    Pair<Integer, Long> ret = new Pair<>(droppedFrames, duration);
    droppedFrames = 0;
    return ret;
  }
}
