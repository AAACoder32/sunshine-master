package com.example.android.sunshine.utilities;

import com.example.android.sunshine.data.WeatherContract;
import android.content.Context;
import com.example.android.sunshine.R;
import android.net.Uri;
import android.database.Cursor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.content.Intent;
import com.example.android.sunshine.DetailActivity;
import android.support.v4.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.app.NotificationManager;

public class NotificationUtils {

	public static final String[] NOTIFICATION_WEATHER_PROJECTION ={
		WeatherContract.WeatherEntry._ID,
		WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
		WeatherContract.WeatherEntry.COLUMN_MIN_TEMP
	};

	public static final int INDEX_WEATHER_ID = 0;
	public static final int INDEX_WEATHER_MAX_TEMP = 1;
	public static final int INDEX_WEATHER_MIN_TEMP = 2;

	public static final int WEATHER_NOTIFICATION_ID = 577;


	public static final void notifyUserToNewWeather(Context context) {

		Uri todayWeatherUri = WeatherContract.WeatherEntry.buildWeatherUriWithDate(
			SunshineDateUtils.normalizeDate(System.currentTimeMillis()));

		Cursor todayWeatherCursor = context.getContentResolver().query(
			todayWeatherUri,
			NOTIFICATION_WEATHER_PROJECTION,
			null,
			null,
			null);

		if (todayWeatherCursor.moveToFirst()) {

			int weatherId = todayWeatherCursor.getInt(INDEX_WEATHER_ID);
			double high = todayWeatherCursor.getDouble(INDEX_WEATHER_MAX_TEMP);
			double low = todayWeatherCursor.getDouble(INDEX_WEATHER_MIN_TEMP);

			Resources resources = context.getResources();

			int largeArtResourceId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

			Bitmap largeIcon = BitmapFactory.decodeResource(resources, largeArtResourceId);

			int smallArtResourceId = SunshineWeatherUtils.getSmallArtResourceIdForWeatherCondition(weatherId);

			String notificationTitle = context.getString(R.string.app_name);

			String notificationText = getNotificationText(context, weatherId, high, low);

			NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
				.setColor(ContextCompat.getColor(context, R.color.colorPrimary))
				.setSmallIcon(smallArtResourceId)
				.setLargeIcon(largeIcon)
				.setContentTitle(notificationTitle)
				.setContentText(notificationText)
				.setAutoCancel(true);


			Intent detailIntentForToday = new Intent(context, DetailActivity.class);
			detailIntentForToday.setData(todayWeatherUri);

			TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
			taskStackBuilder.addNextIntentWithParentStack(detailIntentForToday);

			PendingIntent resultPendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

			notificationBuilder.setContentIntent(resultPendingIntent);

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			notificationManager.notify(WEATHER_NOTIFICATION_ID, notificationBuilder.build());

		}

	}

	private static String getNotificationText(Context context, int weatherId,
											  double high, double low) {

		String shortDescription = SunshineWeatherUtils.getStringForWeatherCondition(context, weatherId);
		String notificationFormat = context.getString(R.string.format_notification);

		String notificationText = String.format(notificationFormat,
												shortDescription,
												SunshineWeatherUtils.formatTemperature(context, high),
												SunshineWeatherUtils.formatTemperature(context, low));
		return notificationText;
	}

}
