package tellh.com.gitclub.presentation.view.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewStub;

import tellh.com.gitclub.R;
import tellh.com.gitclub.common.base.BaseActivity;
import tellh.com.gitclub.common.base.BaseView;
import tellh.com.gitclub.common.config.ExtraKey;
import tellh.com.gitclub.common.utils.Utils;
import tellh.com.gitclub.common.wrapper.Note;
import tellh.com.gitclub.presentation.contract.ShowError;
import tellh.com.gitclub.presentation.view.adapter.BaseRecyclerAdapter;
import tellh.com.gitclub.presentation.view.adapter.FooterLoadMoreAdapterWrapper;
import tellh.com.gitclub.presentation.view.adapter.FooterLoadMoreAdapterWrapper.UpdateType;
import tellh.com.gitclub.presentation.view.fragment.search.ListLoadingListener;
import tellh.com.gitclub.presentation.widget.ErrorViewHelper;

/**
 * Created by tlh on 2016/9/16 :)
 */
public abstract class BaseListActivity extends BaseActivity
        implements BaseView, SwipeRefreshLayout.OnRefreshListener, ListLoadingListener, ShowError,
        FooterLoadMoreAdapterWrapper.OnReachFooterListener {
    protected ProgressDialog progressDialog;

    protected SwipeRefreshLayout refreshLayout;
    protected ErrorViewHelper errorView;
    protected RecyclerView recyclerView;
    protected FooterLoadMoreAdapterWrapper loadMoreWrapper;
    protected String user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        if (intent != null) {
            user = intent.getStringExtra(ExtraKey.USER_NAME);
        }
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        initDagger();

        recyclerView = (RecyclerView) findViewById(R.id.list);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshLayout);
        errorView = new ErrorViewHelper((ViewStub) findViewById(R.id.vs_error));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(getToolbarTitle());
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadMoreWrapper = new FooterLoadMoreAdapterWrapper(getListAdapter());
        loadMoreWrapper.addFooter(R.layout.footer_load_more);
        loadMoreWrapper.setOnReachFooterListener(recyclerView, this);
        recyclerView.setAdapter(loadMoreWrapper);

        //swipe refresh layout
        refreshLayout.setProgressViewOffset(false, -100, 230);
        refreshLayout.setColorSchemeResources(R.color.blue, R.color.brown, R.color.purple, R.color.green);
        refreshLayout.setOnRefreshListener(this);
    }

    protected abstract BaseRecyclerAdapter getListAdapter();

    protected abstract String getToolbarTitle();

    protected abstract void initDagger();

    @Override
    public int getLayoutId() {
        return R.layout.activity_list;
    }

    @Override
    public void initData(Bundle savedInstanceState) {
        refreshLayout.setRefreshing(true);
        onRefresh();
    }


    @Override
    public void showOnError(String s) {
        progressDialog.dismiss();
        Note.show(s);
    }

    @Override
    public void showOnLoading() {
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    @Override
    public void showOnSuccess() {
        progressDialog.dismiss();
        Note.show(getString(R.string.success_loading));
    }

    @Override
    public void showLoading() {
        if (refreshLayout.isRefreshing())
            return;
        refreshLayout.setRefreshing(true);
    }

    @Override
    public void hideLoading() {
        if (!refreshLayout.isRefreshing())
            return;
        refreshLayout.setRefreshing(false);
    }

    @Override
    public void showErrorView() {
        errorView.showErrorView(refreshLayout, new ErrorViewHelper.OnReLoadCallback() {
            @Override
            public void reload() {
                hideLoading();
                onRefresh();
            }
        });
    }

    @Override
    public void onToLoadMore(int curPage) {
        loadMoreWrapper.setFooterStatus(FooterLoadMoreAdapterWrapper.FooterState.LOADING);
    }

    @Override
    public void showOnError(String msg, UpdateType updateType) {
        showOnError(msg);
        handleError(msg, updateType);
    }

    protected void handleError(String msg, UpdateType updateType) {
        if (updateType == UpdateType.REFRESH)
            refreshLayout.setRefreshing(false);
        else
            loadMoreWrapper.setFooterStatus(FooterLoadMoreAdapterWrapper.FooterState.PULL_TO_LOAD_MORE);

        if (updateType == UpdateType.REFRESH && !msg.equals(Utils.getString(R.string.reqest_flying))) {
            errorView.showErrorView(refreshLayout, new ErrorViewHelper.OnReLoadCallback() {
                @Override
                public void reload() {
                    refreshLayout.setRefreshing(true);
                    onRefresh();
                }
            });
        }
    }

    @Override
    public Context getViewContext() {
        return this;
    }
}
