package me.sahidur.foodiography.main.utils;

import android.content.Context;

import me.sahidur.foodiography.R;

import java.util.Date;



public class TimeDifference {
    int years;
    int months;
    int days;
    int hours;
    int minutes;
    int seconds;
    String differenceString;

    public TimeDifference(Context mContext, Date curdate, Date olddate) {
        String y  = " "+mContext.getString(R.string.year);
        String ys = " "+mContext.getString(R.string.years);
        String m  = " "+mContext.getString(R.string.month);
        String ms = " "+mContext.getString(R.string.months);
        String d  = " "+mContext.getString(R.string.day);
        String ds = " "+mContext.getString(R.string.days);
        String h  = " "+mContext.getString(R.string.hour);
        String hs = " "+mContext.getString(R.string.hours);
        String mm = " "+mContext.getString(R.string.minute);
        String mms= " "+mContext.getString(R.string.minutes);
        String s  = " "+mContext.getString(R.string.second);
        String ss = " "+mContext.getString(R.string.seconds);
        String a  = " "+mContext.getString(R.string.ago);

        float diff=curdate.getTime() - olddate.getTime();
        if (diff >= 0) {
            int yearDiff = Math.round( ( diff/ (365l*2592000000f))>=1?( diff/ (365l*2592000000f)):0);
            if (yearDiff > 0) {
                years = yearDiff;
                setDifferenceString(years + (years == 1 ? y : ys) + a);
            } else {
                int monthDiff = Math.round((diff / 2592000000f)>=1?(diff / 2592000000f):0);
                if (monthDiff > 0) {
                    if (monthDiff > 11)
                        monthDiff = 11;

                    months = monthDiff;
                    setDifferenceString(months + (months == 1 ? m : ms) + a);
                } else {
                    int dayDiff = Math.round((diff / (86400000f))>=1?(diff / (86400000f)):0);
                    if (dayDiff > 0) {
                        days = dayDiff;
                        if(days==30)
                            days=29;
                        setDifferenceString(days + (days == 1 ? d : ds) + a);
                    } else {
                        int hourDiff = Math.round((diff / (3600000f))>=1?(diff / (3600000f)):0);
                        if (hourDiff > 0) {
                            hours = hourDiff;
                            setDifferenceString( hours + (hours == 1 ? h : hs) + a);
                        } else {
                            int minuteDiff = Math.round((diff / (60000f))>=1?(diff / (60000f)):0);
                            if (minuteDiff > 0) {
                                minutes = minuteDiff;
                                setDifferenceString(minutes + (minutes == 1 ? mm : mms) + a);
                            } else {
                                int secondDiff =Math.round((diff / (1000f))>=1?(diff / (1000f)):0);
                                if (secondDiff > 0)
                                    seconds = secondDiff;
                                else
                                    seconds = 1;
                                setDifferenceString(seconds + (seconds == 1 ? s : ss) + a);
                            }
                        }
                    }

                }
            }

        }

    }
    public String getDifferenceString() {
        return differenceString;
    }

    public void setDifferenceString(String differenceString) {
        this.differenceString = differenceString;
    }
    public int getYears() {
        return years;
    }

    public void setYears(int years) {
        this.years = years;
    }

    public int getMonths() {
        return months;
    }

    public void setMonths(int months) {
        this.months = months;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

}