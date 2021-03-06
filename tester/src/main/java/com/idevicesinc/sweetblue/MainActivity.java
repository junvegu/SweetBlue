package com.idevicesinc.sweetblue;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.idevicesinc.sweetblue.utils.Interval;
import java.util.ArrayList;
import java.util.List;
import com.idevicesinc.sweetblue.tester.R;


public class MainActivity extends AppCompatActivity
{

    BleManager mgr;
    private ListView mListView;
    private Button mStartScan;
    private Button mStopScan;
    private ScanAdaptor mAdaptor;
    private ArrayList<BleDevice> mDevices;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView);
        mDevices = new ArrayList<>(0);
        mAdaptor = new ScanAdaptor(this, mDevices);
        mListView.setAdapter(mAdaptor);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                BleDevice device = mDevices.get(position);
                device.setListener_State(new BleDevice.StateListener()
                {
                    @Override public void onEvent(StateEvent e)
                    {
                        int i = 0;
                        i++;
                    }
                });
                device.connect();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                BleDevice device = mDevices.get(position);
                if (device.is(BleDeviceState.CONNECTED))
                {
                    device.disconnect();
                    return true;
                }
                return false;
            }
        });

        mStartScan = (Button) findViewById(R.id.startScan);
        mStartScan.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                mgr.startPeriodicScan(Interval.FIVE_SECS, Interval.FIVE_SECS);
            }
        });
        mStopScan = (Button) findViewById(R.id.stopScan);
        mStopScan.setOnClickListener(new View.OnClickListener()
        {
            @Override public void onClick(View v)
            {
                mgr.stopScan();
            }
        });

        BleManagerConfig config = new BleManagerConfig();
        config.loggingEnabled = true;
        config.scanMode = BleScanMode.POST_LOLLIPOP;
        mgr = BleManager.get(this, config);
        mgr.setListener_State(new BleManager.StateListener()
        {
            @Override public void onEvent(StateEvent event)
            {
                if (event.didEnter(BleManagerState.ON))
                {
                    mStartScan.setEnabled(true);
                }
                else if (event.didEnter(BleManagerState.SCANNING))
                {
                    mStartScan.setEnabled(false);
                    mStopScan.setEnabled(true);
                }
                else if (event.didExit(BleManagerState.SCANNING))
                {
                    mStartScan.setEnabled(true);
                    mStopScan.setEnabled(false);
                }
            }
        });
        mgr.setListener_Discovery(new BleManager.DiscoveryListener()
        {
            @Override public void onEvent(BleManager.DiscoveryListener.DiscoveryEvent e)
            {
                if (e.was(BleManager.DiscoveryListener.LifeCycle.DISCOVERED))
                {
                    mDevices.add(e.device());
                    mAdaptor.notifyDataSetChanged();
                }
                else if (e.was(BleManager.DiscoveryListener.LifeCycle.REDISCOVERED))
                {

                }
            }
        });

        if (mgr.is(BleManagerState.OFF))
        {
            mStartScan.setEnabled(false);
            mgr.turnOn();
        }
        else
        {
            mStartScan.setEnabled(true);
        }
    }

    private class ScanAdaptor extends ArrayAdapter<BleDevice>
    {

        private List<BleDevice> mDevices;


        public ScanAdaptor(Context context, List<BleDevice> objects)
        {
            super(context, R.layout.scan_listitem_layout, objects);
            mDevices = objects;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent)
        {
            ViewHolder v;
            if (convertView == null)
            {
                convertView = View.inflate(getContext(), R.layout.scan_listitem_layout, null);
                v = new ViewHolder();
                v.name = (TextView) convertView.findViewById(R.id.name);
                v.rssi = (TextView) convertView.findViewById(R.id.rssi);
                convertView.setTag(v);
            }
            else
            {
                v = (ViewHolder) convertView.getTag();
            }
            v.name.setText(mDevices.get(position).toString());
            //v.rssi.setText(String.valueOf(mDevices.get(position).getRssi()));
            return convertView;
        }

    }

    private static class ViewHolder
    {
        private TextView name;
        private TextView rssi;
    }
}
