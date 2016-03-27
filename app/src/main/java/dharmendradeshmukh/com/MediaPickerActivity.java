package dharmendradeshmukh.com;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import java.io.File;

/**
 * Created by dharmendra on 3/27/16.
 */
public abstract class MediaPickerActivity extends AppCompatActivity {
    private static final int IMAGE_DIMENSION = 500;

    protected static final int REQ_CODE_TAKE_FROM_CAMERA = 0;
    protected static final int REQ_CODE_PICK_FROM_GALLERY = 1;
    protected static final int REQ_CODE_CROP_PHOTO = 2;
    protected static final int REQ_CODE_RECORD_VIDEO = 3;

    private static final String IMAGE_UNSPECIFIED = "image/*";

    private boolean isTakenFromCamera;
    private Uri capturedImageUri;
    protected Bitmap capturedImageBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * This method use for take picture from camera and crop function
     */

    protected void openCamera() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Construct temporary image path and name to save the taken
        // photo
        capturedImageUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), "tmp_"
                + String.valueOf(System.currentTimeMillis()) + ".jpg"));
        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, capturedImageUri);
        intent.putExtra("return-data", true);
        try {
            // Start a camera capturing activity
            // REQUEST_CODE_TAKE_FROM_CAMERA is an integer tag you
            // defined to identify the activity in onActivityResult()
            // when it returns
            startActivityForResult(intent, REQ_CODE_TAKE_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        isTakenFromCamera = true;

        return;

    }

    /**
     * This method use for open gallery and crop image using default crop of
     * android
     */
    protected void openGallery() {
        Intent inteng = new Intent();
        // call android default gallery
        inteng.setType("image/*");
        inteng.setAction(Intent.ACTION_GET_CONTENT);
        // for crop image
        inteng.putExtra("crop", "true");
        inteng.putExtra("aspectX", 1);
        inteng.putExtra("aspectY", 1);
        inteng.putExtra("outputX", IMAGE_DIMENSION);
        inteng.putExtra("outputY", IMAGE_DIMENSION);

        try {

            inteng.putExtra("return-data", true);
            startActivityForResult(Intent.createChooser(inteng, "Complete action using"), REQ_CODE_PICK_FROM_GALLERY);

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

    }

    protected void openVideoCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
        intent.putExtra("EXTRA_VIDEO_QUALITY", 0);
        startActivityForResult(intent, this.REQ_CODE_RECORD_VIDEO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_TAKE_FROM_CAMERA:
                if (resultCode == RESULT_OK) {
                    // Send image taken from camera for cropping
                    cropImage();
                } else {
                    onMediaPickCanceled(REQ_CODE_TAKE_FROM_CAMERA);
                }
                break;
            case REQ_CODE_PICK_FROM_GALLERY:
                if (resultCode == RESULT_OK && null != data.getExtras()) {
                    capturedImageBitmap = data.getExtras().getParcelable("data");
                    onGalleryImageSelected(capturedImageBitmap);
                } else {
                    onMediaPickCanceled(REQ_CODE_PICK_FROM_GALLERY);
                }
                break;
            case REQ_CODE_CROP_PHOTO:
                if (resultCode == RESULT_OK) {
                    // Set the picture image in UI
                    if (data.getExtras() != null) {
                        capturedImageBitmap = (Bitmap) data.getExtras().getParcelable("data");
                        onCameraImageSelected(capturedImageBitmap);
                    }

                    // Delete temporary image taken by camera after crop.
                    if (isTakenFromCamera) {
                        File file = new File(capturedImageUri.getPath());
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                } else {
                    onMediaPickCanceled(REQ_CODE_CROP_PHOTO);
                }
                break;
            case REQ_CODE_RECORD_VIDEO:
                if (resultCode == RESULT_OK) {
                    Uri vid = data.getData();
                    onVideoCaptured(getVideoPathFromURI(vid));
                } else {
                    onMediaPickCanceled(REQ_CODE_RECORD_VIDEO);
                }
                break;

            default:
                break;
        }
    }

    /*
     * This method use for Crop image taken from camera
     */
    private void cropImage() {
        // Use existing crop activity.
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(capturedImageUri, IMAGE_UNSPECIFIED);

        // Specify image size
        intent.putExtra("outputX", IMAGE_DIMENSION);
        intent.putExtra("outputY", IMAGE_DIMENSION);

        // Specify aspect ratio, 1:1
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", true);
        // REQUEST_CODE_CROP_PHOTO is an integer tag you defined to
        // identify the activity in onActivityResult() when it returns
        startActivityForResult(intent, REQ_CODE_CROP_PHOTO);
    }

    private String getVideoPathFromURI(Uri contentUri) {
        String videoPath = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            videoPath = cursor.getString(column_index);
        }
        cursor.close();
        return videoPath;
    }

    protected abstract void onGalleryImageSelected(Bitmap bitmap);

    protected abstract void onCameraImageSelected(Bitmap bitmap);

    protected abstract void onVideoCaptured(String videoPath);

    protected abstract void onMediaPickCanceled(int reqCode);
}
