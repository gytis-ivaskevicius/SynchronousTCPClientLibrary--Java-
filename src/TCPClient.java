
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/*
    Dear geek,
    please note that if you are developing android application, you need to add required permissions.
    In this case you need to add "android.permission.INTERNET". So to add it, you need:

    1. For API level 22 and below:
       Add line to "AndroidManifest.xml" file:
       <uses-permission android:name="android.permission.INTERNET" />

    2. For API level 23 and above:
       Request for permissions at runtime. https://developer.android.com/training/permissions/requesting.html

    Happy coding :)
 */
public class TCPClient {

    private OnMessageReceived mMessageListener = null;
    private OnConnect mConnectListener = null;
    private OnDisconnect mDisconnectListener = null;
    private volatile boolean mRun = false;
    private PrintWriter out;
    private BufferedReader in;

    static Socket socket;


    public void connectInSeperateThread(final String ip, final int port) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                connect(ip, port);
            }
        }).start();
    }

    public void connectInSeperateThread(final String ip, final String port) {
        connectInSeperateThread(ip, Integer.valueOf(port));
    }

    public void connect(String ip, String port) {
        connect(ip, Integer.valueOf(port));
    }

    public void connect(String ip, int port) {
        mRun = true;
        String serverMessage;
        try {
            socket = new Socket(InetAddress.getByName(ip), port);
            if (mConnectListener != null)
                mConnectListener.connected(socket, ip, port);

            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                while (mRun) {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null)
                        mMessageListener.messageReceived(serverMessage);
                    if (serverMessage == null)
                        mRun = false;
                }
            } finally {
                socket.close();
                if (mDisconnectListener != null)
                    mDisconnectListener.disconnected(ip, port);
            }

        } catch (Exception e) {
        }
    }

    public String getIpFromDns(String address) throws UnknownHostException {
        return InetAddress.getByName(address).getHostAddress();
    }

    public void send(String message) {
        if (out != null && !out.checkError()) {
            out.print(message);
            out.flush();
        }
    }

    public void sendLn(String message) {
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient() {
        mRun = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Boolean isConnected() {
        return socket != null ? socket.isConnected() : false;
    }


//----------------------------------------[Listeners]----------------------------------------//

    public void setOnMessageReceivedListener(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    public void setOnConnectListener(OnConnect listener) {
        mConnectListener = listener;
    }

    public void setOnDisconnectListener(OnDisconnect listener) {
        mDisconnectListener = listener;
    }


//----------------------------------------[Interfaces]----------------------------------------//

    public interface OnMessageReceived {
        public void messageReceived(String message);
    }

    public interface OnConnect {
        public void connected(Socket socket, String ip, int port);
    }

    public interface OnDisconnect {
        public void disconnected(String ip, int port);
    }
}
