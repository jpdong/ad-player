package com.eeseetech.nagrand.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.eeseetech.nagrand.Global;
import com.eeseetech.nagrand.R;

public class DebugDialogFragment extends DialogFragment {

    private RecyclerView mRecyclerView;
    private TextView mPasswordView;
    private StringBuilder mPasswordBuilder;
    private CountDownTimer mTimer;

    private String[] digitals = new String[12];
    private String[] letters = new String[12];


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setData();
    }

    private void setData() {
        letters[0] = "";
        char[] chars = new char[3];
        for (int i = 0; i < 9; i++) {
            digitals[i] = String.valueOf(i + 1);
            chars[0] = (char) ('A' + 3*i);
            chars[1] = (char) ('A' + 1 + 3*i);
            chars[2] = (char) ('A' + 2 + 3*i);
            letters[i + 1] = new String(chars);
        }
        digitals[9] = "*";
        digitals[10] = "0";
        digitals[11] = "#";
        letters[8] = "WXYZ";
        letters[7] = "TUV";
        letters[6] = "PQRS";
        letters[9] = "";
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(Global.TAG, "DebugDialogFragment/onCreateView:");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(Global.TAG, "DebugDialogFragment/onCreateDialog:");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_debug, null);
        setViews(view);
        builder.setView(view);
        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(Global.TAG, "DebugDialogFragment/onResume:");
        mTimer = new CountDownTimer(7000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Log.d(Global.TAG, "DebugDialogFragment/onFinish:");
                dismiss();
            }
        };
        mTimer.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(Global.TAG, "DebugDialogFragment/onPause:");
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    private void setViews(View view) {
        mRecyclerView = view.findViewById(R.id.rv_digitalpad);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mRecyclerView.setAdapter(new DigitalAdapter(digitals, letters, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordBuilder.length() < 16) {
                    int position = mRecyclerView.getChildLayoutPosition(v);
                    mPasswordBuilder.append(digitals[position]);
                    mPasswordView.setText(mPasswordBuilder.toString());
                    if ("8231".equals(mPasswordBuilder.toString())) {
                        getActivity().finish();
                    }
                    /*if ("*#*#1#*#*".equals(mPasswordBuilder.toString())) {
                        throw new RuntimeException("test exception");
                    }*/
                }
            }
        }));
        mPasswordView = view.findViewById(R.id.tv_password);
        mPasswordView.setText("");
        mPasswordBuilder = new StringBuilder();
        ImageView deleteView = view.findViewById(R.id.iv_delete);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPasswordBuilder.length() >= 1) {
                    mPasswordBuilder.deleteCharAt(mPasswordBuilder.length() - 1);
                    mPasswordView.setText(mPasswordBuilder.toString());
                }
            }
        });
        TextView versionView = view.findViewById(R.id.tv_version);
        versionView.setText("上海意视科技有限公司2016-2018 \n 当前版本：" + Global.versionName + "（" + Global.versionCode + "）");
    }

    static class DigitalAdapter extends RecyclerView.Adapter<DigitalViewHolder> {

        private String[] digitals;
        private String[] letters;
        private View.OnClickListener listener;

        public DigitalAdapter(String[] digitals, String[] letters, View.OnClickListener listener) {
            this.digitals = digitals;
            this.letters = letters;
            this.listener = listener;
        }

        @NonNull
        @Override
        public DigitalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_digital,null);
            view.setOnClickListener(listener);
            return new DigitalViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DigitalViewHolder holder, int position) {
            holder.digitalView.setText(digitals[position]);
            holder.letterView.setText(letters[position]);
        }

        @Override
        public int getItemCount() {
            return digitals.length;
        }
    }

    static class DigitalViewHolder extends RecyclerView.ViewHolder {

        public TextView digitalView;

        public TextView letterView;

        public DigitalViewHolder(View itemView) {
            super(itemView);
            digitalView = itemView.findViewById(R.id.tv_digital);
            letterView = itemView.findViewById(R.id.tv_letter);
        }
    }
}
