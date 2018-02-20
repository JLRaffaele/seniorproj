package com.raffaele.jeanluc.playerlocator;


import android.os.Bundle;

import com.takisoft.fix.support.v7.preference.PreferenceFragmentCompat;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragmentCompat {


    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState,
                                    String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);



    }


}
