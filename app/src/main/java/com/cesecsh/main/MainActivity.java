package com.cesecsh.main;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import com.cesecsh.statusframelayout.OnRetryListener;
import com.cesecsh.statusframelayout.StatusFrameLayout;

public class MainActivity extends AppCompatActivity {

    private StatusFrameLayout rootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rootView = (StatusFrameLayout) findViewById(R.id.root);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rootView.setStatus(StatusFrameLayout.ERROR);
            }
        }, 3000);
        rootView.setOnRetryListener(new OnRetryListener() {
            @Override
            public void onRetry() {
                rootView.setStatus(StatusFrameLayout.LOADING);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rootView.setStatus(StatusFrameLayout.SUCCESS);
                    }
                }, 3000);

            }
        });
    }
}
