package nl.defacto.notitieapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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
			
			Intent intent = getIntent();
			Bundle extras = intent.getExtras();
			if (extras != null) {
				loadNote(extras.getString("note", ""));
			}
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
	
	private void loadNote(String note) {
		try {
			mDbHelper = new DropboxHelper(getApplicationContext(), this);
			mTitle.setText(note);
			mBody.setText(mDbHelper.loadNote(note));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveNote() {
		String title = mTitle.getText().toString();
		
		mTitle.setEnabled(false);
		mTitle.setInputType(InputType.TYPE_NULL);
		
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
