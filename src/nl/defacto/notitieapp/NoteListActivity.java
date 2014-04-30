package nl.defacto.notitieapp;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class NoteListActivity extends ListActivity implements RestClient {
	static final int ACTIVITY_CREATE = 0;
	static final int ACTIVITY_VIEW = 1;
	
	private DropboxHelper mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		mDbHelper = new DropboxHelper(this);
		updateList();
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
		if(requestCode == DropboxHelper.LINK_DB || requestCode == ACTIVITY_CREATE || requestCode == ACTIVITY_VIEW) {
			if(resultCode == RESULT_OK) {
				updateList();
			}
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        
        TextView view = (TextView) l.getChildAt(position);
        try {
        	viewNote(view.getText().toString());
		} catch (Exception e) {
		}
    }
	
	private void viewNote(String note) {
		Intent intent = new Intent(this, ViewNoteActivity.class);
		intent.putExtra("note", note);
		startActivityForResult(intent, ACTIVITY_VIEW);
	}
	
	private void openCompose() {
		Intent intent = new Intent(this, ComposeActivity.class);
		startActivityForResult(intent, ACTIVITY_CREATE);
	}
	
	private void updateList() {
		try {
			mDbHelper.fetchNotes(this);
		} catch (MalformedURLException e) {
			Toast.makeText(this, R.string.err_file_list, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void handleResponse(Object response, int responseCode, int action) {
		if(responseCode != 200) mDbHelper.handleError(responseCode);
		if(response == null) return;
		
		JSONObject result = (JSONObject) response;
		
		try {
			List<String> notes = new ArrayList<String>();
			JSONArray files = result.getJSONArray("contents");
			
			for(int i=0; i< files.length(); i++) {
				JSONObject file = files.getJSONObject(i);
				String fileName = file.getString("path");
				notes.add(fileName.split("\\.")[0].substring(1));
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.note_row, notes);
			setListAdapter(adapter);
		} catch (Exception e) {}
	}
}
