package com.devahmed.demo.screenshoter.common;

import android.content.Context;
import android.view.View;

public abstract class BaseViewMvc implements MvcView {

    View mRootView;

    @Override
    public View getRootView() {
        return mRootView;
    }

    protected <T extends View> T findViewById(int id) {
        return getRootView().findViewById(id);
    }

    protected Context getContext() {
        return getRootView().getContext();
    }

    protected void setRootView(View view) {
        this.mRootView = view;
    }
}
