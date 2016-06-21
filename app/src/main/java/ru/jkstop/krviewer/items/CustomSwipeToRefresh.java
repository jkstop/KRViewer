package ru.jkstop.krviewer.items;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * SwipeToRefresh для использования с ViewPager
 *
 * корректное перелистывание фрагментов
 *
 * http://stackoverflow.com/questions/23989910/horizontalscrollview-inside-swiperefreshlayout
 */
public class CustomSwipeToRefresh extends SwipeRefreshLayout {

    private int touchSlop;
    private float prevX;

    public CustomSwipeToRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);

        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                prevX = MotionEvent.obtain(event).getX();
                break;

            case MotionEvent.ACTION_MOVE:
                final float eventX = event.getX();
                float xDiff = Math.abs(eventX - prevX);

                if (xDiff > touchSlop) {
                    return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }
}
