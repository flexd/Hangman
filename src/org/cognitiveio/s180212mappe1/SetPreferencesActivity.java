package org.cognitiveio.s180212mappe1;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;


public class SetPreferencesActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
	}
}
