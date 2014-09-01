package com.netlynxtech.advancedmonitor;

import java.util.ArrayList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.manuelpeinado.refreshactionitem.ProgressIndicatorType;
import com.manuelpeinado.refreshactionitem.RefreshActionItem;
import com.manuelpeinado.refreshactionitem.RefreshActionItem.RefreshActionListener;
import com.netlynxtech.advancedmonitor.adapters.DevicesAdapter;
import com.netlynxtech.advancedmonitor.classes.Device;
import com.netlynxtech.advancedmonitor.classes.Utils;
import com.netlynxtech.advancedmonitor.classes.WebRequestAPI;

public class DeviceListActivity extends ActionBarActivity {

	ArrayList<Device> devices;
	DevicesAdapter adapter;
	ListView lvDevices;
	RefreshActionItem mRefreshActionItem;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_list);
		lvDevices = (ListView) findViewById(R.id.lvDevices);
		lvDevices.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				TextView tvDeviceId = (TextView) view.findViewById(R.id.tvDeviceId);
				startActivity(new Intent(DeviceListActivity.this, IndividualDeviceActivity.class).putExtra("deviceId", tvDeviceId.getText().toString().trim()));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_device_list, menu);
		MenuItem item = menu.findItem(R.id.menu_individual_refresh);
		mRefreshActionItem = (RefreshActionItem) MenuItemCompat.getActionView(item);
		mRefreshActionItem.setMenuItem(item);
		mRefreshActionItem.setProgressIndicatorType(ProgressIndicatorType.INDETERMINATE);
		mRefreshActionItem.setRefreshActionListener(new RefreshActionListener() {

			@Override
			public void onRefreshButtonClick(RefreshActionItem sender) {
				new getDevice().execute();
			}
		});
		new getDevice().execute();
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add_device:
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
			mRefreshActionItem.showProgress(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			devices = new WebRequestAPI(DeviceListActivity.this).GetDevices(new Utils(DeviceListActivity.this).getDeviceUniqueId());
			adapter = new DevicesAdapter(DeviceListActivity.this, devices);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			DeviceListActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mRefreshActionItem.showProgress(false);
					lvDevices.setAdapter(adapter);
				}
			});
		}

	}
}