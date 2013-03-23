package com.trnkarthik.hitthefrog2;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class DatabaseDemoActivity extends Activity {
	private static DataManager dm;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        dm = new DataManager(this);
        

        List<Note> notes = dm.getAllNotes();
        
        ListView myListView = (ListView) findViewById(R.id.listView1);        
        ArrayAdapter<Note> adapter = new ArrayAdapter<Note>(this, 
        		android.R.layout.simple_list_item_1, android.R.id.text1, notes);        
        myListView.setAdapter(adapter);
    }

	@Override
	protected void onDestroy() {
		dm.close();
		super.onDestroy();
	}
}