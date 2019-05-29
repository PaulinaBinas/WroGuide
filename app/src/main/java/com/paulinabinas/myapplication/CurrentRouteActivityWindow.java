package com.paulinabinas.myapplication;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class CurrentRouteActivityWindow extends Activity {


    Button btn_close_route;
    TextView text_view_distance;
    TextView text_view_time;
    String distance;
    String time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);

        // ...but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_route_window);
        text_view_distance = findViewById(R.id.distanceOfRoute);
        text_view_time = findViewById(R.id.timeOfRoute);

        Bundle bundle = getIntent().getExtras();
        distance = String.format("%.2f", bundle.getDouble("distance") / 1000) + " km";
        time = String.format("%.2f", bundle.getDouble("time") / 60) + " min";

        text_view_distance.setText(distance);
        text_view_time.setText(time);

        btn_close_route = (Button) findViewById(R.id.btn_close_route);
        btn_close_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                finish();
            }
        });



        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width, (int)(height*0.3));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.BOTTOM;

        getWindow().setAttributes(params);



    }


}
