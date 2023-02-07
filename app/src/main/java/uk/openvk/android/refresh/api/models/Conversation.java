package uk.openvk.android.refresh.api.models;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.droidparts.util.Strings;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import uk.openvk.android.refresh.api.wrappers.JSONParser;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;

public class Conversation {
    public String title;
    public long peer_id;
    public int online;
    public Bitmap avatar;
    public Bitmap lastMsgAvatar;
    public long lastMsgAuthorId;
    public String lastMsgText;
    public long lastMsgTime;
    public String avatar_url;
    private ArrayList<Message> history;
    private JSONParser jsonParser;

    public Conversation() {
        jsonParser = new JSONParser();
        history = new ArrayList<Message>();
    }

    public void getHistory(OvkAPIWrapper ovk, long peer_id) {
        this.peer_id = peer_id;
        ovk.sendAPIMethod("Messages.getHistory", String.format("peer_id=%d&count=150&rev=1", peer_id));
    }

    @SuppressLint("SimpleDateFormat")
    public ArrayList<Message> parseHistory(Context ctx, String response) {
        JSONObject json = jsonParser.parseJSON(response);
        if(json != null) {
            try {
                JSONArray items = json.getJSONObject("response").getJSONArray("items");
                history = new ArrayList<Message>();
                JSONObject prevItem = null;
                for(int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    boolean incoming = false;
                    int type = 0;
                    if(item.getInt("out") == 1) {
                        incoming = false;
                        type = 1;
                    } else {
                        incoming = true;
                        type = 0;
                    }
                    if(prevItem != null) {
                        Date startOfDay = new Date(TimeUnit.SECONDS.toMillis(item.getLong("date")));
                        Calendar startOfDay_calendar = Calendar.getInstance();
                        startOfDay_calendar.setTime(startOfDay);
                        startOfDay_calendar.set(Calendar.HOUR_OF_DAY, 0);
                        startOfDay_calendar.set(Calendar.MINUTE, 0);
                        startOfDay_calendar.set(Calendar.SECOND, 0);
                        startOfDay = startOfDay_calendar.getTime();
                        Date prev_startOfDay = null;
                        Calendar prevStartOfDay_calendar = Calendar.getInstance();
                        if (i > 1) {
                            prevStartOfDay_calendar.setTime(new Date(TimeUnit.SECONDS.toMillis(prevItem.getLong("date"))));
                            prevStartOfDay_calendar.set(Calendar.HOUR_OF_DAY, 0);
                            prevStartOfDay_calendar.set(Calendar.MINUTE, 0);
                            prevStartOfDay_calendar.set(Calendar.SECOND, 0);
                            prev_startOfDay = prevStartOfDay_calendar.getTime();
                        }
                        if(prev_startOfDay != null) {
                            Log.d("compare", String.format("%s", startOfDay.compareTo(prev_startOfDay)));
                            if (startOfDay.compareTo(prev_startOfDay) > 0) {
                                history.add(new Message(2, 0, false, false, item.getLong("date"), new SimpleDateFormat("d MMMM yyyy").format(startOfDay), ctx));
                            }
                        } else {
                            history.add(new Message(2, 0, false, false, item.getLong("date"), new SimpleDateFormat("d MMMM yyyy").format(startOfDay), ctx));
                        }
                    } else {
                        history.add(new Message(2, 0, false, false, item.getLong("date"), new SimpleDateFormat("d MMMM yyyy").format(
                                new Date(TimeUnit.SECONDS.toMillis(item.getLong("date")))), ctx));
                    }
                    Message message = new Message(type, item.getLong("id"), incoming, false, item.getLong("date"), item.getString("text"), ctx);
                    message.timestamp = new SimpleDateFormat("HH:mm").format(TimeUnit.SECONDS.toMillis(item.getLong("date")));
                    message.author_id = item.getLong("from_id");
                    prevItem = item;
                    history.add(message);
                }
            } catch(JSONException ex) {
                ex.printStackTrace();
            }
        }
        return history;
    }

    public void sendMessage(OvkAPIWrapper ovk, String text) {
        ovk.sendAPIMethod("Messages.send", String.format("peer_id=%s&message=%s", peer_id, Strings.urlEncode(text)));
    }
}
