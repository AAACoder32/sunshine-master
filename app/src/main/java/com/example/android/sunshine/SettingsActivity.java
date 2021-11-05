package com.example.android.sunshine;

import android.support.v7.app.*;
import android.os.*;
import android.view.*;

public class SettingsActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();
		
		if(id == android.R.id.home){
			onBackPressed();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
}
