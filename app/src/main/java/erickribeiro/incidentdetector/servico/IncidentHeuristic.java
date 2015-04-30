package erickribeiro.incidentdetector.servico;

import android.content.Context;
import android.hardware.SensorEvent;
import android.util.Log;
import android.widget.Toast;

public class IncidentHeuristic {
    private IncidentHeuristicModerado objPerfilModerado;

    private final double TAXA_ACEITACAO_PROBABILIDADE_QUEDA = 50; //PORCENTAGEM -> VALORES ENTRE: [0-100]
    private Context objContext;

    /**
     * Construtor da classe
     * @param context
     * @param habilitarLogs
     */

    public IncidentHeuristic(Context context, boolean habilitarLogs) {
        if (habilitarLogs) {
            Log.d("Los", "A coleta de logs foi ativada.");
        }

        // Inicializando o servico...
        objContext = context;

        this.objPerfilModerado = new IncidentHeuristicModerado(objContext, habilitarLogs);
    }

    /**
     * Método responsável por realizar o monitoramento de demaios e ataques epilepticos.
     *
     * @param event
     * @return
     */
    public boolean monitorar(SensorEvent event) {
        double probabilidadeQueda = this.objPerfilModerado.monitorar(event);

        if(probabilidadeQueda >= TAXA_ACEITACAO_PROBABILIDADE_QUEDA)
        {
            Toast.makeText(objContext, "IncidentDetector - PROBABILIDADE_DESMAIO(" + Double.toString(probabilidadeQueda) + ")", Toast.LENGTH_SHORT).show();
            return(true);
        }

        return(false);
    }
}