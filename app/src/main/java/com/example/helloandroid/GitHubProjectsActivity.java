package com.example.helloandroid;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GitHubProjectsActivity extends AppCompatActivity {

    private static final String TAG = "HelloAndroid: GitHubProjectsActivity";
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);


        Log.d(TAG, "onCreate: started.");

        listView = findViewById(R.id.listView);
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            ReposListAdapter adapter = new ReposListAdapter(this, bundle.getStringArrayList("reposList"));
            listView.setAdapter(adapter);
        }

    }

    class ReposListAdapter extends ArrayAdapter<String> {

        Context context;
        ArrayList<String> projectNames;

        ReposListAdapter(Context c, ArrayList<String> projectNames) {
            super(c, R.layout.layout_reposlistrow, R.id.project_name, projectNames);
            this.context = c;
            this.projectNames = projectNames;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            @SuppressLint("ViewHolder") View reposListRow = layoutInflater.inflate(R.layout.layout_reposlistrow, parent, false);
            TextView projectName = reposListRow.findViewById(R.id.project_name);

            // now set our resources on views
            projectName.setText(projectNames.get(position));
            return reposListRow;
        }
    }
}
