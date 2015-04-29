package erickribeiro.incidentdetector.servico;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;

public class IncidentHeuristic {
    private IncidentHeuristicModerado objPerfilModerado;

    /**
     * Construtor da classe
     * @param context
     * @param habilitarLogs
     */

    public IncidentHeuristic(Context context, boolean habilitarLogs) {
        if (habilitarLogs) {
            Log.d("Los", "A coleta de logs foi ativada.");
        }

        this.objPerfilModerado = new IncidentHeuristicModerado(context, habilitarLogs);
    }

    /**
     * Método responsável por realizar o monitoramento de demaios e ataques epilepticos.
     *
     * @param event
     * @return
     */

    public boolean monitorar(SensorEvent event) {
        //TODO: O MONITOR DEVERÁ RETORNAR UMA PROBABILIDADE DE ESTAR OCORRENDO UMA QUEDA E/OU DESMAIO...
        return(this.objPerfilModerado.monitorar(event));
    }
}