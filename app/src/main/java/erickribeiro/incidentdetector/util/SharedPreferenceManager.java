package erickribeiro.incidentdetector.util;

import erickribeiro.incidentdetector.servico.EpilepsyHeuristic;

/**
 * Created by erickribeiro on 01/03/15.
 */
public interface SharedPreferenceManager {
    String CHAVE_DURACAO = "pref_key_alerta";
    String VALOR_PADRAO_DURACAO = "60";

    String CHAVE_PERFIL = "pref_key_perfis";
    String VALOR_PADRAO_PERFIL = String.valueOf(EpilepsyHeuristic.PERFIL_MODERADO);

    String CHAVE_ATIVADO = "pref_key_inicio";
    boolean VALOR_PADRAO_ATIVADO = false;

    public int getDuration();
}
