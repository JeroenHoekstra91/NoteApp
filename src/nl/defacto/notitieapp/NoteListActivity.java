package nl.defacto.notitieapp;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class NoteListActivity extends ListActivity {
	private static final int ACTIVITY_CREATE = 0;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
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
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		// TODO: update list on result.
	}
	
	private void openCompose() {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, ACTIVITY_CREATE);
	}
}
