// IRemoteControlDisplay.aidl
package android.media;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;

oneway interface IRemoteControlDisplay {
    void setCurrentClientId(int clientGeneration, in PendingIntent clientMediaIntent, boolean clearing);
    void setEnabled(boolean enabled);
    void setPlaybackState(int generationId, int state, long stateChangeTimeMs, long currentPosMs, float speed);
    void setTransportControlFlags(int generationId, int transportControlFlags);
    void setTransportControlInfo(int generationId, int transportControlFlags, int posCapabilities);
    void setMetadata(int generationId, in Bundle metadata);
    void setArtwork(int generationId, in Bitmap artwork);
    void setAllMetadata(int generationId, in Bundle metadata, in Bitmap artwork);
}