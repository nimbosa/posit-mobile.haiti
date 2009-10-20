/*******************************************************************************
 * Copyright (c) 2009 .
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v2.1
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Prasanna Gautam - initial API and implementation
 *     Ralph Morelli - Supervisor
 *     Trishan deLanerolle - Director
 *     Antonio Alcorn - Summer 2009 Intern
 *     Gong Chen - Summer 2009 Intern
 *     Chris Fei - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 ******************************************************************************/
package org.hfoss.posit;

import org.hfoss.posit.web.SyncThread;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * @author rmorelli
 *
 */
public class PositMain extends Activity {
    private static final String TAG = "PositMain";	
    private ProgressDialog pd;
    private static final int confirm_exit=0;
	/**
	 * Called with the application is first created. 
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
 //     PreferenceManager.setDefaultValues(this, R.xml.posit_preferences, true);
        setContentView(R.layout.main);
        if(savedInstanceState==null)
        	checkPhoneRegistrationAndInitialSync();
    }
    
    
    @Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("notFirst", true);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		setContentView(R.layout.main);
	}


	@Override
	protected void onResume() {
		super.onResume();
		setContentView(R.layout.main);
	}
    
	@Override
	protected void onPause(){
		super.onPause();
	}	



	/**
     * The phone is registered if it has an APP_KEY that matches one of
     * the projects on the server specified in the phone's preferences. 
     * Shared preferences are also checked to see whether the phone should
     * sync up with the server.
     */
    private void checkPhoneRegistrationAndInitialSync() {
       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
       
       String AUTH_KEY = sp.getString("AUTHKEY", null);
       
        if (AUTH_KEY == null || AUTH_KEY.equals("")||AUTH_KEY.equals(null)){
        	Intent intent = new Intent(this, ServerRegistrationActivity.class);
        	startActivity(intent);
        }
        boolean syncIsOn = sp.getBoolean("SYNC_ON_OFF", true);
        if (syncIsOn)
            syncFinds();
    }


	/**
	 * syncFinds creates the SyncThread and starts the synchronization 
	 *  process.
	 */
	private void syncFinds() {
		pd = ProgressDialog.show(this, "Synchronizing", "Please wait.", true,false);
		Thread syncThread = new SyncThread(this,handler);
		syncThread.start();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.positmain_menu, menu);
		return true;
	}

	/* When hit the back key, there should be a confirmation dialog pop-out 
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		Intent intent;
		switch(item.getItemId()) {
			case R.id.new_find_menu_item:
				intent = new Intent(this,FindActivity.class);
				intent.setAction(Intent.ACTION_INSERT);
				startActivity(intent);
				FindActivity.SAVE_CHECK=true;
				break;
			case R.id.list_find_menu_item:
				intent = new Intent(this,ListFindsActivity.class);
				startActivity(intent);
				FindActivity.SAVE_CHECK=false;
				break;
			case R.id.settings_menu_item:
				intent = new Intent(this, SettingsActivity.class);
				startActivity(intent);
				break;
			case R.id.about_menu_item:
				intent = new Intent(this,AboutActivity.class);
				startActivity(intent);
				break;
		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

	if(keyCode==KeyEvent.KEYCODE_BACK){
		showDialog(confirm_exit);
		return true;
	  }
	return super.onKeyDown(keyCode, event);
	}
	
	   @Override
		protected void onStop(){
			super.onStop();
		}

	   @Override
	    protected Dialog onCreateDialog(int id) {
	        switch (id) {
	        case confirm_exit:
	        	return new AlertDialog.Builder(this)
	            .setIcon(R.drawable.alert_dialog_icon)
	            .setTitle(R.string.exit)
	            .setPositiveButton(R.string.alert_dialog_ok, 
	            new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	                    // User clicked OK so do some stuff 
	                            finish();
	                }
	            })
	            
	            .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int whichButton) {
	            		setContentView(R.layout.main);
	                    /* User clicked Cancel so do nothing */
	                }
	            })
	            .create();
	        default:
	        	return null;
	        }
	   }
	   
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		Intent svc = new Intent(this, SyncService.class);
//		stopService(svc);
	}

    
	/**
	 * The handler object dismisses the SyncThread when it
	 *  receives a message that the thread has completed.
	 */
	private Handler handler = new Handler() {

		/* (non-Javadoc)
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			if (msg.what==SyncThread.DONE) {
				pd.dismiss();
			}
		}
	};
	
}
