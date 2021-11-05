package com.example.android.sunshine;

import android.support.v7.preference.*;
import com.example.android.sunshine.data.*;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.example.android.sunshine.sync.SunshineSyncUtils;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{

	private void setPreferenceSummary(Preference preference, Object value)
	{

		String stringValue = value.toString();

		if (preference instanceof ListPreference)
		{

			ListPreference listPreference = (ListPreference) preference;
			int prefIndex = listPreference.findIndexOfValue(stringValue);

			if (prefIndex >= 0)
			{
				preference.setSummary(listPreference.getEntries()[prefIndex]);
			}
		}
		else
		{
			preference.setSummary(stringValue);
		}

	}

	@Override
	public void onCreatePreferences(Bundle savedInstaceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.pref_general);

		SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();

		PreferenceScreen prefScreen = getPreferenceScreen();

		int count = prefScreen.getPreferenceCount();

		for (int i=0; i < count; i++)
		{
			Preference p = prefScreen.getPreference(i);
			if (!(p instanceof CheckBoxPreference))
			{
				String value = sharedPreferences.getString(p.getKey(), "");
				setPreferenceSummary(p, value);
			}
		}
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreference, String key)
	{
		Activity activity = getActivity();
		
		if(key.equals(getString(R.string.pref_location_key))){
			SunshinePreferences.resetLocationCoordinates(activity);
			SunshineSyncUtils.startImmediateSync(activity);
		}
		else if(key.equals(getString(R.string.pref_units_key))){
			activity.getContentResolver().notifyChange(WeatherContract.WeatherEntry.CONTENT_URI,null);
		}
		
		Preference preference = findPreference(key);
		if (null != preference)
		{
			if (!(preference instanceof CheckBoxPreference))
			{
				setPreferenceSummary(preference, sharedPreference.getString(key, ""));
			}
		}
	}

	@Override
	public void onStart()
	{
		super.onStart();
		getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	

}
