package com.example.wroguide;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.io.Serializable;

import static com.example.wroguide.MapsActivity.PICK_DIRECTIONS_REQUEST;

public class MarkerInfoActivityWindow extends Activity {

    Button btn_close;
    Button btn_directions;

    String id;
    String title;
    String snippet;

    StorageReference storageReference;

    public void download(String id) {

        storageReference = FirebaseStorage.getInstance().getReference().child(id+".jpg");
        final ImageView image_view = (ImageView) findViewById(R.id.place_image);
        image_view.setVisibility(View.GONE);

        Log.i("id", id);
        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                image_view.setVisibility(View.VISIBLE);
                image_view.setImageBitmap(bitmap);
                Log.i("Downloading image", "Successful");

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_info_window);

        final Bundle bundle = getIntent().getExtras();

        id = bundle.getString("id");
        title = bundle.getString("title");
        snippet = bundle.getString("snippet");

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        TextView title_view = (TextView) findViewById(R.id.text_title);
        TextView snippet_view = (TextView) findViewById(R.id.text_snippet);

        title_view.setText(title);
        snippet_view.setText(snippet);

        final Intent intent = new Intent();
        intent.putExtras(bundle);
        download(id);

        btn_directions = (Button) findViewById(R.id.btn_directions);
        btn_directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });



        btn_close = (Button) findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setResult(Activity.RESULT_CANCELED, new Intent());
                finish();
            }
        });




        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels - 30;
        int height = dm.heightPixels;

        getWindow().setLayout(width, (int)(height/1.5));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;

        getWindow().setAttributes(params);




    }
}
