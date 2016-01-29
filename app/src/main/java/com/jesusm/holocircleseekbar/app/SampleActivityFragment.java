package com.jesusm.holocircleseekbar.app;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jesusm.holocircleseekbar.lib.HoloCircleSeekBar;

public class SampleActivityFragment extends Fragment {

    public SampleActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sample, container, false);
        HoloCircleSeekBar seekBar = (HoloCircleSeekBar) rootView.findViewById(R.id.picker);
        seekBar.setOnSeekBarChangeListener(new HoloCircleSeekBar.OnCircleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(HoloCircleSeekBar seekBar, int progress, boolean fromUser) {
                if (progress > 50) {
                    seekBar.setNegativeColor(Color.DKGRAY);
                    seekBar.setPointerWheelColor(Color.BLUE);
                }

            }

            @Override
            public void onStartTrackingTouch(HoloCircleSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(HoloCircleSeekBar seekBar) {

            }
        });

        seekBar.setNegativeColor(Color.WHITE);
        return rootView;
    }
}
