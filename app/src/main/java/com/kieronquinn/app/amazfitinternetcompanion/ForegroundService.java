package com.kieronquinn.app.amazfitinternetcompanion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.huami.watch.transport.DataBundle;
import com.huami.watch.transport.TransportDataItem;
import com.kieronquinn.library.amazfitcommunication.Transporter;
import com.kieronquinn.library.amazfitcommunication.Utils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * Created by Kieron on 08/04/2018.
 */

public class ForegroundService extends Service {
    private Transporter transporter;

    @Override
    public void onCreate() {
        super.onCreate();
        int notificationId = 1;
        String channelId = "foreground_service";
        CharSequence name = getString(R.string.channel_name);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notif))
                .setSmallIcon(R.drawable.ic_stat_watch_internet)
                .setChannelId(channelId)
                .build();
        notificationManager.notify(notificationId, notification);
        startForeground(notificationId, notification);
        transporter = Transporter.get(getApplicationContext(), getPackageName());
        transporter.addDataListener(new Transporter.DataListener() {
            @Override
            public void onDataReceived(TransportDataItem item) {
                Log.d("AmazfitCompanion", "onDataReceived");
                if (item.getAction().equals("com.huami.watch.companion.transport.amazfitcommunication.HTTP_REQUEST")) {
                    //Never try if it's a watch (someone made an error)
                    if (Utils.isWatch()) return;
                    //Send pingback immediately to let the app know it's being handled
                    transporter.send("com.huami.watch.companion.transport.amazfitcommunication.HTTP_PINGBACK", item.getData());
                    //Get data
                    DataBundle dataBundle = item.getData();
                    try {
                        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(dataBundle.getString("url")).openConnection();
                        httpURLConnection.setInstanceFollowRedirects(dataBundle.getBoolean("followRedirects"));
                        httpURLConnection.setRequestMethod(dataBundle.getString("requestMethod"));
                        httpURLConnection.setUseCaches(dataBundle.getBoolean("useCaches"));
                        httpURLConnection.setDoInput(true);
                        httpURLConnection.setDoOutput(true);
                        try {
                            JSONArray headers = new JSONArray(dataBundle.getString("requestHeaders"));
                            for (int x = 0; x < headers.length(); x++) {
                                JSONObject header = headers.getJSONObject(x);
                                httpURLConnection.setRequestProperty(header.getString("key"), header.getString("value"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        httpURLConnection.connect();
                        if (httpURLConnection.getInputStream() != null) {
                            byte[] inputStream = IOUtils.toByteArray(httpURLConnection.getInputStream());
                            if (inputStream != null)
                                dataBundle.putByteArray("inputStream", inputStream);
                        }
                        if (httpURLConnection.getErrorStream() != null) {
                            byte[] errorStream = IOUtils.toByteArray(httpURLConnection.getErrorStream());
                            if (errorStream != null)
                                dataBundle.putByteArray("errorStream", errorStream);
                        }
                        dataBundle.putString("responseMessage", httpURLConnection.getResponseMessage());
                        dataBundle.putInt("responseCode", httpURLConnection.getResponseCode());
                        dataBundle.putString("responseHeaders", mapToJSON(httpURLConnection.getHeaderFields()).toString());
                        //Return the data
                        transporter.send("com.huami.watch.companion.transport.amazfitcommunication.HTTP_RESULT", dataBundle);
                        httpURLConnection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        transporter.connectTransportService();
    }

    private JSONArray mapToJSON(Map<String, List<String>> input) {
        JSONArray headers = new JSONArray();
        for (String key : input.keySet()) {
            JSONObject item = new JSONObject();
            try {
                item.put("key", key);
                List<String> items = input.get(key);
                JSONArray itemsArray = new JSONArray();
                for (String itemValue : items) {
                    itemsArray.put(itemValue);
                }
                item.put("value", itemsArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            headers.put(item);
        }
        return headers;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
