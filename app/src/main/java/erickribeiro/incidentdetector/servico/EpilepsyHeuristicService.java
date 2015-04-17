package erickribeiro.incidentdetector.servico;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.Toast;

import erickribeiro.incidentdetector.MainActivity;
import erickribeiro.incidentdetector.R;

public class EpilepsyHeuristicService extends Service implements SensorEventListener{

    public static String TAG = "EpilepsyHeuristicService";

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mProximity;

    private EpilepsyHeuristic objHeuristica;

    private boolean flagLogs = true;

    public IBinder onBind(Intent i) {
        return null;
    }

    public void onCreate() {
        // Inicializando o servico...
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        // LENDO DADOS DO BANCO DE DADOS DO APLICATIVO...
        Context context = getApplicationContext();
        SharedPreferences appDB = PreferenceManager.getDefaultSharedPreferences(context);
        String perfilMonitoramento = appDB.getString("pref_key_perfis", String.valueOf(EpilepsyHeuristic.PERFIL_MODERADO));

        objHeuristica = new EpilepsyHeuristic(getApplicationContext(), Integer.valueOf(perfilMonitoramento), flagLogs);

        // Criando o Servico...
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        // Destroindo o servico...
        mSensorManager.unregisterListener(this);

        super.onDestroy();

        Toast.makeText(this, getString(R.string.txt_monitoramento_desligado), Toast.LENGTH_SHORT).show();
    }

    public void onStart(Intent intent, int startid) {
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mGyroscope, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        Toast.makeText(this, getString(R.string.txt_monitoramento_ligado), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        // Verificando se esta acontecendo algum desmaio ou ataque epileptico...
        if(objHeuristica.monitorar(event))
        {
            exibirAlertaDesmaio();
        }
    }

    public void exibirAlertaDesmaio()
    {
        Intent dialogIntent = new Intent(this, MainActivity.class);
        dialogIntent.putExtra("flagDesmaioDetectadoMonitoramento", true);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        this.startActivity(dialogIntent);
    }
}
