package com.example.android.sunshine;

import android.content.*;
import android.view.*;
import com.example.android.sunshine.utilities.*;

import android.app.LoaderManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import android.widget.ImageView;

public class DetailActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<Cursor>
{

	private static final String FORECAST_SHARE_HASHTAG = "#SunShineApp";

	public static final String[] WEATHER_DETAIL_PROJECTION = {

		WeatherEntry.COLUMN_DATE,
		WeatherEntry.COLUMN_MAX_TEMP,
		WeatherEntry.COLUMN_MIN_TEMP,
		WeatherEntry.COLUMN_HUMIDITY,
		WeatherEntry.COLUMN_PRESSURE,
		WeatherEntry.COLUMN_WIND_SPEED,
		WeatherEntry.COLUMN_DEGREES,
		WeatherEntry.COLUMN_WEATHER_ID


	};

	public static final int INDEX_WEATHER_DATE = 0;
	public static final int INDEX_WEATHER_MAX_TEMP = 1;
	public static final int INDEX_WEATHER_MIN_TEMP = 2;
	public static final int INDEX_WEATHER_HUMIDITY = 3;
	public static final int INDEX_WEATHER_PRESSURE = 4;
	public static final int INDEX_WEATHER_WIND_SPEED = 5;
	public static final int INDEX_WEATHER_DEGREES = 6;
	public static final int INDEX_WEATHER_CONDITION_ID = 7;

	private static final int ID_DETAIL_LOADER = 353;
	
	private String mForecastSummary;
	
	private TextView date;
    private TextView weatherDescription;
    private TextView highTemperature;
    private TextView lowTemperature;
	
    private TextView humidity,humidityLabel;
	
    private TextView windMeasurement,windLabel;
    private TextView pressure,pressureLabel;
	
	private ImageView weatherIcon;
	
	
	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		initViews();
		
		mUri = getIntent().getData();
		
		if(mUri == null ) throw new NullPointerException("Uri for detail activity can not be null");
		
		getLoaderManager().initLoader(ID_DETAIL_LOADER,null,this);

	}
	
	private void initViews(){
		
		//Primary weather info's views
		
		date = findViewById(R.id.date);
        weatherDescription = findViewById(R.id.weather_description);
		weatherIcon = findViewById(R.id.weather_icon);
		highTemperature = findViewById(R.id.high_temperature);
        lowTemperature = findViewById(R.id.low_temperature);
		
		//Extra weather info's views
		
        humidity = findViewById(R.id.humidity);
		humidityLabel = findViewById(R.id.humidity_label);
        pressure = findViewById(R.id.pressure);
		pressureLabel = findViewById(R.id.pressure_label);
		windMeasurement = findViewById(R.id.wind_measurement);
		windLabel = findViewById(R.id.wind_label);
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.detail, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		int id = item.getItemId();

		if (id == R.id.action_settings)
		{
			Intent startSettingsActivity = new Intent(DetailActivity.this, SettingsActivity.class);
			startActivity(startSettingsActivity);
			return true;
		}
		else if(id == R.id.action_share){
			Intent startShareIntent = createShareaForecastIntent();
			startActivity(startShareIntent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	private Intent createShareaForecastIntent()
	{
		Intent shareIntent = ShareCompat.IntentBuilder.from(this)
			.setType("text/plain")
			.setText(mForecastSummary + FORECAST_SHARE_HASHTAG)
			.getIntent();
		shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
		return shareIntent;
	}
	
	
	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle)
	{
		
		switch(loaderId){
			
			case ID_DETAIL_LOADER:
				return new CursorLoader(
					this,
					mUri,
					WEATHER_DETAIL_PROJECTION,
					null,
					null,
					null
				);
			default:
				throw new RuntimeException("Loader not implemented "+loaderId);
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        /*
         * Before we bind the data to the UI that will display that data, we need to check the
         * cursor to make sure we have the results that we are expecting. In order to do that, we
         * check to make sure the cursor is not null and then we call moveToFirst on the cursor.
         * Although it may not seem obvious at first, moveToFirst will return true if it contains
         * a valid first row of data.
         *
         * If we have valid data, we want to continue on to bind that data to the UI. If we don't
         * have any data to bind, we just return from this method.
         */
        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }


        /****************
         * Weather Icon *
         ****************/
        /* Read weather condition ID from the cursor (ID provided by Open Weather Map) */
        int weatherId = data.getInt(INDEX_WEATHER_CONDITION_ID);
        /* Use our utility method to determine the resource ID for the proper art */
        int weatherImageId = SunshineWeatherUtils.getLargeArtResourceIdForWeatherCondition(weatherId);

        /* Set the resource ID on the icon to display the art */
        weatherIcon.setImageResource(weatherImageId);

        /****************
         * Weather Date *
         ****************/
        /*
         * Read the date from the cursor. It is important to note that the date from the cursor
         * is the same date from the weather SQL table. The date that is stored is a GMT
         * representation at midnight of the date when the weather information was loaded for.
         *
         * When displaying this date, one must add the GMT offset (in milliseconds) to acquire
         * the date representation for the local date in local time.
         * SunshineDateUtils#getFriendlyDateString takes care of this for us.
         */
        long localDateMidnightGmt = data.getLong(INDEX_WEATHER_DATE);
        String dateText = SunshineDateUtils.getFriendlyDateString(this, localDateMidnightGmt, true);

        date.setText(dateText);

        /***********************
         * Weather Description *
         ***********************/
        /* Use the weatherId to obtain the proper description */
        String description = SunshineWeatherUtils.getStringForWeatherCondition(this, weatherId);

        /* Create the accessibility (a11y) String from the weather description */
        String descriptionA11y = getString(R.string.a11y_forecast, description);

        /* Set the text and content description (for accessibility purposes) */
        weatherDescription.setText(description);
        weatherDescription.setContentDescription(descriptionA11y);

        /* Set the content description on the weather image (for accessibility purposes) */
        weatherIcon.setContentDescription(descriptionA11y);

        /**************************
         * High (max) temperature *
         **************************/
        /* Read high temperature from the cursor (in degrees celsius) */
        double highInCelsius = data.getDouble(INDEX_WEATHER_MAX_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String highString = SunshineWeatherUtils.formatTemperature(this, highInCelsius);

        /* Create the accessibility (a11y) String from the weather description */
        String highA11y = getString(R.string.a11y_high_temp, highString);

        /* Set the text and content description (for accessibility purposes) */
        highTemperature.setText(highString);
        highTemperature.setContentDescription(highA11y);

        /*************************
         * Low (min) temperature *
         *************************/
        /* Read low temperature from the cursor (in degrees celsius) */
        double lowInCelsius = data.getDouble(INDEX_WEATHER_MIN_TEMP);
        /*
         * If the user's preference for weather is fahrenheit, formatTemperature will convert
         * the temperature. This method will also append either °C or °F to the temperature
         * String.
         */
        String lowString = SunshineWeatherUtils.formatTemperature(this, lowInCelsius);

        String lowA11y = getString(R.string.a11y_low_temp, lowString);

        /* Set the text and content description (for accessibility purposes) */
        lowTemperature.setText(lowString);
        lowTemperature.setContentDescription(lowA11y);

        /************
         * Humidity *
         ************/
        /* Read humidity from the cursor */
        float humidityF = data.getFloat(INDEX_WEATHER_HUMIDITY);
        String humidityString = getString(R.string.format_humidity, humidityF);

        String humidityA11y = getString(R.string.a11y_humidity, humidityString);

        /* Set the text and content description (for accessibility purposes) */
        humidity.setText(humidityString);
        humidity.setContentDescription(humidityA11y);

        humidityLabel.setContentDescription(humidityA11y);

        /****************************
         * Wind speed and direction *
         ****************************/
        /* Read wind speed (in MPH) and direction (in compass degrees) from the cursor  */
        float windSpeed = data.getFloat(INDEX_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(INDEX_WEATHER_DEGREES);
        String windString = SunshineWeatherUtils.getFormattedWind(this, windSpeed, windDirection);

        String windA11y = getString(R.string.a11y_wind, windString);

        /* Set the text and content description (for accessibility purposes) */
        windMeasurement.setText(windString);
        windMeasurement.setContentDescription(windA11y);

        windLabel.setContentDescription(windA11y);

        /************
         * Pressure *
         ************/
        /* Read pressure from the cursor */
        float pressureF = data.getFloat(INDEX_WEATHER_PRESSURE);

        /*
         * Format the pressure text using string resources. The reason we directly access
         * resources using getString rather than using a method from SunshineWeatherUtils as
         * we have for other data displayed in this Activity is because there is no
         * additional logic that needs to be considered in order to properly display the
         * pressure.
         */
        String pressureString = getString(R.string.format_pressure, pressureF);

        String pressureA11y = getString(R.string.a11y_pressure, pressureString);

        /* Set the text and content description (for accessibility purposes) */
        pressure.setText(pressureString);
        pressure.setContentDescription(pressureA11y);

        pressureLabel.setContentDescription(pressureA11y);

        /* Store the forecast summary String in our forecast summary field to share later */
        mForecastSummary = String.format("%s - %s - %s/%s",
										 dateText, description, highString, lowString);
    }

	@Override
	public void onLoaderReset(Loader<Cursor> loader)
	{
		
	}
} 
