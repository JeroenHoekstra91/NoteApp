package nl.defacto.notitieapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class DropboxHelper {
	public static final int LINK_DB = 0;
	
	public static final int ACTION_LIST = 0;
	public static final int ACTION_CREATE = 1;
	public static final int ACTION_READ = 2;
	public static final int ACTION_UPDATE = 3;
	public static final int ACTION_DELETE = 4;
	
	private static final String KEY = "2cavkxlkgqtngx1";
	private static final String SECRET = "sp1cy7i81pudjaw";
	
	private String accessToken;
	private Activity activity;
	
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
					"/1/metadata/sandbox/", RestApiCall.GET, null, client, ACTION_LIST);
			
			apiCall.addParameter("list", "true");
			apiCall.execute();
		}
	}
	
	public void saveNote(String title, String body, boolean overwrite, RestClient client) {
		int action = ACTION_CREATE;
		if(overwrite)
			action = ACTION_UPDATE;
		
		if(accessToken != null) {
			RestApiCall apiCall = new RestApiCall("https://api-content.dropbox.com",
					"/1/files_put/sandbox/" + title + ".md", RestApiCall.PUT, body, client, action);
			
			apiCall.addParameter("overwrite", Boolean.toString(overwrite));
			apiCall.execute();
		}
	}
	
	public void loadNote(String note, RestClient client) {
		if(accessToken != null) {
			RestApiCall apiCall = new RestApiCall("https://api-content.dropbox.com",
					"/1/files/sandbox/" + note + ".md", RestApiCall.GET, null, client, ACTION_READ);
			
			apiCall.execute();
		}
	}
	
	public void deleteNote(String note, RestClient client) {
		if(accessToken != null) {
			RestApiCall apiCall = new RestApiCall("https://api.dropbox.com",
					"/1/fileops/delete", RestApiCall.POST, null, client, ACTION_DELETE);
						
			apiCall.addParameter("root", "sandbox");
			apiCall.addParameter("path", note + ".md");
			apiCall.execute();
		}
	}
	
	public static boolean noteExists(String note, JSONObject notes) throws JSONException {
		JSONArray files = notes.getJSONArray("contents");
		
		for(int i=0; i< files.length(); i++) {
			JSONObject file = files.getJSONObject(i);
			String fileName = file.getString("path");
			
			if(note.equals(fileName.split("\\.")[0].substring(1)))
				return true;
		}
		return false;
	}
	
	
	public void clearToken() {
		accessToken = null;
		
		SharedPreferences preferences = activity.getSharedPreferences("oauth", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.remove("access_token");
		editor.commit();
		
		RestApiCall apiCall = new RestApiCall("https://api.dropbox.com",
				"/1/disable_access_token", RestApiCall.POST, null, null, -1);
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
			case 411: // Missing Content-Length header
				Toast.makeText(activity, R.string.err_dbx_411, Toast.LENGTH_LONG).show();
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
		public static final String PUT = "PUT";
		
		private String host;
		private String path;
		private Map<String, String> params;
		private String verb;
		private String message;
		private int responseCode = -1;
		private int action;
		
		private RestClient client;
		private ProgressDialog dialog;
		
		public RestApiCall(String host, String path, String verb, String message, RestClient client, int action) {
			this.host = host;
			this.path = path;
			this.verb = verb;
			this.message = message;
			this.client = client;
			this.action = action;
			this.params = new HashMap<String, String>();
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Context context = (Context) client;
			
			dialog = new ProgressDialog(context,ProgressDialog.STYLE_SPINNER);
			dialog.setTitle(R.string.loading);
			dialog.setCancelable(false);
			dialog.show();
		}
		
		@Override
		protected String doInBackground(Void... arg0) {		
			return request();
		}
		
		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			
			if(client == null)
				return;
			
			Log.i("response", responseCode + ": " + result + "");
			
			try {
				client.handleResponse(new JSONObject(result), responseCode, action);
			} catch (JSONException e) {
				client.handleResponse(result, responseCode, action);
			} catch (NullPointerException npe) {
				client.handleResponse(result, responseCode, action);
			}
		}
		
		public void addParameter(String key, String value) {
			params.put(key, value);
		}
		
		private String request() {
			HttpsURLConnection connection = null;
			String response = null;
			try {
				connection = (HttpsURLConnection) getUrl().openConnection();
				connection.setRequestMethod(verb);
				connection.setRequestProperty("Authorization", "Bearer " + accessToken);
				
				if(message != null) {
					connection.setRequestProperty("Content-Length", message.length()+"");
					connection.setRequestProperty("Content-Type", "text/plain");
					sendMessage(message, connection);
				}
				
				response = readResponse(connection);
			} catch(IOException ioe) {
				Log.e("error", "", ioe);
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
				String line;
				try {
					while((line=reader.readLine()) != null) {
						Log.e("error", line);
					}
				} catch (IOException e) {}
			} finally {
				if(connection != null) {
					connection.disconnect();
					try {responseCode = connection.getResponseCode();} catch (IOException e) {}
				}
			}
			
			return response;
		}
		
		private URL getUrl() throws MalformedURLException {
			String url = host + path + "?";
			
			for(String key : params.keySet()) {
				url += key + "=" + params.get(key).replaceAll(" ", "%20") + "&";
			}
			
			url = url.substring(0, url.length() -1);
			Log.i("url", new URL(url).toString() + " " + accessToken);
			return new URL(url);
		}
		
		private void sendMessage(String message, HttpsURLConnection connection) throws IOException {
			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(message.toCharArray(), 0, message.length());
			writer.flush();
			writer.close();
		}
		
		private String readResponse(HttpsURLConnection connection) throws IOException {
			StringBuilder response = new StringBuilder();
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			
			while((line = reader.readLine()) != null) {
				response.append(line + "\r\n");
			}
			
			reader.close();
			return response.toString().substring(0, response.length() - 2);
		}
	}
}
