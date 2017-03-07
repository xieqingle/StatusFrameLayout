package com.cesecsh.statusframelayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * StatusFrameLayout
 * Created by RockQ on 2017/3/6.
 */

public class StatusFrameLayout extends FrameLayout {
    public static final int LOADING = 0x1000;
    public static final int SUCCESS = 0x2000;
    public static final int ERROR = 0x3000;
    public static final int NET_ERROR = 0x4000;
    public static final int EMPTY = 0x5000;
    private WeakReference<Context> wfContext;
    private FrameLayout mContentViewContainer;
    private FrameLayout mLoadingLayoutContainer;
    private FrameLayout mEmptyLayoutContainer;
    private FrameLayout mErrorLayoutContainer;
    private FrameLayout mNetErrorLayoutContainer;
    private int currentStatus;
    @LayoutRes
    private int loadingID;
    private View loadingView;
    @LayoutRes
    private int contentID;
    private View contentView;
    @LayoutRes
    private int emptyID;
    private View emptyView;
    @LayoutRes
    private int errorID;
    private View errorView;
    @LayoutRes
    private int netErrorID;
    private View netErrorView;
    private Context mContext;
    /**
     * 重新加载
     */
    @IdRes
    private int retryID = -1;
    // set listener
    private OnRetryListener mListener;

    public StatusFrameLayout(@NonNull Context context) {
        this(context, null, 0);
    }

    public StatusFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StatusFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        wfContext = new WeakReference<Context>(context);
        mContext = wfContext.get();
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.StatusFrameLayout);
        try {
            loadingID = typedArray.getResourceId(R.styleable.StatusFrameLayout_loadingLayout, R.layout.layout_loading);
            contentID = typedArray.getResourceId(R.styleable.StatusFrameLayout_contentLayout, R.layout.layout_content);
            emptyID = typedArray.getResourceId(R.styleable.StatusFrameLayout_emptyLayout, R.layout.layout_empty);
            errorID = typedArray.getResourceId(R.styleable.StatusFrameLayout_errorLayout, R.layout.layout_error);
            netErrorID = typedArray.getResourceId(R.styleable.StatusFrameLayout_netErrorLayout, R.layout.layout_net_error);
            retryID = typedArray.getResourceId(R.styleable.StatusFrameLayout_retryId, -1);
            currentStatus = typedArray.getInteger(R.styleable.StatusFrameLayout_currentStatus, LOADING);
        } finally {
            typedArray.recycle();
        }
        LayoutInflater.from(wfContext.get()).inflate(R.layout.status_layout, this);
        mContentViewContainer = (FrameLayout) findViewById(R.id.status_content);
        @STATUS_LOAD int current = currentStatus;
        setStatus(current);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mContentViewContainer == null) {
            super.addView(child, index, params);
            return;
        }

        if (mContentViewContainer.getChildCount() != 0) {
            super.addView(child, index, params);
        } else {
            if (mContentViewContainer != null && index < mContentViewContainer.getChildCount()) {
                mContentViewContainer.addView(child, index, params);
            } else {
                super.addView(child, index, params);
            }
        }

    }

    public void setStatus(@STATUS_LOAD int mode) {
        currentStatus = mode;
        switch (currentStatus) {
            case LOADING:
                setEmptyLayoutVisibility(false);
                setContentLayoutVisibility(false);
                setErrorLayoutVisibility(false);
                setNetErrorLayoutVisibility(false);
                setLoadingLayoutVisibility(true);
                break;
            case SUCCESS:
                setLoadingLayoutVisibility(false);
                setNetErrorLayoutVisibility(false);
                setErrorLayoutVisibility(false);
                setEmptyLayoutVisibility(false);
                setContentLayoutVisibility(true);
                break;
            case ERROR:
                setLoadingLayoutVisibility(false);
                setContentLayoutVisibility(false);
                setEmptyLayoutVisibility(false);
                setNetErrorLayoutVisibility(false);
                setErrorLayoutVisibility(true);
                break;
            case EMPTY:
                setLoadingLayoutVisibility(false);
                setContentLayoutVisibility(false);
                setNetErrorLayoutVisibility(false);
                setErrorLayoutVisibility(false);
                setEmptyLayoutVisibility(true);
                break;
            case NET_ERROR:
                setLoadingLayoutVisibility(false);
                setEmptyLayoutVisibility(false);
                setContentLayoutVisibility(false);
                setErrorLayoutVisibility(false);
                setNetErrorLayoutVisibility(true);
                break;
        }
    }
    // visibility

    private void setLoadingLayoutVisibility(boolean visible) {
        if (mLoadingLayoutContainer == null) {
            if (visible) {
                inflateLoadingLayoutContainer();
            }
        } else {
            mLoadingLayoutContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setEmptyLayoutVisibility(boolean visible) {
        if (mEmptyLayoutContainer == null) {
            if (visible) {
                inflateEmptyLayoutContainer();
            }
        } else {
            mEmptyLayoutContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setContentLayoutVisibility(boolean visible) {
        mContentViewContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setErrorLayoutVisibility(boolean visible) {
        if (mErrorLayoutContainer == null) {
            if (visible) {
                inflateErrorLayoutContainer();
            }
        } else {
            mErrorLayoutContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private void setNetErrorLayoutVisibility(boolean visible) {
        if (mNetErrorLayoutContainer == null) {
            if (visible) {
                inflateNetErrorLayoutContainer();
            }
        } else {
            mNetErrorLayoutContainer.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    // inflate ViewStub

    /**
     * 将加载中的布局加载到对应的viewStub中，并且初始化dialog
     * 后续应该将imageView资源id设置成loading_images,以便循环播放
     */
    private void inflateLoadingLayoutContainer() {
        if (mLoadingLayoutContainer != null)
            removeView(mLoadingLayoutContainer);
        ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub_progress);
        viewStub.inflate();
        mLoadingLayoutContainer = (FrameLayout) findViewById(R.id.status_progress);
        View inflate = LayoutInflater.from(mContext).inflate(loadingID, mLoadingLayoutContainer);
        ImageView imageView = (ImageView) inflate.findViewById(R.id.loading_images);
        if (imageView != null) {
            AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
            animationDrawable.start();
        }
        mLoadingLayoutContainer.setVisibility(View.VISIBLE);
    }

    private void inflateEmptyLayoutContainer() {
        if (mEmptyLayoutContainer != null)
            removeView(mEmptyLayoutContainer);
        ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub_empty);
        viewStub.inflate();
        mEmptyLayoutContainer = (FrameLayout) findViewById(R.id.status_empty);
        LayoutInflater.from(mContext).inflate(emptyID, mEmptyLayoutContainer);
        mEmptyLayoutContainer.setVisibility(View.VISIBLE);
        setRetry(mErrorLayoutContainer, retryID);
    }

    private void inflateErrorLayoutContainer() {
        if (mErrorLayoutContainer != null)
            removeView(mErrorLayoutContainer);
        ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub_error);
        viewStub.inflate();
        mErrorLayoutContainer = (FrameLayout) findViewById(R.id.status_error);
        LayoutInflater.from(mContext).inflate(errorID, mErrorLayoutContainer);
        mErrorLayoutContainer.setVisibility(View.VISIBLE);
        setRetry(mErrorLayoutContainer, retryID);
    }

    private void inflateNetErrorLayoutContainer() {
        if (mNetErrorLayoutContainer != null)
            removeView(mNetErrorLayoutContainer);
        ViewStub viewStub = (ViewStub) findViewById(R.id.viewStub_net_error);
        viewStub.inflate();
        mNetErrorLayoutContainer = (FrameLayout) findViewById(R.id.status_net_error);
        LayoutInflater.from(mContext).inflate(netErrorID, mNetErrorLayoutContainer);
        mNetErrorLayoutContainer.setVisibility(View.VISIBLE);
        setRetry(mErrorLayoutContainer, retryID);
    }

    // set layout

    public void setLoadingLayout(@LayoutRes int layoutResId) {
        if (mLoadingLayoutContainer == null) {
            inflateLoadingLayoutContainer();
        }
        mLoadingLayoutContainer.removeAllViewsInLayout();
        LayoutInflater.from(mContext).inflate(layoutResId, mLoadingLayoutContainer);
    }

    private void setRetry(FrameLayout mErrorLayoutContainer, int retryID) {
        if (retryID == -1) {
            throw new RetryIdNullException("retry id is null");
        }
        View view = mErrorLayoutContainer.findViewById(retryID);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null)
                    mListener.onRetry();
            }
        });
    }

    public void setLoadingLayout(View layoutView) {
        loadingView = layoutView;
        if (mLoadingLayoutContainer == null) {
            inflateLoadingLayoutContainer();
        }
        mLoadingLayoutContainer.removeAllViewsInLayout();
        mLoadingLayoutContainer.addView(loadingView);
    }

    public void setEmptyLayout(@LayoutRes int layoutResId) {
        if (mEmptyLayoutContainer == null) {
            inflateEmptyLayoutContainer();
        }
        mEmptyLayoutContainer.removeAllViewsInLayout();
        LayoutInflater.from(mContext).inflate(layoutResId, mEmptyLayoutContainer);
    }

    public void setEmptyLayout(View layoutView) {
        emptyView = layoutView;
        if (mEmptyLayoutContainer == null) {
            inflateEmptyLayoutContainer();
        }
        mEmptyLayoutContainer.removeAllViewsInLayout();
        mEmptyLayoutContainer.addView(emptyView);
    }

    public void setErrorLayout(@LayoutRes int layoutResId) {
        if (mErrorLayoutContainer == null) {
            inflateErrorLayoutContainer();
        }
        mErrorLayoutContainer.removeAllViewsInLayout();
        LayoutInflater.from(mContext).inflate(layoutResId, mErrorLayoutContainer);
    }

    public void setErrorLayout(View layoutView) {
        errorView = layoutView;
        if (mErrorLayoutContainer == null) {
            inflateErrorLayoutContainer();
        }
        mErrorLayoutContainer.removeAllViewsInLayout();
        mErrorLayoutContainer.addView(errorView);
    }

    public void setNetErrorLayout(@LayoutRes int layoutResId) {
        if (mNetErrorLayoutContainer == null) {
            inflateNetErrorLayoutContainer();
        }
        mNetErrorLayoutContainer.removeAllViewsInLayout();
        LayoutInflater.from(mContext).inflate(layoutResId, mNetErrorLayoutContainer);
    }

    public void setNetErrorLayout(View layoutView) {
        netErrorView = layoutView;
        if (mNetErrorLayoutContainer == null) {
            inflateNetErrorLayoutContainer();
        }
        mNetErrorLayoutContainer.removeAllViewsInLayout();
        mNetErrorLayoutContainer.addView(netErrorView);
    }

    public void setRetryId(@IdRes int id) {
        this.retryID = id;
    }

    public void setOnRetryListener(OnRetryListener listener) {
        mListener = listener;
    }

    @IntDef({LOADING, SUCCESS, ERROR, EMPTY, NET_ERROR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATUS_LOAD {
    }


}

