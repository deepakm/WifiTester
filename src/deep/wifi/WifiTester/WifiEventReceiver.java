package deep.wifi.WifiTester;

import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WifiEventReceiver extends BroadcastReceiver {
      WifiTester app;

      public WifiEventReceiver(WifiTester app) {
        super();
        this.app = app;
      }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        app.updateWifiDetails();
        String intentAction = intent.getAction();
        if (intentAction.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
        	app.addEventHistory("Done Scanning");
            app.tvScan.setText("Scan results\n\n");
            List<ScanResult> results = app.wifi.getScanResults();
            for (ScanResult result : results) {
                app.tvScan.append(result.toString() + "\n");
              }
        } else if (intentAction.equals(WifiManager.RSSI_CHANGED_ACTION)) {
        	//Lets not do anything for this..
        }
        else { app.addEventHistory(intentAction);}
    }
}
