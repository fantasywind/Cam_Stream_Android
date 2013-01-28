package gov.cga.camstream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Properties;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

public class VideoViewActivity extends Activity  implements Callback
{

	private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
	public 	MediaRecorder mrec = new MediaRecorder();   
	private Camera mCamera;
	private boolean Login = false;
	private String Text;
	private final static String TAG = "VideoView";
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_view);
        
		Log.i(TAG, "onCreate.");
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/testAndroid";
		File file = new File(path);
        
        if(!file.exists())
        {
        	Toast.makeText(this, "create", Toast.LENGTH_LONG).show();
        	file.mkdir();
        }
		
		surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mCamera = Camera.open();
       
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		Log.i(TAG, "Create Menu.");
		getMenuInflater().inflate(R.menu.activity_video_view, menu);
		menu.add(0, 0, 0, "Start");
		menu.add(0, 1, 0, "Regist");
		menu.add(0, 2, 0, "Connect");		
        return super.onCreateOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(item.getTitle().equals("Start"))
        {
        	//Toast.makeText(this, "Start", Toast.LENGTH_LONG).show();    
            try {
               
                startRecording();
                item.setTitle("Stop");

            } catch (Exception e) {

                String message = e.getMessage();
                Log.i(null, "Problem " + message);
                mrec.release();
            }

        }
        else if(item.getTitle().equals("Stop"))
        {
        	//Toast.makeText(this, "Stop", Toast.LENGTH_LONG).show();
            mrec.stop();
            mrec.release();
            mrec = null;
            item.setTitle("Start");
        }
        else if(item.getTitle().equals("Regist"))
        {
        	Toast.makeText(this, "Regist", Toast.LENGTH_LONG).show();
        	SocketConnectRunnable socketCR = new SocketConnectRunnable();
        	Thread mThread = new Thread(socketCR);
        	
        	mThread.start();
        	
        	while(mThread.isAlive());
        	
        	Toast.makeText(this, socketCR.getAuthReq(), Toast.LENGTH_LONG).show();
        	saveToken(socketCR.getPort(), socketCR.getAuthReq());
        	//item.setTitle(socketCR.regist());
        	
        }
        else if(item.getTitle().equals("Connect"))
        {
        	loadToken();
        	Toast.makeText(this,Text, Toast.LENGTH_LONG).show();
        }

        return super.onOptionsItemSelected(item);
    }
	
	private void loadToken()
	{
		//構建properties物件
		Properties properties = new Properties();
		try{
		//創建兩個檔
		FileInputStream stream1 = this.openFileInput("TEXT.cfg");

		 
		//讀取兩個檔的內容
		properties.load(stream1);
		properties.load(stream1);
		 
		}catch(FileNotFoundException e)
		{
		return;
		}catch(IOException e){
		return;
		}

		 
		// 獲得資料
		Text = String.valueOf(properties.get("String"));
		}
	
	private boolean saveToken(String port, String Auth)
	{ 
		// 同樣來構建Properties 物件
		Properties properties = new Properties();

		 
		// 將要寫入的資料打成兩個包
		properties.put("String",port);
		properties.put("String",port+"\n"+Auth);
		try
		{	
			//設置兩個檔的類型都是可寫入的
			FileOutputStream stream1 = this.openFileOutput("TEXT.cfg", Context.MODE_PRIVATE); 
			//將大包好的資料寫入檔中
			properties.store(stream1,"");
		}
		catch(FileNotFoundException e)
		{
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return false;
		}
		return true;
	}
		

	
    protected void startRecording() throws IOException
    {
        if(mCamera==null)
            mCamera = Camera.open();
       
        String filename;
        String path= Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/testAndroid";
      
        Toast.makeText(this, path, Toast.LENGTH_LONG).show();
        Date date=new Date();
        filename="/rsc"+date.toString().replace(" ", "_").replace(":", "_")+".mp4";
        
         //create empty file it must use
        File file = new File(path,filename);	
        
        mrec = new MediaRecorder();

        mCamera.lock();
        mCamera.unlock();

        // Please maintain sequence of following code.

        // If you change sequence it will not work.
        mrec.setCamera(mCamera);   
        mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mrec.setAudioSource(MediaRecorder.AudioSource.MIC);    
        mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mrec.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
        mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mrec.setPreviewDisplay(surfaceHolder.getSurface());
        mrec.setOutputFile(path+filename);
        mrec.prepare();
        mrec.start();

       
    }

    protected void stopRecording() {

        if(mrec!=null)
        {
            mrec.stop();
            mrec.release();
            mCamera.release();
            mCamera.lock();
        }
    }

    private void releaseMediaRecorder() {

        if (mrec != null) {
            mrec.reset(); // clear recorder configuration
            mrec.release(); // release the recorder object
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }

    }

	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		 if (mCamera != null)
		 {
			 Parameters params = mCamera.getParameters();
	         mCamera.setParameters(params);
	         Log.i("Surface", "Created");
	     }
		 else
		 {
			 Toast.makeText(getApplicationContext(), "Camera not available!",
	         Toast.LENGTH_LONG).show();

	         finish();
	     }
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) 
	{
		// TODO Auto-generated method stub
		mCamera.stopPreview();
		mCamera.release();       
	}

}
