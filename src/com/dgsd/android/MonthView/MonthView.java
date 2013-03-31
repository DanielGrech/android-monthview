package com.dgsd.android.MonthView;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created: 31/03/13 11:58 AM
 */
public class MonthView extends LinearLayout implements View.OnClickListener {

    private CalendarGridView mGrid;

    private DateFormat mDayOfWeekFormat;

    private CheckedTextView mCurrentCheckedCell;

    private OnDateClickedListener mDateClickedListener;

    private int mMonth;
    private int mYear;

    public MonthView(final Context context) {
        super(context);
        init();
    }

    public MonthView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.month, this, true);

        mGrid = (CalendarGridView) findViewById(R.id.calendar_grid);

        mDayOfWeekFormat = new SimpleDateFormat(getContext().getString(R.string.day_name_format));
    }

    public void set(int year, int month, int firstDayOfWeek) {
        mYear = year;
        mMonth = month;

        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        cal.setFirstDayOfWeek(firstDayOfWeek);

        //Headers..
        final CalendarRowView headerRow = (CalendarRowView) mGrid.getChildAt(0);
        headerRow.setIsHeaderRow(true);
        for (int offset = 0; offset < 7; offset++) {
            cal.set(Calendar.DAY_OF_WEEK, firstDayOfWeek + offset);
            final TextView textView = (TextView) headerRow.getChildAt(offset);
            textView.setText(mDayOfWeekFormat.format(cal.getTime()));
        }

        //Reset the calendar..
        cal.set(year, month, 1);
        cal.setFirstDayOfWeek(firstDayOfWeek);

        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == firstDayOfWeek) {
            //Hooray, no offset needed..
        } else if (dayOfWeek > firstDayOfWeek) {
            cal.add(Calendar.DAY_OF_YEAR, firstDayOfWeek - dayOfWeek);
        } else if (dayOfWeek < firstDayOfWeek) {
            cal.add(Calendar.DAY_OF_YEAR, dayOfWeek - firstDayOfWeek);
        }

        for (int i = 1; i < 7; i++) {
            CalendarRowView row = (CalendarRowView) mGrid.getChildAt(i);
            final int currentMonth = cal.get(Calendar.MONTH);

            if(currentMonth > month) {
                //Dont show rows with only next month..
                row.setVisibility(GONE);
                break;
            } else {
                row.setVisibility(VISIBLE);
            }

            for (int c = 0; c < 7; c++) {
                final CheckedTextView cell = (CheckedTextView) row.getChildAt(c);

                cell.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
                cell.setTag(cal.getTime());
                cell.setOnClickListener(this);
                cell.setEnabled(cal.get(Calendar.MONTH) == month);

                cal.set(Calendar.DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR) + 1);
            }
        }
    }


    @Override
    public void onClick(final View v) {
        if (mCurrentCheckedCell != v) {
            if (mCurrentCheckedCell != null) {
                mCurrentCheckedCell.setChecked(false);
                mCurrentCheckedCell.setSelected(false);
            }

            mCurrentCheckedCell = (CheckedTextView) v;
            mCurrentCheckedCell.setChecked(true);
            mCurrentCheckedCell.setSelected(true);

            Date d = (Date) mCurrentCheckedCell.getTag();
            if (mDateClickedListener != null) {
                mDateClickedListener.onDateClicked(d);
            }
        }
    }

    public void setSelected(int monthDay) {
        CheckedTextView cell = getCellForDay(monthDay);
        if(cell != null)
            onClick(cell);
    }

    public void mark(int monthDay, boolean marked) {
        CheckedTextView cell = getCellForDay(monthDay);
        if(cell != null) {
            if(marked) {
                cell.setTextColor(getResources().getColorStateList(R.color.calendar_text_marked_selector));
            } else {
                cell.setTextColor(getResources().getColorStateList(R.color.calendar_text_selector));
            }
        }
    }

    private CheckedTextView getCellForDay(int monthDay){
        final Calendar cal = Calendar.getInstance();
        for (int i = 1; i < 7; i++) {
            CalendarRowView row = (CalendarRowView) mGrid.getChildAt(i);
            for (int c = 0; c < 7; c++) {
                final CheckedTextView cell = (CheckedTextView) row.getChildAt(c);
                cal.setTime((Date) cell.getTag());
                if(cal.get(Calendar.DAY_OF_MONTH) == monthDay) {
                    return cell;
                }
            }
        }

        return null;
    }

    public void setDateClickedListener(final OnDateClickedListener dateClickedListener) {
        mDateClickedListener = dateClickedListener;
    }

    public static interface OnDateClickedListener {
        public void onDateClicked(Date d);
    }
}
