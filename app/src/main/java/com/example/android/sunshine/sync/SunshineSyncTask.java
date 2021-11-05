package com.example.android.sunshine.sync;

import android.content.Context;
import java.net.URL;
import com.example.android.sunshine.utilities.NetworkUtils;
import android.content.ContentValues;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import android.content.ContentResolver;
import com.example.android.sunshine.data.WeatherContract;
import com.example.android.sunshine.data.SunshinePreferences;
import android.text.format.DateUtils;
import com.example.android.sunshine.utilities.NotificationUtils;


public class SunshineSyncTask {

	synchronized public static void syncWeather(Context context) {

		try {

			URL weatherRequestUrl = NetworkUtils.getUrl(context);
			String jsonWeatherResponse = NetworkUtils.getResponseFromHttpUrl(weatherRequestUrl);

			ContentValues[] weatherValues = OpenWeatherJsonUtils.getWeatherContentValuesDataFromJson(context, jsonWeatherResponse);

			if (weatherValues != null && weatherValues.length != 0) {

				ContentResolver sunshineContentResolver = context.getContentResolver();

				sunshineContentResolver.delete(
					WeatherContract.WeatherEntry.CONTENT_URI,
					null,
					null);

				sunshineContentResolver.bulkInsert(
					WeatherContract.WeatherEntry.CONTENT_URI,
					weatherValues);

				boolean notificationEnabled = SunshinePreferences.areNotificationEnable(context);

				long timeSinceLastNotification = SunshinePreferences.getEllapsedTimeSinceLastNotification(context);

				boolean oneDayPassedSinceLastNotification = false;

				if (timeSinceLastNotification >= DateUtils.DAY_IN_MILLIS) {
					oneDayPassedSinceLastNotification = true;
				}

				if (notificationEnabled && oneDayPassedSinceLastNotification) {
					NotificationUtils.notifyUserToNewWeather(context);
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
