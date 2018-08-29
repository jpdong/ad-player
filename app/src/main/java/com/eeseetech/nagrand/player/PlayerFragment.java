package com.eeseetech.nagrand.player;

import android.app.Fragment;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
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
import com.eeseetech.nagrand.entity.MessageInfo;
import com.eeseetech.nagrand.view.IMessage;
import com.eeseetech.nagrand.view.MediaView;
import com.eeseetech.nagrand.view.MessageView;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;


/**
 * Created by dongjiangpeng@eeseetech.com on 2017/4/6.
 */

public class PlayerFragment extends Fragment implements MediaView,IMessage {

    private static final int MSG_READY = 10;
    private static final int MSG_PLAYING = 11;
    private static final int MSG_COMPLETED = 12;
    private static final int MSG_ERROR = 13;
    private static final int READY = 14;
    private static final int PLAYING = 15;
    private static final int IDLE = 17;

    private int mCurrentState = IDLE;
    private int mCurrentVideoPosition = 0;
    private int mCurrentMsgPosition = 0;
    private boolean isPlaying = false;
    private boolean isMessageShowing = false;



    private String mCurrentFileName;
    private List<String> mPlayList;
    private List<MessageInfo> mMsgList;

    private PlayerStateHandler mHandler;
    private FrameLayout mIdleStateView;
    private MessageView mMessageView;
    private CompositeDisposable mCompositeDisposable;
    private PlayerView mPlayerView;
    private SimpleExoPlayer mExoPlayer;
    private PlayerPresenter mPresenter;

    private ViewController.EventListener mEventListener = new ViewController.EventListener() {

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

    public PlayerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new PlayerStateHandler(this);
        mCompositeDisposable = new CompositeDisposable();
        mPresenter = new PlayerPresenter(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exoplayer, container, false);
        setPlayerView(view);
        setIdleView(view);
        setMessageView(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Global.LOG) {
            Log.d(Global.TAG, "PlayerFragment/onStart:");
        }
        mPresenter.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Global.LOG) {
            Log.d(Global.TAG, "PlayerFragment/onStop:");
        }
        mPresenter.stop();
        mExoPlayer.stop();
        isPlaying = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (Global.LOG) {
            Log.d(Global.TAG, "PlayerFragment/onDestroyView:");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setPlayerView(View view) {
        mPlayerView = view.findViewById(R.id.exoplayer_view);
        RenderersFactory renderersFactory = new DefaultRenderersFactory(getContext());
        DefaultTrackSelector trackSelector = new DefaultTrackSelector();
        LoadControl loadControl = new DefaultLoadControl();
        mExoPlayer = ExoPlayerFactory.newSimpleInstance(renderersFactory, trackSelector, loadControl);
        mExoPlayer.addListener(mEventListener);
        mPlayerView.setPlayer(mExoPlayer);
        mPlayerView.setUseController(false);
    }

    private void setIdleView(View view) {
        mIdleStateView = (FrameLayout) view.findViewById(R.id.layout_idle_state);
        mIdleStateView.setVisibility(View.VISIBLE);
    }

    private void setMessageView(View view) {
        mMessageView = (MessageView) view.findViewById(R.id.message_view);
        mMessageView.setSingleLine();
        mMessageView.setStatusCallback(new MessageView.StatusCallback() {
            @Override
            public void finish() {
                isMessageShowing = false;
                showNextMessage();
            }
        });
    }

    private void playVideo() {
        mExoPlayer.setPlayWhenReady(true);
    }

    private void play() {
        Log.d(Global.TAG, "PlayerFragment/play:");
        mIdleStateView.setVisibility(View.GONE);
        isPlaying = true;
        playVideo();
        mCurrentState = PLAYING;
        mPresenter.sendPlayerStatus("play");
        mPresenter.addPlayHistory(mCurrentFileName, String.valueOf(System.currentTimeMillis()));
    }

    private void complete() {
        mCurrentState = IDLE;
        isPlaying = false;
        mIdleStateView.setVisibility(View.VISIBLE);
        mPresenter.sendPlayerStatus("idle");
        if (!Global.hasXServer) {
            playNext();
        }
    }

    private void error() {
        mCurrentState = IDLE;
        mPresenter.sendPlayerStatus("error");
        mPresenter.deleteFile(mCurrentFileName);
    }

    private void ready() {
        mCurrentState = READY;
        mPresenter.sendPlayerStatus("ready");
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

    @Override
    public Context getContext() {
        return getActivity();
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
        File file = new File(Global.LOCAL_MEDIA_DIR + videoName);
        Uri fileUri;
        if (getContext() != null) {
            if (file.exists()) {
                fileUri = Uri.fromFile(file);
                DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "com.eeseetech.nagrand"));
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                final MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory).setExtractorsFactory(extractorsFactory).createMediaSource(fileUri);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mExoPlayer.prepare(mediaSource);
                    }
                });
            } else {
                mPresenter.deleteFile(videoName);
                mPlayList.remove(videoName);
                playNext();
                Log.e(Global.TAG, "PlayerFragment/fileReady:" + videoName + " not exist.");
            }
        } else {
            Log.e(Global.TAG, "PlayerFragment/fileReady: context is null");
        }
    }

    public void playDelay(long delay) {
        mHandler.sendEmptyMessageDelayed(MSG_PLAYING, delay);
    }

    public boolean playerState() {
        return isPlaying;
    }

    @Override
    public void replaceMessages(final List<MessageInfo> msgList) {
        mHandler.post(new Runnable() {
            public void run() {
                if (msgList == null) {
                    return;
                }
                mMsgList = msgList;
                if (!isMessageShowing) {
                    showNextMessage();
                }
            }
        });
    }

    public void showNextMessage() {
        if (mMsgList.size() == 0) {
            return;
        }
        if (mCurrentMsgPosition >= mMsgList.size()) {
            mCurrentMsgPosition = 0;
        }
        MessageInfo messageInfo = mMsgList.get(mCurrentMsgPosition);
        mCurrentMsgPosition++;
        mMessageView.setText(messageInfo.message);
        setPosition(messageInfo.position);
        mMessageView.showMessage();
        isMessageShowing = true;
    }

    private void setPosition(String position) {
        RelativeLayout.LayoutParams messageParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if ("0".equals(position)) {
            //messageParams.setMargins(0, 0, 0, 0);
            messageParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        } else {
            //messageParams.setMargins(0, getActivity().getWindowManager().getDefaultDisplay().getHeight() - 35, 0, 0);
            messageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }
        mMessageView.setLayoutParams(messageParams);
    }

    static class PlayerStateHandler extends Handler {

        private WeakReference<PlayerFragment> playerFragmentWR;

        public PlayerStateHandler(PlayerFragment playerFragment) {
            this.playerFragmentWR = new WeakReference<>(playerFragment);
        }

        @Override
        public void handleMessage(Message msg) {
            PlayerFragment fragment = playerFragmentWR.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case MSG_READY:
                    fragment.ready();
                    break;
                case MSG_PLAYING:
                    fragment.play();
                    break;
                case MSG_COMPLETED:
                    fragment.complete();
                    break;
                case MSG_ERROR:
                    fragment.error();
                    break;
                default:
                    break;
            }
        }
    }
}
