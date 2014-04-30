package nl.defacto.notitieapp;

import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class OAuthActivity extends Activity {
	private static final String OAUTH_URL = "https://www.dropbox.com/1/oauth2/authorize";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		String key = intent.getStringExtra("key");
		
		final String csrf = generateCsrf();
		String url = OAUTH_URL + 
				"?client_id=" + key +
				"&response_type=token" +
				"&redirect_uri=http://localhost" +
				"&state=" + csrf;
		
		setContentView(R.layout.auth_view);
		WebView webView = (WebView) findViewById(R.id.oauth);
				
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl(url);
		
		webView.setWebViewClient(new WebViewClient() {
			@Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
				super.onPageStarted(view, url, favicon);
            }
			
			@Override
			public void onPageFinished(WebView webView, String url) {
				super.onPageFinished(webView, url);
				
				if(url.contains("access_token")) {
					url = url.replace("#", "?");
					Uri uri = Uri.parse(url);
					
					String access_token = uri.getQueryParameter("access_token");
					String uid = uri.getQueryParameter("uid");
					String state = uri.getQueryParameter("state");
										
					if(state.equals(csrf)) {
						storeOauthInformation(access_token, uid);
						setResult(RESULT_OK);
						finish();
					} else {
						storeOauthInformation(null, null);
						setResult(RESULT_CANCELED);
						finish();
					}
				} else if(url.contains("error=access_denied")) {
					storeOauthInformation(null, null);
					setResult(RESULT_CANCELED);
					finish();
				}
			}
		});	
	}
	
	private void storeOauthInformation(String access_token, String uid) {
		SharedPreferences preferences = getSharedPreferences("oauth", MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		
		editor.putString("access_token", access_token);
		editor.putString("userid", uid);
		editor.commit();
	}
	
	private String generateCsrf() {
		return UUID.randomUUID().toString();
	}
}
