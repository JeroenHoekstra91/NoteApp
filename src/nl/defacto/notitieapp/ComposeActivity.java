package nl.defacto.notitieapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.dropbox.sync.android.DbxException.Unauthorized;

public class ComposeActivity extends Activity {
	private DropboxHelper mDbHelper;
	private EditText mTitle;
	private EditText mBody;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		
		mTitle = (EditText) findViewById(R.id.note_title);
		mBody = (EditText) findViewById(R.id.note_body);
		
		try {
			mDbHelper = new DropboxHelper(getApplicationContext(), this);
		} catch (Unauthorized e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compose, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()){
			case R.id.action_save:
				saveNote();
				return true;
			case R.id.action_discard:
				discardNote();
				return true;
			default:
				return super.onOptionsItemSelected(item);				
		}
	}
	
	private void saveNote() {
		String title = mTitle.getText().toString();
		String body = mBody.getText().toString();
		
		try {
			mDbHelper.saveNote(title, body);
			setResult(RESULT_OK);
		} catch (Exception e) {
			setResult(RESULT_CANCELED);
		} finally {
			finish();
		}
	}
	
	private void discardNote() {
		setResult(RESULT_CANCELED);
        finish();
	}
}
