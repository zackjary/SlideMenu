package com.example.slidingmenu2.widget;

import com.nineoldandroids.view.ViewHelper;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

public class SlideMenu extends ViewGroup {
	private ViewGroup mMenu, mContent;
	private int mMenuRightPadding = 300;
	private int mMenuWidth;
	private int mScreenWidth;
	private boolean once;
	private boolean isOpen;
	private Scroller mScroller;
	private GestureDetector mDetector;

	public SlideMenu(Context context) {
		this(context, null);
	}

	public SlideMenu(Context context, AttributeSet attrs) {
		super(context, attrs);

		// 获取屏幕宽度
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		mScreenWidth = outMetrics.widthPixels;
		mMenuWidth = mScreenWidth - mMenuRightPadding;

		mScroller = new Scroller(context);
		mDetector = new GestureDetector(context,
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onFling(MotionEvent e1, MotionEvent e2,
							float velocityX, float velocityY) {
						if (Math.abs(velocityX) > Math.abs(velocityY)) {
							if (velocityX > 0) {
								if (!isOpen) {
									openMenu();
									return true;
								}
							} else {
								if (isOpen) {
									closeMenu();
									return true;
								}
							}
						}
						return false;
					}

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						if (isOpen && e.getRawX() > mMenuWidth) {
							closeMenu();
							return true;
						}
						return false;
					}
				});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (!once) {
			mMenu = (ViewGroup) getChildAt(0);
			mContent = (ViewGroup) getChildAt(1);
			mMenu.getLayoutParams().width = mMenuWidth;
			mContent.getLayoutParams().width = mScreenWidth;

			// 测量Menu
			LayoutParams layoutParams = mMenu.getLayoutParams();
			int menuWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMenuWidth,
					MeasureSpec.EXACTLY);
			int menuHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
					0, layoutParams.height);
			mMenu.measure(menuWidthMeasureSpec, menuHeightMeasureSpec);

			// 测量Content
			LayoutParams layoutParams2 = mContent.getLayoutParams();
			int contentWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
					mScreenWidth, MeasureSpec.EXACTLY);
			int contentHeightMeasureSpec = getChildMeasureSpec(
					heightMeasureSpec, 0, layoutParams2.height);
			mContent.measure(contentWidthMeasureSpec, contentHeightMeasureSpec);

			once = true;
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if (changed) {
			mMenu.layout(0, 0, mMenuWidth, getHeight());
			mContent.layout(mMenuWidth, 0, mMenuWidth + mScreenWidth,
					getHeight());
			// 将菜单隐藏
			scrollTo(mMenuWidth, 0);
		}

	}

	private float lastx;
	private float scrollX;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mDetector.onTouchEvent(event)) {
			return true;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastx = event.getRawX();
			break;

		case MotionEvent.ACTION_MOVE:
			scrollX = getScrollX() - (event.getRawX() - lastx);
			scrollX = scrollX > mMenuWidth ? mMenuWidth : scrollX;
			scrollX = scrollX < 0 ? 0 : scrollX;
			scrollTo((int) scrollX, 0);
			lastx = event.getRawX();
			break;

		case MotionEvent.ACTION_UP:
			if (getScrollX() > mMenuWidth / 2) {
				smoothScrollTo(mMenuWidth, 0);
				isOpen = false;
			} else {
				smoothScrollTo(0, 0);
				isOpen = true;
			}
			break;
		}
		return true;
	}

	public void closeMenu() {
		if (!isOpen) {
			return;
		}
		smoothScrollTo(mMenuWidth, 0);
		isOpen = false;
	}

	public void openMenu() {
		if (isOpen) {
			return;
		}
		smoothScrollTo(0, 0);
		isOpen = true;
	}

	public void toggle() {
		if (isOpen) {
			closeMenu();
		} else {
			openMenu();
		}
	}

	private void smoothScrollTo(int x, int y) {
		int duration = 500;
		mScroller.startScroll(getScrollX(), getScrollY(), x - getScrollX(), y
				- getScrollY(), duration);
		invalidate();
	}

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = mScroller.getCurrX();
				int y = mScroller.getCurrY();
				if (oldX != x || oldY != y) {
					scrollTo(x, y);
				}
				invalidate();
			}
		}
	}

	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		ViewHelper.setTranslationX(mMenu, l);
		ViewHelper.setScaleX(mMenu, 1.0f - 0.4f * l / mMenuWidth);
		ViewHelper.setScaleY(mMenu, 1.0f - 0.4f * l / mMenuWidth);

		ViewHelper.setPivotX(mContent, 0);
		ViewHelper.setPivotY(mContent, getHeight() / 2);
		ViewHelper.setScaleX(mContent, 0.7f + 0.3f * l / mMenuWidth);
		ViewHelper.setScaleY(mContent, 0.7f + 0.3f * l / mMenuWidth);
	}

}
