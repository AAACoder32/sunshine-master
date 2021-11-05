package com.example.android.sunshine;

import android.app.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.os.*;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.view.*;
import android.widget.*;
import com.example.android.sunshine.data.*;
import com.example.android.sunshine.data.WeatherContract.*;
import com.example.android.sunshine.utilities.*;
import com.example.android.sunshine.sync.SunshineSyncUtils;

public class MainActivity extends AppCompatActivity implements
ForecastAdapter.ForecastAdapterOnClickHandler,
LoaderManager.LoaderCallbacks<Cursor>
{
	
	public static final String[] MAIN_FORECAST_PROJECTION ={
		WeatherEntry.COLUMN_DATE,
		WeatherEntry.COLUMN_MAX_TEMP,
		WeatherEntry.COLUMN_MIN_TEMP,
		WeatherEntry.COLUMN_WEATHER_ID
	};

	public static final int INDEX_WEATHER_DATE = 0;
	public static final int INDEX_WEATHER_MAX_TEMP = 1;
	public static final int INDEX_WEATHER_MIN_TEMP = 2;
	public static final int INDEX_WEATHER_CONDITION_ID = 3;

	public ProgressBar mLoadingIndicator;

	private RecyclerView mRecyclerView;
	private ForecastAdapter mForecastAdapter;


	private static final int ID_FORECAST_LOADER = 44;
	
	private int mPosition = RecyclerView.NO_POSITION;


    @Override
    protected void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);


		mRecyclerView = findViewById(R.id.recyclerview_forecast);
		mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

		LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
		mRecyclerView.setLayoutManager(layoutManager);
		mForecastAdapter = new ForecastAdapter(this, this);

		mRecyclerView.setAdapter(mForecastAdapter);

		showLoading();

		getLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);
		
		SunshineSyncUtils.initialize(this);

    }


	private void openPreferredLocationInMap()
	{

		double[] coord = SunshinePreferences.getLocationCoordinates(this);
		String posLat = Double.toString(coord[0]);
		String posLon = Double.toString(coord[1]);

		Uri addressUri = Uri.parse("geo:" + posLat + "," + posLon);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(addressUri);
		if (intent.resolveActivity(getPackageManager()) != null)
		{
			startActivity(intent);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle)
	{
		switch (loaderId)
		{
			case ID_FORECAST_LOADER:
				Uri forecastQueryUri = WeatherEntry.CONTENT_URI;
				String sortOrder = WeatherEntry.COLUMN_DATE + " ASC";
				String selection = WeatherEntry.getSqlSelectForTodayOnward();

				return new CursorLoader(
					this,
					forecastQueryUri,
					MAIN_FORECAST_PROJECTION,
					selection,
					null,
					sortOrder
				);
			default:
				throw new RuntimeException("Loader not implemented "+loaderId);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data)
	{
		mForecastAdapter.swapCursor(data);
		
		if(mPosition == RecyclerView.NO_POSITION) mPosition = 0;
		
		mRecyclerView.smoothScrollToPosition(mPosition);
		
		if(data.getCount() != 0) showWeatherDataView();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> p1)
	{
		mForecastAdapter.swapCursor(null);
	}


	@Override
	public void onClick(long date)
	{
		Intent startDetailActivity = new Intent(this, DetailActivity.class);
		
		Uri uri = WeatherEntry.buildWeatherUriWithDate(date);
		startDetailActivity.setData(uri);
		startActivity(startDetailActivity);
	}

	private void showWeatherDataView()
	{
		mRecyclerView.setVisibility(View.VISIBLE);
		
		mLoadingIndicator.setVisibility(View.INVISIBLE);
	}

	private void showLoading()
	{
		mRecyclerView.setVisibility(View.INVISIBLE);
		mLoadingIndicator.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.forecast, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		if (id == R.id.action_map)
		{
			openPreferredLocationInMap();
			return true;
		}
		else if (id == R.id.action_settings)
		{
			Intent startSettingsActivity = new Intent(MainActivity.this, SettingsActivity.class);
			startActivity(startSettingsActivity);
		}
		return super.onOptionsItemSelected(item);
	}

}
