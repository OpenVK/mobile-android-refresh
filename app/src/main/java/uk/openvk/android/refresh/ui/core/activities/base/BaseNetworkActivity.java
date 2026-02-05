package uk.openvk.android.refresh.ui.core.activities.base;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.kieronquinn.monetcompat.app.MonetCompatActivity;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import uk.openvk.android.refresh.BuildConfig;
import uk.openvk.android.refresh.OvkApplication;
import uk.openvk.android.refresh.api.OpenVKAPI;
import uk.openvk.android.refresh.api.enumerations.HandlerMessages;
import uk.openvk.android.refresh.api.interfaces.OvkAPIListeners;
import uk.openvk.android.refresh.receivers.OvkAPIReceiver;

public class BaseNetworkActivity extends MonetCompatActivity {
   public OpenVKAPI ovk_api;
   public SharedPreferences global_prefs;
   public SharedPreferences instance_prefs;
   public SharedPreferences.Editor global_prefs_editor;
   public SharedPreferences.Editor instance_prefs_editor;
   public Handler handler;
   private OvkAPIReceiver receiver;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      global_prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
      instance_prefs = ((OvkApplication) getApplicationContext()).getAccountPreferences();
      global_prefs_editor = global_prefs.edit();
      instance_prefs_editor = instance_prefs.edit();
      handler = new Handler(Looper.myLooper());
      ovk_api = new OpenVKAPI(this, global_prefs, instance_prefs, handler);
      OvkAPIListeners apiListeners = new OvkAPIListeners();
      setAPIListeners(apiListeners);
      registerAPIDataReceiver();
   }

   public void registerAPIDataReceiver() {
      receiver = new OvkAPIReceiver(this);
      LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(
              "uk.openvk.android.refresh.API_DATA_RECEIVE"));
   }

   private void setAPIListeners(final OvkAPIListeners listeners) {
      listeners.from = getLocalClassName();
      listeners.successListener = (ctx, msg_code, data) -> {
         if(!BuildConfig.BUILD_TYPE.equals("release"))
            Log.d(OvkApplication.APP_TAG,
                    String.format(
                            "Handling API message %s in %s (%s)",
                            msg_code,
                            getLocalClassName(),
                            data.getString("address")
                    )
            );
         if(msg_code == HandlerMessages.PARSE_JSON) {
            new Thread(() -> {
               Intent intent = new Intent();
               intent.setAction("uk.openvk.android.refresh.API_DATA_RECEIVE");
               intent.putExtras(data);
               LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
            }).start();
         } else {
            receiveState(msg_code, data);
         }
      };
      listeners.failListener = (ctx, msg_code, data) -> {
         if(!BuildConfig.BUILD_TYPE.equals("release"))
            Log.d(OvkApplication.APP_TAG,
                    String.format(
                            "Handling API message %s in %s",
                            msg_code,
                            getLocalClassName()
                    )
            );
         receiveState(msg_code, data);
      };
      listeners.processListener = (ctx, data, value, length) -> {
         if(!BuildConfig.BUILD_TYPE.equals("release"))
            Log.d(OvkApplication.APP_TAG,
                    String.format(
                            "Handling API message %s in %s",
                            HandlerMessages.UPLOAD_PROGRESS,
                            getLocalClassName()
                    )
            );
         receiveState(HandlerMessages.UPLOAD_PROGRESS, data);
      };
      ovk_api.wrapper.setAPIListeners(listeners);
      ovk_api.dlman.setAPIListeners(listeners);
      ovk_api.ulman.setAPIListeners(listeners);
   }

   public void receiveState(int message, Bundle data) {
   }

   @Override
   protected void onDestroy() {
      LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
      super.onDestroy();
   }
}
