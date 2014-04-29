package nl.defacto.notitieapp;

import org.json.JSONObject;

public interface RestClient {
	public void handleResponse(JSONObject response, int responseCode);
}
