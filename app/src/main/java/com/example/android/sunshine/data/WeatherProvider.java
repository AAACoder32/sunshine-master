package com.example.android.sunshine.data;

import android.content.*;
import android.net.*;
import android.database.*;
import android.database.sqlite.*;
import com.example.android.sunshine.data.WeatherContract.WeatherEntry;
import com.example.android.sunshine.utilities.*;

public class WeatherProvider extends ContentProvider
{
	public static final int CODE_WEATHER = 100;
	public static final int CODE_WEATHER_WITH_DATE = 101;

	private WeatherDbHelper mDbHelper;

	private static final UriMatcher sUriMatcher = buildUriMatcher();

	public static UriMatcher buildUriMatcher()
	{

		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = WeatherContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, WeatherContract.PATH_WEATHER, CODE_WEATHER);
		matcher.addURI(authority, WeatherContract.PATH_WEATHER + "/#", CODE_WEATHER_WITH_DATE);

		return matcher;
	}

	@Override
	public boolean onCreate()
	{
		mDbHelper = new WeatherDbHelper(getContext());
		return true;
	}

	@Override
	public int bulkInsert(Uri uri, ContentValues[] values)
	{
		final SQLiteDatabase db = mDbHelper.getWritableDatabase();

		switch (sUriMatcher.match(uri))
		{

			case CODE_WEATHER:
				db.beginTransaction();
				int rowsInserted = 0;
				try
				{
					for (ContentValues value:values)
					{

						long weatherDate = value.getAsLong(WeatherEntry.COLUMN_DATE);
						if (!SunshineDateUtils.isDateNormalized(weatherDate))
						{
							throw new IllegalArgumentException("This date must be normalised");
						}

						long _id = db.insert(WeatherEntry.TABLE_NAME, null, value);
						if (_id != -1)
						{
							rowsInserted++;
						}
					}
					db.setTransactionSuccessful();
				}
				finally
				{
					db.endTransaction();
				}
				if (rowsInserted > 0)
				{
					getContext().getContentResolver().notifyChange(uri, null);
				}

				return rowsInserted;
			default:
				return super.bulkInsert(uri, values);

		}

	}


	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
	{

		Cursor cursor;

		switch (sUriMatcher.match(uri))
		{

			case CODE_WEATHER_WITH_DATE:

				String normalizedUtcDateString = uri.getLastPathSegment();
				String[] selectionArguments = new String[]{normalizedUtcDateString};

				cursor = mDbHelper.getReadableDatabase()
					.query(
					WeatherContract.WeatherEntry.TABLE_NAME,
					projection,
					WeatherContract.WeatherEntry.COLUMN_DATE + "=?",
					selectionArguments,
					null,
					null,
					sortOrder
				);
				break;
			case CODE_WEATHER:
				cursor = mDbHelper.getReadableDatabase()
					.query(
					WeatherContract.WeatherEntry.TABLE_NAME,
					projection,
					selection,
					selectionArgs,
					null,
					null,
					sortOrder
				);
			    break;
			default:
				throw new UnsupportedOperationException("Unknown uri " + uri);
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri)
	{
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values)
	{
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs)
	{
		int numOfRowsDeleted;
		if (null == selection) selection = "1";
		switch (sUriMatcher.match(uri))
		{
			case CODE_WEATHER:
				numOfRowsDeleted = mDbHelper.getWritableDatabase()
					.delete(WeatherEntry.TABLE_NAME, selection, selectionArgs);
				break;
			default:
			    throw new UnsupportedOperationException("Unknown uri " + uri);
		}
		if (numOfRowsDeleted != 0)
		{
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return numOfRowsDeleted;
	}

	@Override
	public int update(Uri uri, ContentValues values, String p3, String[] p4)
	{
		
		return 0;
	}

}
