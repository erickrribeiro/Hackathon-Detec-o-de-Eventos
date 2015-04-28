package erickribeiro.incidentdetector.databe;

import java.util.Date;

/**
 * Created by eribeiro on 26/04/15.
 * email: erick.ribeiro.16@gmail.com
 */
public class History {
    private boolean statusIncidente;
    private Date date;
    private int icon;

    public History(int icon, boolean statusIncidente, Date date) {
        this.icon = icon;
        this.statusIncidente = statusIncidente;
        this.date = date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setStatusIncidente(boolean statusIncidente) {
        this.statusIncidente = statusIncidente;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean isStatusIncidente() {
        return statusIncidente;
    }

    public Date getDate() {
        return date;
    }

    public int getIcon() {
        return icon;
    }


}
