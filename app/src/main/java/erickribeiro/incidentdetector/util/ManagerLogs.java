package erickribeiro.incidentdetector.util;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by eribeiro on 14/04/15.
 * email: erick.ribeiro.16@gmail.com
 */
public class ManagerLogs {
    private String pathOftheLogFile;
    private String keyLogFile;

    private final String CATACTER_SEPARADOR_ARQ_LOGS = ";";

    public ManagerLogs(){
        pathOftheLogFile = Environment.getExternalStorageDirectory().toString() + "/logs_EpilepsyApp/";
        this.keyLogFile = getKeyLogFile();
    }

    public void createAccelerometerLogFile(Double miliTimeAtual, Double x, Double y, Double z, Double moduloVetor)
    {
        try{
            File logDirectory = new File(this.pathOftheLogFile);
            if (!logDirectory.exists()) {
                logDirectory.mkdirs();
            }

            File arqLog = new File(logDirectory, "logsEpilepsyApp_Accelerometer_" + this.keyLogFile + ".txt");
            FileOutputStream escreverLog = new FileOutputStream(arqLog, true);

            escreverLog.write(Double.toString(miliTimeAtual).getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(x.toString().getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(y.toString().getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(z.toString().getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(Double.toString(moduloVetor).getBytes());
            escreverLog.write("\n".getBytes());

            escreverLog.flush();
            escreverLog.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }


    public void createGyroLogFile(double miliTimeAtual, double x, double y, double z, double moduloVetor) {
        try {
            File logDirectory = new File(this.pathOftheLogFile);
            if (!logDirectory.exists()) {
                logDirectory.mkdirs(); //mkdir() cria somente um diretório, mkdirs() cria diretórios e subdiretórios.
            }
            File arqLog = new File(logDirectory, "logsEpilepsyApp_Gyroscope_" + this.keyLogFile + ".txt");
            FileOutputStream escreverLog = new FileOutputStream(arqLog, true);

            /* Instante de tempo que o log foi gerado... */
            escreverLog.write(Double.toString(miliTimeAtual).getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(Double.toString(x).getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(Double.toString(y).getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(Double.toString(z).getBytes());
            escreverLog.write(CATACTER_SEPARADOR_ARQ_LOGS.getBytes());

            escreverLog.write(Double.toString(moduloVetor).getBytes());
            escreverLog.write("\n".getBytes());

            escreverLog.flush();
            escreverLog.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getKeyLogFile() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }
}
