package erickribeiro.incidentdetector;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import erickribeiro.incidentdetector.configuracao.Contato;
import erickribeiro.incidentdetector.databe.HistoryContract;
import erickribeiro.incidentdetector.servico.IncidentHeuristicService;
import erickribeiro.incidentdetector.util.GPSTracker;
import erickribeiro.incidentdetector.util.SharedPreferenceManager;

public class MainActivity extends ActionBarActivity implements OnClickListener, SharedPreferenceManager {
    Boolean flagDesmaioDetectadoMonitoramento = false;
    // Iniciando objetos de musica do android...
    Ringtone objRing;
    Button btnAlertDesmaio;

    /**
     * Variavel utilizadas na localizacao
     */
    GPSTracker gpsTracker;
    Contato contato;

    /**
     * Constante com o valor do tempo em milisegundos que a mensagem de desmaio será mostrada ao usuário.<br>
     * INTERVAL = 1000
     */
    private final int INTERVAL = 1000;
    int contSms = 0;

    private ImageButton imgAtivado;
    private ImageButton imgCalibracao;
    private ImageButton imgAlerta;
    private ImageButton imgAgenda;

    private TextView txtAtivado;
    private PendingIntent pendingIntent;
    private NotificationManager mNotifyManager;
    private Builder mBuilder;
    private SharedPreferences prefAlerta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefAlerta = PreferenceManager.getDefaultSharedPreferences(this);

        imgAtivado = (ImageButton) findViewById(R.id.img_ativar);
        imgCalibracao = (ImageButton) findViewById(R.id.img_calibracao);
        imgAlerta = (ImageButton) findViewById(R.id.img_alerta);
        imgAgenda = (ImageButton) findViewById(R.id.img_agenda);

        txtAtivado = (TextView) findViewById(R.id.txt_ativado);

        imgAtivado.setOnClickListener(this);
        imgCalibracao.setOnClickListener(this);
        imgAlerta.setOnClickListener(this);
        imgAgenda.setOnClickListener(this);

        //showDialogDesmaio();

        /** Verificando se houve algum desmaio detectado pelo sistema de monitoramento... **/
        if(getIntent().hasExtra("flagDesmaioDetectadoMonitoramento")) {
            Bundle extras = getIntent().getExtras();
            flagDesmaioDetectadoMonitoramento = extras.getBoolean("flagDesmaioDetectadoMonitoramento");
            if(flagDesmaioDetectadoMonitoramento == true)
            {
                showDialogDesmaio();
            }
        }
        else{
            flagDesmaioDetectadoMonitoramento = false;
        }

        //Toast.makeText(getApplicationContext(), String.valueOf(prefAlerta.getBoolean(SharedPreferenceManager.CHAVE_ATIVADO, false)), Toast.LENGTH_SHORT).show();
        if (prefAlerta.getBoolean(SharedPreferenceManager.CHAVE_ATIVADO, false)){
            imgAtivado.setBackgroundResource(R.drawable.checkv);
            txtAtivado.setText("Ativado");
        }else{
            imgAtivado.setBackgroundResource(R.drawable.desativado);
            txtAtivado.setText("Desativado");
            stopSensing();
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {

            case R.id.img_ativar:
                boolean value = prefAlerta.getBoolean(SharedPreferenceManager.CHAVE_ATIVADO, SharedPreferenceManager.VALOR_PADRAO_ATIVADO);
                //Toast.makeText(getApplicationContext(), String.valueOf(value), Toast.LENGTH_SHORT).show();
                if(value){
                    imgAtivado.setBackgroundResource(R.drawable.desativado);
                    txtAtivado.setText("Desativado");
                    stopSensing();
                    prefAlerta.edit().putBoolean(SharedPreferenceManager.CHAVE_ATIVADO, false).apply();
                }else{
                    imgAtivado.setBackgroundResource(R.drawable.checkv);
                    txtAtivado.setText("Ativado");
                    startSensing();
                    prefAlerta.edit().putBoolean(SharedPreferenceManager.CHAVE_ATIVADO, true).apply();
                }
                break;

            case R.id.img_calibracao:
                intent = new Intent(getApplicationContext(), CalibrarActivity.class);
                startActivity(intent);
                break;

            case R.id.img_alerta:
                intent = new Intent(getApplicationContext(), ConfiguracoesActivity.class);
                startActivity(intent);
                break;

            case R.id.img_agenda:
                intent = new Intent(getApplicationContext(), HistoryActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    /***
     * Método responsável por gerar uma mensagem que será mostrada ao usuario informando que um possivel desmaio foi detectado.
     */

    boolean acao = false;
    public void showDialogDesmaio(){
        contato = new Contato(this);
        // Prepara o Dialog informando o título, mensagem e cria o Positive Button
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.title);
        alertBuilder.setMessage(getString(R.string.msg_notificacao));

        // Aqui você pode definir a ação de clique do botão
        alertBuilder.setPositiveButton("OK", null);

        // Criar o AlertDialog
        final AlertDialog alert = alertBuilder.create();

        alert.show();

        // Iniciando objetos de musica do android...
        Uri objNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        objRing = RingtoneManager.getRingtone(getApplicationContext(), objNotification);

        // Pega o botão do Alert
        btnAlertDesmaio = alert.getButton(AlertDialog.BUTTON_POSITIVE);

        // Objeto vibracao...
        final Vibrator objVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        // Start without a delay
        // Each element then alternates between vibrate, sleep, vibrate, sleep...
        long[] pattern = {300, 100, 1000, 300, 200, 100, 500, 200, 100};
        objVibrator.vibrate(pattern, 0);

        // Cria um objeto da classe CountDownTimer
        // informando a duração e o intervalo
        final CountDownTimer timerDialog = new CountDownTimer(getDuration(), INTERVAL) {

            // A cada 1 segundo o onTick é invocado
            @Override
            public void onTick(long millisUntilFinished) {
                // Formata o texto do botão com os segundos.
                // Ex. OK (9)
                btnAlertDesmaio.setText(("Cancelar envio de SMS? ("+(millisUntilFinished/INTERVAL)) + ")");

                // 	Toca o alerta sonoro...
                try {
                    if(!objRing.isPlaying()){
                        objRing.play();
                    }
                }catch (Exception e) {}
            }

            @Override
            public void onFinish(){
                contato.enviarSmsParaContatos();
                alert.dismiss();

                HistoryContract historyContract = new HistoryContract(getApplicationContext());
                historyContract.insert(true);
                acao =  true;
                Log.d("TAG","terminou");
            }
        }.start();

        alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                // Para o alerta sonoro...
                try {
                    timerDialog.cancel(); // Para o contador do alerte...

                    objVibrator.cancel(); // Faz o celular parar de vibrar...

                    objRing.stop(); // Para de tocar a musica...

                    if(!acao) {
                        Log.d("TAG", "cancelado");
                        HistoryContract historyContract = new HistoryContract(getApplicationContext());
                        historyContract.insert(false);
                    }
                }catch (Exception e) {}
            }
        });
    }

    /**
     * Método que retorna a duração de tempo que o alerta de posível desmaio será mostrado ao usuário.
     * @return tempo em milisegundos
     */
    @Override
    public int getDuration(){
        SharedPreferences prefAlerta = PreferenceManager.getDefaultSharedPreferences(this);
        String alerta = prefAlerta.getString(SharedPreferenceManager.CHAVE_DURACAO, SharedPreferenceManager.VALOR_PADRAO_DURACAO);
        return Integer.valueOf(alerta)*INTERVAL;
    }

    /************************************ SERVIÇO ************************************/

    private void toggleService(){
        Context context = getApplicationContext();
        Intent intent = new Intent(context, IncidentHeuristicService.class);
        // Try to stop the service if it is already running
        intent.addCategory(IncidentHeuristicService.TAG);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), TIMER_MONITORAMENTO_HEURISTICA, pendingIntent);

        if(!stopService(intent)){
            startService(intent);
        }
    }

    public void startSensing(){
        showNotification();
        toggleService();
        //this.finish();
    }

    private void stopSensing(){
        Intent intent = new Intent(getApplicationContext(), IncidentHeuristicService.class);
        intent.addCategory(IncidentHeuristicService.TAG);
        stopService(intent);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.cancelAll();

        Context context = getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    /******************************	BARRA DE NOTIFICAÇÃO ******************************/

    @SuppressLint("NewApi")
    public void showNotification(){
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notificacao_texto))
                .setSmallIcon(R.drawable.logo);
        Intent resultIntent = new Intent(this, MainActivity.class);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_ONE_SHOT
                );

        mBuilder.setOngoing(true);
        mBuilder.setContentIntent(resultPendingIntent);
        mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyManager.notify(ID, mBuilder.build());
    }
    int ID = 101;
    private final int TIMER_MONITORAMENTO_HEURISTICA = 30;
}
