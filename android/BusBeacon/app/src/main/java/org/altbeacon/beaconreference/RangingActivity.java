package org.altbeacon.beaconreference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.speech.tts.TextToSpeech;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.w3c.dom.Text;

public class RangingActivity extends Activity implements BeaconConsumer {
    protected static final String TAG = "RangingActivity";
    private BeaconManager beaconManager = BeaconManager.getInstanceForApplication(this);
    private ListView list;
    ArrayList<Beacon> beacons_l;
    Adapter adapter;
    private TextToSpeech busTTS;
    Integer ticks;
    //private TextView app_title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        verifyBluetooth();
        list = (ListView)findViewById(R.id.listView);
        beaconManager.bind(this);
        beaconManager.debug = true;
        beacons_l = new ArrayList<Beacon>();
        adapter = new Adapter(RangingActivity.this, beacons_l);
        busTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

            }
        });
        ticks = 0;
        //app_title = (TextView)findViewById(R.id.app_medium_alert);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
    @Override
    protected void onPause() {
    	super.onPause();
    	if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(true);
    }
    @Override
    protected void onResume() {
    	super.onResume();
    	if (beaconManager.isBound(this)) beaconManager.setBackgroundMode(false);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beacons_l.clear();
                    beacons_l.addAll(beacons);

                    //app_title.setText("Hay buses!");
                    ticks ++;
                    logToDisplay(beacons_l);
                    speech(beacons_l);

                }// else {
                  //  noBuses();
                //}
            }
        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {   }
    }

    private void speech(final ArrayList<Beacon> beacons) {
        Integer current = ticks%beacons.size();
        if(ticks%5 == 0) {
            Beacon current_beacon = nearBeacon(beacons);
            String report = "Recorrido " + current_beacon.getId3() + ", a " + String.format("%.1f", current_beacon.getDistance()) + "metros.";
            busTTS.speak(report, TextToSpeech.QUEUE_ADD, null);
        }
    }

    private void logToDisplay(final ArrayList<Beacon> l) {
    	runOnUiThread(new Runnable() {
    	    public void run() {
                adapter = new Adapter(RangingActivity.this,l);
                list.setAdapter(adapter);
    	    }
    	});
    }

    private Beacon nearBeacon (final ArrayList<Beacon> beacons){
        Beacon beacon_max = beacons.get(0);
        double max = 0;
        for(int i=0; i==beacons.size(); i++){
            beacon_max = beacons.get(i);
            double Distance = beacon_max.getDistance();
            if(Distance > max) {
                max = Distance;
                beacon_max = beacons.get(i);
            }
        }
        return beacon_max;
    }

    private void verifyBluetooth() {

        try {
            if (!BeaconManager.getInstanceForApplication(this).checkAvailability()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Bluetooth not enabled");
                builder.setMessage("Please enable bluetooth in settings and restart this application.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        finish();
                        System.exit(0);
                    }
                });
                builder.show();
            }
        }
        catch (RuntimeException e) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Bluetooth LE not available");
            builder.setMessage("Sorry, this device does not support Bluetooth LE.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    finish();
                    System.exit(0);
                }

            });
            builder.show();

        }

    }
    /*
    private void noBuses(){
        app_title.setText("No Buses");
    }
    */

}
