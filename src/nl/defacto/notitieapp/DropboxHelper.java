package nl.defacto.notitieapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.dropbox.sync.android.DbxPath.InvalidPathException;

public class DropboxHelper {
	public static final int LINK_DB = 0;
	
	private static final String KEY = "2cavkxlkgqtngx1";
	private static final String SECRET = "sp1cy7i81pudjaw";
	
	private String accessToken;
	private Activity activity;
	private DbxAccountManager mDbxAcctMgr;
	
	public DropboxHelper(Activity activity) {
		this.activity = activity;
		link();
	}
	
	public void link() {
		SharedPreferences preferences = activity.getSharedPreferences("oauth", Activity.MODE_PRIVATE);
		accessToken = preferences.getString("access_token", null);	
		
		if(accessToken == null) {
			Intent intent = new Intent(activity, OAuthActivity.class);
			intent.putExtra("key", KEY);
			
			activity.startActivityForResult(intent, LINK_DB);
		}
	}
	
	public void fetchNotes(RestClient client) throws MalformedURLException {
		if(accessToken != null) {
			RestApiCall apiCall = new RestApiCall("https://api.dropbox.com",
					"/1/metadata/sandbox/", RestApiCall.GET, client);
			
			apiCall.addParameter("list", "true");
			apiCall.execute();
		}
	}
	
	public void saveNote(String title, String body) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.create(new DbxPath(title + ".md"));
		
		try {
		    testFile.writeString(body);
		} finally {
		    testFile.close();
		}
	}
	
	public void updateNote(String title, String body) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.open(new DbxPath(title + ".md"));
		
		try {
		    testFile.writeString(body);
		} finally {
		    testFile.close();
		}
	}
	
	public String loadNote(String note) throws InvalidPathException, IOException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		DbxFile testFile = dbxFs.open(new DbxPath(note + ".md"));
		String content = null;
		
		try {
		    content = testFile.readString();
		} finally {
		    testFile.close();
		}
		
		return content;
	}
	
	public void deleteNote(String note) throws InvalidPathException, DbxException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		dbxFs.delete(new DbxPath(note + ".md"));
	}
	
	public boolean noteExists(String note) throws InvalidPathException, DbxException {
		DbxFileSystem dbxFs = DbxFileSystem.forAccount(mDbxAcctMgr.getLinkedAccount());
		return dbxFs.exists(new DbxPath(note + ".md"));
	}
	
	
	public void clearToken() {
		accessToken = null;
		
		SharedPreferences preferences = activity.getSharedPreferences("oauth", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("access_token");
		editor.commit();
		
		RestApiCall apiCall = new RestApiCall("https://api.dropbox.com",
				"/1/disable_access_token", RestApiCall.POST, null);
		apiCall.execute();
	}
	
	public void resetToken() {
		clearToken();
		link();
	}
	
	public void handleError(int responseCode) {
		switch(responseCode) {
			case 400: // Bad input parameter
				Toast.makeText(activity, R.string.err_dbx_400, Toast.LENGTH_LONG).show();
				break;
			case 401: // Bad or expired token
				resetToken();
				Toast.makeText(activity, R.string.err_dbx_401, Toast.LENGTH_LONG).show();
				break;
			case 403: // Bad OAuth request
				clearToken();
				Toast.makeText(activity, R.string.err_dbx_403, Toast.LENGTH_LONG).show();
				break;
			case 404: // File or folder not found
				Toast.makeText(activity, R.string.err_dbx_404, Toast.LENGTH_LONG).show();
				break;
			case 405: // Request method not expected
				Toast.makeText(activity, R.string.err_dbx_405, Toast.LENGTH_LONG).show();
				break;
			case 429: // Your app is making too many requests and is being rate limited
				Toast.makeText(activity, R.string.err_dbx_429, Toast.LENGTH_LONG).show();
				break;
			case 503: // Transient server error. retry request
				Toast.makeText(activity, R.string.err_dbx_503, Toast.LENGTH_LONG).show();
				break;
			case 507: // User is over Dropbox storage quota
				Toast.makeText(activity, R.string.err_dbx_507, Toast.LENGTH_LONG).show();
				break;
			default: // Unknown error
				Toast.makeText(activity, R.string.err_dbx_unknown, Toast.LENGTH_LONG).show();
				break;
		}
	}
	
	private class RestApiCall extends AsyncTask<Void, Void, String> {
		public static final String GET = "GET";
		public static final String POST = "POST";
		
		private String host;
		private String path;
		private Map<String, String> params;
		private String verb;
		private int responseCode = -1;
		
		private RestClient client;
		
		public RestApiCall(String host, String path, String verb, RestClient client) {
			this.host = host;
			this.path = path;
			this.verb = verb;
			this.client = client;
			this.params = new HashMap<String, String>();
		}

		@Override
		protected String doInBackground(Void... arg0) {
			return request();
		}
		
		@Override
		protected void onPostExecute(String result) {
			if(client == null)
				return;
			
			try {
				client.handleResponse(new JSONObject(result), responseCode);
			} catch (JSONException e) {
				client.handleResponse(null, responseCode);
			}
		}
		
		public void addParameter(String key, String value) {
			params.put(key, value);
		}
		
		private String request() {
			HttpsURLConnection connection = null;
			StringBuilder response = new StringBuilder();
			
			try {
				connection = (HttpsURLConnection) getUrl().openConnection();
				connection.setRequestMethod(verb);
				connection.setRequestProperty("Authorization", "Bearer " + accessToken);
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String line;
				
				while((line = reader.readLine()) != null) {
					response.append(line);
				}
			} catch(IOException ioe) {
				response = null;
			} finally {
				if(connection != null) {
					connection.disconnect();
					try {responseCode = connection.getResponseCode();} catch (IOException e) {}
				}
			}
			
			if(response == null)
					return null;
			return response.toString();
		}
		
		private URL getUrl() throws MalformedURLException {
			String url = host + path + "?";
			
			for(String key : params.keySet()) {
				
				url += key + "=" + params.get(key) + "&";
			}
			
			return new URL(url.substring(0, url.length() -1));
		}
	}
}
