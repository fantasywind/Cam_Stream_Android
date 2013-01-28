package gov.cga.camstream;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.HeaderGroup;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
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
	private Button mSubmitBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_login);

		// Check Auth Service
		Boolean auth_service = this.auth_service();
				
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

		mSubmitBtn = (Button) findViewById(R.id.sign_in_button);
		mSubmitBtn.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						if (mSubmitBtn.getText().equals("取得憑證")) {
							attemptLogin();
						} else {
							Boolean auth_service = auth_service();
							//auth_service = true;
							if (auth_service) {
								mPasscodeView.setVisibility(1);
								mDeviceView.setVisibility(1);
								mSubmitBtn.setText(R.string.action_login);
							}
						}
					}
				});
		if (auth_service) {
			mPasscodeView.setVisibility(1);
			mDeviceView.setVisibility(1);
			mSubmitBtn.setText(R.string.action_login);
		}
	}
	
	private boolean auth_service () {
		Log.i("Login", "Do Login Event.");
		
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet("http://127.0.0.1/auth");
		HttpResponse response;
		Boolean serviceStatus = false;
		
		try {
			response = httpClient.execute(httpGet);
			
			HttpEntity entity = response.getEntity();
			String contentType = response.getHeaders("Content-Type")[0].getValue().toString();
			String typePattern = "application/json.*";
			
			if (entity != null && contentType.matches(typePattern)) {
				
				InputStream inStream = entity.getContent();
				String result = convert_stream_to_string(inStream);
								
				JSONObject json = new JSONObject(result);
				Log.i(TAG, "JSON: " + json.toString());
				
				JSONArray nameArray = json.names();
				JSONArray valueArray = json.toJSONArray(nameArray);
								
				for (int i = 0; i < valueArray.length(); i++) {
					if (nameArray.getString(i) == "status");
						serviceStatus = true;
						//serviceStatus = valueArray.getString(i);
					//Log.i(TAG, "<jsonname" + i + ">\n" + nameArray.getString(i) + "\n</jsonname" + i + ">\n" + "<jsonvalue" + i + ">\n" + valueArray.getString(i) + "\n</jsonvalue" + i + ">");
				}
				inStream.close();
			} else {
				Log.i(TAG, "Failed on connecting server.");
			}
			// Simulate network access.
			Thread.sleep(2000);
		} catch (RuntimeException ex) {
			Log.e("Connection", ex.getMessage());
		} catch (InterruptedException ex) {
			Log.e("Connection", ex.getMessage());
		} catch (IOException ex) {
			Log.e("Connection", ex.getMessage());
		} catch (JSONException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
			Log.e("Connection", ex.getMessage());
		}
		return serviceStatus;
	}

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
	
	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... param) {
			// TODO: attempt authentication against a network service.
			Log.i("Login", "Do Login Event.");
			
			HttpClient httpClient = new DefaultHttpClient();
			HttpGet httpGet = new HttpGet("http://127.0.0.1/auth");
			HttpResponse response;
			String serviceStatus = "";
			
			try {
				response = httpClient.execute(httpGet);
				
				HttpEntity entity = response.getEntity();
				String contentType = response.getHeaders("Content-Type")[0].getValue().toString();
				String typePattern = "application/json.*";
				
				if (entity != null && contentType.matches(typePattern)) {
					
					InputStream inStream = entity.getContent();
					String result = convert_stream_to_string(inStream);
									
					JSONObject json = new JSONObject(result);
					Log.i(TAG, "JSON: " + json.toString());
					
					JSONArray nameArray = json.names();
					JSONArray valueArray = json.toJSONArray(nameArray);
									
					for (int i = 0; i < valueArray.length(); i++) {
						if (nameArray.getString(i) == "status");
							serviceStatus = valueArray.getString(i);
						//Log.i(TAG, "<jsonname" + i + ">\n" + nameArray.getString(i) + "\n</jsonname" + i + ">\n" + "<jsonvalue" + i + ">\n" + valueArray.getString(i) + "\n</jsonvalue" + i + ">");
					}
					inStream.close();
				} else {
					Log.i(TAG, "Failed on connecting server.");
				}
				// Simulate network access.
				Thread.sleep(2000);
			} catch (RuntimeException ex) {
				Log.e("Connection", ex.getMessage());
			} catch (InterruptedException ex) {
				Log.e("Connection", ex.getMessage());
			} catch (IOException ex) {
				Log.e("Connection", ex.getMessage());
			} catch (JSONException ex) {
				// TODO Auto-generated catch block
				ex.printStackTrace();
				Log.e("Connection", ex.getMessage());
			}
			// ------- Auth
			Log.i(TAG, serviceStatus);
			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mPasscode)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mDevice);
				}
			}

			// TODO: register the new account here.
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				finish();
			} else {
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
