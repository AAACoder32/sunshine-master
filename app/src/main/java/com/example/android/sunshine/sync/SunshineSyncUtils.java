package com.example.android.sunshine.sync;

import android.annotation.NonNull;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import com.example.android.sunshine.data.WeatherContract;
import android.database.Cursor;
import java.util.concurrent.TimeUnit;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

public class SunshineSyncUtils {


	private static final int SYNC_INTERVAL_TIME = 3;
	private static final int SYNC_INTERVAL_SECONDS = (int) (TimeUnit.HOURS.toSeconds(SYNC_INTERVAL_TIME));
	private static final int SYNC_FLEXTIME_SECONDS = SYNC_INTERVAL_SECONDS / 3;

	private static final String SUNSHINE_SYNC_TAG = "sunshine_sync";

	private static boolean sInitialized;

	static void scheduleFirebaseJobDispatcherSync(@NonNull final Context contex) {


		Driver driver = new GooglePlayDriver(contex);

		FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

		Job syncSunshineJob = dispatcher.newJobBuilder()
			.setTag(SUNSHINE_SYNC_TAG)
			.setService(SunshineFirebaseJobService.class)
			.setConstraints(Constraint.ON_ANY_NETWORK)
			.setLifetime(Lifetime.FOREVER)
			.setRecurring(true)
			.setTrigger(Trigger.executionWindow(
							SYNC_INTERVAL_SECONDS,
							SYNC_FLEXTIME_SECONDS + SYNC_INTERVAL_SECONDS
						))
			.setReplaceCurrent(true)
			.build();

		dispatcher.schedule(syncSunshineJob);

	}

	synchronized public static void initialize(@NonNull final Context context) {

		if (sInitialized) return;

		sInitialized = true;

		scheduleFirebaseJobDispatcherSync(context);

		Thread checkForEmpty = new Thread(
			new Runnable(){

				@Override
				public void run() {
					Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;

					String[] projectionColumns = {WeatherContract.WeatherEntry._ID};
					String selectionStatement = WeatherContract.WeatherEntry.getSqlSelectForTodayOnward();

					Cursor cursor = context
						.getContentResolver().query(
						forecastQueryUri,
						projectionColumns,
						selectionStatement,
						null,
						null);

					if (null == cursor || cursor.getCount() == 0) {
						startImmediateSync(context);
					}
					cursor.close();
				}
			}
		);
		
		checkForEmpty.start();
		
	}

	public static void startImmediateSync(@NonNull final Context contex) {

		Intent intentToStartSyncImmediately =
			new Intent(contex, SunshineSyncIntentService.class);

		contex.startService(intentToStartSyncImmediately);

	}
}
