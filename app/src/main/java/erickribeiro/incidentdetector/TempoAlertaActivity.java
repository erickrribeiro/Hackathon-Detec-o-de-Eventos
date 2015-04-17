package erickribeiro.incidentdetector;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import erickribeiro.incidentdetector.util.SharedPreferenceManager;

public class TempoAlertaActivity extends ActionBarActivity implements View.OnClickListener {

    private ImageButton img_10, img_20, img_30, img_60;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tempo_alerta);

        img_10 = (ImageButton) findViewById(R.id.img_10);
        img_20 = (ImageButton) findViewById(R.id.img_20);
        img_30 = (ImageButton) findViewById(R.id.img_30);
        img_60 = (ImageButton) findViewById(R.id.img_60);

        img_10.setOnClickListener(this);
        img_20.setOnClickListener(this);
        img_30.setOnClickListener(this);
        img_60.setOnClickListener(this);

        SharedPreferences prefAlerta = PreferenceManager.getDefaultSharedPreferences(this);
        String alerta = prefAlerta.getString(SharedPreferenceManager.CHAVE_DURACAO, SharedPreferenceManager.VALOR_PADRAO_DURACAO);

        switch (Integer.valueOf(alerta)){
            case 10:
                img_10.setBackgroundResource(R.drawable.dez_seg_verde);
            break;
            case 20:
                img_20.setBackgroundResource(R.drawable.vinte_seg_verde);
                break;
            case 30:
                img_30.setBackgroundResource(R.drawable.trinta_seg_verde);
                break;
            case 60:
                img_60.setBackgroundResource(R.drawable.sessenta_seg_verde);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        SharedPreferences prefAlerta = PreferenceManager.getDefaultSharedPreferences(this);
        switch (view.getId()){
            case R.id.img_10:
                img_10.setBackgroundResource(R.drawable.dez_seg_verde);
                img_20.setBackgroundResource(R.drawable.vinte_seg);
                img_30.setBackgroundResource(R.drawable.trinta_seg);
                img_60.setBackgroundResource(R.drawable.sessenta_seg);

                prefAlerta.edit().putString(SharedPreferenceManager.CHAVE_DURACAO, "10").apply();
                Toast.makeText(getApplicationContext(), "10 segundos selecionado.", Toast.LENGTH_LONG).show();
                break;
            case R.id.img_20:
                img_10.setBackgroundResource(R.drawable.dez_seg);
                img_20.setBackgroundResource(R.drawable.vinte_seg_verde);
                img_30.setBackgroundResource(R.drawable.trinta_seg);
                img_60.setBackgroundResource(R.drawable.sessenta_seg);
                prefAlerta.edit().putString(SharedPreferenceManager.CHAVE_DURACAO, "20").apply();

                Toast.makeText(getApplicationContext(), "20 segundos selecionado.", Toast.LENGTH_LONG).show();
                break;

            case R.id.img_30:
                img_10.setBackgroundResource(R.drawable.dez_seg);
                img_20.setBackgroundResource(R.drawable.vinte_seg);
                img_30.setBackgroundResource(R.drawable.trinta_seg_verde);
                img_60.setBackgroundResource(R.drawable.sessenta_seg);
                prefAlerta.edit().putString(SharedPreferenceManager.CHAVE_DURACAO, "30").apply();

                Toast.makeText(getApplicationContext(), "30 segundos selecionado.", Toast.LENGTH_LONG).show();
                break;
            case R.id.img_60:
                img_10.setBackgroundResource(R.drawable.dez_seg);
                img_20.setBackgroundResource(R.drawable.vinte_seg);
                img_30.setBackgroundResource(R.drawable.trinta_seg);
                img_60.setBackgroundResource(R.drawable.sessenta_seg_verde);
                prefAlerta.edit().putString(SharedPreferenceManager.CHAVE_DURACAO, "60").apply();

                Toast.makeText(getApplicationContext(), "60 segundos selecionado.", Toast.LENGTH_LONG).show();

                break;
        }


    }
}
