package com.example.abnervictor.lab9;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.sql.Struct;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static android.content.ContentValues.TAG;

/**
 * Created by abnervictor on 2017/12/19.
 */

public class GithubData {

    private final Context context;

    public class Profiles{
        public String login;
        public String id;
        public String blog;
    }

    public class Repository{
        public String name;
        public String language;
        public String description;
    }

    public interface GitHubProfileService{
        @GET("users/{user}")
        //rx.Observable<Profiles> getUser(@Path("user") String user);
        rx.Observable<Profiles> getUser(@Path("user") String user);
    }

    public interface GitHubRepoService{
        @GET("users/{user}/repos")
        Call<List<Repository>> getRepoList(@Path("user") String user);
    }

    GithubData(Context context){
        this.context = context;
    }

    private static Retrofit createRetrofit(String baseUrl){
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(createOkHttp())
                .build();
    }

    private static OkHttpClient createOkHttp(){
        //HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        //interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                //.addInterceptor(interceptor)
                //.retryOnConnectionFailure(true)
                .connectTimeout(10, TimeUnit.SECONDS) //连接超时
                .readTimeout(30, TimeUnit.SECONDS)    //读取超时
                .writeTimeout(10, TimeUnit.SECONDS)   //写入超时
                .build();
        return client;
    }

    public static void addProfileData(Context context, Profiles profiles){
        Map<String,Object> profile = new LinkedHashMap<>();
        profile.put("name",profiles.login);
        profile.put("id",profiles.id);
        profile.put("blog",profiles.blog);
        Toast.makeText(context,"login: "+profiles.login+", id: "+profiles.id+", blog: "+profiles.blog,Toast.LENGTH_SHORT).show();
        MainActivity.onFetchResult(profile);
    }

    public static void addRepoData(List<Repository> repositorys){
        List<Map<String,Object>> temp = new ArrayList<>();
        for (Repository r:repositorys){
            Map<String,Object> repository = new LinkedHashMap<>();
            repository.put("name",r.name);
            repository.put("language",r.language);
            repository.put("description",r.description);
            temp.add(repository);
        }
        MainActivity.onGetRepoResult(temp);
    }

    public void fetchProfile(String keyword){
        Retrofit retrofit = createRetrofit("https://api.github.com/");
        GitHubProfileService service = retrofit.create(GitHubProfileService.class);
        //Observable，异步调用
        rx.Observable<Profiles> call = service.getUser(keyword);
        call.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Profiles>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: 请求完成");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG,"onError: "+e.getMessage());
                        MainActivity.setProgressGone();
                        Toast.makeText(context,"未找到相关信息!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Profiles profiles) {
                        GithubData.addProfileData(context, profiles);
                        //这里需要通过调用全局函数的方式，返回一个数值
                    }
                });
    }
    public void fetchRepo(String keyword){
        Retrofit retrofit = createRetrofit("https://api.github.com/");
        GitHubRepoService service = retrofit.create(GitHubRepoService.class);
        Call<List<Repository>> call = service.getRepoList(keyword);
        //Call异步方法
        call.enqueue(new Callback<List<Repository>>() {
            @Override
            public void onResponse(Call<List<Repository>> call, Response<List<Repository>> response) {
                Log.d(TAG, "onResponse: 请求完成");
                GithubData.addRepoData(response.body());
            }

            @Override
            public void onFailure(Call<List<Repository>> call, Throwable t) {
                Log.e(TAG,"onFailure: "+t.getMessage());
                MainActivity.setProgressGone();
                Toast.makeText(context,"未找到相关信息!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}

