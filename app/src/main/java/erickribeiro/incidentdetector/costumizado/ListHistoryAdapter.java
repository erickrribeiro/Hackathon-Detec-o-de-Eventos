package erickribeiro.incidentdetector.costumizado;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import erickribeiro.incidentdetector.R;
import erickribeiro.incidentdetector.databe.History;

/**
 * Created by eribeiro on 27/04/15.
 * email: erick.ribeiro.16@gmail.com
 */
public class ListHistoryAdapter extends BaseAdapter{

    private static LayoutInflater layoutInflater;
    private Context context;
    private List<History> histories;

    public ListHistoryAdapter(Context context, List<History> histories) {
        this.histories = histories;
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return this.histories.size();
    }

    @Override
    public Object getItem(int position) {
        return this.histories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.drawer_list_item, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.thumbnail);
        TextView txtTitle = (TextView) convertView.findViewById(R.id.title);
        TextView txtRating = (TextView) convertView.findViewById(R.id.rating);
        TextView txtGenre = (TextView) convertView.findViewById(R.id.genre);
        TextView txtReleaseYear = (TextView) convertView.findViewById(R.id.releaseYear);

        Log.d("STATUS", histories.get(position).isStatusIncidente()+"");
        if(histories.get(position).isStatusIncidente()){
            imgIcon.setImageResource(R.drawable.checkv);
            imgIcon.setScaleType(ImageView.ScaleType.FIT_XY);
            txtTitle.setText("Incidente detectado");
            txtRating.setText("Mensagens foram inviadas para os contatos cadastrados.");
        }else {
            imgIcon.setImageResource(R.drawable.desativado);
            imgIcon.setScaleType(ImageView.ScaleType.FIT_XY);
            txtTitle.setText("Falso positivo detectado");
            txtRating.setText("Esta notificação não enviou mensagem aos contatos.");
        }
        Log.d("DATA", histories.get(position).getDate().toString());
        String[] vetor = histories.get(position).getDate().toString().split(" ");

        txtGenre.setText(vetor[3]);
        txtReleaseYear.setText(vetor[0]+" "+vetor[1]+" "+vetor[2]+" "+vetor[5]);

        return convertView;

    }
}
