package com.willwhitney.jane;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

public class XmppClient extends Activity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        org.jivesoftware.smackx.ConfigureProviderManager.configureProviderManager();
//        org.jivesoftware.smackx.initStaticCode(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
}
