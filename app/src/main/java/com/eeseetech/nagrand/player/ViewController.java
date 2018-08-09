package com.eeseetech.nagrand.player;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.R;
import com.eeseetech.nagrand.view.MediaView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

public class ViewController implements MediaView {

    private static final int MSG_READY = 10;
    private static final int MSG_PLAYING = 11;
    private static final int MSG_COMPLETED = 12;
    private static final int MSG_ERROR = 13;
    private static final int PLAYING = 15;
    private static final int IDLE = 17;

    private int mCurrentState = IDLE;
    private int mCurrentVideoPosition = 0;
    private boolean isPlaying = false;
    private boolean needHidden = false;


    private String mCurrentFileName;
    private List<String> mPlayList;


    private PlayerStateHandler mHandler;
    private FrameLayout mIdleStateView;
    private PlayerView mPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private View mView;
    private Context mContext;
    private PlayerPresenter mPresenter;

    private EventListener mEventListener = new EventListener() {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.d(Global.TAG, "ViewController/onPlayerStateChanged:playWhenReady:" + playWhenReady + ",playbackState:" + playbackState);
            switch (playbackState) {
                case Player.STATE_ENDED:
                    if (mCurrentState == PLAYING) {
                        mHandler.sendEmptyMessage(MSG_COMPLETED);
                    }
                    break;
                case Player.STATE_READY:
                    if (mCurrentState == IDLE) {
                        mHandler.sendEmptyMessage(MSG_READY);
                    }
                    break;
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            super.onPlayerError(error);
            mHandler.sendEmptyMessage(MSG_ERROR);
        }
    };

    public ViewController(View view, Context context) {
        this.mView = view;
        this.mContext = context;
        mHandler = new PlayerStateHandler(this);
        initData();
        initView(this.mView);
    }

    private void initData() {
        mPresenter = new PlayerPresenter(this);
    }

    private void initView(View view) {
        setPlayerView(view);
        //setIdleView(view);
    }

    private void setPlayerView(View view) {
        mPlayerView = view.findViewById(R.id.exoplayer_view);
        RenderersFactory renderersFactory = new DefaultRenderersFactory(mContext);
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        mExoPlayer.addListener(mEventListener);
        mPlayerView.setPlayer(mExoPlayer);
        mPlayerView.setUseController(false);
    }

    /*private void setIdleView(View view) {
        mIdleStateView = (FrameLayout) view.findViewById(R.id.layout_idle_state);
        showIdleView();
    }*/

    /*private void showIdleView() {
        mIdleStateView.setVisibility(View.VISIBLE);
    }

    private void hideIdleView() {
        mIdleStateView.setVisibility(View.INVISIBLE);
    }*/

    private void playVideo() {
        mExoPlayer.setPlayWhenReady(true);
    }

    private void play() {
        //hideIdleView();
        isPlaying = true;
        playVideo();
        mCurrentState = PLAYING;
        mPresenter.addPlayHistory(mCurrentFileName, String.valueOf(System.currentTimeMillis()));
    }

    public void start() {
        Log.d(Global.TAG, "ViewController/start:");
        mPresenter.start();
        needHidden = false;
        /*mExoPlayer.setPlayWhenReady(true);
        isPlaying = true;*/
    }

    public void stop() {
        Log.d(Global.TAG, "ViewController/stop:");
        mPresenter.stop();
        needHidden = true;
        mExoPlayer.stop();
        isPlaying = false;
    }


    private void complete() {
        Log.d(Global.TAG, "ViewController/complete:");
        mCurrentState = IDLE;
        isPlaying = false;
        if(!needHidden) {
            playNext();
        }
    }

    private void error() {
        mCurrentState = IDLE;
        mPresenter.deleteFile(mCurrentFileName);
    }

    private void ready() {
        mHandler.sendEmptyMessage(MSG_PLAYING);
    }


    @Override
    public Context getContext() {
        return mContext;
    }

    @Override
    public void replaceList(final List<String> playList) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (playList == null || playList.size() == 0) {
                    return;
                }
                mPlayList = playList;
                Log.d(Global.TAG, "ViewController/replaceList:playing ?" + isPlaying);
                if (!isPlaying) {
                    playNext();
                }
            }
        });
    }

    @Override
    public void prepareFile(String fileName) {

    }

    @Override
    public void playDelay(long delay) {

    }

    @Override
    public boolean playerState() {
        return isPlaying;
    }

    private void playNext() {
        Log.d(Global.TAG, "ViewController/playNext:");
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
        Log.d(Global.TAG, "ViewController/fileReady:" + videoName);
        if (videoName == null) {
            return;
        }
        mCurrentFileName = videoName;
        File file = new File(Global.LOCAL_MEDIA_DIR + videoName);
        Uri fileUri;
        if (mContext != null) {
            if (file.exists()) {
                fileUri = Uri.fromFile(file);
                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(mContext, Util.getUserAgent(mContext, "com.eeseetech.nagrand"));
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(fileUri);
                mExoPlayer.prepare(mediaSource);
            } else {
                playNext();
                Log.e(Global.TAG, "ViewController/fileReady:" + videoName + " not exist.");
            }
        } else {
            Log.e(Global.TAG, "ViewController/fileReady: context is null");
        }
    }

    static class PlayerStateHandler extends Handler {

        private WeakReference<ViewController> viewControllerWR;

        public PlayerStateHandler(ViewController viewController) {
            this.viewControllerWR = new WeakReference<>(viewController);
        }

        @Override
        public void handleMessage(Message msg) {
            ViewController viewController = viewControllerWR.get();
            if (viewController == null) {
                return;
            }
            switch (msg.what) {
                case MSG_READY:
                    viewController.ready();
                    break;
                case MSG_PLAYING:
                    viewController.play();
                    break;
                case MSG_COMPLETED:
                    viewController.complete();
                    break;
                case MSG_ERROR:
                    viewController.error();
                    break;
                default:
                    break;
            }
        }
    }

    static abstract class EventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

        }

        @Override
        public void onLoadingChanged(boolean isLoading) {

        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {

        }

        @Override
        public void onPositionDiscontinuity(int reason) {

        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    }

}
