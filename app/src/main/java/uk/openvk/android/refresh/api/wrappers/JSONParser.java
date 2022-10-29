package uk.openvk.android.refresh.api.wrappers;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONParser {

    public JSONParser() {
    }

    public JSONObject parseJSON(String string) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(string);
        } catch (JSONException ex){
            ex.printStackTrace();
        }
        return jsonObject;
    }
}
