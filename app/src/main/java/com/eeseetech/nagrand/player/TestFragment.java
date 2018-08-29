package com.eeseetech.nagrand.player;


import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.R;

public class TestFragment extends Fragment {

    ViewController viewController;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exoplayer, container, false);
        viewController = new ViewController(view, getActivity());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(Global.TAG, "TestFragment/onStart:");
        viewController.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(Global.TAG, "TestFragment/onStop:");
        viewController.stop();
    }
}
