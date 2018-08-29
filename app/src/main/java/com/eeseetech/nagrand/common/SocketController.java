package com.eeseetech.nagrand.common;

import android.os.Message;
import android.os.Process;
import android.util.Log;

import com.eeseetech.nagrand.App;
import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.client.Ack;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketController {

    private StateCallback mCallback;
    private Socket mSocket;
    private boolean socketConnected = false;

    public interface StateCallback {

        void onLoadFile(String fileName);

        void onStartPlay(long delay);

    }

    public SocketController(StateCallback callback) {
        this.mCallback = callback;
        this.mSocket = connectXServer();
    }

    public void setCallback(StateCallback callback) {
        this.mCallback = callback;
    }

    private Socket connectSocket() {
        Socket socket = null;
        String gatewayIp = Utils.getGatewayIpAddress(App.getInstance());
        Log.d(Global.TAG, "SocketChannel/connectSocket:" + gatewayIp);
        if (!"0.0.0.1".equals(Utils.getGatewayIpAddress(App.getInstance()))) {
            try {
                socket = IO.socket("http://" + gatewayIp + ":3002");
            } catch (URISyntaxException e) {
                Log.e(Global.TAG, "SocketChannel/connectSocket:" + e.toString());
                e.printStackTrace();
            }
        }
        return socket;
    }

    private Socket connectXServer() {
        Log.d(Global.TAG, "SocketChannel/connectXServer:");
        Socket socket = connectSocket();
        if (socket != null && !socket.connected()) {
            socket.on("socket:connect", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(Global.TAG, "SocketController/call:connect");
                    socketConnected = true;
                    sendAppConnect();
                }
            });
            socket.on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.d(Global.TAG, "SocketController/call:disconnect");
                    socketConnected = false;
                    Global.appConnected = false;
                }
            });
            socket.on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(Global.TAG, "SocketController/call:connect_error");
                    socketConnected = false;
                    Global.appConnected = false;
                }
            });
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    Log.e(Global.TAG, "SocketController/call:connect_timeout");
                    socketConnected = false;
                    Global.appConnected = false;
                }
            });
            socket.on("x:load:file", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    String fileName = (String) args[0];
                    if (mCallback != null) {
                        mCallback.onLoadFile(fileName);
                    }
                }
            });
            socket.on("x:play", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    long time = 0;
                    long delay = 0;
                    long wait;
                    try {
                        time = data.getLong("time");
                        delay = data.getLong("delay");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e(Global.TAG, "SocketController/call:" + e.toString());
                    }
                    wait = delay - (System.currentTimeMillis() + Global.timeGap - time);
                    if (wait < 0) {
                        wait = 0;
                    }
                    if (mCallback != null) {
                        mCallback.onStartPlay(wait);
                    }
                }
            });
            return socket.connect();
        }
        return socket;
    }

    public void sendAppConnect() {
        Log.d(Global.TAG, "SocketController/sendAppConnect:");
        emit("app:connect", null, new Ack() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                String serverMac = null;
                try {
                    serverMac = data.getString("mac");
                    Global.appConnected = true;
                    Global.gateMacAddress = serverMac;
                } catch (JSONException e) {
                    Log.e(Global.TAG, "SocketController/sendAppConnect:" + e.toString());
                }
                Log.i(Global.TAG, "app connected to x-server.");
                Log.i(Global.TAG, "x-server mac: " + serverMac);
            }

        });
    }

    public void sendAppDisconnect() {
        emit("app:disconnect");
        Log.i(Global.TAG, "app disconnect to x-server.");
        Global.appConnected = false;
    }

    public void syncTime() {
        if (!socketConnected) {
            return;
        }
        final int emitTimes = 3;
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
        final Vector<Long> times = new Vector<>(3);
        final AtomicInteger resultNum = new AtomicInteger(0);
        for (int i = 0; i < emitTimes; i++) {
            JSONObject params = new JSONObject();
            try {
                params.put("time", System.currentTimeMillis());
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            emit("app:time:sync", params, new Ack() {
                @Override
                public void call(Object... args) {
                    JSONObject data = (JSONObject) args[0];
                    long gap, time, result;
                    try {
                        gap = data.getLong("gap");
                        time = data.getLong("time");
                    } catch (JSONException e) {
                        return;
                    }
                    result = (time - System.currentTimeMillis() + gap) / 2;
                    synchronized (times) {
                        times.add(result);
                        if (resultNum.getAndIncrement() >= emitTimes) {
                            times.notifyAll();
                        }
                    }
                    if (Global.timeGap == 0) {
                        Global.timeGap = result;
                    }
                }
            });
        }
        synchronized (times) {
            try {
                times.wait(4000);
            } catch (InterruptedException e) {
                Log.e(Global.TAG, "SocketController/syncTime:" + e.toString());
                e.printStackTrace();
            }
            if (times.size() == 0) {
                return;
            }
            if (times.size() > 4) {
                Collections.sort(times);
                times.remove(0);
                times.remove(times.size() - 1);
            }
            long sum = 0;
            for (long time : times) {
                if (Global.LOG) {
                    Log.d(Global.TAG, "SocketChannel/syncTime:time = " + time);
                }
                sum += time;
            }
            long gapTime = sum / times.size();
            Global.timeGap = gapTime;
            Log.i(Global.TAG, "current time: " + System.currentTimeMillis());
            Log.i(Global.TAG, "time gap:" + gapTime);
        }
    }


    public void emit(final String event, final Object... args) {
        if (mSocket != null) {
            JSONObject params;
            if (args.length > 0 && args[0] != null) {
                params = (JSONObject) args[0];
            } else {
                params = new JSONObject();
            }
            try {
                params.put("mac", Utils.getMacAddress(App.getInstance()));
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }
            if (args.length > 1) {
                mSocket.emit(event, params, args[1]);
            } else {
                mSocket.emit(event, params);
            }
        }
    }

    public void checkSocketState() {
        if (mSocket == null || !mSocket.connected()) {
            mSocket = connectXServer();
        }
    }
}
