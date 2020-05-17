package com.devahmed.demo.screenshoter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;


@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class FloatWindowsService extends Service {


  public static Intent newIntent(Context context, Intent mResultData) {

    Intent intent = new Intent(context, FloatWindowsService.class);

    if (mResultData != null) {
      intent.putExtras(mResultData);
    }
    return intent;
  }

  private MediaProjection mMediaProjection;
  private VirtualDisplay mVirtualDisplay;

  private static Intent mResultData = null;


  private ImageReader mImageReader;
  private WindowManager mWindowManager;
  private WindowManager.LayoutParams mLayoutParams;
  private GestureDetector mGestureDetector;

  private LinearLayout mFloatView;

  private int mScreenWidth;
  private int mScreenHeight;
  private int mScreenDensity;
  private static String _dayOrWeak;

  @Override
  public void onCreate() {
    super.onCreate();
    createFloatView();

    createImageReader();
  }

  public static Intent getResultData() {
    return mResultData;
  }

  public static void setResultData(Intent mResultData , String dayOrWeak) {
    FloatWindowsService.mResultData = mResultData;
    _dayOrWeak = dayOrWeak;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  private void createFloatView() {
    mGestureDetector = new GestureDetector(getApplicationContext(), new FloatGestrueTouchListener());
    mLayoutParams = new WindowManager.LayoutParams();
    mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

    DisplayMetrics metrics = new DisplayMetrics();
    mWindowManager.getDefaultDisplay().getMetrics(metrics);
    mScreenDensity = metrics.densityDpi;
    mScreenWidth = metrics.widthPixels;
    mScreenHeight = metrics.heightPixels;

    int LAYOUT_FLAG;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
    } else {
      LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
    }
    mLayoutParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            LAYOUT_FLAG,
             WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

    mLayoutParams.format = PixelFormat.RGBA_8888;
    mLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
    mLayoutParams.x = mScreenWidth;
    mLayoutParams.y = 500;
    mLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
    mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;



    Context context = getApplicationContext();
    mFloatView = new LinearLayout(context);
    mFloatView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 50));
    mFloatView.setOrientation(LinearLayout.VERTICAL);
    ImageView btn1 , btn2;
    btn1 = new ImageView(context);
    btn2 = new ImageView(context);
    btn1.setImageResource(R.drawable.daily_rb);
    btn2.setImageResource(R.drawable.weakly_rb);

    mFloatView.addView(btn2);
    mFloatView.addView(btn1);

    mWindowManager.addView(mFloatView, mLayoutParams);


    mFloatView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
      }
    });

    btn1.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        _dayOrWeak = "day";
        return mGestureDetector.onTouchEvent(event);
      }
    });

    btn2.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        _dayOrWeak = "weak";
        return mGestureDetector.onTouchEvent(event);
      }
    });


  }


  private class FloatGestrueTouchListener implements GestureDetector.OnGestureListener {
    int lastX, lastY;
    int paramX, paramY;

    @Override
    public boolean onDown(MotionEvent event) {
      lastX = (int) event.getRawX();
      lastY = (int) event.getRawY();
      paramX = mLayoutParams.x;
      paramY = mLayoutParams.y;
      return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      showLastScreenShot();

      return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      int dx = (int) e2.getRawX() - lastX;
      int dy = (int) e2.getRawY() - lastY;
      mLayoutParams.x = paramX + dx;
      mLayoutParams.y = paramY + dy;

      mWindowManager.updateViewLayout(mFloatView, mLayoutParams);
      return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

      startScreenShot();
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      return false;
    }
  }

  private void showLastScreenShot() {
    File imgFile = new  File(FileUtil.getScreenShotsName(getApplicationContext() , _dayOrWeak));
    if(imgFile.exists()){

      Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
      Bitmap croppedBitmap;
      Context context = getApplicationContext();
      ImageView myImage = new ImageView(context);
      ImageView closeBtn = new ImageView(context);
      final LinearLayout showImageCOntainer = new LinearLayout(context);
      int orientation = getResources().getConfiguration().orientation;
      if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
        showImageCOntainer.setOrientation(LinearLayout.VERTICAL);
        showImageCOntainer.addView(closeBtn);
        showImageCOntainer.addView(myImage);
        croppedBitmap = Bitmap.createBitmap(myBitmap
                ,myBitmap.getWidth() / 2, 0 ,
                myBitmap.getWidth()/2 , myBitmap.getHeight() );

      } else {
        showImageCOntainer.setOrientation(LinearLayout.VERTICAL);

        croppedBitmap = Bitmap.createBitmap(myBitmap ,0, myBitmap.getHeight() / 2 , myBitmap.getWidth()  , myBitmap.getHeight() /2 );

        showImageCOntainer.addView(myImage);
        showImageCOntainer.addView(closeBtn);
      }


      myImage.setImageBitmap(croppedBitmap);
      closeBtn.setImageResource(R.drawable.ic_close_black_24dp);
      closeBtn.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          mWindowManager.removeView(showImageCOntainer);
        }
      });

      mWindowManager.addView(showImageCOntainer , mLayoutParams);
    }
  }


  private void startScreenShot() {
    mFloatView.setVisibility(View.GONE);

    Handler handler1 = new Handler();
    handler1.postDelayed(new Runnable() {
      public void run() {
        //start virtual
        startVirtual();
      }
    }, 5);

    handler1.postDelayed(new Runnable() {
      public void run() {
        //capture the screen
        startCapture();

      }
    }, 30);

    handler1.postDelayed(new Runnable() {
      @Override
      public void run() {
        //show image animation effect after taking screenshot from any where
        GlobalScreenshot screenshot = new GlobalScreenshot(getApplicationContext());

        Bitmap bitmap = ((ScreenCaptureApplication) getApplication()).getmScreenCaptureBitmap();

        if (bitmap != null) {
          screenshot.takeScreenshot(bitmap, null, true, true);
        }
      }
    }, 120);
  }


  private void createImageReader() {

    mImageReader = ImageReader.newInstance(mScreenWidth, mScreenHeight, PixelFormat.RGBA_8888, 1);

  }

  public void startVirtual() {
    if (mMediaProjection != null) {
      virtualDisplay();
    } else {
      setUpMediaProjection();
      virtualDisplay();
    }
  }

  public void setUpMediaProjection() {
    if (mResultData == null) {
      Intent intent = new Intent(Intent.ACTION_MAIN);
      intent.addCategory(Intent.CATEGORY_LAUNCHER);
      startActivity(intent);
    } else {
      mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK, mResultData);
    }
  }

  private MediaProjectionManager getMediaProjectionManager() {

    return (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
  }

  private void virtualDisplay() {
    if (mVirtualDisplay != null) {
      mVirtualDisplay.release();
    }
    mVirtualDisplay = mMediaProjection.createVirtualDisplay("screen-mirror",
        mScreenWidth, mScreenHeight, mScreenDensity, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        mImageReader.getSurface(), null, null);
  }

  private void startCapture() {
    Image image = mImageReader.acquireLatestImage();

    if (image == null) {
      startScreenShot();
    } else {
      SaveTask mSaveTask = new SaveTask();
      AsyncTaskCompat.executeParallel(mSaveTask, image);

    }
  }


  public class SaveTask extends AsyncTask<Image, Void, Bitmap> {

    @Override
    protected Bitmap doInBackground(Image... params) {

      if (params == null || params.length < 1 || params[0] == null) {

        return null;
      }

      Image image = params[0];

      int width = image.getWidth();
      int height = image.getHeight();
      final Image.Plane[] planes = image.getPlanes();

      final ByteBuffer buffer = planes[0].getBuffer();
      int pixelStride = planes[0].getPixelStride();

      int rowStride = planes[0].getRowStride();
      int rowPadding = rowStride - pixelStride * width;
      Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
      bitmap.copyPixelsFromBuffer(buffer);
      bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
      ((ScreenCaptureApplication) getApplication()).setmScreenCaptureBitmap(bitmap);
      image.close();
      File fileImage = null;
      if (bitmap != null) {
        try {
          fileImage = new File(FileUtil.getScreenShotsName(getApplicationContext() , _dayOrWeak));
          if (!fileImage.exists()) {
            fileImage.createNewFile();
          }
          FileOutputStream out = new FileOutputStream(fileImage);
          if (out != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            Intent media = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(fileImage);
            media.setData(contentUri);
            sendBroadcast(media);
          }
        } catch (FileNotFoundException e) {
          e.printStackTrace();
          fileImage = null;
        } catch (IOException e) {
          e.printStackTrace();
          fileImage = null;
        }
      }

      if (fileImage != null) {
        return bitmap;
      }
      return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
      super.onPostExecute(bitmap);
      if (bitmap != null) {
        ((ScreenCaptureApplication) getApplication()).setmScreenCaptureBitmap(bitmap);
        startActivity(PreviewPictureActivity.newIntent(getApplicationContext()));
      }

      mFloatView.setVisibility(View.VISIBLE);
    }
  }


  private void tearDownMediaProjection() {
    if (mMediaProjection != null) {
      mMediaProjection.stop();
      mMediaProjection = null;

    }
  }

  private void stopVirtual() {
    if (mVirtualDisplay == null) {
      return;
    }
    mVirtualDisplay.release();
    mVirtualDisplay = null;
  }

  @Override
  public void onDestroy() {
    // to remove mFloatLayout from windowManager
    super.onDestroy();
    if (mFloatView != null) {
      mWindowManager.removeView(mFloatView);
    }
    stopVirtual();

    tearDownMediaProjection();
  }

}

