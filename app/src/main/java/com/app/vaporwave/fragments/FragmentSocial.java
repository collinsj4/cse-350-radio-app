package com.app.vaporwave.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.vaporwave.Config;
import com.app.vaporwave.R;
import com.app.vaporwave.activities.ActivityWebView;
import com.app.vaporwave.activities.MainActivity;
import com.app.vaporwave.adapters.AdapterSocial;
import com.app.vaporwave.callbacks.CallbackSocial;
import com.app.vaporwave.database.prefs.SharedPref;
import com.app.vaporwave.database.prefs.ThemePref;
import com.app.vaporwave.models.Social;
import com.app.vaporwave.rests.ApiInterface;
import com.app.vaporwave.rests.RestAdapter;
import com.app.vaporwave.utils.Constant;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSocial extends Fragment {

    View rootView;
//    private Toolbar toolbar;
//    private RelativeLayout parentView;
//    private ImageButton btnBack;
//    private TextView toolbarTitle;
    private RecyclerView recyclerView;
    private AdapterSocial adapterSocial;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Call<CallbackSocial> callbackCall = null;
    ArrayList<Social> items = new ArrayList<>();
    private ShimmerFrameLayout lytShimmer;
    ThemePref themePref;
    SharedPref sharedPref;
    private MainActivity activity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_social, container, false);
        initView();
        return rootView;
    }

    private void initView() {
        themePref = new ThemePref(activity);
        sharedPref = new SharedPref(activity);

//        parentView = rootView.findViewById(R.id.parent_view);
//        toolbar = rootView.findViewById(R.id.toolbar);
//        toolbarTitle = rootView.findViewById(R.id.toolbar_title);
//        btnBack = rootView.findViewById(R.id.btn_back);

        lytShimmer = rootView.findViewById(R.id.shimmer_view_container);
        swipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL));

        adapterSocial = new AdapterSocial(activity, items);
        recyclerView.setAdapter(adapterSocial);

        adapterSocial.setOnItemClickListener((view, obj, position) -> {
            if (Config.OPEN_SOCIAL_MENU_IN_EXTERNAL_BROWSER) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(obj.url));
                startActivity(intent);
            } else {
                Intent intent = new Intent(activity, ActivityWebView.class);
                intent.putExtra("title", obj.name);
                intent.putExtra("url", obj.url);
                startActivity(intent);
            }
        });

        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            adapterSocial.resetListData();
            requestAction();
        });

        requestAction();
//        setupToolbar();

    }

    private void displayApiResult(final ArrayList<Social> socials) {
        adapterSocial.setItems(socials);
        swipeProgress(false);
        if (socials.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi() {
        ApiInterface apiInterface = RestAdapter.createAPI(sharedPref.getBaseUrl());
        callbackCall = apiInterface.getSocial(Config.REST_API_KEY);
        callbackCall.enqueue(new Callback<CallbackSocial>() {
            @Override
            public void onResponse(@NonNull Call<CallbackSocial> call, @NonNull Response<CallbackSocial> response) {
                CallbackSocial resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.social);
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CallbackSocial> call, @NonNull Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void onFailRequest() {
        swipeProgress(false);
        showFailedView(true, getString(R.string.failed_text));
    }

    private void requestAction() {
        showFailedView(false, "");
        showNoItemView(false);
        swipeProgress(true);
        new Handler().postDelayed(this::requestListPostApi, Constant.DELAY_PROGRESS);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lytShimmer.stopShimmer();
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = rootView.findViewById(R.id.lyt_failed);
        ((TextView) rootView.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        rootView.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = rootView.findViewById(R.id.lyt_no_item);
        ((TextView) rootView.findViewById(R.id.no_item_message)).setText(R.string.no_social_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.GONE);
            lytShimmer.stopShimmer();
            return;
        }
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(show);
            lytShimmer.setVisibility(View.VISIBLE);
            lytShimmer.startShimmer();
        });
    }

//    private void setupToolbar() {
//        btnBack.setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
//                FragmentManager fm = activity.getSupportFragmentManager();
//                if (fm.getBackStackEntryCount() > 0) {
//                    fm.popBackStack();
//                }
//            dismiss();
//        }, Constant.DELAY_CLICK));
//
//        themeColor();
//    }

//    private void themeColor() {
//        if (themePref.getCurrentTheme() == THEME_LIGHT) {
//            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
//            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
//            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.grey));
//            btnBack.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
//        } else if (themePref.getCurrentTheme() == THEME_DARK) {
//            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorBackgroundDark));
//            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorToolbarDark));
//            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.white));
//        } else {
//            parentView.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorLight));
//            toolbar.setBackgroundColor(ContextCompat.getColor(activity, R.color.colorPrimary));
//            toolbarTitle.setTextColor(ContextCompat.getColor(activity, R.color.white));
//        }
//    }

//    @NonNull
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        return dialog;
//    }

}
