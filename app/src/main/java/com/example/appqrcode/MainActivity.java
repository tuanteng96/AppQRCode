package com.example.appqrcode;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.internal.DialogRedirect;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    CameraView cameraView;
    boolean isDetected = false;
    Button btn_start;

    FirebaseVisionBarcodeDetector detector;
    FirebaseVisionBarcodeDetectorOptions detectorOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dexter.withActivity(this).withPermission(Manifest.permission.CAMERA).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                setupCamera();
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                Toast.makeText(MainActivity.this, "Camera Denied", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

            }
        }).check();
    }

    private void setupCamera() {
        btn_start = (Button) findViewById(R.id.btn_again);
        btn_start.setEnabled(isDetected);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDetected = !isDetected;
            }
        });
        cameraView = findViewById(R.id.cameraView);
        cameraView.setLifecycleOwner(this);
        cameraView.addFrameProcessor(new FrameProcessor() {
            @Override
            public void process(@NonNull Frame frame) {
                processImage();
            }
        });
    }

    private void  processImage(FirebaseVisionImage image) {
        if(!isDetected) {
            detector.detectInImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                @Override
                public void onSuccess(@NonNull List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                    processResult(firebaseVisionBarcodes);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void processResult(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        if (firebaseVisionBarcodes.size() > 0) {
            isDetected = true;
            btn_start.setEnabled(isDetected);
            for(FirebaseVisionBarcode item:firebaseVisionBarcodes){
                int value_item = item.getValueType();
                switch (value_item) {
                    case  FirebaseVisionBarcode.TYPE_TEXT:
                    {
                        createDialog(item.getRawBytes());
                    }
                }
            }
        }
    }

    private void createDialog(String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(text).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
    }

    private Object getvisionImageFromFrame(Frame frame) {
        byte[] data = frame.getData();
        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setHeight(frame.getSize().getHeight())
                .setWidth(frame.getSize().getWidth())
                .build();
        return FirebaseVisionImage.fromByteArray(data,metadata);
    }
}