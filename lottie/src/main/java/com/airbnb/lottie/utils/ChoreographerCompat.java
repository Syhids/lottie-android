/*
 * Heavily modified version of ChoreographerCompat from facebook/rebound repository.
 */
package com.airbnb.lottie.utils;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Choreographer;

/**
 * Wrapper class for abstracting away availability of the JellyBean Choreographer. If Choreographer
 * is unavailable we fallback to using a normal Handler.
 */
public class ChoreographerCompat {
  private static final long ONE_FRAME_MILLIS = 17;
  private static final ChoreographerCompat INSTANCE = new ChoreographerCompat();
  private Impl impl;

  private ChoreographerCompat() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      impl = new JellyBeanPlusImpl();
    } else {
      impl = new BaseImpl();
    }
  }

  public static ChoreographerCompat getInstance() {
    return INSTANCE;
  }

  public void postFrameCallback(FrameCallback callbackWrapper) {
    impl.postFrameCallback(callbackWrapper);
  }

  public void postFrameCallbackDelayed(FrameCallback callbackWrapper, long delayMillis) {
    impl.postFrameCallbackDelayed(callbackWrapper, delayMillis);
  }

  public void removeFrameCallback(FrameCallback callbackWrapper) {
    impl.removeFrameCallback(callbackWrapper);
  }

  interface Impl {
    void postFrameCallback(FrameCallback callback);

    void postFrameCallbackDelayed(FrameCallback callback, long delayMillis);

    void removeFrameCallback(FrameCallback callback);
  }

  public interface FrameCallback extends Runnable {
    void doFrame(long frameTimeNanos);

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    Choreographer.FrameCallback getJellyBeanFrameCallback();
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  static class JellyBeanPlusImpl implements Impl {
    private Choreographer mChoreographer;

    JellyBeanPlusImpl() {
      mChoreographer = Choreographer.getInstance();
    }

    @Override public void postFrameCallback(FrameCallback callback) {
      mChoreographer.postFrameCallback(callback.getJellyBeanFrameCallback());
    }

    @Override public void postFrameCallbackDelayed(FrameCallback callback, long delayMillis) {
      mChoreographer.postFrameCallbackDelayed(callback.getJellyBeanFrameCallback(), delayMillis);
    }

    @Override public void removeFrameCallback(FrameCallback callback) {
      mChoreographer.removeFrameCallback(callback.getJellyBeanFrameCallback());
    }
  }

  static class BaseImpl implements Impl {
    private Handler mHandler;

    BaseImpl() {
      mHandler = new Handler(Looper.getMainLooper());
    }

    @Override public void postFrameCallback(final FrameCallback callback) {
      mHandler.postDelayed(callback, 0);
    }

    @Override public void postFrameCallbackDelayed(FrameCallback callback, long delayMillis) {
      mHandler.postDelayed(callback, delayMillis + ONE_FRAME_MILLIS);
    }

    @Override public void removeFrameCallback(FrameCallback callback) {
      mHandler.removeCallbacks(callback);
    }
  }

  /**
   * This class provides a compatibility wrapper around the JellyBean FrameCallback with methods
   * to access cached wrappers for submitting a real FrameCallback to a Choreographer or a Runnable
   * to a Handler.
   */
  public abstract static class FrameCallbackCompat implements FrameCallback {
    private Choreographer.FrameCallback jellyBeanFrameCallback;

    public FrameCallbackCompat() {
      if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        jellyBeanFrameCallback = new Choreographer.FrameCallback() {
          @Override public void doFrame(long frameTimeNanos) {
            FrameCallbackCompat.this.doFrame(frameTimeNanos);
          }
        };
      }
    }

    /**
     * Just a wrapper for frame callback, see {@link android.view.Choreographer.FrameCallback#doFrame(long)}.
     */
    public abstract void doFrame(long frameTimeNanos);

    @Override public Choreographer.FrameCallback getJellyBeanFrameCallback() {
      return jellyBeanFrameCallback;
    }

    @Override public void run() {
      doFrame(System.nanoTime());
    }
  }
}