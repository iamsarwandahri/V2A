package com.example.v2a;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
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
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button pick, play, convert;
    private MediaPlayer mediaPlayer;
    private SurfaceView surfaceView;
    private String sourcePath, destPath, destDirectory, fileName;
    private  TextView textView;
    PermissionDeniedResponse permissonDeniedResponse = null;
    PermissionGrantedResponse permissionGrantedResponse;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = findViewById(R.id.surfaceView);
        pick = findViewById(R.id.pick);
        play = findViewById(R.id.play);
        convert = findViewById(R.id.convert);
        textView = findViewById(R.id.textView);
        play.setVisibility(View.INVISIBLE);
        convert.setVisibility(View.INVISIBLE);



        Dexter.withContext(this)
                .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
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

                        convert.setOnClickListener(v -> {
                            AudioExtractor audioExtractor = new AudioExtractor();
                            try {
                                audioExtractor.VideotoAudio(sourcePath, destPath, true, false);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setCancelable(true);
                            alert.setCancelable(false);
                            alert.setTitle("Convert Finished");
                            alert.setMessage("Your file is saved in: " + destPath);
                            alert.setPositiveButton("close", (dialog, which) -> {
                                dialog.dismiss();
                            });
                            alert.show();
                        });

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                        if (response.isPermanentlyDenied() == true) {
                            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                            alert.setCancelable(false);
                            alert.setTitle("Permission");
                            alert.setMessage("Give Permission to app: go to setting>permissions>storage and enable");
                            alert.setPositiveButton("Go", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            });
                            alert.show();
//                            Intent i = new Intent(MainActivity.this, MainActivity.class);
//                            startActivity(i);
                        }

                        if (response.isPermanentlyDenied() == true) {
                            convert.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    AlertDialog.Builder alert2 = new AlertDialog.Builder(MainActivity.this);
                                    alert2.setCancelable(true);
                                    alert2.setCancelable(false);
                                    alert2.setTitle("Permission is not Given");
                                    alert2.setMessage("Restart the app and give Permisson: ");
                                    alert2.setPositiveButton("Restart", (dialog, which) -> {
                                        Intent i = new Intent(MainActivity.this, MainActivity.class);
                                        startActivity(i);
                                    });
                                    alert2.show();
                                }
                            });
                        }
                    }

                }).check();



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