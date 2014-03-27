package com.djc.logintest.mycalendar;

import java.util.Calendar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout.LayoutParams;

import com.djc.logintest.activities.SwipeCalendarActivity;
import com.djc.logintest.bean.SwipeInfoOnCalendar;

/**
 * 日历控件单元格绘制类
 * 
 * @Description: 日历控件单元格绘制类
 * 
 * @FileName: DateWidgetDayCell.java
 * 
 * @Package com.calendar.demo
 * 
 * @Author Hanyonglu
 * 
 * @Date 2012-3-17 ����03:19:34
 * 
 * @Version V1.0
 */
public class DateWidgetDayCell extends View {
	// 默认日期字体大小
	private int dateTextSize = 24;
	// 默认签到信息字体大小
	private int swipeTextSize = 18;

	// 基本元素
	private OnItemClick itemClick = null;
	private Paint pt = new Paint();
	private RectF rect = new RectF();
	private String sDate = "";

	// 当前日期
	private int iDateYear = 0;
	private int iDateMonth = 0;
	private int iDateDay = 0;

	private boolean bSelected = false;
	private boolean bIsActiveMonth = false;
	private boolean bToday = false;
	private boolean bTouchedDown = false;
	private boolean bHoliday = false;
	private SwipeInfoOnCalendar record = new SwipeInfoOnCalendar();

	public static int ANIM_ALPHA_DURATION = 100;

	public interface OnItemClick {
		public void OnClick(DateWidgetDayCell item);
	}

	public DateWidgetDayCell(Context context, int iWidth, int iHeight) {
		super(context);
		setFocusable(true);
		setLayoutParams(new LayoutParams(iWidth, iHeight));
	}

	public SwipeInfoOnCalendar getRecord() {
		return record;
	}

	public void setRecord(SwipeInfoOnCalendar record) {
		this.record = record;
	}

	public Calendar getDate() {
		Calendar calDate = Calendar.getInstance();
		calDate.clear();
		calDate.set(Calendar.YEAR, iDateYear);
		calDate.set(Calendar.MONTH, iDateMonth);
		calDate.set(Calendar.DAY_OF_MONTH, iDateDay);
		return calDate;
	}

	public void setFont(int dateFont, int swipeFont) {
		this.dateTextSize = dateFont;
		this.swipeTextSize = swipeFont;
	}

	public void setData(int iYear, int iMonth, int iDay, Boolean bToday,
			Boolean bHoliday, int iActiveMonth) {
		iDateYear = iYear;
		iDateMonth = iMonth;
		iDateDay = iDay;

		this.sDate = Integer.toString(iDateDay);
		this.bIsActiveMonth = (iDateMonth == iActiveMonth);
		this.bToday = bToday;
		this.bHoliday = bHoliday;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		rect.set(0, 0, this.getWidth(), this.getHeight());
		rect.inset(1, 1);

		final boolean bFocused = IsViewFocused();

		drawDayView(canvas, bFocused);
		drawDayNumber(canvas);
	}

	public boolean IsViewFocused() {
		return (this.isFocused() || bTouchedDown);
	}

	// 绘制日历方格
	private void drawDayView(Canvas canvas, boolean bFocused) {

		if (bSelected || bFocused) {
			LinearGradient lGradBkg = null;

			if (bFocused) {
				lGradBkg = new LinearGradient(rect.left, 0, rect.right, 0,
						0xffaa5500, 0xffffddbb, Shader.TileMode.CLAMP);
			}

			if (bSelected) {
				lGradBkg = new LinearGradient(rect.left, 0, rect.right, 0,
						0xff225599, 0xffbbddff, Shader.TileMode.CLAMP);
			}

			if (lGradBkg != null) {
				pt.setShader(lGradBkg);
				canvas.drawRect(rect, pt);
			}

			pt.setShader(null);

		} else {
			pt.setColor(getColorBkg(bHoliday, bToday));
			canvas.drawRect(rect, pt);
		}

	}

	// 绘制日历中的数字
	public void drawDayNumber(Canvas canvas) {
		// draw day number
		pt.setTypeface(null);
		pt.setAntiAlias(true);
		pt.setShader(null);
		pt.setFakeBoldText(true);
		pt.setTextSize(dateTextSize);
		pt.setColor(SwipeCalendarActivity.isPresentMonth_FontColor);
		pt.setUnderlineText(false);

		// if (!bIsActiveMonth)
		// pt.setColor(MainActivity.unPresentMonth_FontColor);

		// 只绘制属于本月的数字
		if (bIsActiveMonth) {
			if (bToday)
				pt.setUnderlineText(true);

			final int iPosX = (int) rect.left + ((int) rect.width() >> 1)
					- ((int) pt.measureText(sDate) >> 1);

			// 置顶显示
			final int iPosY = (int) getTextHeight();

			// 居中显示
			// final int iPosY = (int) (this.getHeight() - (this.getHeight() -
			// getTextHeight()) / 2 - pt
			// .getFontMetrics().bottom);

			canvas.drawText(sDate, iPosX, iPosY, pt);

			if (!record.isEmpty()) {
				TextPaint textPaint = new TextPaint();
				// textPaint.setARGB(0xFF, 0xFF, 0, 0);
				textPaint.setColor(Color.BLUE);
				textPaint.setTextSize(swipeTextSize);
				int height = (int) (-textPaint.ascent() + textPaint.descent());

				String checkin = TextUtils.isEmpty(record.getCheckin()) ? "未签到"
						: record.getCheckin();
				djcdraw(canvas, checkin, rect.width(), iPosY + 1, textPaint);
				// 注意不要累加高度，执行完上面一句后，canvas的高度就是当前高度，加上textPaint
				// 的高度就是第二个textPaint的起始y坐标
				String checkout = TextUtils.isEmpty(record.getCheckout()) ? "未签退"
						: record.getCheckout();
				djcdraw(canvas, checkout, rect.width(), height, textPaint);
			}

			pt.setUnderlineText(false);
		}

	}

	private static void djcdraw(Canvas canvas, String text, float x, int posY,
			TextPaint textPaint) {
		StaticLayout layout = new StaticLayout(text, textPaint, (int) x,
				Alignment.ALIGN_CENTER, 1.0F, 0.0F, true);
		canvas.translate(0, posY);
		layout.draw(canvas);
	}

	// 得到字体高度
	private int getTextHeight() {
		return (int) (-pt.ascent() + pt.descent());
	}

	// 根据条件返回不同颜色值ֵ
	public static int getColorBkg(boolean bHoliday, boolean bToday) {
		if (bToday)
			return SwipeCalendarActivity.isToday_BgColor;
		// if (bHoliday) //如需周末有特殊背景色，可去掉注释
		// return Calendar_TestActivity.isHoliday_BgColor;
		return SwipeCalendarActivity.calendar_DayBgColor;
	}

	// 设置是否被选中
	@Override
	public void setSelected(boolean bEnable) {
		if (this.bSelected != bEnable) {
			this.bSelected = bEnable;
			this.invalidate();
		}
	}

	public void setItemClick(OnItemClick itemClick) {
		this.itemClick = itemClick;
	}

	public void doItemClick() {
		if (itemClick != null)
			itemClick.OnClick(this);
	}

	// 点击事件
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean bHandled = false;
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			bHandled = true;
			bTouchedDown = true;
			invalidate();
			startAlphaAnimIn(DateWidgetDayCell.this);
		}
		if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			bHandled = true;
			bTouchedDown = false;
			invalidate();
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			bHandled = true;
			bTouchedDown = false;
			invalidate();
			doItemClick();
		}
		return bHandled;
	}

	// 点击事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean bResult = super.onKeyDown(keyCode, event);
		if ((keyCode == KeyEvent.KEYCODE_DPAD_CENTER)
				|| (keyCode == KeyEvent.KEYCODE_ENTER)) {
			doItemClick();
		}
		return bResult;
	}

	// 不透明度渐变
	public static void startAlphaAnimIn(View view) {
		AlphaAnimation anim = new AlphaAnimation(0.5F, 1);
		anim.setDuration(ANIM_ALPHA_DURATION);
		anim.startNow();
		view.startAnimation(anim);
	}

	public void CreateReminder(Canvas canvas, int Color) {
		pt.setStyle(Paint.Style.FILL_AND_STROKE);
		pt.setColor(Color);
		Path path = new Path();
		path.moveTo(rect.right - rect.width() / 4, rect.top);
		path.lineTo(rect.right, rect.top);
		path.lineTo(rect.right, rect.top + rect.width() / 4);
		path.lineTo(rect.right - rect.width() / 4, rect.top);
		path.close();
		canvas.drawPath(path, pt);
	}
}