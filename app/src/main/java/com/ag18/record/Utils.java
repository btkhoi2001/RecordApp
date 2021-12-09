package com.ag18.record;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Utils {

    static public String getTime(long duration) {
        Date now = new Date();

        long seconds = TimeUnit.MILLISECONDS.toSeconds(now.getTime() - duration);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - duration);
        long hours = TimeUnit.MILLISECONDS.toHours(now.getTime() - duration);
        long days = TimeUnit.MILLISECONDS.toDays(now.getTime() - duration);

        if(seconds < 60){
            return "just now";
        } else if (minutes == 1) {
            return "a minute ago";
        } else if (minutes > 1 && minutes < 60) {
            return minutes + " minutes ago";
        } else if (hours == 1) {
            return "an hour ago";
        } else if (hours > 1 && hours < 24) {
            return hours + " hours ago";
        } else if (days == 1) {
            return "a day ago";
        } else {
            return days + " days ago";
        }
    }

    static public String millisecondsToTimer(long milliSeconds) {
        String timerString = "";
        String secondsString;

        int hours = (int)(milliSeconds / (1000 * 60 * 60));
        int minutes = (int)(milliSeconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int)((milliSeconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0)
            timerString = hours + ":";

        if (seconds < 10)
            secondsString = "0" + seconds;
        else
            secondsString = "" + seconds;

        timerString = timerString + minutes + ":" + secondsString;
        return timerString;
    }
}
