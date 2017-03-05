package project.wifidirectchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;

import static android.R.id.message;

public class SearchPeerActivity extends AppCompatActivity {
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WifiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    ListView list_available_connection;
    private MessageReceiver messageReceiver = null;

    private List<WifiP2pDevice> peers = new List<WifiP2pDevice>() {
        @Override
        public void add(int location, WifiP2pDevice object) {

        }

        @Override
        public boolean add(WifiP2pDevice object) {
            return false;
        }

        @Override
        public boolean addAll(int location, Collection<? extends WifiP2pDevice> collection) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends WifiP2pDevice> collection) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public boolean contains(Object object) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            return false;
        }

        @Override
        public WifiP2pDevice get(int location) {
            return null;
        }

        @Override
        public int indexOf(Object object) {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<WifiP2pDevice> iterator() {
            return null;
        }

        @Override
        public int lastIndexOf(Object object) {
            return 0;
        }

        @Override
        public ListIterator<WifiP2pDevice> listIterator() {
            return null;
        }

        @Override
        public ListIterator<WifiP2pDevice> listIterator(int location) {
            return null;
        }

        @Override
        public WifiP2pDevice remove(int location) {
            return null;
        }

        @Override
        public boolean remove(Object object) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            return false;
        }

        @Override
        public WifiP2pDevice set(int location, WifiP2pDevice object) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public List<WifiP2pDevice> subList(int start, int end) {
            return null;
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] array) {
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_peer);

        enableWiFi();

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, SearchPeerActivity.this);

        discoverPeers(mChannel);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        displayPeers();

        receiveMessageFromClient(this);
    }

    protected void displayPeers() {
        if (peers.size() > 0 && peers != null) {
            //displayWiFiDevice(peers);
            list_available_connection = (ListView) findViewById(R.id.list_available_connections);

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, android.R.id.text1, displayWiFiDevice(peers));
            list_available_connection.setAdapter(adapter);
            list_available_connection.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    WifiP2pConfig config = new WifiP2pConfig();
                    for (WifiP2pDevice peer : peers) {
                        if (peer.deviceName.equals(((TextView) view).getText())) {
                            config.deviceAddress = peer.deviceAddress;
                            config.wps.setup = WpsInfo.PBC;
                            connect(config, peer);
                            Intent msg = new Intent(SearchPeerActivity.this,MessageActivity.class);
                            startActivity(msg);
                        }
                    }
                }
            });
        }
        Log.d(this.getApplicationInfo().name, peers.size() + " Android device found.");
    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    public void discoverPeers(final WifiP2pManager.Channel mChannel) {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(SearchPeerActivity.this, "Searching for peers.", Toast.LENGTH_LONG).show();
                peers = mReceiver.getPeers();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(SearchPeerActivity.this, "Sorry could not find peers.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void enableWiFi() {
        WifiManager wifiManager = (WifiManager) getSystemService(this.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            WifiEnableDialog wifiDialog = new WifiEnableDialog(this);
            wifiDialog.show(this.getFragmentManager(), "Wifi Status");
        }
    }

    public ArrayList<String> displayWiFiDevice(List<WifiP2pDevice> devices) {
        ArrayList<String> deviceNames = new ArrayList<String>();
        for (WifiP2pDevice device : devices) {
            deviceNames.add(device.deviceName);
            //final EditText view = (EditText) fragmentView.findViewById(R.id.section_label);
            //view.setText(device.deviceName,TextView.BufferType.EDITABLE);
        }
        return deviceNames;
    }

    public void connect(final WifiP2pConfig config, final WifiP2pDevice device) {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(SearchPeerActivity.this, "Connected with"+device.deviceName, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }


    private class MessageReceiver extends AsyncTask {

        private Context context;

        public MessageReceiver(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Object[] params) {
            try {

                /**
                 * Create a server socket and wait for client connections. This
                 * call blocks until a connection is accepted from a client
                 */
                ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();

                /**
                 * If this code is reached, a client has connected and transferred data
                 * Save the input stream from the client as a string
                 */

                InputStream inputstream = client.getInputStream();
                String message = getMessageFromInputStream(inputstream);
                serverSocket.close();

                return message;
            } catch (IOException e) {
                Log.e("SearchPeerActivity", e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Object message) {
                if(message!="") {
                    Intent showMsg = new Intent(SearchPeerActivity.this,MessageActivity.class);

                    Bundle bundle = new Bundle();

                    //Add your data to bundle
                    bundle.putString("message",(String)message);

                    //Add the bundle to the intent
                    showMsg.putExtras(bundle);

                    startActivity(showMsg);
                }
            }

    }


    private void receiveMessageFromClient(Context context) {
        if (messageReceiver != null) {
            return;
        }
        messageReceiver = new MessageReceiver(this);
        messageReceiver.execute();
    }

    private String getMessageFromInputStream(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        String result = s.hasNext() ? s.next() : "";
        return result;
    }

}