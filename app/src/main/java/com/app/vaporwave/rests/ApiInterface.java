package com.app.vaporwave.rests;

import com.app.vaporwave.callbacks.CallbackAlbumArt;
import com.app.vaporwave.callbacks.CallbackCategory;
import com.app.vaporwave.callbacks.CallbackCategoryDetail;
import com.app.vaporwave.callbacks.CallbackHome;
import com.app.vaporwave.callbacks.CallbackRadio;
import com.app.vaporwave.callbacks.CallbackSettings;
import com.app.vaporwave.callbacks.CallbackSocial;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Your Radio App";

    @Headers({CACHE, AGENT})
    @GET("api.php?home")
    Call<CallbackHome> getHome(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?radios")
    Call<CallbackRadio> getRadios(
            @Query("count") int count,
            @Query("page") int page,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?categories")
    Call<CallbackCategory> getCategories(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?search")
    Call<CallbackRadio> getSearch(
            @Query("search") String search,
            @Query("count") int count,
            @Query("page") int page,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?search_rtl")
    Call<CallbackRadio> getSearchRtl(
            @Query("search") String search,
            @Query("count") int count,
            @Query("page") int page,
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?settings")
    Call<CallbackSettings> getSettings(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?social")
    Call<CallbackSocial> getSocial(
            @Query("api_key") String api_key
    );

    @Headers({CACHE, AGENT})
    @GET("api.php?category_detail")
    Call<CallbackCategoryDetail> getCategoryDetail(
            @Query("id") String id,
            @Query("count") int count,
            @Query("page") int page
    );

    @Headers({CACHE, AGENT})
    @GET("search")
    Call<CallbackAlbumArt> getAlbumArt(
            @Query("term") String term,
            @Query("media") String media,
            @Query("limit") int limit
    );

}
