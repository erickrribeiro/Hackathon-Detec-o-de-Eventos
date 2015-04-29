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

import java.util.Stack;

import erickribeiro.incidentdetector.util.SharedPreferenceManager;

public class CalibrarActivity extends ActionBarActivity implements OnClickListener, SensorEventListener {

    private final boolean MODO_DEBUG = true;
    private SharedPreferences prefCalibracao;
    private final double ACELERACAO_NORMAL_GRAVIDADE = 9.8;

    private long timestampInicialCalibracao;
    private long timestampInicialCalibracao_Salto_1;
    private long timestampInicialCalibracao_Salto_2;
    private long timestampInicialCalibracao_Salto_3;

    private boolean flagColetarDadosSalto1 = false;
    private boolean flagColetarDadosSalto2 = false;
    private boolean flagColetarDadosSalto3 = false;

    private final int ESTADO_INICIAL = 0;
    private final int ESTADO_PRIMEIRO_SALTO_ESTABILIZAR_SENSORES = 1;
    private final int ESTADO_PRIMEIRO_SALTO_COLETAR_DADOS = 2;
    private final int ESTADO_SEGUNDO_SALTO_ESTABILIZAR_SENSORES = 3;
    private final int ESTADO_SEGUNDO_SALTO_COLETAR_DADOS = 4;
    private final int ESTADO_TERCEIRO_SALTO_ESTABILIZAR_SENSORES = 5;
    private final int ESTADO_TERCEIRO_SALTO_COLETAR_DADOS = 6;
    private final int ESTADO_FINAL = 7;
    private int estadoAtualCalibracao = ESTADO_INICIAL;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mGyroscope;
    private Sensor mProximity;

    private final double LIMITE_ACELERACAO_PICO_INFERIOR = 7;
    private final double LIMITE_ACELERACAO_PICO_SUPERIOR = 12;
    double maiorVariacaoModAceleracao = 0;
    double menorVariacaoModAceleracao = 0;

    private final double MARGEM_ERRO_AMOSTRAGEM_ACELERACAO_SINAL_ESTABILIZADO = 0.8;
    private final int JANELA_TEMPO_AMOSTRAGEM_ACELERACAO = 4; //(EM MILISSEGUNDOS) JANELA DE TEMPO DE CAPTURA DOS DADOS DA ACELERACAO.
    private final int QTD_MAX_AMOSTRAGEM_ACELERACAO = 500; // JANELA DE AMOSTRAGEM DA ACELERACAO PODERÁ TER NO MAXIMO 500 DADOS.
    Stack<Double> arrayAmostragemAceleracao = new Stack<Double>();
    Stack<Double> arrayTimelineAceleracao = new Stack<Double>();

    private boolean flagCalibracao = false;
    private boolean flagCelularProxAoCorpo = false;

    private Button buttonIniciarCalibracao;
    private ImageView passo_1;
    private ImageView passo_2;
    private ImageView passo_3;
    private ImageView passo_4;
    private ImageView passo_5;

    // Iniciando objetos de musica do android...
    Uri objNotification;
    Ringtone objRing;

    Uri objNotificationFimCalibracao;
    Ringtone objRingFimCalibracao;

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
        passo_5 = (ImageView) findViewById(R.id.imageView5);

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

        //  ALERTA SONORO QUE INDICA O FIM DO PROCESSO DE CALIBRACAO...
        objNotificationFimCalibracao = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        objRingFimCalibracao = RingtoneManager.getRingtone(getApplicationContext(), objNotificationFimCalibracao);
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
                if(buttonIniciarCalibracao.getText().toString().equals(getString(R.string.button_iniciar_calibracao)))
                {
                    flagCalibracao = true;

                    buttonIniciarCalibracao.setText("Cancelar");
                    passo_1.setImageResource(R.drawable.passo_01);
                    //prefCalibracao.edit().putString(SharedPreferenceManager.CHAVE_PERFIL, "0").apply();
                }
                else
                {
                    flagCalibracao = false;
                    Toast.makeText(getApplicationContext(), "Processo de calibração cancelado.", Toast.LENGTH_LONG).show();

                    resetarVariaveisCalibracao();
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    /**
     *  Esta funcao retorna a maior variacao da aceleracao... Ex.: -0.21, 0.50, 0.01, etc
     * @return
     */
    private double obterMaiorVariacaoModAceleracao() {
        double maiorVetorAceleracao = 0;
        double maiorVariacaoAceleracao = 0;

        for(Double valorAceleracao : arrayAmostragemAceleracao) {
            if(valorAceleracao >= maiorVetorAceleracao)
                maiorVetorAceleracao = valorAceleracao;
        }

        maiorVariacaoAceleracao = Math.abs(maiorVetorAceleracao - ACELERACAO_NORMAL_GRAVIDADE);
        return(maiorVariacaoAceleracao);
    }

    /**
     *  Esta funcao retorna a menor variacao da aceleracao... Ex.: -0.21, 0.50, 0.01, etc
     * @return
     */
    private double obterMenorVariacaoModAceleracao() {
        double menorVetorAceleracao = 0;
        double menorVariacaoAceleracao = 0;

        for(Double valorAceleracao : arrayAmostragemAceleracao) {
            if(valorAceleracao <= menorVetorAceleracao)
                menorVetorAceleracao = valorAceleracao;
        }

        menorVariacaoAceleracao = Math.abs(menorVetorAceleracao - ACELERACAO_NORMAL_GRAVIDADE);
        return(menorVariacaoAceleracao);
    }

    /**
     *  Esta funcao grava os dados do acelerometro na pilha.
     * @return
     */
    private void gravarDadosAcelerometro(double moduloAceleracao, double timeInicialCalibracao) {
        // Instante de tempo que o log foi gerado...
        long timestampAtualSistema = System.currentTimeMillis();
        double timestampAtualSegundos = (timestampAtualSistema - timeInicialCalibracao) / 1000.0; //Convertendo de milissegundos para segundos...

        // Atualizando a pilha de amostragens do acelerometro...
        if(arrayAmostragemAceleracao.size() >= QTD_MAX_AMOSTRAGEM_ACELERACAO || timestampAtualSegundos >= JANELA_TEMPO_AMOSTRAGEM_ACELERACAO)
        {
            arrayAmostragemAceleracao.pop();
            arrayTimelineAceleracao.pop();
        }
        arrayAmostragemAceleracao.add(0, moduloAceleracao);
        arrayTimelineAceleracao.add(0, timestampAtualSegundos);
    }

    private boolean verificarEstabilizacaoAcelerometro(double timeInicialCalibracaoSalto)
    {
        long timestampAtualSistema = System.currentTimeMillis();
        double timestampAtualSegundos = (timestampAtualSistema - timeInicialCalibracaoSalto) / 1000.0; //Convertendo de milissegundos para segundos...

        // Obtem o modulo do maior pico de variacao do acelerometro...
        double maiorVariacaoAceleracao = obterMaiorVariacaoModAceleracao();

        if(timestampAtualSegundos >= JANELA_TEMPO_AMOSTRAGEM_ACELERACAO && maiorVariacaoAceleracao <= MARGEM_ERRO_AMOSTRAGEM_ACELERACAO_SINAL_ESTABILIZADO)
        {
            return(true);
        }
        return(false);
    }

    /**
     *  reinicializa as variaveis do processo de calibracao...
     */
    private void resetarVariaveisCalibracao() {
        flagCalibracao = false;
        flagCelularProxAoCorpo = false;
        estadoAtualCalibracao = ESTADO_INICIAL;

        flagColetarDadosSalto1 = false;
        flagColetarDadosSalto2 = false;
        flagColetarDadosSalto3 = false;

        maiorVariacaoModAceleracao = 0;
        menorVariacaoModAceleracao = ACELERACAO_NORMAL_GRAVIDADE;

        timestampInicialCalibracao = 0;
        timestampInicialCalibracao_Salto_1 = 0;
        timestampInicialCalibracao_Salto_2 = 0;
        timestampInicialCalibracao_Salto_3 = 0;

        arrayAmostragemAceleracao.clear();
        arrayTimelineAceleracao.clear();

        buttonIniciarCalibracao.setText(R.string.button_iniciar_calibracao);
        passo_1.setImageResource(R.drawable.passo_01_cinza);
        passo_2.setImageResource(R.drawable.passo_02_cinza);
        passo_3.setImageResource(R.drawable.passo_03_cinza);
        passo_4.setImageResource(R.drawable.passo_04_cinza);
        passo_5.setImageResource(R.drawable.passo_05_cinza);

        objRing.stop();
        objRingFimCalibracao.stop();
    }

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        double x = 0;
        double y = 0;
        double z = 0;
        int typeSensor = event.sensor.getType();
        double proximityValue = 0.0;
        double moduloVetorAceleracao = ACELERACAO_NORMAL_GRAVIDADE;

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
                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];

                    // Calculando os modulos resultantes dos eixos x, y e z
                    moduloVetorAceleracao = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
                    break;

                case Sensor.TYPE_GYROSCOPE:
                    break;
            }

            if(flagCelularProxAoCorpo)
            {
                switch (estadoAtualCalibracao) {
                    case ESTADO_INICIAL:
                        Toast.makeText(getApplicationContext(), "Iniciando processo de calibração.", Toast.LENGTH_LONG).show();

                        // Obtendo instante inicial do log...
                        timestampInicialCalibracao = System.currentTimeMillis();
                        timestampInicialCalibracao_Salto_1 = System.currentTimeMillis();

                        gravarDadosAcelerometro(moduloVetorAceleracao, timestampInicialCalibracao);

                        estadoAtualCalibracao = ESTADO_PRIMEIRO_SALTO_ESTABILIZAR_SENSORES;
                        passo_2.setImageResource(R.drawable.passo_02);

                        break;

                    case ESTADO_PRIMEIRO_SALTO_ESTABILIZAR_SENSORES:
                        gravarDadosAcelerometro(moduloVetorAceleracao, timestampInicialCalibracao);

                        if(verificarEstabilizacaoAcelerometro(timestampInicialCalibracao_Salto_1)) {
                            maiorVariacaoModAceleracao = obterMaiorVariacaoModAceleracao();
                            menorVariacaoModAceleracao = obterMenorVariacaoModAceleracao();

                            estadoAtualCalibracao = ESTADO_PRIMEIRO_SALTO_COLETAR_DADOS;

                            if(!objRing.isPlaying()) /** EMITINDO ALERTA SONORO PARA COLETAR NOVOS DADOS... **/
                                objRing.play();
                        }
                        else
                            estadoAtualCalibracao = ESTADO_PRIMEIRO_SALTO_ESTABILIZAR_SENSORES;

                        break;

                    case ESTADO_PRIMEIRO_SALTO_COLETAR_DADOS:
                        gravarDadosAcelerometro(moduloVetorAceleracao, timestampInicialCalibracao);

                        // Detecta o menor pico de aceleracao
                        if(moduloVetorAceleracao < menorVariacaoModAceleracao)
                            menorVariacaoModAceleracao = moduloVetorAceleracao;

                        // Detecta o maior pico de aceleracao
                        if(moduloVetorAceleracao >= maiorVariacaoModAceleracao)
                            maiorVariacaoModAceleracao = moduloVetorAceleracao;

                        if(flagColetarDadosSalto1)
                        {
                            if(verificarEstabilizacaoAcelerometro(timestampInicialCalibracao_Salto_1)) {
                                Toast.makeText(getApplicationContext(), "Inf: " + Double.toString(menorVariacaoModAceleracao) + " Sup: " + Double.toString(maiorVariacaoModAceleracao), Toast.LENGTH_LONG).show();

                                timestampInicialCalibracao_Salto_2 = System.currentTimeMillis();
                                estadoAtualCalibracao = ESTADO_SEGUNDO_SALTO_ESTABILIZAR_SENSORES;
                                passo_3.setImageResource(R.drawable.passo_03);

                                flagColetarDadosSalto1 = false;
                            }
                            else
                                estadoAtualCalibracao = ESTADO_PRIMEIRO_SALTO_COLETAR_DADOS;
                        }
                        else if(menorVariacaoModAceleracao <= LIMITE_ACELERACAO_PICO_INFERIOR && maiorVariacaoModAceleracao >= LIMITE_ACELERACAO_PICO_SUPERIOR)
                        {
                            flagColetarDadosSalto1 = true;
                        }

                        break;

                    case ESTADO_SEGUNDO_SALTO_ESTABILIZAR_SENSORES:
                        gravarDadosAcelerometro(moduloVetorAceleracao, timestampInicialCalibracao);

                        if(verificarEstabilizacaoAcelerometro(timestampInicialCalibracao_Salto_2)) {
                            maiorVariacaoModAceleracao = obterMaiorVariacaoModAceleracao();
                            menorVariacaoModAceleracao = obterMenorVariacaoModAceleracao();

                            estadoAtualCalibracao = ESTADO_SEGUNDO_SALTO_COLETAR_DADOS;

                            if(!objRing.isPlaying()) /** EMITINDO ALERTA SONORO PARA COLETAR NOVOS DADOS... **/
                                objRing.play();
                        }
                        else
                            estadoAtualCalibracao = ESTADO_SEGUNDO_SALTO_ESTABILIZAR_SENSORES;

                        break;

                    case ESTADO_SEGUNDO_SALTO_COLETAR_DADOS:
                        gravarDadosAcelerometro(moduloVetorAceleracao, timestampInicialCalibracao);

                        // Detecta o menor pico de aceleracao
                        if(moduloVetorAceleracao < menorVariacaoModAceleracao)
                            menorVariacaoModAceleracao = moduloVetorAceleracao;

                        // Detecta o maior pico de aceleracao
                        if(moduloVetorAceleracao >= maiorVariacaoModAceleracao)
                            maiorVariacaoModAceleracao = moduloVetorAceleracao;

                        if(flagColetarDadosSalto2)
                        {
                            if(verificarEstabilizacaoAcelerometro(timestampInicialCalibracao_Salto_2)) {
                                Toast.makeText(getApplicationContext(), "Inf: " + Double.toString(menorVariacaoModAceleracao) + " Sup: " + Double.toString(maiorVariacaoModAceleracao), Toast.LENGTH_LONG).show();

                                timestampInicialCalibracao_Salto_3 = System.currentTimeMillis();
                                estadoAtualCalibracao = ESTADO_TERCEIRO_SALTO_ESTABILIZAR_SENSORES;
                                passo_4.setImageResource(R.drawable.passo_04);

                                flagColetarDadosSalto2 = false;
                            }
                            else
                                estadoAtualCalibracao = ESTADO_SEGUNDO_SALTO_COLETAR_DADOS;
                        }
                        else if(menorVariacaoModAceleracao <= LIMITE_ACELERACAO_PICO_INFERIOR && maiorVariacaoModAceleracao >= LIMITE_ACELERACAO_PICO_SUPERIOR)
                        {
                            flagColetarDadosSalto2 = true;
                        }

                        break;

                    case ESTADO_TERCEIRO_SALTO_ESTABILIZAR_SENSORES:
                        gravarDadosAcelerometro(moduloVetorAceleracao, timestampInicialCalibracao);

                        if(verificarEstabilizacaoAcelerometro(timestampInicialCalibracao_Salto_3)) {
                            maiorVariacaoModAceleracao = obterMaiorVariacaoModAceleracao();
                            menorVariacaoModAceleracao = obterMenorVariacaoModAceleracao();

                            estadoAtualCalibracao = ESTADO_TERCEIRO_SALTO_COLETAR_DADOS;

                            if(!objRing.isPlaying()) /** EMITINDO ALERTA SONORO PARA COLETAR NOVOS DADOS... **/
                                objRing.play();
                        }
                        else
                            estadoAtualCalibracao = ESTADO_TERCEIRO_SALTO_ESTABILIZAR_SENSORES;

                        break;

                    case ESTADO_TERCEIRO_SALTO_COLETAR_DADOS:
                        gravarDadosAcelerometro(moduloVetorAceleracao, timestampInicialCalibracao);

                        // Detecta o menor pico de aceleracao
                        if(moduloVetorAceleracao < menorVariacaoModAceleracao)
                            menorVariacaoModAceleracao = moduloVetorAceleracao;

                        // Detecta o maior pico de aceleracao
                        if(moduloVetorAceleracao >= maiorVariacaoModAceleracao)
                            maiorVariacaoModAceleracao = moduloVetorAceleracao;

                        if(flagColetarDadosSalto3)
                        {
                            if(verificarEstabilizacaoAcelerometro(timestampInicialCalibracao_Salto_3)) {
                                Toast.makeText(getApplicationContext(), "Inf: " + Double.toString(menorVariacaoModAceleracao) + " Sup: " + Double.toString(maiorVariacaoModAceleracao), Toast.LENGTH_LONG).show();

                                estadoAtualCalibracao = ESTADO_FINAL;
                                passo_5.setImageResource(R.drawable.passo_05);

                                flagColetarDadosSalto3 = false;
                            }
                            else
                                estadoAtualCalibracao = ESTADO_TERCEIRO_SALTO_COLETAR_DADOS;
                        }
                        else if(menorVariacaoModAceleracao <= LIMITE_ACELERACAO_PICO_INFERIOR && maiorVariacaoModAceleracao >= LIMITE_ACELERACAO_PICO_SUPERIOR)
                        {
                            flagColetarDadosSalto3 = true;
                        }

                        break;

                    case ESTADO_FINAL:
                        resetarVariaveisCalibracao();

                        if(!objRingFimCalibracao.isPlaying()) /** EMITINDO ALERTA SONORO PARA INDICAR O FIM DO PROCESSO DE CALIBRACAO... **/
                            objRingFimCalibracao.play();

                        Toast.makeText(getApplicationContext(), "Processo de calibração finalizado.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
            else
            {
                switch (estadoAtualCalibracao) {
                    case ESTADO_INICIAL:
                        timestampInicialCalibracao = System.currentTimeMillis();
                        timestampInicialCalibracao_Salto_1 = System.currentTimeMillis();
                        break;

                    case ESTADO_PRIMEIRO_SALTO_ESTABILIZAR_SENSORES:
                        timestampInicialCalibracao_Salto_1 = System.currentTimeMillis();
                        break;

                    case ESTADO_SEGUNDO_SALTO_ESTABILIZAR_SENSORES:
                        timestampInicialCalibracao_Salto_2 = System.currentTimeMillis();
                        break;

                    case ESTADO_TERCEIRO_SALTO_ESTABILIZAR_SENSORES:
                        timestampInicialCalibracao_Salto_3 = System.currentTimeMillis();
                        break;

                    case ESTADO_FINAL:
                        break;
                }
            }
        }
    }
}
