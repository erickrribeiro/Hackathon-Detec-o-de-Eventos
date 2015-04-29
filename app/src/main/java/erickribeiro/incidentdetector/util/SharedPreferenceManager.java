package erickribeiro.incidentdetector.util;

import android.util.FloatMath;

import erickribeiro.incidentdetector.servico.IncidentHeuristic;

/**
 * Created by erickribeiro on 01/03/15.
 */
public interface SharedPreferenceManager {
    String CHAVE_DURACAO = "pref_key_alerta";
    String VALOR_PADRAO_DURACAO = "60";

    String CHAVE_ATIVADO = "pref_key_inicio";
    boolean VALOR_PADRAO_ATIVADO = false;

    String CHAVE_MENOR_PICO_INFERIOR = "pref_key_menor_pico_inferior";
    String VALOR_PADRAO_MENOR_PICO_INFERIOR = "5.0"; // força G

    String CHAVE_MAIOR_PICO_INFERIOR = "pref_key_maior_pico_inferior";
    String VALOR_PADRAO_MAIOR_PICO_INFERIOR = "17.0"; // força G

    String CHAVE_TEMPO_ENTRE_MENOR_MAIOR_PICO = "pref_key_tempo_entre_menor_maior_pico";
    String VALOR_PADRAO_TEMPO_ENTRE_MENOR_MAIOR_PICO = "40"; // milissegundos

    public int getDuration();
}
