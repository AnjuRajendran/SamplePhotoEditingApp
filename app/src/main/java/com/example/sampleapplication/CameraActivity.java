package com.example.sampleapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
//import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;

public class CameraActivity extends AppCompatActivity {

    TextureView view_finder;
    ImageButton imgCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        view_finder=findViewById(R.id.view_finder);
        imgCapture=findViewById(R.id.imgCapture);

        startCamera();
    }

  //  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void startCamera(){

        CameraX.unbindAll();
        Rational aspectRatio=new Rational(view_finder.getWidth(),view_finder.getHeight());
        Size screen = new Size(view_finder.getWidth(),view_finder.getHeight());

        PreviewConfig pConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        Preview preview=new Preview(pConfig);
        preview.setOnPreviewOutputUpdateListener(new Preview.OnPreviewOutputUpdateListener(){
            @Override
            public void onUpdated(Preview.PreviewOutput output) {
                ViewGroup parent=(ViewGroup)view_finder.getParent();
                parent.removeView(view_finder);
                parent.addView(view_finder,0);

                view_finder.setSurfaceTexture(output.getSurfaceTexture());
                updateTransform();
            }
        });

        ImageCaptureConfig imageCaptureConfig= new ImageCaptureConfig.Builder().setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation()).build();

        final ImageCapture imgCap=new ImageCapture(imageCaptureConfig);

        imgCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file =new File(Environment.getExternalStorageDirectory()+"/"+System.currentTimeMillis()+".jpg");
                imgCap.takePicture(file, new ImageCapture.OnImageSavedListener() {
                    @Override
                    public void onImageSaved(@NonNull File file) {
                        String msg="Pic captured at"+file.getAbsolutePath();
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();

                        Intent intent= new Intent(CameraActivity.this,ShowPhotoActivity.class);
                        intent.putExtra("path",file.getAbsoluteFile()+"");
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                        String msg="Pic captured Failed"+message;
                        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_SHORT).show();

                        if(cause!=null){

                            cause.printStackTrace();
                        }
                    }
                });
            }
        });

CameraX.bindToLifecycle((LifecycleOwner)this,preview,imgCap);
    }

    public void updateTransform(){
        Matrix mx=new Matrix();
        float w=view_finder.getMeasuredWidth();
        float h=view_finder.getMeasuredHeight();

        float cx=w/2f;
        float cy=h/2f;

        int rotationDgr=90;
        int rotation=(int)view_finder.getRotation();

        switch(rotation){
            case Surface
                    .ROTATION_0:
                rotationDgr=0;
            break;
            case Surface
                    .ROTATION_90:
                rotationDgr=90;
                break;
            case Surface
                    .ROTATION_180:
                rotationDgr=180;
                break;
            case Surface
                    .ROTATION_270:
                rotationDgr=270;
                break;
            default:
                return;
        }
        mx.postRotate((float)rotationDgr,cx,cy);
        view_finder.setTransform(mx);
    }
}