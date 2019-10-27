package com.example.helloandroid.GitResponse;

import java.util.List;

public interface IGetReposCallback {
    void onGetRepos(List<Repo> repoList);
}
