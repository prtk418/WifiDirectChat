package project.wifidirectchat;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import static android.R.id.message;

public class MessageActivity extends AppCompatActivity {
    private MessageSender messageSender = null;
    private boolean sent = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Button sendBtn = (Button)findViewById(R.id.sendBtn);
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

    }

    private class MessageSender extends AsyncTask<String,Void,Void>{
        Context context = MessageActivity.this;
        String host;
        int port = 8888;
        int len;
        Socket socket = new Socket();
        byte buf[]  = new byte[1024];
        String host = intent.getStringExtra(group_owner_address);

        @Override
        protected Void doInBackground(String... message) {
            try {
                /**
                 * Create a client socket with the host,
                 * port, and timeout information.
                 */
                socket.bind(null);
                socket.connect((new InetSocketAddress(port)), 500);

                /**
                 * Create a byte stream from a JPEG file and pipe it to the output stream
                 * of the socket. This data will be retrieved by the server device.
                 */
                OutputStream os = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                BufferedWriter bw = new BufferedWriter(osw);
                bw.write(message[0]);
                bw.flush();
            } catch (FileNotFoundException e) {
                //catch logic
            } catch (IOException e) {
                //catch logic
            }

            /**
             * Clean up any open sockets when done
             * transferring or if an exception occurred.
             */
            finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            //catch logic
                        }
                    }
                }
            }
            return null;

        }
    }

    private void sendMessage() {
        if(messageSender!=null)
            return;
        String msg = getMsg();
        messageSender = new MessageSender();
        messageSender.execute(msg);
    }

    private String getMsg() {
        EditText msgText = (EditText) findViewById (R.id.messageText);
        String msg = new String();
        msg = msgText.getText().toString();

        if(sent) {
            TextView show = (TextView)findViewById(R.id.showMessageSent);
            show.setText(msg);
        }else{
            TextView show = (TextView)findViewById(R.id.showMessageReceived);
            Bundle bundle = new Bundle();
            show.setText(bundle.getString("message"));
        }
        return msg;
    }

}
