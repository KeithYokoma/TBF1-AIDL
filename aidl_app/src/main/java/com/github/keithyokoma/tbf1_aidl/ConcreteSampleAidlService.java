package com.github.keithyokoma.tbf1_aidl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

/**
 * @author KeishinYokomaku
 */
public class ConcreteSampleAidlService extends Service {
	private final SampleAidlService.Stub binder = new SampleAidlService.Stub() {
		@Override
		public String getSomething() throws RemoteException {
			return "Hello AIDL World!";
		}

		@Override
		public SampleParcelable getParcelable() throws RemoteException {
			return new SampleParcelable("Hello AIDL World!");
		}

		@Override
		public void doSomething(SampleParcelable item) throws RemoteException {
			Toast.makeText(getApplicationContext(), String.valueOf(item), Toast.LENGTH_SHORT).show();
		}
	};

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
}
