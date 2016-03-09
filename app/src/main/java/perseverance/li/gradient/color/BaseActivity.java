package perseverance.li.gradient.color;/*
* -----------------------------------------------------------------
* Copyright (C) 2012-2015, by www.dianhua.cn, Beijing, All rights reserved.
* -----------------------------------------------------------------
* Author: peng.wang
* Create: 2015-7-10
*
* Changes (from 2015-7-10)
* -----------------------------------------------------------------
* 2015-7-10 : 创建 perseverance.li.gradient.color.BaseActivity.java (作者:peng.wang);
* -----------------------------------------------------------------
*/

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.lang.ref.WeakReference;

/**
 * Activity基类
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "perseverance.li.gradient.color.BaseActivity";

    protected ActionBar mActionBar;

    protected Handler mHandler = new SafeHandler(this){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BaseActivity.this.handleMessage(msg);
        }
    };

    /**
     * 防止内部Handler类引起内存泄露
     *
     */
    static class SafeHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public SafeHandler(Activity activity) {
            mActivity = new WeakReference<Activity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if(mActivity.get() == null) {
                return;
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
    }

    /**
     * 初始化ActionBar called onCreate(Bundle);
     */
    protected void initActionBar(){
        this.mActionBar = getActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setDisplayShowTitleEnabled(true);
            mActionBar.setDisplayShowHomeEnabled(false);
        }
    }


    protected final void sendCallbackEmptyMessage(int what){
        sendCallbackMessage(what,null);
    }

    /**
     * Handler消息发送
     * @param what
     * @param obj
     */
    protected final void sendCallbackMessage(int what, Object obj) {
        sendCallbackMessageDelay(what, obj, 0);
    }


    /**
     *Handler消息延时发送
     * @param what
     * @param obj
     * @param delay
     */
    protected final void sendCallbackMessageDelay(int what, Object obj,long delay) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage();
            msg.what = what;
            msg.obj = obj;
            mHandler.sendMessageDelayed(msg,delay);
        }
    }

    /**
     * Handler消息处理
     * @param msg
     */
    protected abstract void handleMessage(Message msg);

    /**
     * ActionBar 返回键监听处理
     */
    protected abstract void onMenuHome();


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onMenuHome();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
