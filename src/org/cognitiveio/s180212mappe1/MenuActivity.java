package org.cognitiveio.s180212mappe1;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_menu);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

	}

	public void onClick (View v) {
		
		// when you click setting menu
        switch (v.getId()) {
	        case R.id.btnNewGame:
	        	Intent intent = new Intent(this, MainActivity.class);
	            startActivity(intent);
	            break;
	        case R.id.btnRules:
	        	startActivity(new Intent(this, RulesActivity.class));
	        	break;
	        case R.id.btnSettings:
	        	startActivity(new Intent(this, SetPreferencesActivity.class));
	        	break;
	        case R.id.btnExit:
	        	finish();
	        	break;
        }
	}

}
