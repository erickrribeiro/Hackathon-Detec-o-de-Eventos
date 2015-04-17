package erickribeiro.incidentdetector;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import erickribeiro.incidentdetector.util.SharedPreferenceManager;

public class EscolherPerfilActivity extends ActionBarActivity implements OnClickListener {
    private ImageButton imgModerado;
    private ImageButton imgPreciso;
    private SharedPreferences prefAlerta;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_escolher_perfil);

        imgModerado = (ImageButton) findViewById(R.id.img_moderado);
        imgPreciso = (ImageButton) findViewById(R.id.img_preciso);

        imgModerado.setOnClickListener(this);
        imgPreciso.setOnClickListener(this);

        prefAlerta = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if(prefAlerta.getString(SharedPreferenceManager.CHAVE_PERFIL, SharedPreferenceManager.VALOR_PADRAO_PERFIL)
                .equals(SharedPreferenceManager.VALOR_PADRAO_PERFIL)){
            imgModerado.setBackgroundResource(R.drawable.perfil_verde);
        }else{
            imgPreciso.setBackgroundResource(R.drawable.perfil_verde);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.img_moderado:
                Toast.makeText(getApplicationContext(), "Perfil moderado selecionado.", Toast.LENGTH_LONG).show();
                imgModerado.setBackgroundResource(R.drawable.perfil_verde);
                imgPreciso.setBackgroundResource(R.drawable.user);
                prefAlerta.edit().putString(SharedPreferenceManager.CHAVE_PERFIL, "0").apply();
                break;
            case R.id.img_preciso:
                Toast.makeText(getApplicationContext(), "Perfil preciso selecionado.", Toast.LENGTH_LONG).show();
                imgModerado.setBackgroundResource(R.drawable.user);
                imgPreciso.setBackgroundResource(R.drawable.perfil_verde);
                prefAlerta.edit().putString(SharedPreferenceManager.CHAVE_PERFIL, "1").apply();
        }
    }
}
