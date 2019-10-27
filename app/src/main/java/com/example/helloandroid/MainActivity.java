package com.example.helloandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.helloandroid.GitResponse.IGetReposCallback;
import com.example.helloandroid.GitResponse.IGetUsersCallback;
import com.example.helloandroid.GitResponse.Repo;
import com.example.helloandroid.GitResponse.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "HelloAndroid: MainActivity";

    SwipeRefreshLayout pullToRefresh;

    ListView listView;
    List<String> users;
    List<String> emails;
    List<String> avatars;

    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String AVATARS = "avatars";
    private static final String NAMES = "names";
    private static final String EMAILS = "emails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate: started.");

        pullToRefresh = findViewById(R.id.pulldowntorefresh);

        Intent intent = new Intent(this, BackgroundService.class);
        startService(intent);

        //displays GitHub's users list from the stored list in sharedPreferences
        loadData();
        listView = findViewById(R.id.listView);
        final UsersListAdapter adapter = new UsersListAdapter(this, users, emails, avatars);
        listView.setAdapter(adapter);

        pullToRefresh.setOnRefreshListener((new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GitHubAsyncRequest gitHubAsyncRequest = new GitHubAsyncRequest();
                gitHubAsyncRequest.getUsers(usersCallback);
                adapter.notifyDataSetChanged();
                pullToRefresh.setRefreshing(false);
            }
        }));

        //short click - generates user's projects list
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: short click on: " + listView.getItemAtPosition(position).toString());

                Toast.makeText(MainActivity.this, "Projects List of: " + listView.getItemAtPosition(position).toString(),
                        Toast.LENGTH_LONG).show();

                GitHubAsyncRequest gitHubAsyncRequest = new GitHubAsyncRequest();
                gitHubAsyncRequest.getRepos(reposCallback, listView.getItemAtPosition(position).toString());

            }
        });

        //long click - opens a default email application in order to send an email to this user
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick: long click on: " + listView.getItemAtPosition(position).toString());

                Toast.makeText(MainActivity.this, "Default email application",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle this
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{listView.getItemAtPosition(position).toString() + "@email.privateaddress"});
                intent.putExtra(Intent.EXTRA_SUBJECT, "GitHub User - " + listView.getItemAtPosition(position).toString());
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
                return false;
            }
        });
    }//end of onCreate

    IGetUsersCallback usersCallback = new IGetUsersCallback() {
        @Override
        public void onGetUsers(List<User> userList) {
            users = new ArrayList<>();
            emails = new ArrayList<>();
            avatars = new ArrayList<>();

            if (userList == null) throw new AssertionError();

            SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            for (User user : userList) {
                users.add(user.name);
                emails.add("Private email address");
                if (user.avatar_url != null)
                    avatars.add(user.avatar_url);
            }

            Log.d(TAG, "initUsersListView: started.");
            listView = findViewById(R.id.listView);
            UsersListAdapter adapter = new UsersListAdapter(getApplicationContext(), users, emails, avatars);
            listView.setAdapter(adapter);

            editor.putString(AVATARS, String.join(",", avatars));
            editor.putString(NAMES, String.join(",", users));
            editor.putString(EMAILS, String.join(",", emails));

            editor.apply();
        }
    };

    IGetReposCallback reposCallback = new IGetReposCallback() {
        @Override
        public void onGetRepos(List<Repo> repoList) {
            ArrayList<String> projectNames = new ArrayList<>();
            for (Repo repo : repoList) {
                projectNames.add(repo.projectName);
            }

            Intent intent = new Intent(MainActivity.this, GitHubProjectsActivity.class);
            intent.putExtra("reposList", projectNames);
            startActivity(intent);
        }
    };


    //load GitHub's users list from the stored list in sharedPreferences
    public void loadData() {
        users = new ArrayList<>();
        emails = new ArrayList<>();
        avatars = new ArrayList<>();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        String[] str2 = (sharedPreferences.getString(NAMES, "List is empty")).split(",");
        users.addAll(Arrays.asList(str2));
        String[] str3 = (sharedPreferences.getString(EMAILS, "")).split(",");
        emails.addAll(Arrays.asList(str3));
        String[] str1 = (sharedPreferences.getString(AVATARS, "")).split(",");
        avatars.addAll(Arrays.asList(str1));

    }


    class UsersListAdapter extends ArrayAdapter<String> {

        Context context;
        List<String> userNames;
        List<String> emails;
        List<String> avatars;

        UsersListAdapter(Context c, List<String> userN, List<String> emails, List<String> avatars) {
            super(c, R.layout.layout_userslistrow, R.id.user_name, userN);
            this.context = c;
            this.userNames = userN;
            this.emails = emails;
            this.avatars = avatars;

        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert layoutInflater != null;
            @SuppressLint("ViewHolder") View usersListRow = layoutInflater.inflate(R.layout.layout_userslistrow, parent, false);
            CircleImageView avatar = usersListRow.findViewById(R.id.avatar);
            TextView userName = usersListRow.findViewById(R.id.user_name);
            TextView email = usersListRow.findViewById(R.id.email_add);

            // now set our resources on views
            Glide.with(context)
                    .asBitmap()
                    .load(avatars.get(position))
                    .into(avatar);
            userName.setText(userNames.get(position));
            email.setText(emails.get(position));

            return usersListRow;
        }
    }
}
