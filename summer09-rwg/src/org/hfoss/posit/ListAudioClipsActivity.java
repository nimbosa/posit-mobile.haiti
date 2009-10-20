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
 *     Phil Fritzsche - Summer 2009 Intern
 *     James Jackson - Summer 2009 Intern
 *     Qianqian Lin - Summer 2009 Intern 
 *     Khanh Pham - Summer 2009 Intern
 ******************************************************************************/

package org.hfoss.posit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * Activity to list any audio clips associated with a specific find as a list adapter.
 * Clicking on any audio clip in the list causes it to be played back.
 * @author Phil Fritzsche
 */
public class ListAudioClipsActivity extends ListActivity implements ViewBinder,OnCreateContextMenuListener  {

	private static final String TAG = "ListAudioClipsActivity";

	private static final int CONFIRM_DELETE_DIALOG = 0;

	private MyDBHelper mDbHelper;
	private Cursor mCursor;  // Used for DB accesses

	/** 
	 * This method is invoked when the user clicks on the review audio button on the view
	 * find screen. It creates a DBHelper and calls fillData() to fetch data from the DB.
	 * @param savedInstanceState contains the Activity's previously frozen state.  In this case it is unused.
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	/** 
	 * This method is called when the activity is ready to start 
	 *  interacting with the user. It is at the top of the Activity
	 *  stack.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		fillData();
	}

	/**
	 * Called when the system is about to resume some other activity.
	 *  It can be used to save state, if necessary.  In this case
	 *  we close the cursor to the DB to prevent memory leaks.
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onPause(){
		super.onPause();
		stopManagingCursor(mCursor);
		mDbHelper.close(); // NOTE WELL: Can't close while managing cursor
		mCursor.close();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mDbHelper.close();
		mCursor.close();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mDbHelper.close();
		mCursor.close();
	}

	/**
	 * Puts the items from the DB table into the rows of the view. Note that
	 *  once you start managing a Cursor, you cannot close the DB without 
	 *  causing an error.
	 */
	private int fillData() {
		Log.i(TAG, "filldata: refilling the data");

		mDbHelper = new MyDBHelper(this);

		String[] columns = MyDBHelper.list_audio_row_data;
		int[] views = MyDBHelper.list_audio_row_views;

		mCursor = mDbHelper.getAudiosCursor((int) getIntent().getLongExtra("rowId", -1));		
		Log.i(TAG, "rowId from audios cursor " + getIntent().getLongExtra("rowId", -1));
		if (mCursor.getCount() == 0) { // No audio clips
			setContentView(R.layout.list_audio_clips);
			return mCursor.getCount();
		}
		startManagingCursor(mCursor); // NOTE: Can't close DB while managing cursor

		// CursorAdapter binds the data in 'columns' to the views in 'views' 
		// It repeatedly calls ViewBinder.setViewValue() (see below) for each column
		SimpleCursorAdapter adapter = 
			new SimpleCursorAdapter(this, R.layout.list_row_audio, mCursor, columns, views);
		adapter.setViewBinder(this);
		setListAdapter(adapter); 

        final ListView list = getListView();
        list.setFocusable(true);
        list.setOnCreateContextMenuListener(this);
		//registerForContextMenu(this.getListView());
		mDbHelper.close();

		Log.i(TAG, "filldata: refilled the data");
		return mCursor.getCount();
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * This method executes when the user clicks on one of the audios in the
	 *   list. It starts an intent to view the audio.
	 *   @param listView is the ListView that was clicked on 
	 *   @param view is the View within the ListView
	 *   @param position is the View's position in the ListView
	 *   @param id is the audio's RowID
	 */
	@Override
	protected void onListItemClick(ListView listView, View view, int position, long id) {
		super.onListItemClick(listView, view, position, id);
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		Log.i(TAG, "rowId from intent extra: " + getIntent().getLongExtra("rowId", -1));
		Uri uri = mDbHelper.getAudioUriByPosition(getIntent().getLongExtra("rowId", -1), position);
		Log.i(TAG, "uri = " + uri.toString());
		if (uri != null) {
			intent.setData(uri);
			startActivity(intent); 
		}
	}

	/**
	 * This method creates the menus for this activity.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.list_audio_clips_menu, menu);
		return true;
	}

	/** 
	 * This method is invoked when a menu item is selected. It starts
	 *   the appropriate Activity.
	 * @see android.app.Activity#onMenuItemSelected(int, android.view.MenuItem)
	 */
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		int numberOfAudiosLeft;
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();

		switch (item.getItemId()) {

		case R.id.delete_all_audio_clips:
			mDbHelper.close();
			showDialog(CONFIRM_DELETE_DIALOG);
			break;
		case R.id.cancel_delete_video_audio:
			Log.d(TAG, "NO AUDIO DELETED");
			return true;
		case R.id.confirm_delete_video_audio:
			MyDBHelper mDbHelper = new MyDBHelper(ListAudioClipsActivity.this);
			if (mDbHelper.deleteAudio(info.id)){
				mDbHelper.close();
				Utils.showToast(ListAudioClipsActivity.this, R.string.deleted_from_database);
			}
			else {
				mDbHelper.close();
				Utils.showToast(ListAudioClipsActivity.this, R.string.delete_failed);
			}
			numberOfAudiosLeft = fillData();
			if (numberOfAudiosLeft == 0){
				finish();
				Intent intent = new Intent(ListAudioClipsActivity.this, ListAudioClipsActivity.class);
				startActivity(intent);
			return true;
			}
		}
		return true;
	}

	/**
	 * Part of ViewBinder interface. Binds the Cursor column defined 
	 * by the specified index to the specified view. When binding is handled 
	 * by this ViewBinder, this method must return true. If this method returns false, 
	 * SimpleCursorAdapter will attempt to handle the binding on its own.
	 */
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		switch (view.getId()) {
		default:
			return false;
		}
	}

	/**
	 * This method is invoked by showDialog() when a dialog window is created. It displays
	 *  the appropriate dialog box, currently a dialog to confirm that the user wants to 
	 *  delete all the finds.
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {

		case CONFIRM_DELETE_DIALOG:
			return new AlertDialog.Builder(this)
			.setIcon(R.drawable.alert_dialog_icon)
			.setTitle(R.string.alert_dialog_audio)
			.setPositiveButton(R.string.alert_dialog_ok, 
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					MyDBHelper mDbHelper = new MyDBHelper(ListAudioClipsActivity.this);
					if (mDbHelper.deleteAudioClips(mCursor)) {
						mDbHelper.close();
						Utils.showToast(ListAudioClipsActivity.this, R.string.deleted_from_database);
					}
					else {
						mDbHelper.close();
						Utils.showToast(ListAudioClipsActivity.this, R.string.delete_failed);
						dialog.cancel();
					}
					finish();
					Intent intent = new Intent(ListAudioClipsActivity.this, ListAudioClipsActivity.class);
					startActivity(intent);
				}
			}).setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					/* User clicked Cancel so do nothing */
				}
			}).create();

		default:
			return null;

		} // switch
	}
	
	/**
	 * Called when an item in the audio list is clicked and held.
	 * Generating a menu by inflating the "individual_item_selected_menu.xml" file
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.individual_item_selected_menu, menu);
		
	}
}