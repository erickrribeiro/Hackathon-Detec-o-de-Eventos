package erickribeiro.incidentdetector;

import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import erickribeiro.incidentdetector.costumizado.ListHistoryAdapter;
import erickribeiro.incidentdetector.databe.History;
import erickribeiro.incidentdetector.databe.HistoryContract;


public class HistoryActivity extends ActionBarActivity {

    private ListView mDrawerList;
    private ListHistoryAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mDrawerList = (ListView) findViewById(R.id.listView);

        HistoryContract historyContract = new HistoryContract(getApplicationContext());

        List<History> histories= historyContract.query();

        adapter = new ListHistoryAdapter(getApplicationContext(), histories);
        mDrawerList.setAdapter(adapter);
    }
}
