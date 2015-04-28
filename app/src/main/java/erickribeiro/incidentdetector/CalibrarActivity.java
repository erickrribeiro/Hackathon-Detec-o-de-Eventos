package erickribeiro.incidentdetector;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import erickribeiro.incidentdetector.util.SharedPreferenceManager;

public class CalibrarActivity extends ActionBarActivity implements OnClickListener {
    private SharedPreferences prefAlerta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrar);

        prefAlerta = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(prefAlerta.getString(SharedPreferenceManager.CHAVE_PERFIL, SharedPreferenceManager.VALOR_PADRAO_PERFIL)
                .equals(SharedPreferenceManager.VALOR_PADRAO_PERFIL)){
        }else{

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_iniciar_calibracao:
                Toast.makeText(getApplicationContext(), "Perfil moderado selecionado.", Toast.LENGTH_LONG).show();
                prefAlerta.edit().putString(SharedPreferenceManager.CHAVE_PERFIL, "0").apply();
                break;
        }
    }
}
