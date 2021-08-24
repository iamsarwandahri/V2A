package com.example.v2a;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button pick, play, convert;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private String sourcePath, destPath, destDirectory, fileName;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                sourcePath = "/storage/emulated/0/music.mp4";
                destDirectory = "/storage/emulated/0/V2A/";
                fileName = "Myaudio.mp3";
                destPath = destDirectory+fileName;

                File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "V2A");
                if (!mediaStorageDir.exists()) {
                    if (!mediaStorageDir.mkdirs()) {
                        Log.d("App", "failed to create directory");
                    }
                }

                pick = findViewById(R.id.pick);
                play = findViewById(R.id.play);
                convert = findViewById(R.id.convert);
                surfaceView = findViewById(R.id.surfaceView);

                play.setVisibility(View.INVISIBLE);
                convert.setVisibility(View.INVISIBLE);

                pick.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setType("video/");
                    startActivityForResult(intent,355);
                });

                play.setOnClickListener(v -> {
                    if(mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                        play.setText("play");
                    }
                    else{
                        mediaPlayer.start();
                        play.setText("pause");
                    }
                });


                AudioExtractor audioExtractor = new AudioExtractor();
                convert.setOnClickListener(v -> {

                    try {
                        audioExtractor.VideotoAudio(sourcePath,destPath,true,false);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                    alert.setCancelable(true);
                    alert.setCancelable(false);
                    alert.setTitle("Convert Finished");
                    alert.setMessage("Your file is saved in: "+destPath);
                    alert.setPositiveButton("close", (dialog, which) -> {
                        dialog.dismiss();
                    });alert.show();

                });
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();

            }
        }).check();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 355) {

                Uri selectedVideoUri = data.getData();
                // OI FILE Manager
//                String filemanagerstring = selectedImageUri.getPath();
                // MEDIA GALLERY
                String selectedVideoPath = getPath(selectedVideoUri);
                if (selectedVideoPath != null) {
                    String[] array = selectedVideoPath.split("/",0);
                    fileName = array[array.length-1].replace("mp4","mp3");
                     sourcePath = selectedVideoPath;
                     destPath = destDirectory+fileName;
                }

                surfaceView = findViewById(R.id.surfaceView);
                play = findViewById(R.id.play);
                convert = findViewById(R.id.convert);

                play.setVisibility(View.VISIBLE);
                convert.setVisibility(View.VISIBLE);

                surfaceView.setKeepScreenOn(true);
                mediaPlayer = MediaPlayer.create(this,data.getData());
                SurfaceHolder surfaceHolder = surfaceView.getHolder();
                surfaceHolder.addCallback(new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(@NonNull SurfaceHolder holder) {
                        mediaPlayer.setDisplay(surfaceHolder);
                        mediaPlayer.start();
                        mediaPlayer.pause();
                    }

                    @Override
                    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    }

                    @Override
                    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    }
                });
            }
        }
    }

    // UPDATED!
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Video.Media.DATA };
        @SuppressLint("Recycle") Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            // HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            // THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }


}