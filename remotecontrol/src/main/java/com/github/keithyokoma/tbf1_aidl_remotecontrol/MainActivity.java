package com.github.keithyokoma.tbf1_aidl_remotecontrol;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.IRemoteControlDisplay;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Keep;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int MSG_UPDATE_STATE = 100;
	private static final int MSG_SET_METADATA = 101;
	private static final int MSG_SET_TRANSPORT_CONTROLS = 102;
	private static final int MSG_SET_ARTWORK = 103;
	private static final int MSG_SET_GENERATION_ID = 104;
	private ImageView albumArt;
	private TextView songTitle;
	private TextView artistName;
	private Button previous;
	private Button playPause;
	private Button next;
	private Handler handler = new RemoteControlClientHandler(this);
	private IRemoteControlDisplay display;
	private PendingIntent clientIntent;
	private Metadata metadata = new Metadata();
	private int clientGeneration;
	private int controlFlags;
	private int currentPlayState;
	private AudioManager audioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		albumArt = (ImageView) findViewById(R.id.album_art);
		songTitle = (TextView) findViewById(R.id.song_title);
		artistName = (TextView) findViewById(R.id.artist_name);
		previous = (Button) findViewById(R.id.previous_track);
		playPause = (Button) findViewById(R.id.play_pause);
		next = (Button) findViewById(R.id.next_track);

		previous.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
			}
		});
		next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
			}
		});
		playPause.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
			}
		});

		display = new IRemoteControlDisplayWeak(handler);
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		try {
			Class clazz = audioManager.getClass();
			Method method = clazz.getDeclaredMethod("registerRemoteControlDisplay", IRemoteControlDisplay.class);
			method.invoke(audioManager, display);
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "no method found", e);
		} catch (InvocationTargetException e) {
			Log.e(TAG, "something went wrong while executing the method.", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "cannot access to the method", e);
		}
	}

	@Override
	protected void onDestroy() {
		try {
			Class clazz = audioManager.getClass();
			Method method = clazz.getDeclaredMethod("unregisterRemoteControlDisplay", IRemoteControlDisplay.class);
			method.invoke(audioManager, display);
		} catch (NoSuchMethodException e) {
			Log.e(TAG, "no method found", e);
		} catch (InvocationTargetException e) {
			Log.e(TAG, "something went wrong while executing the method.", e);
		} catch (IllegalAccessException e) {
			Log.e(TAG, "cannot access to the method", e);
		}
		super.onDestroy();
	}

	private String getMdString(Bundle bundle, int i) {
		return bundle.getString(Integer.toString(i));
	}

	private void handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_UPDATE_STATE:
				if (clientGeneration == msg.arg1) {
					currentPlayState = msg.arg2;
					updatePlaybackControl();
				}
				break;
			case MSG_SET_TRANSPORT_CONTROLS:
				if (clientGeneration == msg.arg1) {
					controlFlags = msg.arg2;
					updatePlaybackControl();
				}
				break;
			case MSG_SET_METADATA:
				if (clientGeneration == msg.arg1) {
					Bundle data = (Bundle) msg.obj;
					metadata.artist = getMdString(data, MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
					metadata.trackTitle = getMdString(data, MediaMetadataRetriever.METADATA_KEY_TITLE);
					updateMetadata();
				}
				break;
			case MSG_SET_ARTWORK:
				if (clientGeneration == msg.arg1) {
					metadata.bitmap = (Bitmap) msg.obj;
					updateMetadata();
				}
				break;
			case MSG_SET_GENERATION_ID:
				clientGeneration = msg.arg1;
				clientIntent = (PendingIntent) msg.obj;
				break;
		}
	}

	private void updateMetadata() {
		artistName.setText(metadata.artist);
		songTitle.setText(metadata.trackTitle);
		albumArt.setImageBitmap(metadata.bitmap);
	}

	private void updatePlaybackControl() {
		previous.setVisibility((controlFlags & RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS) != 0 ? View.VISIBLE : View.GONE);
		next.setVisibility((controlFlags & RemoteControlClient.FLAG_KEY_MEDIA_NEXT) != 0 ? View.VISIBLE : View.GONE);
		playPause.setText(currentPlayState == RemoteControlClient.PLAYSTATE_PLAYING ? R.string.button_pause : R.string.button_play);
	}

	private void sendMediaButtonEvent(int keyCode) {
		if (clientIntent == null)
			return;

		KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
		Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		try {
			clientIntent.send(getApplication(), 0, intent);
		} catch (PendingIntent.CanceledException e) {
			Log.e(TAG, "Error sending intent for media button down: " + e);
			e.printStackTrace();
		}

		keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
		intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
		intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
		try {
			clientIntent.send(getApplication(), 0, intent);
		} catch (PendingIntent.CanceledException e) {
			Log.e(TAG, "Error sending intent for media button up: " + e);
			e.printStackTrace();
		}
	}

	private class Metadata {
		private String artist;
		private Bitmap bitmap;
		private String trackTitle;
	}

	private static class RemoteControlClientHandler extends Handler {
		private MainActivity activity;

		public RemoteControlClientHandler(MainActivity activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			activity.handleMessage(msg);
		}
	}

	private static class IRemoteControlDisplayWeak extends android.media.IRemoteControlDisplay.Stub {
		private Handler handler;

		public IRemoteControlDisplayWeak(Handler handler) {
			this.handler = handler;
		}

		@Override
		public void setCurrentClientId(int clientGeneration, PendingIntent clientMediaIntent, boolean clearing) throws RemoteException {
			handler.obtainMessage(MSG_SET_GENERATION_ID, clientGeneration, (clearing ? 1 : 0), clientMediaIntent).sendToTarget();
		}

		@Override
		public void setEnabled(boolean enabled) throws RemoteException {

		}

		@Keep
		public void setPlaybackState(int generationId, int state, long stateChangeTimeMs) {
			handler.obtainMessage(MSG_UPDATE_STATE, generationId, state).sendToTarget();
		}

		@Override
		public void setPlaybackState(int generationId, int state, long stateChangeTimeMs, long currentPosMs, float speed) throws RemoteException {
			handler.obtainMessage(MSG_UPDATE_STATE, generationId, state).sendToTarget();
		}

		@Override
		public void setTransportControlFlags(int generationId, int transportControlFlags) throws RemoteException {
			handler.obtainMessage(MSG_SET_TRANSPORT_CONTROLS, generationId, transportControlFlags).sendToTarget();
		}

		@Override
		public void setTransportControlInfo(int generationId, int transportControlFlags, int posCapabilities) throws RemoteException {
			handler.obtainMessage(MSG_SET_TRANSPORT_CONTROLS, generationId, transportControlFlags).sendToTarget();
		}

		@Override
		public void setMetadata(int generationId, Bundle metadata) throws RemoteException {
			handler.obtainMessage(MSG_SET_METADATA, generationId, 0, metadata).sendToTarget();
		}

		@Override
		public void setArtwork(int generationId, Bitmap artwork) throws RemoteException {
			handler.obtainMessage(MSG_SET_ARTWORK, generationId, 0, artwork).sendToTarget();

		}

		@Override
		public void setAllMetadata(int generationId, Bundle metadata, Bitmap artwork) throws RemoteException {
			handler.obtainMessage(MSG_SET_METADATA, generationId, 0, metadata).sendToTarget();
			handler.obtainMessage(MSG_SET_ARTWORK, generationId, 0, artwork).sendToTarget();
		}
	}
}
