package nl.defacto.notitieapp;

import java.util.UUID;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class OAuthActivity extends Activity {
	private static final String OAUTH_URL = "https://www.dropbox.com/1/oauth2/authorize";
	
	private static final String KEY = "2cavkxlkgqtngx1";
	private static final String SECRET = "sp1cy7i81pudjaw";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final String csrf = generate_csrf();
		String url = OAUTH_URL + 
				"?client_id=" + KEY +
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
					
					SharedPreferences preferences = getSharedPreferences("oauth", MODE_PRIVATE);
					SharedPreferences.Editor editor = preferences.edit();
					
					if(state.equals(csrf)) {
						editor.putString("access_token", access_token);
						editor.putString("userid", uid);
						editor.commit();
						
						setResult(RESULT_OK);
						finish();
					} else {
						setResult(RESULT_CANCELED);
						finish();
					}
				} else if(url.contains("error=access_denied")) {
					setResult(RESULT_CANCELED);
					finish();
				}
			}
		});	
	}
	
	private String generate_csrf() {
		return UUID.randomUUID().toString();
	}
}
