package com.eeseetech.nagrand.player;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.R;
import com.eeseetech.nagrand.data.VideoRepository;
import com.eeseetech.nagrand.view.DebugDialogFragment;

/**
 * Created by dongjiangpeng@eeseetech.com on 2017/4/6.
 */

public class PlayerActivity extends Activity {

    private static final String TAG = "nagrand";

    private PlayerFragment playerFragment;
    private PlayerPresenter mPresenter;
    private VideoRepository mVideoRepository;
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        playerFragment = new PlayerFragment();
        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.framelayout_player, playerFragment);
        fragmentTransaction.commit();
        View decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            hideNavigationbar();
                        }
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Global.LOG) {
            Log.d(TAG, "PlayerActivity/onResume:");
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (Global.LOG) {
            Log.d(TAG, "PlayerActivity/onNewIntent:");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Global.LOG) {
            Log.d(TAG, "PlayerActivity/onPause:");
        }
        //bug();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Global.LOG) {
            Log.d(TAG,"PlayerActivity/onStop:");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Global.LOG) {
            Log.d(TAG,"PlayerActivity/onDestroy:");
        }
    }

    private void hideNavigationbar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            DebugDialogFragment fragment = new DebugDialogFragment();
            fragment.show(getFragmentManager(),"DebugDialogFragment");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
