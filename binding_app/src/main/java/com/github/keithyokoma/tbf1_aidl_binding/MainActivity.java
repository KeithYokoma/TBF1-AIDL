package com.github.keithyokoma.tbf1_aidl_binding;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.keithyokoma.tbf1_aidl.SampleAidlService;

public class MainActivity extends AppCompatActivity {
	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(getApplicationContext(), "Service has been connected!", Toast.LENGTH_LONG).show();
			sampleService = SampleAidlService.Stub.asInterface(service);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Toast.makeText(getApplicationContext(), "Service has been disconnected!", Toast.LENGTH_LONG).show();
			sampleService = null;
		}
	};
	private SampleAidlService sampleService;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		Intent intent = new Intent(SampleAidlService.class.getName());
		boolean result = bindService(intent, serviceConnection, BIND_AUTO_CREATE);
		Log.v("MainActivity", "bind result: " + result);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		if (fab == null)
			return;
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (sampleService == null)
					return;
				try {
					Toast.makeText(getApplicationContext(), sampleService.getSomething(), Toast.LENGTH_LONG).show();
				} catch (RemoteException e) {
					Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		unbindService(serviceConnection);
		super.onDestroy();
	}
}
