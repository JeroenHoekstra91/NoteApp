package nl.defacto.notitieapp;

import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class ViewNoteActivity extends Activity {
	private DropboxHelper mDbHelper;
	private TextView mBody;
	private String note;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_note);
		mBody = (TextView) findViewById(R.id.note_view);
		
		Intent intent = getIntent();
		note = intent.getStringExtra("note");
		
		getActionBar().setTitle(note);
		
		try {
			mDbHelper = new DropboxHelper(getApplicationContext(), this);
			mBody.setText(mDbHelper.loadNote(note));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_note, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.action_remove:
				removeNote();
				return true;
			case R.id.action_back:
				goBack();
				return true;
			default:
				return super.onOptionsItemSelected(item);				
		}
	}
	
	private void removeNote() {
		try {
			mDbHelper.deleteNote(note);
			setResult(RESULT_OK);
			finish();
		} catch (Exception e) {
			// TODO: show error.
		}
	}
	
	private void goBack() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
