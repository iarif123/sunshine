package com.example.android.sunshine.app;

import android.text.format.Time;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by Shams on 1/30/16.
 */
public class WeatherDataParser {

    public static double getMaxTemperatureForDay(String weatherJsonStr,int dayIndex) throws JSONException{
        JSONObject jsonObject = new JSONObject(weatherJsonStr);
        JSONArray days = jsonObject.getJSONArray("list");
        JSONObject dayInfo = days.getJSONObject(dayIndex);
        JSONObject temperatureInfo = dayInfo.getJSONObject("temp");
        return temperatureInfo.getDouble("max");
    }
}
