package com.cocobabys.activities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import com.cocobabys.R;
import com.cocobabys.bean.SwipeInfoOnCalendar;
import com.cocobabys.constant.ConstantValue;
import com.cocobabys.constant.EventType;
import com.cocobabys.dbmgr.DataMgr;
import com.cocobabys.dbmgr.info.InfoHelper;
import com.cocobabys.handler.MyHandler;
import com.cocobabys.mycalendar.DateWidgetDayCell;
import com.cocobabys.mycalendar.DateWidgetDayHeader;
import com.cocobabys.mycalendar.DayStyle;
import com.cocobabys.mycalendar.MyCalendarFont;
import com.cocobabys.taskmgr.UpdateCalendarTask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * 
 * @File: MainActivity.java
 * 
 * @Package com.calendar.demo
 * 
 * @Author Hanyonglu
 * 
 * @Date 2012-6-21 ����11:42:32
 * 
 * @Version V1.0
 */
public class SwipeCalendarActivity extends UmengStatisticsActivity{
    // 每一个日历单元格，高度比宽度多8，因为需要绘制刷卡信息
    private static final int             EXTRA_HEIGHT             = 8;
    private LinearLayout                 layContent               = null;
    private ArrayList<DateWidgetDayCell> days                     = new ArrayList<DateWidgetDayCell>();

    public static Calendar               calStartDate             = Calendar.getInstance();
    private Calendar                     calToday                 = Calendar.getInstance();
    private Calendar                     calCalendar              = Calendar.getInstance();
    private Calendar                     calSelected              = Calendar.getInstance();

    private int                          iMonthViewCurrentMonth   = 0;
    private int                          iMonthViewCurrentYear    = 0;
    private int                          iFirstDayOfWeek          = Calendar.MONDAY;

    private int                          calendar_Width           = 0;
    private int                          cell_Width               = 0;

    TextView                             top_Date                 = null;
    ImageButton                          btn_pre_month            = null;
    ImageButton                          btn_next_month           = null;
    TextView                             arrange_text             = null;
    LinearLayout                         mainLayout               = null;
    LinearLayout                         arrange_layout           = null;

    ArrayList<String>                    calendar_Source          = null;
    Hashtable<Integer, Integer>          calendar_Hashtable       = new Hashtable<Integer, Integer>();
    Boolean[]                            flag                     = null;
    Calendar                             startDate                = null;
    Calendar                             endDate                  = null;
    int                                  dayvalue                 = -1;

    public static int                    calendar_WeekBgColor     = 0;
    public static int                    calendar_DayBgColor      = 0;
    public static int                    isHoliday_BgColor        = 0;
    public static int                    unPresentMonth_FontColor = 0;
    public static int                    isPresentMonth_FontColor = 0;
    public static int                    isToday_BgColor          = 0;
    public static int                    special_Reminder         = 0;
    public static int                    common_Reminder          = 0;
    public static int                    calendar_WeekFontColor   = 0;
    String                               UserName                 = "";
    private MyCalendarFont               calendarFont;
    private Handler                      handler;
    private ProgressDialog               dialog;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        initDeviceConfig();
        bindview();
        initDlg();
        initCalendarView();
        initColor();
        initHandler();
        runUpdateCalendarTask();
    }

    private void initDlg(){
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setMessage(getResources().getString(R.string.getting_swipe_info));
    }

    private void initHandler(){
        handler = new MyHandler(this, dialog){
            @Override
            public void handleMessage(Message msg){
                if(SwipeCalendarActivity.this.isFinishing()){
                    Log.w("djc", "do nothing when activity finishing!");
                    return;
                }
                super.handleMessage(msg);
                Log.d("DDD", "SwipeCalendarActivity receice msg:" + msg.what);
                switch(msg.what){
                    case EventType.GET_SWIPE_RECORD_SUCCESS:
                        updateCalendar();
                        break;
                    default:
                        // 错对都得刷新
                        updateCalendar();
                        break;
                }
            }

        };
    }

    private void runUpdateCalendarTask(){
        dialog.show();
        Calendar current = Calendar.getInstance();
        current.set(Calendar.MONTH, iMonthViewCurrentMonth);
        current.set(Calendar.YEAR, iMonthViewCurrentYear);
        current.set(Calendar.DATE, 1);// 把日期设置为当月第一天
        current.set(Calendar.HOUR_OF_DAY, 0);
        current.set(Calendar.MINUTE, 0);
        current.set(Calendar.SECOND, 0);
        current.set(Calendar.MILLISECOND, 0);
        long from = current.getTimeInMillis();
        current.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
        int maxDate = current.get(Calendar.DATE);
        // 误差30s左右，也就是说当晚11点59分30秒之后的刷卡记录可能无法获取，但是不影响
        long to = from + maxDate * (24L * 60 * 60 - 1L) * 1000L;

        new UpdateCalendarTask(handler, from, to).execute();
    }

    public void initCalendarView(){
        // 计算本月日历中的第一天(一般是上月的某天)，并更新日历
        calStartDate = getCalendarStartDate();
        mainLayout.addView(generateCalendarMain());
        DateWidgetDayCell daySelected = updateCalendar();

        if(daySelected != null)
            daySelected.requestFocus();

        LinearLayout.LayoutParams Param1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        ScrollView view = new ScrollView(this);
        arrange_layout = createLayout(LinearLayout.VERTICAL);
        arrange_layout.setPadding(5, 2, 0, 0);
        arrange_text = new TextView(this);
        mainLayout.setBackgroundColor(Color.WHITE);
        arrange_text.setTextColor(Color.BLACK);
        arrange_text.setTextSize(18);
        arrange_layout.addView(arrange_text);

        startDate = GetStartDate();
        calToday = GetTodayDate();

        endDate = GetEndDate(startDate);
        view.addView(arrange_layout, Param1);
        mainLayout.addView(view);
    }

    public void initColor(){
        calendar_WeekBgColor = this.getResources().getColor(R.color.Calendar_WeekBgColor);
        calendar_DayBgColor = this.getResources().getColor(R.color.Calendar_DayBgColor);
        isHoliday_BgColor = this.getResources().getColor(R.color.isHoliday_BgColor);
        unPresentMonth_FontColor = this.getResources().getColor(R.color.unPresentMonth_FontColor);
        isPresentMonth_FontColor = this.getResources().getColor(R.color.isPresentMonth_FontColor);
        isToday_BgColor = this.getResources().getColor(R.color.isToday_BgColor);
        special_Reminder = this.getResources().getColor(R.color.specialReminder);
        common_Reminder = this.getResources().getColor(R.color.commonReminder);
        calendar_WeekFontColor = this.getResources().getColor(R.color.Calendar_WeekFontColor);
    }

    public void bindview(){
        // 制定布局文件，并设置属性
        mainLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.calendar_main, null);
        // mainLayout.setPadding(2, 0, 2, 0);
        setContentView(mainLayout);

        ActivityHelper.setBackKeyLitsenerOnTopbar(this, R.string.swap);

        // 声明控件，并绑定事件
        top_Date = (TextView)findViewById(R.id.Top_Date);
        btn_pre_month = (ImageButton)findViewById(R.id.btn_pre_month);
        btn_next_month = (ImageButton)findViewById(R.id.btn_next_month);
        btn_pre_month.setOnClickListener(new Pre_MonthOnClickListener());
        btn_next_month.setOnClickListener(new Next_MonthOnClickListener());
    }

    public void initDeviceConfig(){
        // 获得屏幕宽和高，并計算出屏幕寬度分七等份的大小
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        int screenWidth = display.getWidth();
        calendar_Width = screenWidth;
        cell_Width = calendar_Width / 7 + 1;
        initFont(calendar_Width);
    }

    private void initFont(int width){
        calendarFont = new MyCalendarFont(width);
    }

    protected String GetDateShortString(Calendar date){
        String returnString = date.get(Calendar.YEAR) + "/";
        returnString += date.get(Calendar.MONTH) + 1 + "/";
        returnString += date.get(Calendar.DAY_OF_MONTH);

        return returnString;
    }

    // 得到当天在日历中的序号
    private int getNumFromDate(Calendar now, Calendar returnDate){
        Calendar cNow = (Calendar)now.clone();
        Calendar cReturnDate = (Calendar)returnDate.clone();
        setTimeToMidnight(cNow);
        setTimeToMidnight(cReturnDate);

        long todayMs = cNow.getTimeInMillis();
        long returnMs = cReturnDate.getTimeInMillis();
        long intervalMs = todayMs - returnMs;
        int index = millisecondsToDays(intervalMs);

        return index;
    }

    private int millisecondsToDays(long intervalMs){
        return Math.round((intervalMs / (1000 * 86400)));
    }

    private void setTimeToMidnight(Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    // 生成布局
    private LinearLayout createLayout(int iOrientation){
        LinearLayout lay = new LinearLayout(this);
        lay.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        lay.setOrientation(iOrientation);

        return lay;
    }

    // 生成日历头部
    private View generateCalendarHeader(){
        LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
        // layRow.setBackgroundColor(Color.argb(255, 207, 207, 205));

        for(int iDay = 0; iDay < 7; iDay++){
            DateWidgetDayHeader day = new DateWidgetDayHeader(this, cell_Width, 35);

            final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
            day.setData(iWeekDay);
            layRow.addView(day);
        }

        return layRow;
    }

    // 生成日历主体
    private View generateCalendarMain(){
        layContent = createLayout(LinearLayout.VERTICAL);
        // layContent.setPadding(1, 0, 1, 0);
        layContent.setBackgroundColor(Color.argb(255, 105, 105, 103));
        layContent.addView(generateCalendarHeader());
        days.clear();

        for(int iRow = 0; iRow < 6; iRow++){
            layContent.addView(generateCalendarRow());
        }

        return layContent;
    }

    // 生成日历中的一行，仅画矩形
    private View generateCalendarRow(){
        LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);

        for(int iDay = 0; iDay < 7; iDay++){
            DateWidgetDayCell dayCell = new DateWidgetDayCell(this, cell_Width, cell_Width + EXTRA_HEIGHT);
            dayCell.setItemClick(mOnDayCellClick);
            days.add(dayCell);
            layRow.addView(dayCell);
        }

        return layRow;
    }

    // 设置当天日期和被选中日期
    private Calendar getCalendarStartDate(){
        calToday.setTimeInMillis(System.currentTimeMillis());
        calToday.setFirstDayOfWeek(iFirstDayOfWeek);

        if(calSelected.getTimeInMillis() == 0){
            calStartDate.setTimeInMillis(System.currentTimeMillis());
            calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
        } else{
            calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
            calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
        }

        updateStartDateForMonth();
        return calStartDate;
    }

    // 由于本日历上的日期都是从周一开始的，此方法可推算出上月在本月日历中显示的天数
    private void updateStartDateForMonth(){
        iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
        iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
        calStartDate.set(Calendar.DAY_OF_MONTH, 1);
        calStartDate.set(Calendar.HOUR_OF_DAY, 0);
        calStartDate.set(Calendar.MINUTE, 0);
        calStartDate.set(Calendar.SECOND, 0);
        // update days for week
        updateCurrentMonthDisplay();
        int iDay = 0;
        int iStartDay = iFirstDayOfWeek;

        if(iStartDay == Calendar.MONDAY){
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
            if(iDay < 0)
                iDay = 6;
        }

        if(iStartDay == Calendar.SUNDAY){
            iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
            if(iDay < 0)
                iDay = 6;
        }

        calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
    }

    // 更新日历
    private DateWidgetDayCell updateCalendar(){
        DateWidgetDayCell daySelected = null;
        boolean bSelected = false;
        final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
        final int iSelectedYear = calSelected.get(Calendar.YEAR);
        final int iSelectedMonth = calSelected.get(Calendar.MONTH);
        final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);
        calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());

        for(int i = 0; i < days.size(); i++){
            final int iYear = calCalendar.get(Calendar.YEAR);
            final int iMonth = calCalendar.get(Calendar.MONTH);
            final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
            final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);
            DateWidgetDayCell dayCell = days.get(i);

            // 判断是否当天
            boolean bToday = false;

            if(calToday.get(Calendar.YEAR) == iYear){
                if(calToday.get(Calendar.MONTH) == iMonth){
                    if(calToday.get(Calendar.DAY_OF_MONTH) == iDay){
                        bToday = true;
                    }
                }
            }

            // check holiday
            boolean bHoliday = false;
            if((iDayOfWeek == Calendar.SATURDAY) || (iDayOfWeek == Calendar.SUNDAY))
                bHoliday = true;
            if((iMonth == Calendar.JANUARY) && (iDay == 1))
                bHoliday = true;

            // 是否被选中
            bSelected = false;

            if(bIsSelection){
                if((iSelectedDay == iDay) && (iSelectedMonth == iMonth) && (iSelectedYear == iYear)){
                    bSelected = true;
                }
            }

            dayCell.setSelected(bSelected);

            if(bSelected){
                daySelected = dayCell;
            }

            SwipeInfoOnCalendar record = getRecord(calCalendar);
            dayCell.setRecord(record);
            dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday, iMonthViewCurrentMonth);
            dayCell.setFont(calendarFont.getCalendarDateFont(), calendarFont.getSwipeFont());

            calCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        layContent.invalidate();

        return daySelected;
    }

    private SwipeInfoOnCalendar getRecord(Calendar calendar){
        String format = InfoHelper.getYearMonthDayFormat().format(calendar.getTime());
        String checkin = DataMgr.getInstance().getLastestSwipeIn(format);
        String checkout = DataMgr.getInstance().getLatestSwipeOut(format);

        if(!TextUtils.isEmpty(checkout)){
            long out = Long.valueOf(Timestamp.valueOf(checkout).getTime());

            // 如果最晚入园时间大于最晚离园时间，说明小孩在中途离园后，重新入园且未离园,中途离园记录不显示
            // 如果当天没有入园记录，那么离园记录有效，需要显示
            if(!TextUtils.isEmpty(checkin)){
                long in = Long.valueOf(Timestamp.valueOf(checkin).getTime());
                if(in > out){
                    checkout = "";
                }
            }
        }

        SwipeInfoOnCalendar onCalendar = new SwipeInfoOnCalendar();
        onCalendar.setCheckin(getFormattedResult(checkin));
        onCalendar.setCheckout(getFormattedResult(checkout));
        return onCalendar;
    }

    // 只显示时分信息
    public String getFormattedResult(String in){
        if(TextUtils.isEmpty(in)){
            return in;
        }
        Timestamp Date = Timestamp.valueOf(in);
        String result = InfoHelper.getHourMinuteFormat().format(Date);
        return result;
    }

    // 更新日历标题上显示的年月
    private void updateCurrentMonthDisplay(){
        String date = calStartDate.get(Calendar.YEAR) + "年" + (calStartDate.get(Calendar.MONTH) + 1) + "月";
        top_Date.setText(date);
    }

    // 点击上月按钮，触发事件
    class Pre_MonthOnClickListener implements OnClickListener{
        @Override
        public void onClick(View v){
            arrange_text.setText("");
            calSelected.setTimeInMillis(0);
            iMonthViewCurrentMonth--;

            if(iMonthViewCurrentMonth == -1){
                iMonthViewCurrentMonth = 11;
                iMonthViewCurrentYear--;
            }

            calStartDate.set(Calendar.DAY_OF_MONTH, 1);
            calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
            calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
            calStartDate.set(Calendar.HOUR_OF_DAY, 0);
            calStartDate.set(Calendar.MINUTE, 0);
            calStartDate.set(Calendar.SECOND, 0);
            calStartDate.set(Calendar.MILLISECOND, 0);
            updateStartDateForMonth();

            startDate = (Calendar)calStartDate.clone();
            endDate = GetEndDate(startDate);
            // updateCalendar();
            runUpdateCalendarTask();
        }

    }

    // 点击下月按钮，触发事件
    class Next_MonthOnClickListener implements OnClickListener{
        @Override
        public void onClick(View v){
            arrange_text.setText("");
            calSelected.setTimeInMillis(0);
            iMonthViewCurrentMonth++;

            if(iMonthViewCurrentMonth == 12){
                iMonthViewCurrentMonth = 0;
                iMonthViewCurrentYear++;
            }

            calStartDate.set(Calendar.DAY_OF_MONTH, 1);
            calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
            calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
            updateStartDateForMonth();

            startDate = (Calendar)calStartDate.clone();
            endDate = GetEndDate(startDate);
            // updateCalendar();
            runUpdateCalendarTask();
        }
    }

    // 点击日历，触发事件
    private DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick(){
                                                              public void OnClick(DateWidgetDayCell item){
                                                                  if(!item.getRecord().isEmpty()){
                                                                      Date date = item.getDate().getTime();
                                                                      String format = InfoHelper
                                                                              .getYearMonthDayFormat().format(date);
                                                                      startToSwipeListActivity(format);
                                                                  } else{
                                                                      arrange_text.setText("没有刷卡记录");
                                                                  }
                                                                  // item.setSelected(true);
                                                                  // updateCalendar();
                                                              }
                                                          };

    private void startToSwipeListActivity(String date){
        Intent intent = new Intent(this, SwipeListActivity.class);
        intent.putExtra(ConstantValue.SWIPE_DATE, date);
        startActivity(intent);
    }

    public Calendar GetTodayDate(){
        Calendar cal_Today = Calendar.getInstance();
        cal_Today.set(Calendar.HOUR_OF_DAY, 0);
        cal_Today.set(Calendar.MINUTE, 0);
        cal_Today.set(Calendar.SECOND, 0);
        cal_Today.setFirstDayOfWeek(Calendar.MONDAY);

        return cal_Today;
    }

    // 得到当前日历中的第一天
    public Calendar GetStartDate(){
        int iDay = 0;
        Calendar cal_Now = Calendar.getInstance();
        cal_Now.set(Calendar.DAY_OF_MONTH, 1);
        cal_Now.set(Calendar.HOUR_OF_DAY, 0);
        cal_Now.set(Calendar.MINUTE, 0);
        cal_Now.set(Calendar.SECOND, 0);
        cal_Now.setFirstDayOfWeek(Calendar.MONDAY);

        iDay = cal_Now.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;

        if(iDay < 0){
            iDay = 6;
        }

        cal_Now.add(Calendar.DAY_OF_WEEK, -iDay);

        return cal_Now;
    }

    public Calendar GetEndDate(Calendar startDate){
        // Calendar end = GetStartDate(enddate);
        Calendar endDate = Calendar.getInstance();
        endDate = (Calendar)startDate.clone();
        endDate.add(Calendar.DAY_OF_MONTH, 41);
        return endDate;
    }
}