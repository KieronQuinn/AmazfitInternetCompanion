package com.kieronquinn.app.amazfitinternetcompanion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Kieron on 15/04/2018.
 */

public class LaunchActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

}
