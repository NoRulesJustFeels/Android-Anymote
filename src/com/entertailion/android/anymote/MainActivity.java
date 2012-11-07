/*
 * Copyright (C) 2012 ENTERTAILION, LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.entertailion.android.anymote;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.entertailion.java.anymote.client.AnymoteClientService;
import com.entertailion.java.anymote.client.AnymoteSender;
import com.entertailion.java.anymote.client.ClientListener;
import com.entertailion.java.anymote.client.DeviceSelectListener;
import com.entertailion.java.anymote.client.InputListener;
import com.entertailion.java.anymote.client.PinListener;
import com.entertailion.java.anymote.connection.TvDevice;
import com.entertailion.java.anymote.util.Constants;
import com.google.anymote.Key.Code;

public class MainActivity extends Activity implements ClientListener, InputListener {
	private static final String LOG_TAG = "MainActivity";
	private AnymoteClientService anymoteClientService;
	private AnymoteSender anymoteSender;
	private ProgressDialog progressBar;  
	private AndroidPlatform platform;
	private Handler handler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        platform = new AndroidPlatform(this);
        anymoteClientService = AnymoteClientService.getInstance(platform);
		anymoteClientService.attachClientListener(this);  // client service callback
		anymoteClientService.attachInputListener(this);  // user interaction callback
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_switch:
			if (!platform.isWifiAvailable()) {
				AlertDialog alertDialog = buildNoWifiDialog();
				alertDialog.show();
			} else {
				anymoteClientService.selectDevice();
			}
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
    @Override
	protected void onResume() {
    	Log.d(LOG_TAG, "onResume");
		super.onResume();
		
		if (!platform.isWifiAvailable()) {
			// run on the main UI thread
			handler.post(new Runnable() {
				public void run() {
					AlertDialog alertDialog = buildNoWifiDialog();
					alertDialog.show();
				}
			});
			return;
		}
		if (anymoteClientService.getCurrentDevice()==null) {  // not already connected
			// Find Google TV devices to connect to
			anymoteClientService.selectDevice();
			
			// OR connect to a specific device
//			try {
//	            Inet4Address address = (Inet4Address) InetAddress.getByName("192.168.0.13");
//	            anymoteClientService.connectDevice(new TvDevice(Constants.string.manual_ip_default_box_name, address));
//	        } catch (UnknownHostException e) {
//	        }
		}
    }
    
    /**
     * ClientListener callback when attempting a connecion to a Google TV device
     * @see com.entertailion.java.anymote.client.ClientListener#attemptToConnect(com.entertailion.java.anymote.connection.TvDevice)
     */
    public void attemptToConnect(TvDevice device) {
    	if (progressBar==null || !progressBar.isShowing()) {
			// run on the main UI thread
			handler.post(new Runnable() {
				public void run() {
					progressBar = ProgressDialog.show(MainActivity.this, getString(R.string.progress_title), getString(R.string.progress_connecting_message));  
				}
			});
		}
	}

    /** 
	 * ClientListener callback when Anymote is conneced to a Google TV device
	 * @see com.entertailion.java.anymote.client.ClientListener#onConnected(com.entertailion.java.anymote.client.AnymoteSender)
	 */
	public void onConnected(final AnymoteSender anymoteSender) {
		Log.d(LOG_TAG, "onConnected");
		if (progressBar!=null && progressBar.isShowing()) {
			progressBar.dismiss();
        }
	    if (anymoteSender != null) {
	    	Log.d(LOG_TAG, anymoteClientService.getCurrentDevice().toString());
	        // Send events to Google TV using anymoteSender.
	        // save handle to the anymoteSender instance.
	        this.anymoteSender = anymoteSender;
	        
	        // Connect the buttons to the anymoteSender key codes
	        Button button = (Button)findViewById(R.id.cell1);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_HOME);
				}
	        	
	        });
	        button = (Button)findViewById(R.id.cell2);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_LIVE);
				}
	        	
	        });
	        button = (Button)findViewById(R.id.cell3);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_BACK);
				}
	        	
	        });
	        button = (Button)findViewById(R.id.cell5);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_DPAD_UP);
				}
	        	
	        });
	        button = (Button)findViewById(R.id.cell7);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_DPAD_LEFT);
				}
	        	
	        });
	        button = (Button)findViewById(R.id.cell8);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_DPAD_CENTER);
				}
	        	
	        });
	        button = (Button)findViewById(R.id.cell9);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_DPAD_RIGHT);
				}
	        	
	        });
	        button = (Button)findViewById(R.id.cell11);
	        button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (MainActivity.this.anymoteSender!=null)
						MainActivity.this.anymoteSender.sendKeyPress(Code.KEYCODE_DPAD_DOWN);
				}
	        	
	        });
	        
	        // Attach touch handler to the touchpad
			new TouchHandler(findViewById(R.id.touch_pad), TouchHandler.Mode.POINTER_MULTITOUCH, anymoteSender);
	    } else {
	    	Log.d(LOG_TAG, "Connection failed");
	    }
	}

	/**
	 * ClientListener callback when the Anymote service is disconnected from the Google TV device
	 * @see com.entertailion.java.anymote.client.ClientListener#onDisconnected()
	 */
	public void onDisconnected() { 
		Log.d(LOG_TAG, "onDisconnected");
		if (progressBar!=null && progressBar.isShowing()) {
			progressBar.dismiss();
        }
	    anymoteSender = null;
	    
	    if (!platform.isWifiAvailable()) {
	    	// run on the main UI thread
			handler.post(new Runnable() {
				public void run() {
					AlertDialog alertDialog = buildNoWifiDialog();
					alertDialog.show();
				}
			});
			return;
		}
	    // Find Google TV devices to connect to
		anymoteClientService.selectDevice();
	}

	/**
	 * ClientListener callback when the attempted connection to the Google TV device failed
	 * @see com.entertailion.java.anymote.client.ClientListener#onConnectionFailed()
	 */
	public void onConnectionFailed() {
		Log.d(LOG_TAG, "onConnectionFailed");
		if (progressBar!=null && progressBar.isShowing()) {
			progressBar.dismiss();
        }
	    anymoteSender = null;
	    
	    if (!platform.isWifiAvailable()) {
	    	// run on the main UI thread
			handler.post(new Runnable() {
				public void run() {
					AlertDialog alertDialog = buildNoWifiDialog();
					alertDialog.show();
				}
			});
			return;
		}
	    // Find Google TV devices to connect to
		anymoteClientService.selectDevice();
	}
	
	/**
	 * Cleanup
	 */
	@Override
	public void onDestroy() {
		Log.d(LOG_TAG, "onDestroy");
        if (anymoteClientService != null) {
        	anymoteClientService.detachClientListener(this);
        	anymoteClientService.detachInputListener(this);
        	anymoteSender = null;
        }
        super.onDestroy();
    }
	
	/** 
	 * InputListener callback for feedback on starting the device discovery process
	 * @see com.entertailion.java.anymote.client.InputListener#onDiscoveringDevices()
	 */
	public void onDiscoveringDevices() {
		Log.d(LOG_TAG, "onDiscoveringDevices");
		if (progressBar==null || !progressBar.isShowing()) {
			// run on the main UI thread
			handler.post(new Runnable() {
				public void run() {
					progressBar = ProgressDialog.show(MainActivity.this, getString(R.string.progress_title), getString(R.string.progress_finding_message));  
				}
			});
		}
	}
	
	/** 
	 * InputListener callback when a Google TV device needs to be selected
	 * @see com.entertailion.java.anymote.client.InputListener#onSelectDevice(java.util.List, com.entertailion.java.anymote.client.DeviceSelectListener)
	 */
	public void onSelectDevice(final List<TvDevice> trackedDevices, final DeviceSelectListener listener) {
		Log.d(LOG_TAG, "onSelectDevice");
		if (progressBar!=null && progressBar.isShowing()) {
			progressBar.dismiss();
        }
		// run on the main UI thread
		handler.post(new Runnable() {
			public void run() {
				DeviceSelectDialog deviceSelectDialog = new DeviceSelectDialog(MainActivity.this, trackedDevices, anymoteClientService.getCurrentDevice(), listener);
				deviceSelectDialog.show();
			}
		});
	}
	
	/**
	 * InputListener callback when PIN required to pair with Google TV device
	 * @see com.entertailion.java.anymote.client.InputListener#onPinRequired(com.entertailion.java.anymote.client.PinListener)
	 */
	public void onPinRequired(final PinListener listener) {
		Log.d(LOG_TAG, "onPinRequired");
		// run on the main UI thread
		handler.post(new Runnable() {
			public void run() {
				PairingPINDialogBuilder pairingPINDialogBuilder = new PairingPINDialogBuilder(MainActivity.this, listener);
				pairingPINDialogBuilder.show();
			}
		});
	}
	
	/**
     * Construct a no-wifi dialog.
     * 
     * @return AlertDialog asking user to turn on WIFI.
     */
    private AlertDialog buildNoWifiDialog() { 
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.finder_wifi_not_available);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.finder_configure, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
                Intent intent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.finder_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int id) {
            }
        });
        return builder.create();
    }
    
}
