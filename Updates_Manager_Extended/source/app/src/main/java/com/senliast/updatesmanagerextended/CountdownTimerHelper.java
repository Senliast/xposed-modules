package com.senliast.updatesmanagerextended;

import android.os.Handler;
import android.os.Looper;

public class CountdownTimerHelper {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable countdownRunnable;
    private boolean isRunning = false;

    public interface OnTickListener {
        void onTick(String timeLeft);
    }

    public interface OnTimerFinishListener {
        void onTimerFinished(int position);
    }

    public interface OnFinishListener {
        void onFinish();
    }

    public void startCountdown(long endTimeMillis,
                               OnTickListener tickListener,
                               OnTimerFinishListener finishListener,
                               int position) {

        startInternal(endTimeMillis, tickListener, finishListener, null, position);
    }

    public void startCountdown(long endTimeMillis,
                               OnTickListener tickListener,
                               OnFinishListener finishListener) {

        startInternal(endTimeMillis, tickListener, null, finishListener, 0);
    }

    private void startInternal(long endTimeMillis,
                               OnTickListener tickListener,
                               OnTimerFinishListener finishWithPosition,
                               OnFinishListener simpleFinishListener,
                               int position) {

        stop();

        isRunning = true;

        countdownRunnable = new Runnable() {
            @Override
            public void run() {
                long timeLeft = endTimeMillis - System.currentTimeMillis();

                if (timeLeft <= 0) {
                    isRunning = false;

                    if (finishWithPosition != null) {
                        finishWithPosition.onTimerFinished(position);
                    } else if (simpleFinishListener != null) {
                        simpleFinishListener.onFinish();
                    }
                    return;
                }

                if (tickListener != null) {
                    tickListener.onTick(formatTime(timeLeft));
                }

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(countdownRunnable);
    }

    public void stop() {
        isRunning = false;
        if (countdownRunnable != null) {
            handler.removeCallbacks(countdownRunnable);
        }
    }

    private String formatTime(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}