package com.github.keithyokoma.tbf1_aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author KeishinYokomaku
 */
public class SampleParcelable implements Parcelable {
	public static final Creator<SampleParcelable> CREATOR = new Creator<SampleParcelable>() {
		public SampleParcelable createFromParcel(Parcel in) {
			return new SampleParcelable(in);
		}

		public SampleParcelable[] newArray(int size) {
			return new SampleParcelable[size];
		}
	};
	private String name;

	public SampleParcelable(String name) {
		this.name = name;
	}

	protected SampleParcelable(Parcel in) {
		this.name = in.readString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
	}
}