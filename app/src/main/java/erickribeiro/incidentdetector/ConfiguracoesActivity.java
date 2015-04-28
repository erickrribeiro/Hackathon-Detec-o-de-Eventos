package erickribeiro.incidentdetector;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;


public class ConfiguracoesActivity extends ActionBarActivity implements OnClickListener {

    private ImageButton imageButtonAgenda;
    private ImageButton imageButtonAlerta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracoes);

        imageButtonAgenda = (ImageButton) findViewById(R.id.img_agenda);
        imageButtonAlerta = (ImageButton) findViewById(R.id.img_alerta);

        imageButtonAgenda.setOnClickListener(this);
        imageButtonAlerta.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.img_agenda:
                intent = new Intent(getApplicationContext(), ConfiguracaoContatosActivity.class);
                startActivity(intent);
                break;

            case R.id.img_alerta:
                intent = new Intent(getApplicationContext(), TempoAlertaActivity.class);
                startActivity(intent);
                break;
        }
    }
}
