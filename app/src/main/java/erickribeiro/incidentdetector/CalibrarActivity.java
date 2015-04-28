package erickribeiro.incidentdetector;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import erickribeiro.incidentdetector.util.SharedPreferenceManager;

public class CalibrarActivity extends ActionBarActivity implements OnClickListener, SensorEventListener {
    private SharedPreferences prefCalibracao;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mProximity;

    private boolean flagCalibracao = false;
    private boolean flagCalibracaoFinalizada = false;
    private boolean flagCelularProxAoCorpo = false;

    private Button buttonIniciarCalibracao;
    private ImageView passo_1;
    private ImageView passo_2;
    private ImageView passo_3;
    private ImageView passo_4;

    // Iniciando objetos de musica do android...
    Uri objNotification;
    Ringtone objRing;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrar);

        buttonIniciarCalibracao = (Button) findViewById(R.id.button_iniciar_calibracao);
        buttonIniciarCalibracao.setOnClickListener(this);

        passo_1 = (ImageView) findViewById(R.id.imageView1);
        passo_2 = (ImageView) findViewById(R.id.imageView2);
        passo_3 = (ImageView) findViewById(R.id.imageView3);
        passo_4 = (ImageView) findViewById(R.id.imageView4);

        // Inicializando o servico...
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        prefCalibracao = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(prefCalibracao.getString(SharedPreferenceManager.CHAVE_PERFIL, SharedPreferenceManager.VALOR_PADRAO_PERFIL)
                .equals(SharedPreferenceManager.VALOR_PADRAO_PERFIL)){
        }

        /** BEGIN: Iniciando objetos de musica do android... **/
        objNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        objRing = RingtoneManager.getRingtone(getApplicationContext(), objNotification);
        /** BEGIN: Iniciando objetos de musica do android... **/
    }

    @Override
    public void onDestroy() {
        // Destroindo o servico...
        mSensorManager.unregisterListener(this);

        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_iniciar_calibracao:
                flagCalibracao = true;
                Toast.makeText(getApplicationContext(), "Iniciando processo de calibração.", Toast.LENGTH_LONG).show();

                buttonIniciarCalibracao.setEnabled(false);

                passo_1.setImageResource(R.drawable.passo_01);
                //prefCalibracao.edit().putString(SharedPreferenceManager.CHAVE_PERFIL, "0").apply();
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        double x = 0;
        double y = 0;
        double z = 0;
        int typeSensor = event.sensor.getType();
        double proximityValue = 0.0;

        if(flagCalibracao)
        {
            switch (typeSensor) {
                case Sensor.TYPE_PROXIMITY:
                    proximityValue = event.values[0];
                    if(proximityValue < 1)
                    {
                        flagCelularProxAoCorpo = true;
                    }
                    else
                    {
                        flagCelularProxAoCorpo = false;
                    }
                    Toast.makeText(getApplicationContext(), "Incident Detector - Proximidade Working: " + Double.toString(proximityValue), Toast.LENGTH_SHORT).show();
                    break;

                case Sensor.TYPE_ACCELEROMETER:
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    break;
            }

            if(flagCelularProxAoCorpo)
            {
                Toast.makeText(getApplicationContext(), "Monitorando a calibracao....", Toast.LENGTH_LONG).show();

                if(!objRing.isPlaying()) /** Emitindo beep... para validar um possivel desmaio... **/
                    objRing.play();

                flagCalibracao = false;
                flagCalibracaoFinalizada = true;
            }
        }

        if(flagCalibracaoFinalizada)
        {
            flagCelularProxAoCorpo = false;
            flagCalibracaoFinalizada = false;

            Toast.makeText(this, "Processo de calibração finalizado.", Toast.LENGTH_SHORT).show();

            passo_1.setImageResource(R.drawable.passo_01_cinza);
            passo_2.setImageResource(R.drawable.passo_02_cinza);
            passo_3.setImageResource(R.drawable.passo_03_cinza);
            passo_4.setImageResource(R.drawable.passo_04_cinza);

            buttonIniciarCalibracao.setEnabled(true);

        }
    }
}
