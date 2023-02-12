package uk.openvk.android.refresh.longpoll_api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import uk.openvk.android.refresh.BuildConfig;
import uk.openvk.android.refresh.longpoll_api.wrappers.LongPollWrapper;
import uk.openvk.android.refresh.api.wrappers.OvkAPIWrapper;

public class LongPollService extends Service {
    private String lp_server;
    private String key;
    private int ts;
    private OvkAPIWrapper ovk_api;
    private LongPollWrapper lpW;
    private Context ctx;
    private String access_token;
    private boolean use_https = false;

    public class LongPollBinder extends Binder {
        public LongPollService getService() {
            return LongPollService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("OpenVK", "Creating LongPoll Service...");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("OpenVK", String.format("Getting LPS start ID: %d", startId));
        Bundle data = intent.getExtras();
        if (data != null) {
            access_token = data.getString("access_token");
        }
        return flags;
    }

    public void run(Context ctx, String instance, String lp_server, String key, int ts, boolean use_https) {
        this.ctx = ctx;
        this.use_https = use_https;
        if(lpW == null) {
            lpW = new LongPollWrapper(ctx, use_https);
        }
        ovk_api = new OvkAPIWrapper(ctx);
        ovk_api.setServer(instance);
        ovk_api.setAccessToken(access_token);
        if(BuildConfig.BUILD_TYPE.equals("release")) ovk_api.log(false);
        runLongPull(lp_server, key, ts, use_https);
    }

    private void runLongPull(String lp_server, String key, int ts, boolean use_https) {
        if(BuildConfig.BUILD_TYPE.equals("release")) lpW.log(false);
        lpW.updateCounters(ovk_api);
        lpW.keepUptime(ovk_api);
        if(lp_server != null && key != null) {
            lpW.longPoll(lp_server, key, ts);
        }
    }

    private final IBinder myBinder = new LongPollBinder();

    @Override
    public IBinder onBind(Intent intent) {
        Log.d("OpenVK", "Service ONBIND");
        return mBinder;
    }

    private final IBinder mBinder = new LongPollBinder();

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("OpenVK Legacy", "Stopping LongPoll Service...");
    }

    public void setProxyConnection(boolean useProxy, String proxy_address) {
        if(lpW == null) {
            lpW = new LongPollWrapper(ctx, use_https);
        }
        lpW.setProxyConnection(useProxy, proxy_address);
    }
}
