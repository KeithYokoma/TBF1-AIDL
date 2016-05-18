// SampleAidlService.aidl
package com.github.keithyokoma.tbf1_aidl;

// Declare any non-default types here with import statements
import com.github.keithyokoma.tbf1_aidl.SampleParcelable;

interface SampleAidlService {
    String getSomething();
    SampleParcelable getParcelable();
}
