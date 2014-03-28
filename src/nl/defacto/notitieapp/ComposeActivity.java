package nl.defacto.notitieapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class ComposeActivity extends Activity {	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compose);
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
		setResult(RESULT_OK);
        finish();
	}
	
	private void discardNote() {
		setResult(RESULT_CANCELED);
        finish();
	}
}
