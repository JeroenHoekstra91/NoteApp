package nl.defacto.notitieapp;


public interface RestClient {
	public void handleResponse(Object response, int responseCode, int action);
}
