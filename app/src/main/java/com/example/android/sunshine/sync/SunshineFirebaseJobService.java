package com.example.android.sunshine.sync;

import com.firebase.jobdispatcher.JobService;
import com.firebase.jobdispatcher.JobParameters;
import android.os.AsyncTask;
import android.content.Context;

public class SunshineFirebaseJobService extends JobService {
	private AsyncTask<Void,Void,Void> mFetchWeatherTask;

	@Override
	public boolean onStartJob(final JobParameters jobParameters) {

		mFetchWeatherTask = new AsyncTask<Void,Void,Void>(){

			@Override
			protected Void doInBackground(Void[] p1) {

				Context context = getApplicationContext();
				SunshineSyncTask.syncWeather(context);
				return null;
			}

			@Override
			protected void onPostExecute(Void aVoid) {
				jobFinished(jobParameters, false);
			}

		};
		mFetchWeatherTask.execute();
		return true;
	}

	@Override
	public boolean onStopJob(JobParameters jobParameters) {

		if (mFetchWeatherTask != null) {
			mFetchWeatherTask.cancel(true);
		}
		return true;
	}

}
