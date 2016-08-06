package com.hiroshi.cimoc.ui.fragment;

import com.hiroshi.cimoc.CimocApplication;
import com.hiroshi.cimoc.R;
import com.hiroshi.cimoc.presenter.BasePresenter;
import com.hiroshi.cimoc.utils.PreferenceMaster;

import butterknife.OnClick;

/**
 * Created by Hiroshi on 2016/7/20.
 */
public class AboutFragment extends BaseFragment {

    private boolean isEnable;
    private int count;

    @OnClick(R.id.about_resource_btn) void onClick() {
        if (++count > 9) {
            isEnable = !isEnable;
            CimocApplication.getPreferences().putBoolean(PreferenceMaster.PREF_EX, isEnable);
            showSnackbar(isEnable ? "重启软件开启 EHentai" : "重启软件关闭 EHentai");
            count = 0;
        }
    }

    @Override
    protected void initView() {
        isEnable = CimocApplication.getPreferences().getBoolean(PreferenceMaster.PREF_EX, false);
        count = 0;
    }

    @Override
    protected int getLayoutView() {
        return R.layout.fragment_about;
    }

}
