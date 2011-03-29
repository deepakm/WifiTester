package deep.wifi.WifiTester;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


public class WifiEventReceiver extends BroadcastReceiver {
      WifiTester app;
      Hashtable<Integer, String> chtable;
      
      public WifiEventReceiver(WifiTester app) {
        super();
        this.app = app;
        chtable = new Hashtable<Integer, String>();
        chtable.put(2412 ,"1");
        chtable.put(2417 ,"2");
        chtable.put(2422 ,"3") ; 
        chtable.put(2427 ,"4") ; 
        chtable.put(2432 ,"5")  ;
        chtable.put(2437 ,"6")  ;
        chtable.put(2442 ,"7")  ;
        chtable.put(2447 ,"8")  ;
        chtable.put(2452 ,"9")  ;
        chtable.put(2457 ,"10") ;
        chtable.put(2462 ,"11") ;
        chtable.put(2467 ,"12") ;
        chtable.put(2472 ,"13") ;
        chtable.put(2484 ,"14") ;
        chtable.put(4915 ,"183");
        chtable.put(4920 ,"184");
        chtable.put(4925 ,"185");
        chtable.put(4935 ,"187");
        chtable.put(4940 ,"188");
        chtable.put(4945 ,"189");
        chtable.put(4960 ,"192");
        chtable.put(4980 ,"196");
        chtable.put(5035 ,"7"  );
        chtable.put(5040 ,"8"   );
        chtable.put(5045 ,"9"   );
        chtable.put(5055 ,"11"  );
        chtable.put(5060 ,"12"  );
        chtable.put(5080 ,"16"  );
        chtable.put(5170 ,"34"  );
        chtable.put(5180 ,"36"  );
        chtable.put(5190 ,"38"  );
        chtable.put(5200 ,"40"  );
        chtable.put(5210 ,"42"  );
        chtable.put(5220 ,"44"  );
        chtable.put(5230 ,"46"  );
        chtable.put(5240 ,"48"  );
        chtable.put(5260 ,"52"  );
        chtable.put(5280 ,"56"  );
        chtable.put(5300 ,"60"  );
        chtable.put(5320 ,"64"  );
        chtable.put(5500 ,"100" );
        chtable.put(5520 ,"104" );
        chtable.put(5540 ,"108" );
        chtable.put(5560 ,"112" );
        chtable.put(5580 ,"116" );
        chtable.put(5600 ,"120" );
        chtable.put(5620 ,"124" );
        chtable.put(5640 ,"128" );
        chtable.put(5660 ,"132" );
        chtable.put(5680 ,"136" );
        chtable.put(5700 ,"140" );
        chtable.put(5745 ,"149" );
        chtable.put(5765 ,"153" );
        chtable.put(5785 ,"157" );
        chtable.put(5805 ,"161" );
        chtable.put(5825 ,"165" );
      }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        app.updateWifiDetails();
        String intentAction = intent.getAction();
        if (intentAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
        	app.addEventHistory("Done Scanning");        	
        	app.scanlist.clear();
            List<ScanResult> results = app.wifi.getScanResults();
            for (ScanResult result : results) {
                HashMap<String, String> item = new HashMap<String, String>();
                item.put("top", result.SSID);
                item.put("bottom", result.BSSID + " : "  + Integer.toString(result.level) + " : " + chtable.get(result.frequency) );
                app.scanlist.add(item);
            }
            app.lvScanAdapter.notifyDataSetChanged();
        } else if (intentAction.equals(WifiManager.RSSI_CHANGED_ACTION)) {
        	//Lets not do anything for this..
            WifiInfo info = app.wifi.getConnectionInfo();
        	app.rssi = info.getRssi();
        } else if (intentAction.equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)){
            WifiInfo info = app.wifi.getConnectionInfo();
            HashMap<String, String> item = null;
            app.rssi = info.getRssi();
            if (info.getSupplicantState() == android.net.wifi.SupplicantState.COMPLETED) {
            	if (info.getBSSID() != null) {
	                item = new HashMap<String, String>();
	                item.put("top", "Connected : " + info.getBSSID());
	                item.put("bottom", app.getDateTime() + " : " + info.getSSID() + " : "  + Integer.toString(info.getRssi()));
	        	}
            } else if (info.getSupplicantState() == android.net.wifi.SupplicantState.DISCONNECTED) {
            	item = new HashMap<String, String>();
                item.put("top", "Disconnected");
                item.put("bottom", app.getDateTime());
                
            }
            if (item != null) {
            	//Avoid duplicates..
            	boolean add_item = true;
                if (app.roamlist.size() > 0) {
                	HashMap<String, String> eitem = app.roamlist.get(app.roamlist.size()-1);
                	if (eitem.get("top").equals(item.get("top")) && eitem.get("bottom").equals(item.get("bottom"))) {
                		add_item = false;
                	}
                }
                if (add_item) {
                	app.roamlist.add(item);
                	app.lvRoamAdapter.notifyDataSetChanged();
                }
            }
            app.addEventHistory(intentAction + " : " + info.getSupplicantState().toString());
        }
        else { app.addEventHistory(intentAction);}
    }
}
