package com.jay.gitsquare;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private DatabaseHandler db;
    private List<Contributor> contributors = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContributorsAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.activity_title));
        db = new DatabaseHandler(this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new ContributorsAdapter(contributors);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        if(db.getContactsCount() <= 0) {
            downloadContributors();
            mAdapter.notifyDataSetChanged();
        }
        else {
            for(Contributor contributor : db.getAllContributors()){
                contributors.add(contributor);
                mAdapter.notifyDataSetChanged();
            }
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                downloadContributors();
                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void downloadContributors() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);

        Call<List<Contributor>> call = apiService.getContributors();
        call.enqueue(new Callback<List<Contributor>>() {
            @Override
            public void onResponse(Call<List<Contributor>>call, Response<List<Contributor>> response) {
                for (Contributor cn : response.body()) {
                    contributors.add(cn);
                    db.addContributor(new Contributor(cn.getLogin(),cn.getId(), cn.getAvatarUrl(),cn.getGravatarId(),cn.getUrl(),cn.getHtmlUrl(),cn.getFollowersUrl(),cn.getFollowingUrl(),cn.getGistsUrl(), cn.getStarredUrl(),cn.getSubscriptionsUrl(),cn.getOrganizationsUrl(),cn.getReposUrl(),cn.getEventsUrl(),cn.getReceivedEventsUrl(),cn.getType(),cn.isSiteAdmin(),cn.getContributions()));
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Contributor>>call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error while downloading data!",Toast.LENGTH_LONG).show();
            }
        });
    }
}