package nl.defacto.notitieapp;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFileInfo;

public class NoteListActivity extends ListActivity {
	static final int ACTIVITY_CREATE = 0;
	static final int ACTIVITY_VIEW = 1;
	private DropboxHelper mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
		
		try {
			mDbHelper = new DropboxHelper(getApplicationContext(), this);
		} catch (Unauthorized e) {
			e.printStackTrace();
		}
		
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
			List<String> notes = new ArrayList<String>();
			for(DbxFileInfo file : mDbHelper.fetchNotes()) {
				notes.add(file.path.getName());
			}
			
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.note_row, notes);
			setListAdapter(adapter);
		} catch (Exception e) {
			// TODO: show exception.
		}
	}
}
