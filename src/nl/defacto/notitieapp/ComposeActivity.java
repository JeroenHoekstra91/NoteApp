package nl.defacto.notitieapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.Toast;

import com.dropbox.sync.android.DbxException.Unauthorized;

public class ComposeActivity extends Activity {
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
			mTitle.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					try {
						String title = mTitle.getText().toString();
						if(!hasFocus && mDbHelper.noteExists(title)) {
							Toast.makeText(
									getApplicationContext(),
									"Notitie \"" + title + "\" bestaat al.",
									Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
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
			mDbHelper = new DropboxHelper(this);
			mTitle.setText(note);
			mBody.setText(mDbHelper.loadNote(note));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void saveNote() {
		String title = mTitle.getText().toString();		
		String body = mBody.getText().toString();
		
		try {
			if(!update) {
				if(mDbHelper.noteExists(title)) {
					overrideNote(title, body);
					return;
				}else {
					mDbHelper.saveNote(title, body);
				}
			} else {
				mDbHelper.updateNote(title, body);
			}
			setResult(RESULT_OK);
			finish();
		} catch (Exception e) {
			setResult(RESULT_CANCELED);
			finish();
		}
	}
	
	private void discardNote() {
		setResult(RESULT_CANCELED);
        finish();
	}
	
	private void overrideNote(final String title, final String body) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Weet je zeker dat je deze notitie wilt overschrijven?");

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					mDbHelper.updateNote(title, body);
					setResult(RESULT_OK);
				} catch (Exception e) {
					setResult(RESULT_CANCELED);
				}
				finally {
					finish();
				}
			}
		});
		
		builder.setNegativeButton(R.string.cancel, null);
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
