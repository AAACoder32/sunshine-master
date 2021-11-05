package com.example.android.sunshine.data;

import android.content.Context;
import android.content.*;
import android.support.v7.preference.*;

public class SunshinePreferences {
    /*
     * In order to uniquely pinpoint the location on the map when we launch the
     * map intent, we store the latitude and longitude.
     */
    public static final String PREF_COORD_LAT = "coord_lat";
    public static final String PREF_COORD_LONG = "coord_long";

    /**
     * Helper method to handle setting location details in Preferences (City Name, Latitude,
     * Longitude)
     *
     * @param c        Context used to get the SharedPreferences
     * @param cityName A human-readable city name, e.g "Mountain View"
     * @param lat      The latitude of the city
     * @param lon      The longitude of the city
     */
    static public void setLocationDetails(Context context, double lat, double lon) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(PREF_COORD_LAT, Double.doubleToRawLongBits(lat));
		editor.putLong(PREF_COORD_LONG, Double.doubleToRawLongBits(lon));
		editor.apply();
    }

    /**
     * Resets the stored location coordinates.
     *
     * @param c Context used to get the SharedPreferences
     */
    static public void resetLocationCoordinates(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();
		editor.remove(PREF_COORD_LAT);
		editor.remove(PREF_COORD_LONG);
		editor.apply();
    }

    /**
     * Returns the location currently set in Preferences. The default location this method
     * will return is "94043,USA", which is Mountain View, California. Mountain View is the
     * home of the headquarters of the Googleplex!
     *
     * @param context Context used to get the SharedPreferences
     * @return Location The current user has set in SharedPreferences. Will default to
     * "94043,USA" if SharedPreferences have not been implemented yet.
     */
    public static String getPreferredWeatherLocation(Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String keyForLoaction = context.getString(R.string.pref_location_key);
		String defaultLocation = context.getString(R.string.pref_location_default);

		return prefs.getString(keyForLoaction, defaultLocation);

    }

    /**
     * Returns true if the user has selected metric temperature display.
     *
     * @param context Context used to get the SharedPreferences
     * @return true If metric display should be used
     */
    public static boolean isMetric(Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		String keyForUnits = context.getString(R.string.pref_units_key);
		String defaultUnits = context.getString(R.string.pref_units_metric);

		String preferredUnits = prefs.getString(keyForUnits, defaultUnits);
		String metric = context.getString(R.string.pref_units_metric);

		boolean userPreferenceMetric;

		if (metric.equals(preferredUnits)) {
			userPreferenceMetric = true;
		} else {
			userPreferenceMetric = false;
		}

        return userPreferenceMetric;
    }

    /**
     * Returns the location coordinates associated with the location.  Note that these coordinates
     * may not be set, which results in (0,0) being returned. (conveniently, 0,0 is in the middle
     * of the ocean off the west coast of Africa)
     *
     * @param context Used to get the SharedPreferences
     * @return An array containing the two coordinate values.
     */
    public static double[] getLocationCoordinates(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

		double[] preferredCoordinates = new double[2];

		preferredCoordinates[0] = Double
			.longBitsToDouble(sp.getLong(PREF_COORD_LAT, Double.doubleToRawLongBits(0.0)));

		preferredCoordinates[1] = Double
			.longBitsToDouble(sp.getLong(PREF_COORD_LONG, Double.doubleToRawLongBits(0.0)));

		return preferredCoordinates;

    }

    /**
     * Returns true if the latitude and longitude values are available. The latitude and
     * longitude will not be available until the lesson where the PlacePicker API is taught.
     *
     * @param context used to get the SharedPreferences
     * @return true if lat/long are set
     */
    public static boolean isLocationLatLonAvailable(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

		boolean spContainLatitude = sp.contains(PREF_COORD_LAT);
		boolean spContainLongitude = sp.contains(PREF_COORD_LONG);

		boolean spContainBothLatitudeAndLongitude = false;

		if (spContainLatitude && spContainLongitude) {
			spContainBothLatitudeAndLongitude = true;
		}
        return spContainBothLatitudeAndLongitude;
    }

	public static boolean areNotificationEnable(Context context) {

		String displayNotificationKey = context.getString(R.string.pref_enable_notifications_key);

		boolean shouldDisplayNotificationByDefault = context.getResources()
			.getBoolean(R.bool.show_notification_by_default);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		boolean  shouldDisplayNotification =sp.getBoolean(displayNotificationKey, shouldDisplayNotificationByDefault);
		return shouldDisplayNotification;
		
	}
	
	public static long getLastNotificationTimeInMillis(Context context){
		
		String lastNotificationKey = context.getString(R.string.pref_last_notification);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		
		long lastNotificationTime = sp.getLong(lastNotificationKey,0);
		
		return lastNotificationTime;
	}
	
	
	public static long getEllapsedTimeSinceLastNotification(Context context){
		
		long lastNotificationTimeInMillis = getLastNotificationTimeInMillis(context);
		
		long timeSinceLastNotification = System.currentTimeMillis() - lastNotificationTimeInMillis;
		
		return timeSinceLastNotification;
		
	}

	public static void saveLastNotificationTime(Context context, long lastNotificationTime) {

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = sp.edit();

		String lastNotificationKey = context.getString(R.string.pref_last_notification);
		editor.putLong(lastNotificationKey, lastNotificationTime);
		editor.apply();
	}
}
