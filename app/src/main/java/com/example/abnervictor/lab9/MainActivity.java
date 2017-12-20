package com.example.abnervictor.lab9;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;
import jp.wasabeef.recyclerview.animators.OvershootInLeftAnimator;

public class MainActivity extends AppCompatActivity {

    private View search_layout, repo_layout;
    private static RecyclerView search_recycler, repo_recycler;
    private EditText search_text;
    private Button clr_btn, fetch_btn;
    private static CommonAdapter<Map<String,Object>> search_commonAdapter, repo_commonAdapter;
    private static List<Map<String,Object>> search_list, repo_list;
    private static ProgressBar search_progress, repo_progress;
    private GithubData githubData;
    private int mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mode = 1;//搜索界面为1
        findView();
        initData();
        setListener();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        /*
         * 监听Back键按下事件
         * 注意,返回值表示:是否能完全处理该事件,true表示可以
         * 若返回false,表示需要继续传播该事件.
         */
        if ((keyCode == KeyEvent.KEYCODE_BACK) && mode == 2) {
            //System.out.println("按下了back键   onKeyDown()");
            setVisibility("search");
            return true;//仅repo界面拦截返回键
        }
        else {
            return super.onKeyDown(keyCode, event);
        }
    }


    private void findView(){
        search_layout = findViewById(R.id.search_layout);
        repo_layout = findViewById(R.id.repository_layout);
        search_recycler = findViewById(R.id.search_recycler);
        repo_recycler = findViewById(R.id.repository_recycler);
        search_text = findViewById(R.id.search_text);
        clr_btn = findViewById(R.id.search_clear);
        fetch_btn = findViewById(R.id.search_fetch);
        search_progress = findViewById(R.id.search_progress);
        repo_progress = findViewById(R.id.repo_progress);
        githubData = new GithubData(MainActivity.this);
        setVisibility("search");
    }

    private void setVisibility(String mode){
        switch (mode){
            case "repo":
                search_layout.setVisibility(View.GONE);
                repo_layout.setVisibility(View.VISIBLE);
                this.mode = 2;
                break;
            default:
            case "search":
                search_layout.setVisibility(View.VISIBLE);
                repo_layout.setVisibility(View.GONE);
                search_text.setText("");
                this.mode = 1;
                break;
        }
    }

    private void initData(){
        //
        search_list = new ArrayList<>();
        repo_list = new ArrayList<>();
        //
        initSearchRecycler();
        initRepoRecycler();
        githubData.fetchProfile("abnervictor");
    }

    public static void onFetchResult(Map<String,Object> profile){
        //搜索得到结果时，这一全局函数将被GitHubData类调用
        setProgressGone();
        search_list.add(profile);
        search_commonAdapter.notifyDataSetChanged();
    }

    public static void onGetRepoResult(List<Map<String,Object>> repository){
        //搜索得到结果时，这一全局函数将被GitHubData类调用
        if (!repo_list.isEmpty()) repo_list.clear();
        setProgressGone();
        repo_list.addAll(repository);
        repo_commonAdapter.notifyDataSetChanged();
    }

    public static void setProgressGone(){
        search_progress.setVisibility(View.GONE);
        repo_progress.setVisibility(View.GONE);
        search_recycler.setVisibility(View.VISIBLE);
    }

    private void setListener(){
        clr_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //清空搜索结果
                search_text.setText("");
                //search_list.clear();
                //search_commonAdapter.notifyDataSetChanged();
            }
        });
        fetch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyword = search_text.getText().toString();
                search_recycler.setVisibility(View.GONE);
                search_progress.setVisibility(View.VISIBLE);
                githubData.fetchProfile(keyword);
            }
        });
    }

    private void initSearchRecycler(){
        search_recycler.setLayoutManager(new LinearLayoutManager(this));
        search_commonAdapter = new CommonAdapter<Map<String, Object>>(this,R.layout.search_recycler_item,search_list) {
            @Override
            public void convert(ViewHolder holder, Map<String, Object> stringObjectMap) {
                //修改显示内容
                TextView login_name,login_id,login_blog;
                login_name = holder.getView(R.id.login_name);
                login_id = holder.getView(R.id.login_id);
                login_blog = holder.getView(R.id.login_blog);
                login_name.setText(stringObjectMap.get("name").toString());
                String temp = "id: "+stringObjectMap.get("id").toString();
                login_id.setText(temp);
                temp = "blog: "+stringObjectMap.get("blog").toString();
                login_blog.setText(temp);
            }
        };
        search_commonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                setVisibility("repo");
                //传入关键字，拉取repo信息
                repo_list.clear();
                repo_commonAdapter.notifyDataSetChanged();
                repo_progress.setVisibility(View.VISIBLE);
                githubData.fetchRepo(search_list.get(position).get("name").toString());
                //
            }

            @Override
            public void onLongClick(int position) {
                Toast.makeText(MainActivity.this,"已移除条目: "+search_list.get(position).get("name").toString(),Toast.LENGTH_SHORT).show();
                search_list.remove(position);
                search_commonAdapter.notifyDataSetChanged();
                //长按删除
            }
        });
        ScaleInAnimationAdapter animationAdapter = new ScaleInAnimationAdapter(search_commonAdapter);
        animationAdapter.setDuration(500); //设置动画 
        search_recycler.setAdapter(animationAdapter);
        search_recycler.setItemAnimator(new OvershootInLeftAnimator());
    }



    private void initRepoRecycler(){
        repo_recycler.setLayoutManager(new LinearLayoutManager(this));
        repo_commonAdapter = new CommonAdapter<Map<String, Object>>(this,R.layout.repository_recycler_item,repo_list) {
            @Override
            public void convert(ViewHolder holder, Map<String, Object> stringObjectMap) {
                //修改显示内容
                TextView repo_name,repo_language,repo_description;
                repo_name = holder.getView(R.id.repository_name);
                repo_language = holder.getView(R.id.language);
                repo_description = holder.getView(R.id.description);
                repo_name.setText(stringObjectMap.get("name").toString());
                String temp = (stringObjectMap.get("language") == null)? "":stringObjectMap.get("language").toString();
                repo_language.setText(temp);
                temp = (stringObjectMap.get("description") == null)? "":stringObjectMap.get("description").toString();
                repo_description.setText(temp);
            }
        };
        repo_commonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {

            }
            @Override
            public void onLongClick(int position) {

            }
        });
        ScaleInAnimationAdapter animationAdapter = new ScaleInAnimationAdapter(repo_commonAdapter);
        animationAdapter.setDuration(500); //设置动画 
        repo_recycler.setAdapter(animationAdapter);
        repo_recycler.setItemAnimator(new OvershootInLeftAnimator());
    }



}
