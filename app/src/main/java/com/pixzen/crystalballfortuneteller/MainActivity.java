package com.pixzen.crystalballfortuneteller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final int FADE_DURATION = 1500;
    public static final int START_OFFSET = 1000;
    public static final int VIBRATE_TIME = 250;
    public static final int THRESHOLD = 240;
    public static final int SHAKE_COUNT = 2;
    private static Random RANDOM = new Random();
    private Vibrator vibrator;
    private SensorManager sensorManager;
    private Sensor sensor;
    private float lastX, lastY, lastZ;
    private int shakeCount = 0;
    private TextView answerTextView;
    private ImageView crystalBallImageView;
    private Animation shakeAnimation;
    private ArrayList<String> fortune_teller_answers;
    private TextToSpeech answerTalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // allow volume buttons to control app volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // load services, sensor, sensorManager, and animations
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);

        // load animation and answers
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
        fortune_teller_answers = loadAnswers();

        // get references to GUI components
        crystalBallImageView = (ImageView) findViewById(R.id.crystalballImageView);
        answerTextView = (TextView) findViewById(R.id.answer_textView);

        // text to speech used to read answers
        answerTalk=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            // Overrides the default voice with the UK voice
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    answerTalk.setLanguage(Locale.UK);
                }
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        showAnswer(getString(R.string.answerTextView), false);
    }

    @Override
    public void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    // looks for sensor changes
    public void onSensorChanged(SensorEvent sensorEvent){
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (isShakeEnough(sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2])) {
                showAnswer(getAnswer(), false);
            }
        }
    }

    // not implemented - required for interface
    public void onAccuracyChanged(Sensor sensor, int i){

    }

    // checks to see if the shake is enough register event
    private boolean isShakeEnough(float x, float y, float z) {
        double force = 0d;

        force += Math.pow((x - lastX) / SensorManager.GRAVITY_EARTH, 2.0);
        force += Math.pow((y - lastY) / SensorManager.GRAVITY_EARTH, 2.0);
        force += Math.pow((z - lastZ) / SensorManager.GRAVITY_EARTH, 2.0);

        force = Math.sqrt(force);

        lastX = x;
        lastY = y;
        lastZ = z;

        if (force > ((float) THRESHOLD / 100f)) {
            crystalBallImageView.startAnimation(shakeAnimation);
            shakeCount++;

            if (shakeCount > SHAKE_COUNT) {
                shakeCount = 0;
                lastX = 0;
                lastY = 0;
                lastZ = 0;
                return true;
            }
        }

        return false;
    }

    // shows the answer in the text view and speaks results
    private void showAnswer(String answer, boolean withAnim) {
        if (withAnim) {
            crystalBallImageView.startAnimation(shakeAnimation);
        }

        answerTextView.setVisibility(View.INVISIBLE);
        answerTextView.setText(answer);
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setStartOffset(START_OFFSET);
        answerTextView.setVisibility(View.VISIBLE);
        animation.setDuration(FADE_DURATION);

        answerTextView.startAnimation(animation);
        vibrator.vibrate(VIBRATE_TIME);

        answerTalk.speak(answer, TextToSpeech.QUEUE_FLUSH, null);
    }

    // gets a random answer from the array of possibilities
    private String getAnswer() {
        int randomInt = RANDOM.nextInt(fortune_teller_answers.size());
        return fortune_teller_answers.get(randomInt);
    }

    // loads the answer to a string array and returns it
    public ArrayList<String> loadAnswers(){
        ArrayList<String> list = new ArrayList<>();
        String[] tab = getResources().getStringArray(R.array.fortune_teller_answers);

        if (tab != null && tab.length > 0) {
            for (String str : tab) {
                list.add(str);
            }
        }

        return list;
    }


}
