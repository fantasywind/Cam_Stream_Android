package gov.cga.camstream;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class VideoViewActivity extends Activity  implements Callback
{
	private static final Void Void = null;
	private Activity mActivity;
	private PortFetcher port_fetcher;
	private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
	public 	MediaRecorder mrec = new MediaRecorder();   
	private Camera mCamera;
	private boolean Login = false;
	private String Text;
	private String token = "";
	private int PORT = 0;
	private final static String HOST = "10.0.2.2";
	private final static String TAG = "VideoView";
	
	private SharedPreferences mPrefs;
	private Button mController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_view);
		mActivity = this;
		Log.i(TAG, "Camera View");
		
		// 取得 Server listen port
		Context mContext = this.getApplicationContext();
		mPrefs = mContext.getSharedPreferences("auth", 0);
		
		token =  mPrefs.getString("token", "");
		if (token.equals("")) {
			Intent intent = new Intent(this, LoginActivity.class);
			startActivity(intent);
		} else {
			port_fetcher = new PortFetcher();
			port_fetcher.execute();
		}
		
		String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/testAndroid";
		File file = new File(path);
        
        if(!file.exists())
        {
        	Toast.makeText(this, "create", Toast.LENGTH_LONG).show();
        	file.mkdir();
        }
        
        mController = (Button) findViewById(R.id.camera_controller);
        
        mController.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String contextText = (String) mController.getText();
				
				if (contextText.equals("開始轉播")){
					try {
		                startRecording();
		            } catch (Exception e) {
		            	e.getStackTrace();
		                String message = e.getMessage();
		                Log.i(null, "Problem " + message);
		                mrec.release();
		            }
				}
			}
        });
        
		surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mCamera = Camera.open();
       
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        
        // 開啟預覽畫面在開始轉播前
        //Log.d(TAG, "Set Preview Done!");
        //surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	// 自訂選單選項
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_video_view, menu);
		/*menu.add(0, 0, 0, "Start");
		menu.add(0, 1, 0, "Regist");
		menu.add(0, 2, 0, "Connect");	*/	
        return super.onCreateOptionsMenu(menu);
	}

	// 選單事件處理
	@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        /*if(item.getTitle().equals("Start"))
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
        }*/

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
		 
		} catch (FileNotFoundException e){
			return;
		} catch (IOException e){
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
	
	//  開始錄影
	
    protected void startRecording() throws IOException
    {
        if (mCamera == null)
            mCamera = Camera.open();
        
        if (PORT == 0){
        	Toast.makeText(this, R.string.port_fetching, Toast.LENGTH_LONG).show();
        } else {
	        String filename;
	        String path = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/testAndroid";
	      
	        Toast.makeText(this, path, Toast.LENGTH_LONG).show();
	        Date date = new Date();
	        filename = "/rsc" + date.toString().replace(" ", "_").replace(":", "_")+".mp4";
	        
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
	
	// Stream 轉換 String
	private static String convert_stream_to_string(InputStream inputStream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		StringBuilder streamBuilder = new StringBuilder();
		
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				streamBuilder.append(line + "\n");				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return streamBuilder.toString();
	}
	
	// 取得轉播通道
	public class PortFetcher extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
						
			try {
				Socket port_getter = new Socket(HOST, 36152);
				InputStreamReader isr = new InputStreamReader(port_getter.getInputStream());
				BufferedReader isbr = new BufferedReader(isr);
				OutputStreamWriter osw = new OutputStreamWriter(port_getter.getOutputStream());
				BufferedWriter osbw = new BufferedWriter(osw);
				String inflow;
				
				osbw.append(token);
				osbw.flush();
				
				while ((inflow = isbr.readLine()) != null) {
					if (inflow.equals("denied")) {
						Log.i(TAG, "Denied Request");
						SharedPreferences.Editor edit = mPrefs.edit();
						edit.putString("token", "");
						edit.commit();
						
						Intent intent = new Intent(mActivity, LoginActivity.class);
						startActivity(intent);
					} else {
						PORT = Integer.parseInt(inflow.trim(), 10);
						Log.i(TAG, "PORT: " + PORT);
					}
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
		}

		@Override
		protected void onCancelled() {
			port_fetcher = null;
		}
	}
}
