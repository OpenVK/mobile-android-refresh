package uk.openvk.android.refresh.api;

import org.json.JSONException;
import org.json.JSONObject;

import uk.openvk.android.refresh.api.wrappers.JSONParser;

public class Authorization {
    private String access_token;
    private String response;
    private JSONParser jsonParser;

    public Authorization(String response) {
        this.response = response;
        jsonParser = new JSONParser();
        JSONObject json = jsonParser.parseJSON(response);
        if (json != null) {
            try {
                this.access_token = json.getString("access_token");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getAccessToken() {
        return access_token;
    }
}
