package project.wifidirectchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.start;

/**
 * Created by pratik on 4/3/17.
 */

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

        private WifiP2pManager mManager;
        private WifiP2pManager.Channel mChannel;
        private SearchPeerActivity mActivity;
        static List peers = new ArrayList();

        public WifiDirectBroadcastReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                SearchPeerActivity activity) {
            super();
            this.mManager = manager;
            this.mChannel = channel;
            this.mActivity = activity;
        }

        private WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            peers.clear();
            peers.addAll(peerList.getDeviceList());
            if(peers.size() > 0) {
                /**while(peerList.getDeviceList().iterator().hasNext()){
                 String deviceAddress = peerList.getDeviceList().iterator().next().deviceAddress;
                 }**/
                Log.d(mActivity.getApplicationInfo().name, peers.size() + peers.toString());
                mActivity.displayPeers();

            }
            //((WiFiPeerListAdapter) getListAdapter()).notifyDataSetChanged();
            if(peers.size() == 0 ){
                Log.d(mActivity.getApplicationInfo().name, " "+ peers.size());
            }
            }
        };

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                // Check to see if Wi-Fi is enabled and notify appropriate activity
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                Log.d(mActivity.getApplicationInfo().name, "WIFI_P2P_STATE_CHANGED_ACTION");
                if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Toast.makeText(context, "WI-FI p2p is enabled", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(context, "WI-FI p2p is not enabled", Toast.LENGTH_LONG).show();
                }

            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                // Call WifiP2pManager.requestPeers() to get a list of current peers
                Log.d(mActivity.getApplicationInfo().name, "WIFI_P2P_PEERS_CHANGED_ACTION");
                if(mManager != null){
                    mManager.requestPeers(mChannel, peerListListener);

                }
            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                // Respond to new connection or disconnections
                Log.d(mActivity.getApplicationInfo().name, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
            } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
                // Respond to this device's wifi state changing
                Log.d(mActivity.getApplicationInfo().name, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
            }
        }

        public static List getPeers()
        {
            return peers;
        }

}
