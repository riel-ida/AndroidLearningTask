package com.example.helloandroid;

import com.example.helloandroid.GitResponse.Repo;
import com.example.helloandroid.GitResponse.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface APIInterface {

    @GET("/users")
    Call<List<User>> getUsers(@Query(value = "since")  String since, @Query(value = "per_page") String per_page);

    @GET("/users/{user}/repos")
    Call <List<Repo>> getReposList(@Path("user") String user);

}
