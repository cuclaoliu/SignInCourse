package edu.cuc.stephen.signincourse;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;


/**
 * A simple {@link Fragment} subclass.

 */
public class SignInFragment extends Fragment {


    private static final int PICK_CODE = 0x110;
    private ImageView imageViewPhoto;
    private Bitmap bitmapPhoto;
    private Button buttonGetImage, buttonOpenCamera;
    private TextView textViewTip;
    private Camera camera;
    private int defaultCameraId = 0;
    int orientationOfCamera;
    private final int maxNumberOfFaces = 5;

    //private View flWaiting;

    private String currentPhotoPath;
    private Context appContext;

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_sign_in, container, false);
        imageViewPhoto = (ImageView) view.findViewById(R.id.iv_picture);
        buttonGetImage = (Button) view.findViewById(R.id.button_get_image);
        buttonOpenCamera = (Button) view.findViewById(R.id.button_open_camera);
        textViewTip = (TextView) view.findViewById(R.id.tv_tip);
        //flWaiting = findViewById(R.id.fl_waiting);
        appContext = getActivity().getApplicationContext();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        bitmapPhoto = BitmapFactory.decodeResource(getResources(), R.drawable.baby, options);
        getCameraInfo();
        detectAndRedraw();
        initEvents();
        initViews();
        return view;
    }


    private void initEvents() {
        buttonGetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_CODE);
            }
        });
        buttonOpenCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Camera camera = Camera.open(defaultCameraId);       // 摄像头对象实例
                    Camera.Parameters parameters = camera.getParameters();
                    setCameraDisplayOrientation(defaultCameraId, camera);             //设置预览方向
                    //long scanBeginTime = System.currentTimeMillis();// 记录下系统开始扫描的时间
                    camera.setPreviewCallback(new Camera.PreviewCallback() {
                        public void onPreviewFrame(byte[] data, Camera camera) {
                            //long scanEndTime = System.currentTimeMillis();   //记录摄像头返回数据的时间
                            //long mSpecPreviewTime = scanEndTime - scanBeginTime;  //从onPreviewFrame获取摄像头数据的时间
                            Camera.Size localSize = camera.getParameters().getPreviewSize();  //获得预览分辨率
                            YuvImage localYuvImage = new YuvImage(data, 17, localSize.width, localSize.height, null);
                            ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
                            localYuvImage.compressToJpeg(new Rect(0, 0, localSize.width, localSize.height), 80, localByteArrayOutputStream);    //把摄像头回调数据转成YUV，再按图像尺寸压缩成JPEG，从输出流中转成数组
                            byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
                            /*if (camera != null)
                                camera.release();   //及早释放camera资源，避免影响camera设备的正常调用
                            camera = null;*/
                            StoreByteImage(arrayOfByte);
                        }
                    });
                    camera.startPreview();         //该语句可放在回调后面，当执行到这里，调用前面的setPreviewCallback
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(appContext, getString(R.string.no_camera), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void setCameraDisplayOrientation(int paramInt, Camera paramCamera){
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(paramInt, info);
        int rotation = ((WindowManager)appContext.getSystemService(appContext.WINDOW_SERVICE)).getDefaultDisplay().getRotation();  //获得显示器件角度
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;    //获得摄像头的安装旋转角度
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        paramCamera.setDisplayOrientation(result);  //注意前后置的处理，前置是映象画面，该段是SDK文档的标准DEMO
        orientationOfCamera = result;
    }

    private void initViews() {
    }

    private void getCameraInfo() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for(int i=0; i < numberOfCameras; i++){
            Camera.getCameraInfo(i, cameraInfo);
            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                defaultCameraId = i;
                Toast.makeText(appContext, "找到前置摄像头", Toast.LENGTH_LONG).show();
            }
        }
        if(numberOfCameras <= 0){
            Toast.makeText(appContext, getString(R.string.no_camera), Toast.LENGTH_LONG).show();
            buttonOpenCamera.setEnabled(false);
        }
    }

    public void StoreByteImage(byte[] paramArrayOfByte){
        //mSpecStopTime = System.currentTimeMillis();
        //mSpecCameraTime = mSpecStopTime - mScanBeginTime;
        //Log.i(TAG, "StoreByteImage and mSpecCameraTime is " + String.valueOf(mSpecCameraTime));

        BitmapFactory.Options localOptions = new BitmapFactory.Options();
        Matrix localMatrix = new Matrix();
        bitmapPhoto = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length, localOptions);
        int width = bitmapPhoto.getWidth();
        int height = bitmapPhoto.getHeight();
        /*Bitmap localBitmap;
        switch(orientionOfCamera){   //根据前置安装旋转的角度来重新构造BMP
            case 0:
                //localFaceDetector = new FaceDetector(i, j, 1);
                localMatrix.postRotate(0.0F, width / 2, height / 2);
                localBitmap = Bitmap.createBitmap(bitmapPhoto, width, height, Bitmap.Config.RGB_565);
                break;
            case 90:
                localMatrix.postRotate(-270.0F, width / 2, height / 2);  //正90度的话就反方向转270度，一样效果
                localBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                break;
            case 180:
                localFaceDetector = new FaceDetector(i, j, 1);
                localMatrix.postRotate(-180.0F, i / 2, j / 2);
                localBitmap2 = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
                break;
            case 270:
                localFaceDetector = new FaceDetector(j, i, 1);
                localMatrix.postRotate(-90.0F, j / 2, i / 2);
                localBitmap2 = Bitmap.createBitmap(j, i, Bitmap.Config.RGB_565);  //localBitmap2应是没有数据的
                break;
        }
        scaledPhoto();
        //int k = cameraResOr;
        Bitmap localBitmap2 = null;
        FaceDetector localFaceDetector = null;
        */
    }

    private void detectAndRedraw() {
        int photoWidth = bitmapPhoto.getWidth();
        int photoHeight = bitmapPhoto.getHeight();
        FaceDetector.Face[] faces = new FaceDetector.Face[maxNumberOfFaces];
        FaceDetector faceDetect = new FaceDetector(photoWidth, photoHeight, maxNumberOfFaces);
        int numberOfFaceDetected = faceDetect.findFaces(bitmapPhoto, faces);
        Bitmap bitmap = Bitmap.createBitmap(photoWidth, photoHeight, bitmapPhoto.getConfig());
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmapPhoto, 0, 0, null);
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.GREEN);
        paint.setTextSize(32);
        //Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        //float fontHeight = fontMetrics.bottom - fontMetrics.descent;

        Log.e(getString(R.string.app_name), "find " + numberOfFaceDetected);
        for(int i=0; i< numberOfFaceDetected; i++){
            paint.setStrokeWidth(3);
            PointF midPointF = new PointF();
            faces[i].getMidPoint(midPointF);
            float eyesDistance = faces[i].eyesDistance();

            //得到人脸中心点和眼间距离参数，并对每个人脸进行画框
            canvas.drawRect(            //矩形框的位置参数
                    (int) (midPointF.x - eyesDistance),
                    (int) (midPointF.y - eyesDistance),
                    (int) (midPointF.x + eyesDistance),
                    (int) (midPointF.y + eyesDistance),
                    paint);
            //paint.setTextAlign(Paint.Align.LEFT);
            String text = String.format("%.2f", faces[i].confidence());
            canvas.drawText(text, midPointF.x - eyesDistance,
                    midPointF.y - eyesDistance - 6, paint);
        }
        bitmapPhoto = bitmap;
        imageViewPhoto.setImageBitmap(bitmapPhoto);
        //bitmap.recycle();
    }

    private void scaledPhoto() {
        //根据imageView获取适当的压缩宽和高
        ViewGroup.LayoutParams layoutParams = imageViewPhoto.getLayoutParams();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int width = imageViewPhoto.getWidth();          //获取imageView的实际宽度
        if(width <= 0)
            width = layoutParams.width;                 //imageView在Layout中的参数
        if(width <= 0)
            width = imageViewPhoto.getMaxWidth();       //检查最大值
        if(width <= 0)
            width = displayMetrics.widthPixels;
        int height = imageViewPhoto.getHeight();
        if(height <= 0)
            height = layoutParams.height;
        if(height <= 0)
            height = imageViewPhoto.getMaxHeight();
        if(height <= 0)
            height = displayMetrics.heightPixels;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(currentPhotoPath, options);
        float ratioWidth = (float)options.outWidth / width;
        float ratioHeight = (float)options.outHeight / height;
        float ratioMax = Math.max(ratioWidth, ratioHeight);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (ratioMax < 1){
            options.inSampleSize = 1;/*
            bitmapPhoto = BitmapFactory.decodeFile(currentPhotoPath, options);
            //float scaledWidth = (float)options.outWidth / ratioMax;
            //float scaledHeight = (float)options.outHeight / ratioMax;
            Matrix matrix = new Matrix();
            //matrix.postScale(scaledWidth, scaledHeight);
            matrix.postScale(1f/ratioMax, 1f/ratioMax);
            //进行缩放
            bitmapPhoto = Bitmap.createBitmap(bitmapPhoto, 0, 0,
                    options.outWidth, options.outHeight, matrix, false);*/
        }else {
            options.inSampleSize = (int) Math.ceil(ratioMax);
        }
        bitmapPhoto = BitmapFactory.decodeFile(currentPhotoPath, options);
        //double ratio = Math.max(options.outWidth/1024f, options.outHeight/1024f);
        //options.inSampleSize = (int) Math.ceil(ratio);
        //Log.e(getString(R.string.app_name), "scaled ratio: "+ options.inSampleSize);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PICK_CODE){
            if(data != null){
                Uri uri = data.getData();
                Cursor cursor = appContext.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    currentPhotoPath = cursor.getString(index);
                    cursor.close();
                }
                Log.e(getString(R.string.app_name), "filePath: "+currentPhotoPath);
                textViewTip.setText(currentPhotoPath.substring(currentPhotoPath.lastIndexOf("/")+1));
                scaledPhoto();
                detectAndRedraw();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //protected Camera mCameraDevice = null;// 摄像头对象实例
    //private String TAG = getString(R.string.app_name);
    /*
    * 动态预览识别人脸代码实例
    *   A，打开摄像头，获得初步摄像头回调数据，用到是setpreviewcallback
    *   private long mScanBeginTime = 0;   // 扫描开始时间
    *   private long mScanEndTime = 0;   // 扫描结束时间
    *   private long mSpecPreviewTime = 0;   // 扫描持续时间
    *   private int orientionOfCamera ;   //前置摄像头layout角度
    *   int numberOfFaceDetected;    //最终识别人脸数目
    *
    public void startFaceDetection() {
        try {
            mCameraDevice = Camera.open(1);     //打开前置
            if (mCameraDevice != null)
                Log.i(TAG, "open cameradevice success! ");
        } catch (Exception e) {             //Exception代替很多具体的异常
            mCameraDevice = null;
            Log.w(TAG, "open cameraFail");
            mHandler.postDelayed(r,5000);   //如果摄像头被占用，人眼识别每5秒检测看有没有释放前置
            return;
        }

        Log.i(TAG, "startFaceDetection");
        Camera.Parameters parameters = mCameraDevice.getParameters();
        setCameraDisplayOrientation(1, mCameraDevice);              //设置预览方向

        mCameraDevice.setPreviewCallback(new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                mScanEndTime = System.currentTimeMillis();   //记录摄像头返回数据的时间
                mSpecPreviewTime = mScanEndTime - mScanBeginTime;  //从onPreviewFrame获取摄像头数据的时间
                Log.i(TAG, "onPreviewFrame and mSpecPreviewTime = " + String.valueOf(mSpecPreviewTime));
                Camera.Size localSize = camera.getParameters().getPreviewSize();  //获得预览分辨率
                YuvImage localYuvImage = new YuvImage(data, 17, localSize.width, localSize.height, null);
                ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream();
                localYuvImage.compressToJpeg(new Rect(0, 0, localSize.width, localSize.height), 80, localByteArrayOutputStream);    //把摄像头回调数据转成YUV，再按图像尺寸压缩成JPEG，从输出流中转成数组
                byte[] arrayOfByte = localByteArrayOutputStream.toByteArray();
                CameraRelease();   //及早释放camera资源，避免影响camera设备的正常调用
                StoreByteImage(arrayOfByte);
            }
        });

        mCameraDevice.startPreview();         //该语句可放在回调后面，当执行到这里，调用前面的setPreviewCallback
        mScanBeginTime = System.currentTimeMillis();// 记录下系统开始扫描的时间
    }*/
    /*
    * 置预览方向的函数说明，该函数比较重要，因为方向直接影响bitmap构造时的矩阵旋转角度，影响最终人脸识别的成功与否

[java] view plaincopy在CODE上查看代码片派生到我的代码片
public void setCameraDisplayOrientation(int paramInt, Camera paramCamera){
        CameraInfo info = new CameraInfo();
        Camera.getCameraInfo(paramInt, info);
        int rotation = ((WindowManager)getSystemService("window")).getDefaultDisplay().getRotation();  //获得显示器件角度
        int degrees = 0;
        Log.i(TAG,"getRotation's rotation is " + String.valueOf(rotation));
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        orientionOfCamera = info.orientation;      //获得摄像头的安装旋转角度
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        paramCamera.setDisplayOrientation(result);  //注意前后置的处理，前置是映象画面，该段是SDK文档的标准DEMO
    }
C，对摄像头回调数据进行转换并最终解成BITMAP后再人脸识别的过程

[java] view plaincopy在CODE上查看代码片派生到我的代码片
public void StoreByteImage(byte[] paramArrayOfByte){
        mSpecStopTime = System.currentTimeMillis();
        mSpecCameraTime = mSpecStopTime - mScanBeginTime;

        Log.i(TAG, "StoreByteImage and mSpecCameraTime is " + String.valueOf(mSpecCameraTime));

        BitmapFactory.Options localOptions = new BitmapFactory.Options();
            Bitmap localBitmap1 = BitmapFactory.decodeByteArray(paramArrayOfByte, 0, paramArrayOfByte.length, localOptions);
            int i = localBitmap1.getWidth();
            int j = localBitmap1.getHeight();   //从上步解出的JPEG数组中接出BMP，即RAW->JPEG->BMP
            Matrix localMatrix = new Matrix();
            //int k = cameraResOr;
            Bitmap localBitmap2 = null;
            FaceDetector localFaceDetector = null;

        switch(orientionOfCamera){   //根据前置安装旋转的角度来重新构造BMP
            case 0:
                localFaceDetector = new FaceDetector(i, j, 1);
                        localMatrix.postRotate(0.0F, i / 2, j / 2);
                        localBitmap2 = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
                break;
            case 90:
                localFaceDetector = new FaceDetector(j, i, 1);   //长宽互换
                        localMatrix.postRotate(-270.0F, j / 2, i / 2);  //正90度的话就反方向转270度，一样效果
                        localBitmap2 = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
                break;
            case 180:
                localFaceDetector = new FaceDetector(i, j, 1);
                        localMatrix.postRotate(-180.0F, i / 2, j / 2);
                        localBitmap2 = Bitmap.createBitmap(i, j, Bitmap.Config.RGB_565);
                break;
            case 270:
                localFaceDetector = new FaceDetector(j, i, 1);
                        localMatrix.postRotate(-90.0F, j / 2, i / 2);
                        localBitmap2 = Bitmap.createBitmap(j, i, Bitmap.Config.RGB_565);  //localBitmap2应是没有数据的
                break;
        }

        FaceDetector.Face[] arrayOfFace = new FaceDetector.Face[1];
            Paint localPaint1 = new Paint();
            Paint localPaint2 = new Paint();
        localPaint1.setDither(true);
            localPaint2.setColor(-65536);
            localPaint2.setStyle(Paint.Style.STROKE);
            localPaint2.setStrokeWidth(2.0F);
            Canvas localCanvas = new Canvas();
            localCanvas.setBitmap(localBitmap2);
            localCanvas.setMatrix(localMatrix);
            localCanvas.drawBitmap(localBitmap1, 0.0F, 0.0F, localPaint1); //该处将localBitmap1和localBitmap2关联（可不要？）

        numberOfFaceDetected = localFaceDetector.findFaces(localBitmap2, arrayOfFace); //返回识脸的结果
            localBitmap2.recycle();
            localBitmap1.recycle();   //释放位图资源

        FaceDetectDeal(numberOfFaceDetected);
    }
    *
    * */

}
