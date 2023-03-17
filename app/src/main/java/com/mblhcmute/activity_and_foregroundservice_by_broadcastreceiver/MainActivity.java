package com.mblhcmute.activity_and_foregroundservice_by_broadcastreceiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private Button btnStartService;
    private Button btnStopService;

    private RelativeLayout layoutBottom;
    private ImageView imgClear;
    private ImageView imgSong;
    private ImageView imgPlayOrPause;
    private TextView tvTitleSong, tvSingleSong;

    private Song mSong;
    private boolean isPlaying;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle == null)
                return;
            else {
                mSong = (Song) bundle.get("object_song");
                isPlaying = bundle.getBoolean("status_player");
                int actionMusic = bundle.getInt("action_music");
                handleLayoutMusic(actionMusic);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("send_data_to_activity"));
        btnStartService = findViewById(R.id.btn_start_service);
        btnStopService = findViewById(R.id.btn_stop_service);
        layoutBottom = findViewById(R.id.Layout_bottom);
        imgClear = findViewById(R.id.img_clear);
        imgSong = findViewById(R.id.img_song);
        imgPlayOrPause = findViewById(R.id.img_play_or_pause);
        tvTitleSong = findViewById(R.id.tv_title_song);
        tvSingleSong = findViewById(R.id.tv_single_song);


        btnStartService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickStartService();
            }
        });
        btnStopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickStopService();
            }
        });
    }

    private void clickStopService() {
        Intent intent = new Intent(this, MyService.class);
        stopService(intent);
    }

    private void clickStartService() {
        Song song = new Song("Let it go", "Idina Menzel", R.drawable.img_music, R.raw.file_music);
        Intent intent = new Intent(this, MyService.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("object_song", song);
        intent.putExtras(bundle);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void showInforSong() {
        if (mSong == null) {
            return;
        }

        imgSong.setImageResource(mSong.getImage());
        tvSingleSong.setText(mSong.getSinger());
        tvTitleSong.setText(mSong.getTitle());

        imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying) {
                    sendActionToService(MyService.ACTION_PAUSE);
                } else
                    sendActionToService(MyService.ACTION_RESUME);
            }
        });
        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendActionToService(MyService.ACTION_CLEAR);
            }
        });
    }

    private void handleLayoutMusic(int action) {
        switch (action) {
            case MyService.ACTION_PAUSE:
                setStatusButtonPlayOrPause();
                break;
            case MyService.ACTION_RESUME:
                setStatusButtonPlayOrPause();
                break;
            case MyService.ACTION_CLEAR:
                layoutBottom.setVisibility(View.GONE);
                break;
            case MyService.ACTION_START:
                layoutBottom.setVisibility(View.VISIBLE);
                showInforSong();
                setStatusButtonPlayOrPause();
                break;
        }
    }


    private void setStatusButtonPlayOrPause() {
        if (isPlaying) {
            imgPlayOrPause.setImageResource(R.drawable.pause);
        } else
            imgPlayOrPause.setImageResource(R.drawable.play);
    }

    private void sendActionToService(int action) {
        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("acton_music_service", action);
        startService(intent);
    }
}