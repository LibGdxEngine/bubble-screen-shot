package com.devahmed.demo.screenshoter.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.devahmed.demo.screenshoter.R;
import com.devahmed.demo.screenshoter.common.BaseObservableMvcView;

public class MainMvcImp extends BaseObservableMvcView<MainMvc.Listener> implements MainMvc {
    private Button screenShotBtn;


    public MainMvcImp(LayoutInflater inflater, ViewGroup parent) {
        setRootView(inflater.inflate(R.layout.activity_main , parent , false));
        screenShotBtn = findViewById(R.id.screenShotBtn);
        screenShotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(Listener listener : getmListeners()){
                    listener.onTakeScreenShotBtnClicked("day");
                }
            }
        });
    }
}
