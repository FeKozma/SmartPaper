package com.fekozma.wallpaperchanger;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fekozma.wallpaperchanger.database.DBLog;

import java.time.LocalDateTime;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

public class CustomErrorActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		String stackTrace =  CustomActivityOnCrash.getStackTraceFromIntent(getIntent());

		DBLog.db.addLog(DBLog.LEVELS.ERROR, "APP CRASH\n" + stackTrace);

		setContentView(R.layout.activity_custom_error);

		final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

		if (config == null) {
			//This should never happen - Just finish the activity to avoid a recursive crash.
			finish();
			return;
		}

		CustomActivityOnCrash.restartApplication(CustomErrorActivity.this, config);
	}

}
