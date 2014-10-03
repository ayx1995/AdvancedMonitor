package com.netlynxtech.advancedmonitor;

import java.util.ArrayList;

import mehdi.sakout.dynamicbox.DynamicBox;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.netlynxtech.advancedmonitor.adapters.DevicesAdapter;
import com.netlynxtech.advancedmonitor.classes.Device;
import com.netlynxtech.advancedmonitor.classes.WebRequestAPI;
import com.securepreferences.SecurePreferences;

public class DeviceListActivity extends ActionBarActivity {
	DynamicBox box;
	ArrayList<Device> devices;
	DevicesAdapter adapter;
	ListView lvDevices;
	AsyncTask<Void, Void, Void> task = new getDevice();
	boolean isProcessing = false;
	int index = 0, top = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		lvDevices = (ListView) findViewById(R.id.lvDevices);
		box = new DynamicBox(DeviceListActivity.this, lvDevices);
		lvDevices.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {
				TextView tvDeviceId = (TextView) view.findViewById(R.id.tvDeviceId);
				TextView tvDescription = (TextView) view.findViewById(R.id.tvDeviceDescription);
				Log.e("DEVICEID", tvDeviceId.getText().toString());
				Device d = devices.get(position);
				startActivity(new Intent(DeviceListActivity.this, IndividualDeviceActivity.class).putExtra("deviceId", tvDeviceId.getText().toString().trim())
						.putExtra("deviceDescription", tvDescription.getText().toString().trim()).putExtra("device", d));
			}
		});
	}

	private void processData() {
		if (!isProcessing) {
			Log.e("PROCESSDATA", "PROCESSDATA");

			(new Thread(new Runnable() {

				@Override
				public void run() {
					if (!Thread.interrupted())
						try {
							Thread.sleep(20000);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									if (task != null) {
										task = null;
										task = new getDevice();
										task.execute();
									}
								}
							});
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			})).start();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_device_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_device:
			SecurePreferences sp = new SecurePreferences(DeviceListActivity.this);
			sp.edit().putString("initial", "0").commit();
			startActivity(new Intent(DeviceListActivity.this, TutorialActivity.class));
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	private class getDevice extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Log.e("getDevice", "loading..");
			isProcessing = true;
			box.showLoadingLayout();
		}

		@Override
		protected Void doInBackground(Void... params) {
			devices = new WebRequestAPI(DeviceListActivity.this).GetDevices();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			DeviceListActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (!isCancelled()) {
						if (devices.size() > 0) {
							adapter = new DevicesAdapter(DeviceListActivity.this, devices);
							index = lvDevices.getFirstVisiblePosition();
							View v = lvDevices.getChildAt(0);
							top = (v == null) ? 0 : v.getTop();
							Log.e("Cancelled", "Not cancelled");
							lvDevices.setAdapter(adapter);
							box.hideAll();
							lvDevices.setSelectionFromTop(index, top);
						} else {
							box.setOtherExceptionMessage("Size :" + devices.size());
							box.showExceptionLayout();
						}
					} else {
						Log.e("Cancelled", "Cancelled");
						box.setOtherExceptionMessage("Cancelled");
						box.showExceptionLayout();
					}
					isProcessing = false;
					processData();
				}
			});
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		try {
			if (task != null) {
				task.execute();
			} else {
				task = new getDevice();
				task.execute();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (task != null) {
			task.cancel(true);
			task = null;
			isProcessing = false;
		}
	}

}
