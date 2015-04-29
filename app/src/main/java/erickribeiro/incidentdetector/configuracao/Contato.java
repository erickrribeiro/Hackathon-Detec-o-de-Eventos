package erickribeiro.incidentdetector.configuracao;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import erickribeiro.incidentdetector.MainActivity;
import erickribeiro.incidentdetector.R;
import erickribeiro.incidentdetector.servico.IncidentHeuristicService;
import erickribeiro.incidentdetector.util.GPSTracker;

/***
 * Classe reponsável por gerir a atuação do uso da agenda telefonica no aplicativo, uma vez que essa classe
 * utiliza métodos criados na classe {@link ContatosUtil}, para criar os seus prórios métodos principais:
 * <p>enviarSmsParaContatos()<br> 
 * enviaSms(String destino)<br>
 * ligarContato(String destino)</p>
 * @author eribeiro
 *
 */
public class Contato {    
  private String id;
  private String nome;
  private List<Telefone> telefones;
  private  String telefone;
  
  private Context context;
  private String phoneNumber;
  private GPSTracker gps;
  private Handler handler;
  private SmsManager sms;
  
  public Contato(){
  }
  
  public Contato(Context context){
	  this.context = context;
  }
  
  /**
   * Método responsável por enviar sms para cada contato cadastrado na tela de configurações
   * do aplicativo. 
   */

  public void enviarSmsParaContatos(){

    int ID = 101;
  	for (int i = 2; i <= 4; i++) {  		
  		
		SharedPreferences pref = context.getSharedPreferences("prefs_do_contato", Context.MODE_PRIVATE);
		String destino = pref.getString("telefoneKey"+i, "Nda");
		
		if(!destino.equals("Nda")){
			enviaSms(destino);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            mBuilder.setContentTitle("Mensagem enviada")
                    .setContentText("Mensagem enviada para o número "+destino)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.logo))
                    .setSmallIcon(R.drawable.sms);

            Intent resultIntent = new Intent(context, MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);

            NotificationManager mNotifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotifyManager.notify(ID+i, mBuilder.build());

            Log.d(IncidentHeuristicService.TAG, "[Contato] nome: "+destino );
        }

    }

	SharedPreferences pref = context.getSharedPreferences("prefs_do_contato", Context.MODE_PRIVATE);
	String destino = pref.getString("telefoneKey"+1, "Nda");
	
	if(!destino.equals("Nda")){
		ligarContato(destino);
	}
  }
  
  /**
   * Método responsável por enviar um sms com mensagem pré-definada para um numero passado por parametro.
   * @param destino Uma String contendo o numero que receberá o sms.
   */
  public void enviaSms(String destino){
		sms = SmsManager.getDefault();
		phoneNumber = destino;

  	handler = new Handler();
  	gps = new GPSTracker(context);
  	
  	Runnable runnable = new Runnable() {
  		public void run(){
  			double latitude = gps.getLatitude();
  			double longitude = gps.getLongitude();
  	    	
  			if(latitude != 0 && longitude != 0){
  				String mensagem = "Ataque epiletico detectado. http://maps.google.com/?q=" + String.valueOf(latitude) + "," +  String.valueOf(longitude);
  				sms.sendTextMessage(phoneNumber, null, mensagem, null, null);
  			}
  			else{
  				handler.postDelayed(this, 100);
					return;
  			}
  	    	
  			Toast.makeText(context.getApplicationContext(), "EpilepsyApp - Mensagem Enviada para " + phoneNumber, Toast.LENGTH_LONG).show();
  			return;
  	    }
  	};
  	
      handler.postDelayed(runnable, 100);
	}
  
  /**
   * Método responsável por efetuar um ligar para um numero passado por parametro.
   * @param destino Uma String contendo o numero que receberá a ligação.
   */
  public void ligarContato(String destino){
  	Uri uri = Uri.parse("tel:"+ destino);
	Intent intent = new Intent(Intent.ACTION_CALL,uri);
	context.startActivity(intent);
  }

  public void setNome(String nome) {
	this.nome = nome;
  }
  public String getNome() {
	return nome;
}
  public void setTelefone(String telefone){
	this.telefone = telefone;
  }
  public String getTelefone() {
	return telefone;
  }
  public void setId(String id) {
	this.id = id;
  }
  public String getId() {
	return id;
  }
  public void setTelefones(List<Telefone> telefones) {
	this.telefones = telefones;
  }
  public List<Telefone> getTelefones() {
	return telefones;
  }
  
  
  
}