package gov.cga.camstream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.HeaderGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
@SuppressLint({ "ShowToast", "CommitPrefEdits" })
public class LoginActivity extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;
	private ServerCheckTask mServerCheck = null;
	private final static String TAG = "Login";

	private static final Void Void = null;

	// Values for email and password at the time of the login attempt.
	private String mPasscode;
	private String mDevice;

	// UI references.
	private EditText mPasscodeView;
	private EditText mDeviceView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private TextView mServiceCheckStatus;
	private Button mSubmitBtn;
	
	private Activity mActivity;
	private final static String HOST = "220.128.105.72";
	
	Handler mViewHandler;
	
	// Token File
	SharedPreferences mPrefs;	
	
	@SuppressLint("HandlerLeak")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "Start Login Page");
		
		// 取得 token 自偏好設定
		Context mContext = this.getApplicationContext();
		mPrefs = mContext.getSharedPreferences("auth", 0);
		
		mActivity = this;
		
		String token = mPrefs.getString("token", "");
		token = "";
		
		Log.i(TAG, "Preference [token]: " + token);
		if (!token.equals("")) {
			Intent intent = new Intent(this, VideoViewActivity.class);
			startActivity(intent);
		}
		
		setContentView(R.layout.activity_login);
		
		// Set up the login form.
		//mPasscode = getIntent();
		mPasscodeView = (EditText) findViewById(R.id.passcode_input);
		//mPasscodeView.setText(mEmail);

		mDeviceView = (EditText) findViewById(R.id.device_name_input);
		mDeviceView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		mServiceCheckStatus = (TextView) findViewById(R.id.login_status_text);
		
		mSubmitBtn = (Button) findViewById(R.id.sign_in_button);
		mSubmitBtn.setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mSubmitBtn.getText().equals("取得憑證")) {
						attemptLogin();
					} else {
						// ServerCheckTask
						mServerCheck = new ServerCheckTask();
						mServerCheck.execute((Void) );
						//auth_service();
					}
				}
			}
		);
		
		/*mViewHandler = new Handler () {
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case 1:
					// available
					Log.v(TAG, "Service Available");
					mPasscodeView.setVisibility(1);
					mDeviceView.setVisibility(1);
					mServiceCheckStatus.setText(R.string.server_available);
					mSubmitBtn.setText(R.string.action_login);
					break;
				case 2:
					//unavailable
					Log.v(TAG, "Service Unavailable");
					mServiceCheckStatus.setText(R.string.server_unavailable);
					break;
				}
			}
		};*/
		mPasscodeView.setVisibility(1);
		mDeviceView.setVisibility(1);
		mServiceCheckStatus.setText(R.string.server_available);
		mSubmitBtn.setText(R.string.action_login);
		// ServerCheckTask
		//mServerCheck = new ServerCheckTask();
		//mServerCheck.execute((Void) );
		//this.auth_service();
	}
	/*
	private boolean auth_service () {
		Log.i("Login", "Do Login Event.");
		Log.d(TAG, "Host: " + HOST);
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://" + HOST + "/auth/check");
		HttpResponse response;
		Boolean serviceStatus = false;
		
		Log.d(TAG, "flags");
		try {
			response = httpClient.execute(httpGet);
			
			HttpEntity entity = response.getEntity();
			String contentType = response.getHeaders("Content-Type")[0].getValue().toString();
			String typePattern = "application/json.*";
			
			if (entity != null && contentType.matches(typePattern)) {
				
				InputStream inStream = entity.getContent();
				String result = convert_stream_to_string(inStream);
				
				//Log.i(TAG, "ReSuLt: " + result);
								
				JSONObject json = new JSONObject(result);
								
				if (!json.getString("status").equals("unavailable")){
					serviceStatus = true;
				}
				inStream.close();
			} else {
				Log.i(TAG, "Failed on connecting server.");
			}
		} catch (RuntimeException ex) {
			Log.e(TAG, ex.toString());
			Log.e(TAG, ex.getMessage());
		} catch (IOException ex) {
			Log.e(TAG, ex.getMessage());
		} catch (JSONException ex) {
			ex.printStackTrace();
		}
		if (serviceStatus) {
			Log.v(TAG, "Service Available");
			mPasscodeView.setVisibility(1);
			mDeviceView.setVisibility(1);
			mServiceCheckStatus.setText(R.string.server_available);
			mSubmitBtn.setText(R.string.action_login);
		} else {
			mServiceCheckStatus.setText(R.string.server_unavailable);
		}
		return serviceStatus;
	}
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mPasscodeView.setError(null);
		mDeviceView.setError(null);

		// Store values at the time of the login attempt.
		mPasscode = mPasscodeView.getText().toString();
		mDevice = mDeviceView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid device name.
		if (TextUtils.isEmpty(mDevice)) {
			mDeviceView.setError(getString(R.string.error_field_required));
			focusView = mDeviceView;
			cancel = true;
		}

		// Check for a valid passcode.
		if (TextUtils.isEmpty(mPasscode)) {
			mPasscodeView.setError(getString(R.string.error_field_required));
			focusView = mPasscodeView;
			cancel = true;
		} else if (mPasscode.length() != 6) {
			mPasscodeView.setError(getString(R.string.error_invalid_password));
			focusView = mPasscodeView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) );
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}
	
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
	
	public void server_available() {
		mPasscodeView.setVisibility(1);
		mDeviceView.setVisibility(1);
		mServiceCheckStatus.setText(R.string.server_available);
		mSubmitBtn.setText(R.string.action_login);
	}
	
	public void server_unavailable() {
		mServiceCheckStatus.setText(R.string.server_unavailable);
	}
	/**
	 * Check Server Status
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class ServerCheckTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... param) {
			Log.i("Login", "Do Server Check Event.");
						
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet("http://" + HOST + "/auth/check");
			HttpResponse response;
						
			try {
				response = httpClient.execute(httpGet);
				
				HttpEntity entity = response.getEntity();
				String contentType = response.getHeaders("Content-Type")[0].getValue().toString();
				String typePattern = "application/json.*";
				
				if (entity != null && contentType.matches(typePattern)) {
					
					InputStream inStream = entity.getContent();
					String result = convert_stream_to_string(inStream);
					
					//Log.i(TAG, "ReSuLt: " + result);
									
					JSONObject json = new JSONObject(result);
									
					if (json.getString("status").equals("unavailable")){
						Log.d(TAG, "unavailable");
					} else {
						Log.d(TAG, "available");
					}
					inStream.close();
					
				} else {
					Log.i(TAG, "Failed on connecting server.");
				}
			} catch (RuntimeException ex) {
				Log.e("Connection1", ex.getMessage());
			} catch (IOException ex) {
				Log.e("Connection2", ex.getMessage());
			} catch (JSONException ex) {
				ex.printStackTrace();
				Log.e("Connection3", ex.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			/*if (success) {
				Log.d(TAG, "Saved Prefernces, intent VideoViewActivity.");
				Intent intent = new Intent(mActivity, VideoViewActivity.class);
				startActivity(intent);
			} else{
				mPasscodeView
						.setError(getString(R.string.error_incorrect_password));
				mPasscodeView.requestFocus();
			}*/
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
	
	/**
	 * 登入系統取得認證碼
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... param) {
			Log.i("Login", "Do Login Event.");
						
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://" + HOST + "/auth/" + mPasscode + "/" + mDevice);
			HttpResponse response;
						
			try {
				response = httpClient.execute(httpPost);
				
				HttpEntity entity = response.getEntity();
				String contentType = response.getHeaders("Content-Type")[0].getValue().toString();
				String typePattern = "application/json.*";
				
				if (entity != null && contentType.matches(typePattern)) {
					
					InputStream inStream = entity.getContent();
					String result = convert_stream_to_string(inStream);
									
					JSONObject json = new JSONObject(result);
					
					inStream.close();
					
					if (json.getString("status").equals("Uncatched Pass")) {
						Log.d(TAG, "Passcode rejected.");
						return false;
					} else if (json.getString("status").equals("Success")){
						Log.d(TAG, "Passcode accepted.");
						String token = json.getString("token");
						
						// 儲存於偏好設定
						SharedPreferences.Editor edit = mPrefs.edit();
						edit.putString("token", token);
						edit.commit();
						
						return true;
					}
				} else {
					Log.i(TAG, "Failed on connecting server.");
				}
			} catch (RuntimeException ex) {
				Log.e("Connection", ex.getMessage());
			} catch (IOException ex) {
				Log.e("Connection", ex.getMessage());
			} catch (JSONException ex) {
				ex.printStackTrace();
				Log.e("Connection", ex.getMessage());
			}
			return false;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				Log.d(TAG, "Saved Prefernces, intent VideoViewActivity.");
				Intent intent = new Intent(mActivity, VideoViewActivity.class);
				startActivity(intent);
			} else{
				mPasscodeView
						.setError(getString(R.string.error_incorrect_password));
				mPasscodeView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
