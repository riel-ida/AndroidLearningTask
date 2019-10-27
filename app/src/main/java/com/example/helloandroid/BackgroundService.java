package com.example.helloandroid;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.helloandroid.GitResponse.IGetUsersCallback;
import com.example.helloandroid.GitResponse.User;

import java.util.ArrayList;
import java.util.List;

public class BackgroundService extends IntentService {

    private static final String TAG = "HelloAndroid: Service";

    ConnectivityManager cm;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String AVATARS = "avatars";
    private static final String NAMES = "names";
    private static final String EMAILS = "emails";

    public BackgroundService() {
        super("GitHub Service");
    }

    @Override
    public void onStart(@Nullable Intent intent, int startId) {
        super.onStart(intent, startId);
        cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm != null) {
            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            cm.registerNetworkCallback(networkRequest,
                    new WifiNetworkConnectivityCallback());
        }
    }

    //for BOOT_COMPLETED broadcast
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.v(TAG, "onHandleIntent.");

        GitHubAsyncRequest gitHubAsyncRequest = new GitHubAsyncRequest();
        gitHubAsyncRequest.getUsers(usersCallback);
    }

    //usersCall callback - save usersList in sharedPreferences
    IGetUsersCallback usersCallback = new IGetUsersCallback() {
        @Override
        public void onGetUsers(List<User> userList) {
            List<String> users = new ArrayList<>();
            List<String> emails = new ArrayList<>();
            List<String> avatars = new ArrayList<>();

            if (userList == null) throw new AssertionError();

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (User user : userList) {
                avatars.add(user.avatar_url);
                users.add(user.name);
                emails.add("Private email address");
            }

            editor.putString(AVATARS, String.join(",", avatars));
            editor.putString(NAMES, String.join(",", users));
            editor.putString(EMAILS, String.join(",", emails));

            editor.apply();
        }
    };

    private class WifiNetworkConnectivityCallback extends ConnectivityManager.NetworkCallback {

        @Override
        public void onAvailable(@NonNull Network network) {
/*            assert cm != null;
            boolean connected = cm.bindProcessToNetwork(network);
            if(connected) {*/
                Toast.makeText(getApplicationContext(), "Connected to Wi-Fi",
                        Toast.LENGTH_LONG).show();

                Log.i(TAG, "Has Connection.");

                GitHubAsyncRequest gitHubAsyncRequest = new GitHubAsyncRequest();
                gitHubAsyncRequest.getUsers(usersCallback);

        }

        @Override
        public void onLost(@NonNull Network network) {
            super.onLost(network);
            Toast.makeText(getApplicationContext(), "Disconnected from Wi-Fi",
                    Toast.LENGTH_LONG).show();
        }
    }
}
