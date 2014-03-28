package nl.defacto.notitieapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class NoteListActivity extends ListActivity {
	static final int ACTIVITY_CREATE = 0;	
	private DropboxHelper mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		mDbHelper = new DropboxHelper(getApplicationContext(), this);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.list, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.add_note:
				openCompose();
				return true;
			default:
				return super.onOptionsItemSelected(item);				
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == DropboxHelper.LINK_DB) {
			if(resultCode == RESULT_OK) {
				updateList();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	private void openCompose() {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, ACTIVITY_CREATE);
	}
	
	private void updateList() {
		// TODO: fetch file list from Dropbox.
	}
}
