package deep.wifi.WifiTester;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class WifiTester extends Activity {

    static final String[] cmds = new String[]{"ls","iperf","ping","top"};
    ArrayList<HashMap<String, String>> scanlist = new ArrayList<HashMap<String, String>>();
    ArrayList<HashMap<String, String>> roamlist = new ArrayList<HashMap<String, String>>();

    RefreshHandler mRedrawHandler = new RefreshHandler();
	WifiManager wifi;
	TextView tvWifiDetails;
	ListView lvScan;
	ListView lvRoam;
	TextView tvEvent;
	TextView tvRun;
	ScrollView svRun;
    BroadcastReceiver receiver;
    private AsyncTask<String, String, Void> sTask = null;
    AutoCompleteTextView acRun;
    Process process;
    CheckBox cbAutoScroll;
    GraphView gvGraph;
    int rssi = 0;
    
    SimpleAdapter lvScanAdapter = null;
    SimpleAdapter lvRoamAdapter = null;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_Translucent_NoTitleBar);
        setContentView(R.layout.main);
        
        //Set wifi
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        // Register Broadcast Receiver
        if (receiver == null)
            receiver = new WifiEventReceiver(this);

        registerReceiver(receiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        registerReceiver(receiver, new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));
        registerReceiver(receiver, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
        registerReceiver(receiver, new IntentFilter(WifiManager.NETWORK_IDS_CHANGED_ACTION));
        
        tvWifiDetails = (TextView)findViewById(R.id.tvWifiDetails);
        lvScan = (ListView)findViewById(R.id.lvScan);
        lvRoam = (ListView)findViewById(R.id.lvRoam);
        tvEvent = (TextView)findViewById(R.id.tvEvent);
        tvRun = (TextView)findViewById(R.id.tvRun);
        svRun = (ScrollView) findViewById(R.id.svRun);
        cbAutoScroll = (CheckBox) findViewById(R.id.cbAutoScroll);
        gvGraph = (GraphView) findViewById(R.id.gvGraph);
        
        gvGraph.setApp(this);
        //Set up autocomplete text view
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, cmds);
        acRun = (AutoCompleteTextView) findViewById(R.id.actvCommand);
        acRun.setAdapter(adapter);
        
        //Set up scan listview
        if (lvScanAdapter == null) {
        	lvScanAdapter = new SimpleAdapter(this, scanlist, android.R.layout.two_line_list_item, new String[] {"top","bottom"}, new int[] { android.R.id.text1, android.R.id.text2 });
        }
        lvScan.setAdapter(lvScanAdapter);
        
        if (lvRoamAdapter == null) {
        	lvRoamAdapter = new SimpleAdapter(this, roamlist, android.R.layout.two_line_list_item, new String[] {"top","bottom"}, new int[] { android.R.id.text1, android.R.id.text2 });
        }
        lvRoam.setAdapter(lvRoamAdapter);
        
        //Button Presses
        ((Button) findViewById(R.id.bScan)).setOnClickListener(mScanListener);
        ((Button) findViewById(R.id.bClear)).setOnClickListener(mClearListener);
        ((Button) findViewById(R.id.bRun)).setOnClickListener(mRunListener);
        ((Button) findViewById(R.id.bStop)).setOnClickListener(mStopListener);
        
        updateWifiDetails();
        gvGraph.init();
        gvGraph.update();
        //Scan on startup
        wifi.startScan();
    }
    
    
    
    public String getDateTime() {
	    DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	    Date date = new Date();
	    return dateFormat.format(date);
    }
    
    public void updateWifiDetails(){
        WifiInfo info = wifi.getConnectionInfo();
        String sInfo = info.toString();
        rssi = info.getRssi();
        int pos = sInfo.indexOf("Supplicant");
        tvWifiDetails.setText("  " + sInfo.substring(0,pos) +"\n  " + sInfo.substring(pos));
    }
    
    public void addEventHistory(String history) {
    	//TODO: Add time 
    	tvEvent.append(history+"\n");
    }
    
    
    //Listeners
    OnClickListener mScanListener = new OnClickListener() {
        public void onClick(View v) {
            addEventHistory("Starting Scan");
            wifi.startScan();
        }
    };

    OnClickListener mClearListener = new OnClickListener() {
        public void onClick(View v) {
        	roamlist.clear();
        	lvRoamAdapter.notifyDataSetChanged();
            tvEvent.setText("");
        }
    };

    OnClickListener mRunListener = new OnClickListener() {
        public void onClick(View v) {
        	//First hide the keyboard
        	InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        	mgr.hideSoftInputFromWindow(acRun.getWindowToken(), 0);
            //Get command and execute..
        	String s = acRun.getText().toString();
            if (sTask != null) {
                sTask.cancel(true);
                sTask = null;
            }
            if (process != null) {
            	process.destroy();
            }
            tvRun.setText("Running "+ s +"\n");
            sTask = new Executer().execute(s);
        }
    };

    OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            if (sTask != null) {
            	sTask.cancel(true);
            	sTask = null;
            }
            if (process != null) {
            	process.destroy();
            	tvRun.append("Terminating...");
            }
        }
    };

    class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            gvGraph.update();
            gvGraph.invalidate();
        }

        public void sleep(long delayMillis) {
            this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };


    //Executer performs command execution and updation..
    private class Executer extends AsyncTask<String, String, Void> {
        protected Void doInBackground(String... params) {
            try {
                // Executes the command, add iperf to the path
                String[] env = { "PATH=" + System.getenv("PATH") + ":/data/data/com.magicandroidapps.iperf/bin/" };
                process = Runtime.getRuntime().exec(params[0], env);

                // Reads stdout
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()));
                String read;
                while ((read = reader.readLine()) != null) {
                    publishProgress(read);
                    if (isCancelled()){
                        publishProgress("Terminating...");
                        process.destroy();
                        process = null;
                        return (null);
                    }
                }
                reader.close();

                // Waits for the command to finish.
                int ret = process.waitFor();
                //Some error ??
                if (ret != 0) {
                    BufferedReader ereader = new BufferedReader(
                            new InputStreamReader(process.getErrorStream()));
                    String eread;
                    while ((eread = ereader.readLine()) != null) {
                        publishProgress(eread);
                        if (isCancelled()){
                            process.destroy();
                        }
                    }
                }
            } catch (IOException e) {
                publishProgress("Io exception");
            } catch (InterruptedException e) {
                publishProgress("interrupted exception");
            }

            return(null);
        }

        protected void onProgressUpdate (String... values) {
            tvRun.append(values[0]+"\n");
            if (cbAutoScroll.isChecked()) {
            	svRun.fullScroll(ScrollView.FOCUS_DOWN);
            }
        }
    };



}