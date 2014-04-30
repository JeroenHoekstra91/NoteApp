package nl.defacto.notitieapp;

import org.json.JSONException;
import org.json.JSONObject;

import us.feras.mdv.MarkdownView;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ViewNoteActivity extends Activity implements RestClient {
	private static int ACTIVITY_EDIT = 0;
	
	private DropboxHelper mDbHelper;
	private MarkdownView mBody;
	private String note;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_note);
		mBody = (MarkdownView) findViewById(R.id.note_view);
		
		Intent intent = getIntent();
		note = intent.getStringExtra("note");
		
		loadNote();
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
			case R.id.action_edit:
				editNote();
				return true;
			default:
				return super.onOptionsItemSelected(item);				
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == ACTIVITY_EDIT) {
			if(resultCode == RESULT_OK) {
				loadNote();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
	public void handleResponse(Object response, int responseCode, int action) {
		if(responseCode != 200) {
			mDbHelper.handleError(responseCode);
			return;
		}
		
		if(action == DropboxHelper.ACTION_READ) {
			mBody.loadMarkDownData((String) response);
		} else {
			try {
				JSONObject result = (JSONObject) response;
				if(result.getBoolean("is_deleted")) {
					setResult(RESULT_OK);
					finish();
				}
			} catch (JSONException e) {	}
		}
	}
	
	private void loadNote() {
		getActionBar().setTitle(note);
		
		try {
			mDbHelper = new DropboxHelper(this);
			mDbHelper.loadNote(note, this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void removeNote() {
		final RestClient client = this;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.msg_confirm_delete);

		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mDbHelper.deleteNote(note, client);
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
	
	private void editNote() {
		Intent intent = new Intent(this, ComposeActivity.class);
		intent.putExtra("note", note);
		
		startActivityForResult(intent, ACTIVITY_EDIT);
	}
}
