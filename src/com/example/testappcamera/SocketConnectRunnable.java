package com.example.testappcamera;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import android.util.Log;
import android.widget.Toast;


public class SocketConnectRunnable implements Runnable
{

	//private final String ServerIP = "120.126.145.103";
	private final String ServerIP = "220.128.105.72";
	//private int Port = 8080; 
	private int Port = 36152;
	private Socket socket; 
	private String message = "shit";
	private String AuthReq = null;
	private boolean regist = false;


	public SocketConnectRunnable()
	{
	
	}

	public SocketConnectRunnable(int port)
	{
		Port = port;
		regist = true;
	}
	public String getMessage()
	{
		return message;
	}
	
	private void communication()
    {
    	try 
    	{
    		//System.out.println("fuck");
    		Socket client = new Socket(ServerIP, Port);	
			

			DataOutputStream outStream =new DataOutputStream(client.getOutputStream());
			outStream.writeUTF("2");
			outStream.writeUTF("123");
			
			DataInputStream inStream =new DataInputStream(client.getInputStream());
			message= inStream.readUTF();
			
           // System.out.println(meg);
           
            client.close();
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}       
    	
    }
	
	
	
	private void register()
	{
		Log.d("SOCKET", "Create Connection.");
		 try {
	        	Socket client = new Socket(ServerIP, Port);
	        	Log.d("SOCKET", "new Socket.");
				DataInputStream inStream = new DataInputStream(client.getInputStream());
				DataOutputStream outStream = new DataOutputStream(client.getOutputStream());
				outStream.writeBytes("47c75e34529ab36ae8104a5cd0a7e056");
	        	String input = inputStreamAsString(inStream);
				//String meg = inStream.readUTF();
				Log.d("SOCKET", input);
				Log.d("SOCKET", "Sent.");
	            //Port = Integer.valueOf(meg);
	            //meg = inStream.readUTF();
	            //AuthReq = meg;
	            //Log.d("SOCKET", meg);
	            client.close();
				
	            
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("SOCKET", e.getMessage());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.d("SOCKET", e.getMessage());
			}    	
		
	}
	
	public String getAuthReq()
	{
		return AuthReq;
	}
	
	public String getPort()
    {
       return String.valueOf(Port);
    }
	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		if(!regist)
			register();
		//communication();
		
	}
	
	public static String inputStreamAsString(InputStream stream) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder sb = new StringBuilder();
        String line = null;
        
        while ((line = br.readLine()) != null) {
        	Log.d("SOCKET", "Line!");
            sb.append(line + "\n");
        }

        br.close();
        return sb.toString();
    }
}