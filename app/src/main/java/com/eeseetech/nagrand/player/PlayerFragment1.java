package com.eeseetech.nagrand.player;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.R;
import com.eeseetech.nagrand.view.MediaView;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;


/**
 * Created by dongjiangpeng@eeseetech.com on 2017/4/6.
 */

public class PlayerFragment1 extends Fragment implements MediaView{

    private static final String TAG = "nagrand";

    private static final int MSG_READY = 10;
    private static final int MSG_PLAYING = 11;
    private static final int MSG_COMPLETED = 12;
    private static final int MSG_ERROR = 13;

    private int mCurrentVideoPosition = 0;
    private int mCurrentMsgPosition = 0;
    private boolean isPlaying = false;
    private boolean isFirstStart = true;
    private boolean isAppConnected = false;

    private String mCurrentFileName;
    private List<String> mPlayList;

    private VideoView mVideoView;
    private PlayerPresenter mPlayerPresenter;
    private PlayerStateHandler mHandler;
    private FrameLayout mIdleStateView;
    private CompositeDisposable mCompositeDisposable;

    public PlayerFragment1() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new PlayerStateHandler(this);
        mCompositeDisposable = new CompositeDisposable();
        mPlayerPresenter = new PlayerPresenter(PlayerFragment1.this);
        //mPlayerPresenter.start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);
        setVideoView(view);
        setIdleView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Global.LOG) {
            Log.d(TAG, "PlayerFragment/onStart:");
        }
        mPlayerPresenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (Global.LOG) {
            Log.d(TAG, "PlayerFragment/onResume:");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Global.LOG) {
            Log.d(TAG, "PlayerFragment/onStop:");
        }
        mPlayerPresenter.stop();
        mVideoView.pause();
        isPlaying = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Global.LOG) {
            Log.d(TAG, "PlayerFragment/onDestroyView:");
        }
        mVideoView.pause();
        isPlaying = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.clear();
        mPlayerPresenter.stop();
    }

    private void setIdleView(View view) {
        mIdleStateView = (FrameLayout) view.findViewById(R.id.layout_idle_state);
    }

    private void setVideoView(View view) {
        mVideoView = (VideoView) view.findViewById(R.id.video_view);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        mVideoView.setLayoutParams(layoutParams);
    }

    private void playVideo() {
        mVideoView.start();
    }

    private void play() {
        Log.d(Global.TAG, "PlayerFragment/play:");
        mIdleStateView.setVisibility(View.INVISIBLE);
        isPlaying = true;
        playVideo();
        mPlayerPresenter.addPlayHistory(mCurrentFileName, String.valueOf(System.currentTimeMillis()));

    }

    private void complete() {
        isPlaying = false;
        mIdleStateView.setVisibility(View.VISIBLE);
        playNext();
    }

    private void error() {
        mPlayerPresenter.deleteFile(mCurrentFileName);
    }

    private void ready() {
        mHandler.sendEmptyMessage(MSG_PLAYING);
    }

    public void replaceList(final List<String> playList) {
        //Log.d(Global.TAG, "PlayerFragment/replaceList:");
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (playList == null || playList.size() == 0) {
                    return;
                }
                mPlayList = playList;
                if (!isPlaying) {
                    playNext();
                }
            }
        });

    }

    @Override
    public void prepareFile(String fileName) {
        fileReady(fileName);
    }

    public void playNext() {
        Log.d(Global.TAG, "PlayerFragment/playNext:");
        if (mPlayList == null || mPlayList.size() == 0) {
            return;
        }
        if (mCurrentVideoPosition >= mPlayList.size()) {
            mCurrentVideoPosition = 0;
        }
        String videoName = mPlayList.get(mCurrentVideoPosition);
        mCurrentVideoPosition++;
        fileReady(videoName);
    }

    private void fileReady(final String videoName) {
        Log.d(Global.TAG, "PlayerFragment/fileReady:");
        if (videoName == null) {
            return;
        }
        mCurrentFileName = videoName;
        mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mHandler.sendEmptyMessage(MSG_ERROR);
                return true;
            }
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mHandler.sendEmptyMessage(MSG_READY);
            }
        });
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mHandler.sendEmptyMessage(MSG_COMPLETED);
            }
        });
        mVideoView.setVideoPath(Global.LOCAL_MEDIA_DIR + videoName);
    }

    public void playDelay(long delay) {
        mHandler.sendEmptyMessageDelayed(MSG_PLAYING, delay);
    }

    public boolean playerState() {
        return isPlaying;
    }

    static class PlayerStateHandler extends Handler {

        private WeakReference<PlayerFragment1> videoPlayerFragmentWR;

        public PlayerStateHandler(PlayerFragment1 playerFragment) {
            this.videoPlayerFragmentWR = new WeakReference<>(playerFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            if (videoPlayerFragmentWR.get() == null) {
                return;
            }
            switch (msg.what) {
                case MSG_READY:
                    videoPlayerFragmentWR.get().ready();
                    break;
                case MSG_PLAYING:
                    videoPlayerFragmentWR.get().play();
                    break;
                case MSG_COMPLETED:
                    videoPlayerFragmentWR.get().complete();
                    break;
                case MSG_ERROR:
                    videoPlayerFragmentWR.get().error();
                    break;
                default:
                    break;
            }
        }
    }
}
