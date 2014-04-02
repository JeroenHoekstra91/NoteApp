package nl.defacto.notitieapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Weet je zeker dat je deze notitie wilt verwijderen?");

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				try {
					mDbHelper.deleteNote(note);
					setResult(RESULT_OK);
					finish();
				} catch (Exception e) {
				}
			}
		});
		
		builder.setNegativeButton(R.string.cancel, null);
		
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	private void goBack() {
		setResult(RESULT_CANCELED);
		finish();
	}
}
