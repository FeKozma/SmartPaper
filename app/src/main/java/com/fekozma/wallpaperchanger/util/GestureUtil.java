package com.fekozma.wallpaperchanger.util;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.fekozma.wallpaperchanger.database.DBLog;

public class GestureUtil {

	public static GestureDetector getGestureDetector(Context context, DialogSwipeListener listener) {

		return new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
			private static final int SWIPE_THRESHOLD = 10;  // min distance
			private static final int SWIPE_VELOCITY_THRESHOLD = 10; // min speed

			@Override
			public boolean onDown(MotionEvent e) {
				return true;
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				float diffX = e2.getX() - e1.getX();
				float diffY = e2.getY() - e1.getY();

				if (Math.abs(diffX) > Math.abs(diffY)) {
					if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
						if (diffX > 0) {
							DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Swipe right");
							listener.swipeRight();
						} else {
							DBLog.db.addLog(DBLog.LEVELS.DEBUG, "Swipe left");
							listener.swipeLeft();
						}
						return true;
					}
				}
				return false;
			}
		});
	}

	public abstract static class DialogSwipeListener {
		public abstract void swipeLeft();

		public abstract void swipeRight();
	}
}
