package com.example.helloandroid;

import android.os.AsyncTask;
import android.util.Log;

import com.example.helloandroid.GitResponse.IGetReposCallback;
import com.example.helloandroid.GitResponse.IGetUsersCallback;
import com.example.helloandroid.GitResponse.Repo;
import com.example.helloandroid.GitResponse.User;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class GitHubAsyncRequest {

    void getUsers(IGetUsersCallback usersCallback) {
        GetUsersAsyncTask usersAsyncTask = new GetUsersAsyncTask(usersCallback);
        usersAsyncTask.execute();
    }


    void getRepos(IGetReposCallback reposCallback, String userName) {
        GetProjectsAsyncTask projectsAsyncTask = new GetProjectsAsyncTask(reposCallback);
        projectsAsyncTask.execute(userName);
    }


    private static class GetUsersAsyncTask extends AsyncTask<Void, Void, List<User>> {

        private static final String TAG = "HelloAndroid: GetUsersAsyncTask";

        private IGetUsersCallback usersCallback;

        public GetUsersAsyncTask(IGetUsersCallback usersCallback) {
            this.usersCallback = usersCallback;
        }

        @Override
        protected List<User> doInBackground(Void... voids) {
            String baseUrl = "https://api.github.com/";

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            APIInterface apiInterface = retrofit.create(APIInterface.class);

            /**
             usersCall
             **/
            Random rand = new Random();
            int since = rand.nextInt(2000);
            Log.i(TAG, "Generating usersList: rand_since_query: " + since);

            try {
                Call<List<User>> callUser = apiInterface.getUsers(Integer.toString(since), "10");
                Response<List<User>> response = callUser.execute();
                final List<User> users = response.body();
                if (users == null) {
                    Log.i(TAG, response.message());
                    return null;
                }
                return users;

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<User> users) {
            super.onPostExecute(users);
            usersCallback.onGetUsers(users);
        }
    }


    private static class GetProjectsAsyncTask extends AsyncTask<String, Void, List<Repo>> {

        private static final String TAG = "HelloAndroid: GetProjectsAsyncTask";
        IGetReposCallback reposCallback;

        public GetProjectsAsyncTask(IGetReposCallback reposCallback) {
            this.reposCallback = reposCallback;
        }

        @Override
        protected List<Repo> doInBackground(String... params) {
            String baseUrl = "https://api.github.com/";

            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();

            APIInterface apiInterface = retrofit.create(APIInterface.class);

            /**
             reposCall
             **/
            Log.i(TAG, "Generating reposList of: " + params[0]);

            try {
                Call<List<Repo>> callRepos = apiInterface.getReposList(params[0]);
                Response<List<Repo>> response = callRepos.execute();
                final List<Repo> repos = response.body();
                return repos;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Repo> repos) {
            super.onPostExecute(repos);
            reposCallback.onGetRepos(repos);
        }
    }
}
