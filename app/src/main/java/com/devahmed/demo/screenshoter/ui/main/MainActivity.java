package com.devahmed.demo.screenshoter.ui.main;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.devahmed.demo.screenshoter.FloatWindowsService;
import com.devahmed.demo.screenshoter.R;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends FragmentActivity implements MainMvc.Listener {


  public static final int REQUEST_MEDIA_PROJECTION = 18;
  private static final int EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 200;
  MainMvcImp mvcImp;
  private String dayOrWeak;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mvcImp = new MainMvcImp(getLayoutInflater() , null);



    setContentView(mvcImp.getRootView());
  }


  public void requestOverlayPermission(){
    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
    startActivityForResult(intent, 0);
  }

  public void requestCapturePermission() {

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
      return;
    }

    MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
        getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    startActivityForResult(
        mediaProjectionManager.createScreenCaptureIntent(),
        REQUEST_MEDIA_PROJECTION);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case REQUEST_MEDIA_PROJECTION:

        if (resultCode == RESULT_OK && data != null) {
          FloatWindowsService.setResultData(data , dayOrWeak);
          startService(new Intent(getApplicationContext(), FloatWindowsService.class));
        }
        break;
    }

  }

  @Override
  protected void onStart() {
    super.onStart();
    mvcImp.registerListener(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    mvcImp.unregisterListener(this);
  }

  @Override
  public void onTakeScreenShotBtnClicked(String dayOrWeak) {
    this.dayOrWeak = dayOrWeak;
    if(!checkPermission()) {

      requestPermission();

    } else {
      //permission already granted
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (!Settings.canDrawOverlays(this)) {
        requestOverlayPermission();
      }
    }
    requestCapturePermission();
  }


  private boolean checkPermission() {
    int result = ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE");
    int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), "android.permission.READ_EXTERNAL_STORAGE");

    return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
  }

  private void requestPermission() {

    ActivityCompat.requestPermissions(this, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE:
        if (grantResults.length > 0) {

          boolean WRITE_EXTERNAL_STORAGE = grantResults[0] == PackageManager.PERMISSION_GRANTED;
          boolean READ_EXTERNAL_STORAGE = grantResults[1] == PackageManager.PERMISSION_GRANTED;

          if (WRITE_EXTERNAL_STORAGE && READ_EXTERNAL_STORAGE)
            Toast.makeText(this, "Permission Granted, Now you can take screen shots.", Toast.LENGTH_SHORT).show();
         else {
            Toast.makeText(this, "Permission Denied, You cannot take screen shots.", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("You need to allow access to both the permissions",
                        new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                              requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"},
                                      EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE);
                            }
                          }
                        });
                return;
              }
            }

          }
        }
        break;
    }
  }


  private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
    new AlertDialog.Builder(MainActivity.this)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show();
  }
}
