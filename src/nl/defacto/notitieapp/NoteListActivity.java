package nl.defacto.notitieapp;

import com.dropbox.sync.android.DbxAccountManager;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class NoteListActivity extends ListActivity {
	static final int ACTIVITY_CREATE = 0;
	static final int LINK_DB = 1;
	
	private DbxAccountManager mDbxAcctMgr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		mDbxAcctMgr = DbxAccountManager.getInstance(getApplicationContext(), "2cavkxlkgqtngx1", "sp1cy7i81pudjaw");
		
		if(!mDbxAcctMgr.hasLinkedAccount())
			mDbxAcctMgr.startLink((Activity)this, LINK_DB);
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
		if(requestCode == LINK_DB) {
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
