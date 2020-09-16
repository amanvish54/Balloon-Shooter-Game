package com.example.argame;

import android.content.DialogInterface;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.Camera;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.collision.Ray;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.Texture;
import com.google.ar.sceneform.ux.BaseArFragment;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private Scene scene;
    private Camera camera;
    private ModelRenderable bulletRenderable;
    private boolean shouldStartTimer = true;
    private int balloonsLeft = 40;
    private Point point;
    private TextView balloonsLeftTxt;
    private SoundPool soundPool;
    private int sound;
    private TextView timer;

//    @Override
//    public void onUserInteraction() {
//        super.onUserInteraction();
//        if (shouldStartTimer) {
//                startTimer();
//                shouldStartTimer = false;
//            }
//
//            shoot();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        point = new Point();
        display.getRealSize(point);

        setContentView(R.layout.activity_main);

        loadSoundPool();

        balloonsLeftTxt = findViewById(R.id.ballons_count_txt);
        CustomArFragment arFragment =
                (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_fragment);


        assert arFragment != null;
        scene = arFragment.getArSceneView().getScene();
        camera = scene.getCamera();

        addBalloonsToScene();
        buildBulletModel();



        Button shoot = findViewById(R.id.shootButton);


        shoot.setOnClickListener(v -> {

            if (shouldStartTimer) {
                startTimer();
                shouldStartTimer = false;
            }

            shoot();

        });


    }

    private void loadSoundPool() {

        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_GAME)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(1)
                .setAudioAttributes(audioAttributes)
                .build();

        sound = soundPool.load(this, R.raw.blop_sound, 1);

    }

    private void shoot() {

        Ray ray = camera.screenPointToRay(point.x / 2f, point.y / 2f);
        Node node = new Node();
        node.setRenderable(bulletRenderable);
        scene.addChild(node);

        new Thread(() -> {

            for (int i = 0;i < 200;i++) {

                int finalI = i;
                runOnUiThread(() -> {

                    Vector3 vector3 = ray.getPoint(finalI * 0.1f);
                    node.setWorldPosition(vector3);

                    Node nodeInContact = scene.overlapTest(node);

                    if (nodeInContact != null) {

                        balloonsLeft--;
                        balloonsLeftTxt.setText("Balloons Left: " + balloonsLeft);
                        scene.removeChild(nodeInContact);

                        soundPool.play(sound, 1f, 1f, 1, 0
                                , 1f);

                        if (balloonsLeft <= 0){
                            AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                            dialog.setTitle("Your Score : " + timer.getText().toString());
                            dialog.setMessage("Do you want to Play again?");
                            dialog.setPositiveButton("Play Again", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    addBalloonsToScene();
                                    balloonsLeft = 40;
                                    balloonsLeftTxt.setText("Balloons Left: 40");
                                }
                            }).setNegativeButton("Exit game", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finishAffinity();
                                }
                            });
                            dialog.show();


                        }

                    }

                });

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            runOnUiThread(() -> scene.removeChild(node));

        }).start();

    }

    private void startTimer() {

        timer = findViewById(R.id.timertxt);

        new Thread(() -> {

            int seconds = 0;

            while (balloonsLeft > 0) {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                seconds++;

                int minutesPassed = seconds / 60;
                int secondsPassed = seconds % 60;

                runOnUiThread(() -> timer.setText(minutesPassed + ":" + secondsPassed));

            }

        }).start();

    }

    private void buildBulletModel() {

        Texture
                .builder()
                .setSource(this, R.drawable.texture)
                .build()
                .thenAccept(texture -> {


                    MaterialFactory
                            .makeOpaqueWithTexture(this, texture)
                            .thenAccept(material -> {

                                bulletRenderable = ShapeFactory
                                        .makeSphere(0.01f,
                                                new Vector3(0f, 0f, 0f),
                                                material);

                            });


                });

    }

    private void addBalloonsToScene() {

        ModelRenderable
                .builder()
                .setSource(this, R.raw.balloon)
                .build()
                .thenAccept(renderable -> {

                    for (int i = 0;i < 40;i++) {

                        Node node = new Node();
                        node.setRenderable(renderable);
                        scene.addChild(node);


                        Random random = new Random();
                        int x = random.nextInt(10);
                        int z = random.nextInt(10);
                        int y = random.nextInt(20);

                        z = -z;

                        node.setWorldPosition(new Vector3(
                                (float) x,
                                y / 10f,
                                (float) z
                        ));


                    }

                });

    }
}
