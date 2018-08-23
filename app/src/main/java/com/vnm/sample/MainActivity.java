package com.vnm.sample;

import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.util.Log;

import com.vnm.numby.view.Numby;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Numby numby = findViewById(R.id.numby);

        numby.setOnValueChangeListener(new Numby.OnValueChangeListener() {
            @Override
            public void onValueChange(Numby view, int oldValue, int newValue) {
                Snackbar snackbar = Snackbar
                        .make(findViewById(R.id.root), "current value: " + newValue, Snackbar.LENGTH_SHORT);
                snackbar.show();

                Log.d("numby: ", "onValueChange: " + newValue);
            }
        });
    }
}
