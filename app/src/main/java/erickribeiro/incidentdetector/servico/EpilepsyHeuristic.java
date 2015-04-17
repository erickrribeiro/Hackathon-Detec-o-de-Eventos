package erickribeiro.incidentdetector.servico;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

public class EpilepsyHeuristic {

    public static int PERFIL_MODERADO = 0;
    public static int PERFIL_PRECISAO = 1;

    private int perfil;
    private EpilepsyHeuristicModerado objPerfilModerado;
    private EpilepsyHeuristicPrecisao objPerfilPrecisao;

    /**
     * Construtor da classe
     * @param context
     * @param perfilMonitoramento
     * @param habilitarLogs
     */

    public EpilepsyHeuristic(Context context, int perfilMonitoramento, boolean habilitarLogs) {
        if (habilitarLogs) {
            Log.d("Los", "A coleta de logs foi ativada.");
        }

        this.perfil = perfilMonitoramento;
        this.objPerfilModerado = new EpilepsyHeuristicModerado(context, habilitarLogs);
        this.objPerfilPrecisao = new EpilepsyHeuristicPrecisao(context, habilitarLogs);
    }

    /**
     * Método responsável por realizar o monitoramento de demaios e ataques epilepticos.
     *
     * @param event
     * @return
     */

    public boolean monitorar(SensorEvent event) {
        if(this.perfil == PERFIL_MODERADO) {
            return(this.objPerfilModerado.monitorar(event));


        }
        else if(this.perfil == PERFIL_PRECISAO) {
            return(this.objPerfilPrecisao.monitorar(event));
        }
        else {
            return false;
        }
    }
}