package nl.defacto.notitieapp;

import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class ComposeActivity extends Activity implements RestClient {
	private DropboxHelper mDbHelper;
	private EditText mTitle;
	private EditText mBody;
	private boolean update = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
		
		mTitle = (EditText) findViewById(R.id.note_title);
		mBody = (EditText) findViewById(R.id.note_body);
		
		mDbHelper = new DropboxHelper(this);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			update = true;
			loadNote(extras.getString("note", ""));
			
			mTitle.setEnabled(false);
			mTitle.setCursorVisible(false);
			mTitle.setKeyListener(null);
			mTitle.setBackgroundColor(Color.TRANSPARENT);
		} else {
//			mTitle.setOnFocusChangeListener(new OnFocusChangeListener() {
//				@Override
//				public void onFocusChange(View v, boolean hasFocus) {
//					try {
//						String title = mTitle.getText().toString();
//						if(!hasFocus && mDbHelper.noteExists(title)) {
//							Toast.makeText(
//									getApplicationContext(),
//									"Notitie \"" + title + "\" bestaat al.",
//									Toast.LENGTH_SHORT).show();
//						}
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
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
				try {
					saveNote();
				} catch (Exception e) {
					Toast.makeText(this, R.string.err_file_store, Toast.LENGTH_SHORT).show();
				}
				return true;
			case R.id.action_discard:
				discardNote();
				return true;
			default:
				return super.onOptionsItemSelected(item);				
		}
	}
	
	@Override
	public void handleResponse(Object response, int responseCode, int action) {
		if(responseCode != 200) {
			mDbHelper.handleError(responseCode);
			return;
		}
		
		switch(action) {
			case DropboxHelper.ACTION_LIST:
				String title = mTitle.getText().toString();		
				String body = mBody.getText().toString();
				
				try {
					if(DropboxHelper.noteExists(title, (JSONObject) response))
						confirmOverwrite(title, body);
					else
						mDbHelper.saveNote(title, body, false, this);
				} catch (JSONException e) {}
				break;
			case DropboxHelper.ACTION_UPDATE:
				setResult(RESULT_OK);
				finish();
				break;
			case DropboxHelper.ACTION_CREATE:
				setResult(RESULT_OK);
				finish();
				break;
			case DropboxHelper.ACTION_READ:
				mBody.setText((String) response);
				break;
		}
	}
	
	private void loadNote(String note) {
		try {
			mDbHelper = new DropboxHelper(this);
			mTitle.setText(note);
			mDbHelper.loadNote(note, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveNote() throws MalformedURLException {
		String title = mTitle.getText().toString();		
		String body = mBody.getText().toString();
		
		if(update) {
			mDbHelper.saveNote(title, body, true, this);
		} else {
			// List the existing notes to determine in handleResponse() whether a note with `title` exists.
			mDbHelper.fetchNotes(this);
		}
	}
	
	private void discardNote() {
		setResult(RESULT_CANCELED);
        finish();
	}
	
	private void confirmOverwrite(final String title, final String body) {
		final RestClient client = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.msg_verify_overwrite);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mDbHelper.saveNote(title, body, true, client);
			}
		});
		
		builder.setNegativeButton(R.string.cancel, null);
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
