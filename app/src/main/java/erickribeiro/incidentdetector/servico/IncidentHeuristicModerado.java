package erickribeiro.incidentdetector.servico;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.util.Stack;

import erickribeiro.incidentdetector.util.ManagerLogs;
import erickribeiro.incidentdetector.util.SharedPreferenceManager;

public class IncidentHeuristicModerado {

    private SharedPreferences prefCalibracao;
    private final boolean MODO_DEBUG = true;

    private long timestampInicialMonitoramento;

    private boolean flagCelularPresoAoCorpo = false;
    private boolean flagGyroscopeAtivado = false;
    private final double ACELERACAO_NORMAL_GRAVIDADE = 9.8;

    private final int ESTADO_INICIAL = 0;
    private final int ESTADO_1 = 1;
    private final int ESTADO_2 = 2;
    private final int ESTADO_3 = 3;
    private final int ESTADO_4 = 4;
    private int desmaioEstadoAtual = ESTADO_INICIAL;

    long desmaioTimestampEstado1 = 0;
    long desmaioTimestampEstado2 = 0;
    long desmaioTimestampEstado3 = 0;
    long desmaioTimestampEstado4 = 0;
    long desmaioTempoValidacao = 0;

    double menorModuloAceleracao = ACELERACAO_NORMAL_GRAVIDADE; // Aceleracao normal da gravidade...
    double maiorModuloAceleracao = 0;
    double maiorModuloGiroscopio = 0;

    private double LIMITE_ACELERACAO_PICO_INFERIOR = 7;
    private double LIMITE_ACELERACAO_PICO_SUPERIOR = 15;
    private final double MARGEM_ERRO_AMPLITUDE_ACELERACAO = 10;
    private final double MARGEM_ERRO_AMOSTRAGEM_ACELERACAO_SINAL_ESTABILIZADO = 0.8;

    private int MARGEM_ERRO_TEMPO_MINIMO_ENTRE_PICOS_QUEDA = 40;
    private final int MARGEM_ERRO_TEMPO_MINIMO_VALIDACAO_QUEDA = 1000;
    private final int MARGEM_ERRO_TEMPO_TOTAL_VALIDACAO_QUEDA = 6000;
    private final int QTD_TOTAL_AMOSTRAGEM_ACELERACAO = 60;
    Stack<Double> arrayAmostragemAceleracao = new Stack<Double>();
    Stack<Double> arrayTimelineAceleracao = new Stack<Double>();

    private final int MARGEM_ERRO_CONTADOR_VARIACOES_QUEDA = 5;
    int contadorMargemErroDesmaio = 0;

    private final int ID_EIXO_X = 1;
    private final int ID_EIXO_Y = 2;
    private final int ID_EIXO_Z = 3;

    private final int ID_EIXO_X_POSITIVO =  1 * ID_EIXO_X;
    private final int ID_EIXO_X_NEGATIVO = -1 * ID_EIXO_X;
    private final int ID_EIXO_Y_POSITIVO =  1 * ID_EIXO_Y;
    private final int ID_EIXO_Y_NEGATIVO = -1 * ID_EIXO_Y;
    private final int ID_EIXO_Z_POSITIVO =  1 * ID_EIXO_Z;
    private final int ID_EIXO_Z_NEGATIVO = -1 * ID_EIXO_Z;
    private final int QTD_TOTAL_AMOSTRAGEM_EIXO_ACELERACAO = 100;

    Stack<Double> eixoNormalAceleracaoAntesX = new Stack<Double>();
    Stack<Double> eixoNormalAceleracaoAntesY = new Stack<Double>();
    Stack<Double> eixoNormalAceleracaoAntesZ = new Stack<Double>();

    Stack<Double> eixoNormalAceleracaoDepoisX = new Stack<Double>();
    Stack<Double> eixoNormalAceleracaoDepoisY = new Stack<Double>();
    Stack<Double> eixoNormalAceleracaoDepoisZ = new Stack<Double>();

    private double maxVariacaoGyroscopeEixoX = 0;
    private double maxVariacaoGyroscopeEixoY = 0;
    private double maxVariacaoGyroscopeEixoZ = 0;
    private final double LIMITE_GIROSCOPIO_PICO_SUPERIOR = 2.0;

    boolean flagHabilitarLogs;

    private Context objContext;
    private ManagerLogs managerLogs;

    // Iniciando objetos de musica do android...
    Uri objNotification;
    Ringtone objRing;

    // Construtor da classe...
    public IncidentHeuristicModerado(Context context, boolean habilitarLogs) {
        managerLogs =  new ManagerLogs();

        /********************************************************************************
         *			HEURISTICA DE DETECCAO DE DESMAIOS E QUEDAS             			*
         ********************************************************************************/
        flagHabilitarLogs = habilitarLogs;
        flagGyroscopeAtivado = false;

        // Obtendo instante inicial do log...
        timestampInicialMonitoramento = System.currentTimeMillis();

        // Inicializando variaveis do monitoramento...
        resetarVariaveisMonitoramentoDesmaio();

        // Inicializando o servico...
        objContext = context;

        /** BEGIN: Iniciando objetos de musica do android... **/
        objNotification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        objRing = RingtoneManager.getRingtone(context, objNotification);
        /** BEGIN: Iniciando objetos de musica do android... **/

        prefCalibracao = PreferenceManager.getDefaultSharedPreferences(context);
        double pref_key_menor_pico_inferior = Double.valueOf(prefCalibracao.getString(SharedPreferenceManager.CHAVE_MENOR_PICO_INFERIOR, SharedPreferenceManager.VALOR_PADRAO_MENOR_PICO_INFERIOR));
        double pref_key_maior_pico_superior = Double.valueOf(prefCalibracao.getString(SharedPreferenceManager.CHAVE_MAIOR_PICO_SUPERIOR, SharedPreferenceManager.VALOR_PADRAO_MAIOR_PICO_SUPERIOR));
        int pref_key_tempo_entre_menor_maior_pico = Integer.valueOf(prefCalibracao.getString(SharedPreferenceManager.CHAVE_TEMPO_ENTRE_MENOR_MAIOR_PICO, SharedPreferenceManager.VALOR_PADRAO_TEMPO_ENTRE_MENOR_MAIOR_PICO));

        if(pref_key_menor_pico_inferior >= LIMITE_ACELERACAO_PICO_INFERIOR || pref_key_maior_pico_superior <= LIMITE_ACELERACAO_PICO_SUPERIOR ||
                pref_key_tempo_entre_menor_maior_pico <= MARGEM_ERRO_TEMPO_MINIMO_ENTRE_PICOS_QUEDA)
        {
            Toast.makeText(objContext, "Atenção: Você deve calibrar o aplicativo para um melhor funcionamento.", Toast.LENGTH_SHORT).show();
            LIMITE_ACELERACAO_PICO_INFERIOR = pref_key_menor_pico_inferior;
            LIMITE_ACELERACAO_PICO_SUPERIOR = pref_key_maior_pico_superior;
            MARGEM_ERRO_TEMPO_MINIMO_ENTRE_PICOS_QUEDA = pref_key_tempo_entre_menor_maior_pico;
        }
    }

    /**
     * Funcao responsavel por realizar o monitoramento de demaios
     * e ataques epilepticos.
     * @param event
     *
     * QUANDO FOR FEITO O MONITORAMENTO COM PRECISAO.
     * http://www.klebermota.eti.br/2012/08/26/sensores-de-movimento-no-android-traducao-da-documentacao-oficial/
     */
    public double monitorar(SensorEvent event) {
        double x = 0;
        double y = 0;
        double z = 0;
        int typeSensor = event.sensor.getType();

        Boolean flagHabilitarMaquinaEstados = false;

        double moduloVetorAceleracao = ACELERACAO_NORMAL_GRAVIDADE;
        double moduloVetorGiroscopio = 0;
        double maiorVariacaoAceleracao = 0;
        double proximityValue = 0.0;

        // Instante de tempo que o log foi gerado...
        long timestampAtualSistema = System.currentTimeMillis();
        double timestampAtualSegundos = (timestampAtualSistema - timestampInicialMonitoramento) / 1000.0;

        switch (typeSensor) {
            case Sensor.TYPE_PROXIMITY:
                proximityValue = event.values[0];
                if(proximityValue < 1)
                    flagCelularPresoAoCorpo = true;
                else
                    flagCelularPresoAoCorpo = false;

                Toast.makeText(objContext, "IncidentDetector - Proximidade: " + Double.toString(proximityValue), Toast.LENGTH_SHORT).show();
                break;

            case Sensor.TYPE_ACCELEROMETER:
                flagHabilitarMaquinaEstados = true;

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                // Calculando os modulos resultantes dos eixos x, y e z
                moduloVetorAceleracao = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                // Atualizando array de amostragens da aceleracao...
                if(arrayAmostragemAceleracao.size() >= QTD_TOTAL_AMOSTRAGEM_ACELERACAO)
                {
                    arrayAmostragemAceleracao.pop();
                    arrayTimelineAceleracao.pop();
                }
                arrayAmostragemAceleracao.add(0, moduloVetorAceleracao);
                arrayTimelineAceleracao.add(0, timestampAtualSegundos);

                // Obtendo o menor e o maior pico de aceleracao... quando a maquina de estado for iniciada.
                if(desmaioEstadoAtual != ESTADO_INICIAL)
                {
                    // Detecta o menor pico de aceleracao
                    if(moduloVetorAceleracao < menorModuloAceleracao)
                    {
                        menorModuloAceleracao = moduloVetorAceleracao;
                    }
                    // Detecta o maior pico de aceleracao
                    if(moduloVetorAceleracao >= maiorModuloAceleracao)
                    {
                        maiorModuloAceleracao = moduloVetorAceleracao;
                        desmaioTimestampEstado2 = timestampAtualSistema; // atualiza o instante em que ocorreu o maior pico de aceleracao...
                    }

                    // Coletando as variacoes de aceleracao de cada eixo (X, Y e Z) durante a queda.
                    int qtdAmostragemEixo = eixoNormalAceleracaoDepoisX.size();
                    if(qtdAmostragemEixo >= QTD_TOTAL_AMOSTRAGEM_EIXO_ACELERACAO)
                    {
                        eixoNormalAceleracaoDepoisX.pop();
                        eixoNormalAceleracaoDepoisY.pop();
                        eixoNormalAceleracaoDepoisZ.pop();
                    }
                    eixoNormalAceleracaoDepoisX.add(0, x);
                    eixoNormalAceleracaoDepoisY.add(0, y);
                    eixoNormalAceleracaoDepoisZ.add(0, z);
                }
                else
                {
                    menorModuloAceleracao = ACELERACAO_NORMAL_GRAVIDADE; // Aceleracao normal da gravidade.
                    maiorModuloAceleracao = 0;

                    // Coletando as variacoes de aceleracao de cada eixo (X, Y e Z) antes da queda.
                    int qtdAmostragemEixo = eixoNormalAceleracaoAntesX.size();
                    if(qtdAmostragemEixo >= QTD_TOTAL_AMOSTRAGEM_EIXO_ACELERACAO)
                    {
                        eixoNormalAceleracaoAntesX.pop();
                        eixoNormalAceleracaoAntesY.pop();
                        eixoNormalAceleracaoAntesZ.pop();
                    }
                    eixoNormalAceleracaoAntesX.add(0, x);
                    eixoNormalAceleracaoAntesY.add(0, y);
                    eixoNormalAceleracaoAntesZ.add(0, z);
                }

                if(flagHabilitarLogs)
                {
                    // Gerando os Logs dos dados coletados no acelerometro...
                    managerLogs.createAccelerometerLogFile(timestampAtualSegundos, x, y, z, moduloVetorAceleracao);
                }
                break;

            case Sensor.TYPE_GYROSCOPE:
                flagGyroscopeAtivado = true;
                flagHabilitarMaquinaEstados = true;

                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                // Calculando os modulos resultantes dos eixos x, y e z
                moduloVetorGiroscopio = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));

                if(desmaioEstadoAtual != ESTADO_INICIAL)
                {
                    double moduloX = Math.abs(x);
                    double moduloY = Math.abs(y);
                    double moduloZ = Math.abs(z);

                    if(moduloX > maxVariacaoGyroscopeEixoX)
                        maxVariacaoGyroscopeEixoX = moduloX;

                    if(moduloY > maxVariacaoGyroscopeEixoY)
                        maxVariacaoGyroscopeEixoY = moduloY;

                    if(moduloZ > maxVariacaoGyroscopeEixoZ)
                        maxVariacaoGyroscopeEixoZ = moduloZ;
                }

                // Obtendo o menor e o maior pico do giroscopio... quando a maquina de estado for iniciada.
                if(desmaioEstadoAtual != ESTADO_INICIAL)
                {
                    // Detecta o maior pico do giroscopio
                    if(moduloVetorGiroscopio >= maiorModuloGiroscopio)
                        maiorModuloGiroscopio = moduloVetorGiroscopio;
                }
                else
                {
                    maiorModuloGiroscopio = 0;
                }

                if(flagHabilitarLogs)
                {
                    // Gerando os Logs dos dados coletados no giroscopio...
                    this.managerLogs.createGyroLogFile(timestampAtualSegundos, x, y, z, moduloVetorGiroscopio);
                }
                break;
        }

        /********************************************************************************
         *						HEURISTICA DE DETECCAO DE DESMAIO						*
         ********************************************************************************/
        if(flagHabilitarMaquinaEstados)
        {
            // Obtem o modulo do maior pico de variacao do acelerometro...
            maiorVariacaoAceleracao = obterMaiorVariacaoAceleracao();

            switch (desmaioEstadoAtual) {
                case ESTADO_INICIAL:
                    desmaioEstadoAtual = ESTADO_INICIAL;
                    if(moduloVetorAceleracao <= LIMITE_ACELERACAO_PICO_INFERIOR)
                    {
                        if(MODO_DEBUG)
                            Toast.makeText(objContext, "IncidentDetector - ESTADO_INICIAL -> ESTADO_1", Toast.LENGTH_SHORT).show();

                        desmaioEstadoAtual = ESTADO_1;
                        desmaioTimestampEstado1 = timestampAtualSistema;
                        menorModuloAceleracao = moduloVetorAceleracao;
                    }
                    break;

                case ESTADO_1:
                    if(moduloVetorAceleracao >= LIMITE_ACELERACAO_PICO_SUPERIOR) // Verifica se alcancou o pico superior
                    {
                        if(MODO_DEBUG)
                            Toast.makeText(objContext, "IncidentDetector - ESTADO_1 -> ESTADO_2", Toast.LENGTH_SHORT).show();

                        desmaioEstadoAtual = ESTADO_2;
                        desmaioTimestampEstado2 = timestampAtualSistema;
                        maiorModuloAceleracao = moduloVetorAceleracao;
                    }
                    else if(maiorVariacaoAceleracao <= MARGEM_ERRO_AMOSTRAGEM_ACELERACAO_SINAL_ESTABILIZADO) // Verificar se o sinal do acelerometro estabilizou... atraves de uma margem de erro em relacao a normal 9,8
                    {
                        //if(MODO_DEBUG)
                        //Toast.makeText(objContext, "IncidentDetector - ESTADO_1 -> ESTADO_INICIAL", Toast.LENGTH_SHORT).show();

                        desmaioEstadoAtual = ESTADO_INICIAL;
                        resetarVariaveisMonitoramentoDesmaio();
                        return(0);
                    }
                    break;

                case ESTADO_2:
                    // Aguarda a estabilizacao do sinal do acelerometro... atraves de uma margem de erro em relacao a normal 9,8
                    if(maiorVariacaoAceleracao <= MARGEM_ERRO_AMOSTRAGEM_ACELERACAO_SINAL_ESTABILIZADO)
                    {
                        desmaioTimestampEstado3 = timestampAtualSistema;
                        long tempoTotalEntrePicosQueda = desmaioTimestampEstado2 - desmaioTimestampEstado1;

                        // Apenas debug...
                        String msgText = " TEMPO_Q(" + Long.toString(tempoTotalEntrePicosQueda) + ")";

                        if(tempoTotalEntrePicosQueda >= MARGEM_ERRO_TEMPO_MINIMO_ENTRE_PICOS_QUEDA) // Validando tempo minimo de queda...
                        {
                            desmaioEstadoAtual = ESTADO_3;
                            if(MODO_DEBUG)
                                Toast.makeText(objContext, "IncidentDetector - ESTADO_2 -> ESTADO_3" + msgText, Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            desmaioEstadoAtual = ESTADO_INICIAL;
                            if(MODO_DEBUG)
                                Toast.makeText(objContext, "IncidentDetector - ESTADO_2 -> ESTADO_INICIAL" + msgText, Toast.LENGTH_SHORT).show();

                            resetarVariaveisMonitoramentoDesmaio();
                            return(0);
                        }
                    }
                    break;

                case ESTADO_3:
                    // Obtendo amplitude de aceleracao entre o menor pico e o maior pico...
                    double amplitudeAceleracao = maiorModuloAceleracao - menorModuloAceleracao;
                    boolean flagHabilitaEstado4 = false;

                    if(!flagGyroscopeAtivado && amplitudeAceleracao > MARGEM_ERRO_AMPLITUDE_ACELERACAO)
                        flagHabilitaEstado4 = true;
                    else if(flagGyroscopeAtivado && amplitudeAceleracao > MARGEM_ERRO_AMPLITUDE_ACELERACAO && maiorModuloGiroscopio >= LIMITE_GIROSCOPIO_PICO_SUPERIOR)
                        flagHabilitaEstado4 = true;

                    if(flagHabilitaEstado4 && flagCelularPresoAoCorpo)
                    {
                        if(MODO_DEBUG)
                        {
                            String msgText = " A(" + Double.toString(amplitudeAceleracao) + ") G("+Double.toString(maiorModuloGiroscopio)+")";
                            Toast.makeText(objContext, "IncidentDetector - ESTADO_3 -> ESTADO_4" + msgText, Toast.LENGTH_SHORT).show();
                        }

                        desmaioEstadoAtual = ESTADO_4;
                        desmaioTimestampEstado4 = timestampAtualSistema;

                        if(!objRing.isPlaying()) /** Emitindo beep... para validar um possivel queda... **/
                            objRing.play();
                    }
                    else
                    {
                        if(MODO_DEBUG)
                            Toast.makeText(objContext, "IncidentDetector - ESTADO_3 -> ESTADO_INICIAL", Toast.LENGTH_SHORT).show();

                        desmaioEstadoAtual = ESTADO_INICIAL;
                        resetarVariaveisMonitoramentoDesmaio();
                        return(0);
                    }
                    break;

                case ESTADO_4:
                    desmaioTempoValidacao = timestampAtualSistema - desmaioTimestampEstado4;
                    if(desmaioTempoValidacao > MARGEM_ERRO_TEMPO_MINIMO_VALIDACAO_QUEDA)
                    {
                        if(desmaioTempoValidacao < MARGEM_ERRO_TEMPO_TOTAL_VALIDACAO_QUEDA)
                        {
                            double variacaoVetorAceleracaoAtual = Math.abs(moduloVetorAceleracao - ACELERACAO_NORMAL_GRAVIDADE);

                            // Verificar se está havendo alguma oscilação no acelerometro... caso contrario o cara apagou mesmo... :P
                            if(variacaoVetorAceleracaoAtual > MARGEM_ERRO_AMOSTRAGEM_ACELERACAO_SINAL_ESTABILIZADO)
                                contadorMargemErroDesmaio++;

                            if(contadorMargemErroDesmaio > MARGEM_ERRO_CONTADOR_VARIACOES_QUEDA)
                            {
                                if(MODO_DEBUG)
                                    Toast.makeText(objContext, "IncidentDetector - ESTADO_4 -> ESTADO_INICIAL", Toast.LENGTH_SHORT).show();

                                desmaioEstadoAtual = ESTADO_INICIAL;
                                resetarVariaveisMonitoramentoDesmaio();
                                return(0);
                            }
                        }
                        else
                        {
                            long tempoTotalEntrePicosQueda = desmaioTimestampEstado2 - desmaioTimestampEstado1;

                            int eixoNormalAntes = obterEixoNormal(eixoNormalAceleracaoAntesX, eixoNormalAceleracaoAntesY, eixoNormalAceleracaoAntesZ);
                            int eixoNormalDepois = obterEixoNormal(eixoNormalAceleracaoDepoisX, eixoNormalAceleracaoDepoisY, eixoNormalAceleracaoDepoisZ);

                            // A condicao abaixo verifica se a pessoa estava de pé e deitou ou virou... ou seja, a pessoa não está na mesma posicao antes do impacto.
                            if(eixoNormalAntes != eixoNormalDepois)
                            {
                                if(MODO_DEBUG)
                                {
                                    String msgTexto = "ANTES(" + Integer.toString(eixoNormalAntes) + ") DEPOIS(" + Integer.toString(eixoNormalDepois) + ")";
                                    Toast.makeText(objContext, "IncidentDetector - ESTADO_4 -> " + msgTexto, Toast.LENGTH_SHORT).show();
                                }

                                if(MODO_DEBUG)
                                    Toast.makeText(objContext, "IncidentDetector - ESTADO_4 -> ESTADO_INICIAL -> DESMAIO DETECTADO(1)", Toast.LENGTH_SHORT).show();

                                desmaioEstadoAtual = ESTADO_INICIAL;
                                double probabilidadeQueda = calcularProbabilidadeQueda(menorModuloAceleracao, maiorModuloAceleracao, tempoTotalEntrePicosQueda);
                                resetarVariaveisMonitoramentoDesmaio();

                                return probabilidadeQueda; // TEM MAIORES CHANCES DE SER UM DESMAIO...
                            }

                            if(MODO_DEBUG)
                                Toast.makeText(objContext, "IncidentDetector - ESTADO_4 -> ESTADO_INICIAL -> DESMAIO DETECTADO(2)", Toast.LENGTH_SHORT).show();

                            desmaioEstadoAtual = ESTADO_INICIAL;
                            double probabilidadeQueda = calcularProbabilidadeQueda(menorModuloAceleracao, maiorModuloAceleracao, tempoTotalEntrePicosQueda);
                            resetarVariaveisMonitoramentoDesmaio();

                            return probabilidadeQueda; // TEM MENORES CHANCES DE SER UM DESMAIO...
                        }
                    }
                    else
                    {
                        contadorMargemErroDesmaio = 0;
                    }
                    break;
            }
        }
        /** FIM - HEURISTICA DE DETECCAO DE DESMAIO	**/

        return(0);
    }

    /**
     * Esta funcao tem como objetivo calcular a probabilidade de realmente estar ocorrendo um incidente com o usuario.
     *
     * @return a probabilidade do usuario ter apagado...
     */
    private double calcularProbabilidadeQueda(double menorModAceleracao, double maiorModAceleracao, long tempoTotalEntreMenorMaiorPico)
    {
        double probabilidadeFinal = 0;
        double probabilidadePicoInferior = 0;
        double probabilidadePicoSuperior = 0;
        double probabilidadeTempoEntrePicos = 0;

        double pref_key_menor_pico_inferior = Double.valueOf(prefCalibracao.getString(SharedPreferenceManager.CHAVE_MENOR_PICO_INFERIOR, SharedPreferenceManager.VALOR_PADRAO_MENOR_PICO_INFERIOR));
        double pref_key_maior_pico_superior = Double.valueOf(prefCalibracao.getString(SharedPreferenceManager.CHAVE_MAIOR_PICO_SUPERIOR, SharedPreferenceManager.VALOR_PADRAO_MAIOR_PICO_SUPERIOR));
        int pref_key_tempo_entre_menor_maior_pico = Integer.valueOf(prefCalibracao.getString(SharedPreferenceManager.CHAVE_TEMPO_ENTRE_MENOR_MAIOR_PICO, SharedPreferenceManager.VALOR_PADRAO_TEMPO_ENTRE_MENOR_MAIOR_PICO));

        if(menorModAceleracao <= pref_key_menor_pico_inferior)
            probabilidadePicoInferior = 100;
        else
            probabilidadePicoInferior = ((menorModAceleracao - pref_key_menor_pico_inferior)*100)/(LIMITE_ACELERACAO_PICO_INFERIOR - pref_key_menor_pico_inferior);

        if(maiorModAceleracao >= pref_key_maior_pico_superior)
            probabilidadePicoSuperior = 100;
        else
            probabilidadePicoSuperior = ((pref_key_maior_pico_superior - maiorModAceleracao)*100)/(pref_key_maior_pico_superior - LIMITE_ACELERACAO_PICO_SUPERIOR);

        if(tempoTotalEntreMenorMaiorPico >= pref_key_tempo_entre_menor_maior_pico)
            probabilidadeTempoEntrePicos = 100;
        else
            probabilidadeTempoEntrePicos = ((pref_key_tempo_entre_menor_maior_pico - tempoTotalEntreMenorMaiorPico)*100)/(pref_key_tempo_entre_menor_maior_pico - LIMITE_ACELERACAO_PICO_SUPERIOR);

        probabilidadeFinal = (probabilidadePicoInferior + probabilidadePicoSuperior + probabilidadeTempoEntrePicos) / 3;
        return(probabilidadeFinal);
    }

    /**
     *  reinicializa os dados do monitoramento de demaios.
     */
    private void resetarVariaveisMonitoramentoDesmaio() {
        desmaioTimestampEstado1 = 0;
        desmaioTimestampEstado2 = 0;
        desmaioTimestampEstado3 = 0;
        desmaioTimestampEstado4 = 0;
        desmaioTempoValidacao = 0;

        contadorMargemErroDesmaio = 0;

        eixoNormalAceleracaoAntesX.clear();
        eixoNormalAceleracaoAntesY.clear();
        eixoNormalAceleracaoAntesZ.clear();

        eixoNormalAceleracaoDepoisX.clear();
        eixoNormalAceleracaoDepoisY.clear();
        eixoNormalAceleracaoDepoisZ.clear();

        flagGyroscopeAtivado = false;
        maxVariacaoGyroscopeEixoX = 0;
        maxVariacaoGyroscopeEixoY = 0;
        maxVariacaoGyroscopeEixoZ = 0;

        flagCelularPresoAoCorpo = false;
    }

    /**
     *  Esta funcao retorna a porcentagem da maior variacao da aceleracao... Ex.: -0.21, 0.50, 0.01, etc
     * @return
     */
    private double obterMaiorVariacaoAceleracao() {
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
     *  A funcao retorna o eixo normal do celular, atraves de comparacoes entre os eixos X, Y e Z
     * @param eixoX
     * @param eixoY
     * @param eixoZ
     * @return
     */
    private int obterEixoNormal(Stack<Double> eixoX, Stack<Double> eixoY, Stack<Double> eixoZ) {
        double totalAceleracaoEixoX_Positivo = 0;
        double totalAceleracaoEixoX_Negativo = 0;
        double totalAceleracaoEixoY_Positivo = 0;
        double totalAceleracaoEixoY_Negativo = 0;
        double totalAceleracaoEixoZ_Positivo = 0;
        double totalAceleracaoEixoZ_Negativo = 0;

        double totalAceleracaoEixoX = 0;
        double totalAceleracaoEixoY = 0;
        double totalAceleracaoEixoZ = 0;

        for(Double valorAceleracao : eixoX) {
            if(valorAceleracao >= 0)
                totalAceleracaoEixoX_Positivo += Math.abs(valorAceleracao);
            else
                totalAceleracaoEixoX_Negativo += Math.abs(valorAceleracao);
        }
        for(Double valorAceleracao : eixoY) {
            if(valorAceleracao >= 0)
                totalAceleracaoEixoY_Positivo += Math.abs(valorAceleracao);
            else
                totalAceleracaoEixoY_Negativo += Math.abs(valorAceleracao);
        }
        for(Double valorAceleracao : eixoZ) {
            if(valorAceleracao >= 0)
                totalAceleracaoEixoZ_Positivo += Math.abs(valorAceleracao);
            else
                totalAceleracaoEixoZ_Negativo += Math.abs(valorAceleracao);
        }

        totalAceleracaoEixoX = totalAceleracaoEixoX_Positivo + totalAceleracaoEixoX_Negativo;
        totalAceleracaoEixoY = totalAceleracaoEixoY_Positivo + totalAceleracaoEixoY_Negativo;
        totalAceleracaoEixoZ = totalAceleracaoEixoZ_Positivo + totalAceleracaoEixoZ_Negativo;

        if(totalAceleracaoEixoX > totalAceleracaoEixoY)
        {
            if(totalAceleracaoEixoX > totalAceleracaoEixoZ)
            {
                if(totalAceleracaoEixoX_Positivo > totalAceleracaoEixoX_Negativo)
                    return(ID_EIXO_X_POSITIVO);
                else
                    return(ID_EIXO_X_NEGATIVO);
            }
            else
            {
                if(totalAceleracaoEixoZ_Positivo > totalAceleracaoEixoZ_Negativo)
                    return(ID_EIXO_Z_POSITIVO);
                else
                    return(ID_EIXO_Z_NEGATIVO);
            }
        }
        else
        {
            if(totalAceleracaoEixoY > totalAceleracaoEixoZ)
            {
                if(totalAceleracaoEixoY_Positivo > totalAceleracaoEixoY_Negativo)
                    return(ID_EIXO_Y_POSITIVO);
                else
                    return(ID_EIXO_Y_NEGATIVO);
            }
            else
            {
                if(totalAceleracaoEixoZ_Positivo > totalAceleracaoEixoZ_Negativo)
                    return(ID_EIXO_Z_POSITIVO);
                else
                    return(ID_EIXO_Z_NEGATIVO);
            }
        }
    }

}