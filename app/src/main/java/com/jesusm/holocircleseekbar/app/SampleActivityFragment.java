package com.jesusm.holocircleseekbar.app;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jesusm.holocircleseekbar.lib.HoloCircleSeekBar;

public class SampleActivityFragment extends Fragment {

    private HoloCircleSeekBar seekBar;
    private EditText maxValue;
    private EditText value;

    public SampleActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sample, container, false);
        seekBar = (HoloCircleSeekBar) rootView.findViewById(R.id.picker);
        maxValue = (EditText) rootView.findViewById(R.id.change_max_value);
        maxValue.setText(String.valueOf(seekBar.getMaxValue()));
        value = (EditText) rootView.findViewById(R.id.change_value);
        value.setText(String.valueOf(seekBar.getValue()));
        rootView.findViewById(R.id.change_max_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue = maxValue.getText().toString();
                if (!TextUtils.isEmpty(newValue)) {
                    seekBar.setMax(Integer.valueOf(newValue));
                }
            }
        });
        rootView.findViewById(R.id.change_value_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newValue = value.getText().toString();
                if (!TextUtils.isEmpty(newValue)) {
                    seekBar.setValue(Integer.valueOf(newValue));
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new HoloCircleSeekBar.OnCircleSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(HoloCircleSeekBar seekBar) {
                // Nothing to do
            }

            @Override
            public void onStopTrackingTouch(HoloCircleSeekBar seekBar) {
                // Nothing to do
            }

            @Override
            public void onProgressChanged(HoloCircleSeekBar seekBar, int progress, boolean fromUser) {
                value.setText(String.valueOf(progress));
            }
        });
        return rootView;
    }
}
