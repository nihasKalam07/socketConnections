Its a websocket client library for Android


**API Overview**

Here's the API in a nutshell.

// Create a new QSocket instance

    QSocket qsocket = new QSocket(options, context);

//connect to socket

    qSocket.connect(new ConnectionEventListener() {
    
            @Override
            public void onConnectionStateChange(ConnectionStateChange change) {
                Log.i("WebsocketConnection", String.format("[%d] Connection state changed from [%s] to [%s]", timestamp(),
                            change.getPreviousState(), change.getCurrentState()));
            }
    
            @Override
            public void onError(String message, String code, Exception e) {
                Log.i("WebsocketConnection", String.format("[%d] An error was received with message [%s], code [%s], exception [%s]",
                            timestamp(), message, code, e));
            }
        }, ConnectionState.ALL);
    }
    
Implement the ConnectionEventListener interface to receive connection state change events: available events are CONNECTING, CONNECTED, DISCONNECTING, DISCONNECTED, ALL. 
Connection state changes will be available in onConnectionStateChange method. If there is any error when trying to connect onError metjod will be called. 

// Subscribe to a channel

QSocket uses the concept of channels as a way of subscribing to data. They are identified and subscribed to by a simple name.
As mentioned above, channel subscriptions need only be registered once per QSocket instance. 
They are preserved across disconnection and re-established with the server on reconnect. They should NOT be re-registered.

    Channel channel = = qSocket.subscribe("my-channel", new ChannelEventListener() {

            @Override
            public void onSubscriptionSucceeded(String channelName) {
                Log.i("channelSubscription", String.format("[%d] Subscription to channel [%s] succeeded", timestamp(), channelName));
            }

            @Override
            public void onEvent(String channelName, String eventName, String data) {
                Log.i("ReceivedEventData:", String.format("[%d] Received event [%s] on channel [%s] with data [%s]", timestamp(),
                        eventName, channelName, data));
            }
        });
 Here "my-channel" is the channel name is to subscribe. 
 ChannelEventListener is used to get both rotocol related events such as subscription succeeds(will be received in onSubscriptionSucceeded method) 
 and data events triggered to that subscribed channel(will be received in onEvent method).
        

// Disconnect from the service (or become disconnected my network conditions)

    qSocket.disconnect();

// Reconnect, with all channel subscriptions and event bindings automatically recreated

    qSocket.connect();
    
// The state change listener is notified when the connection has been re-established,
// the subscription to "my-channel" still exist.
More information in reference format can be found below.

**The QSocket constructor**

The standard constructor take an QSocketOptions instance and current context. To add Authorization, you can put AuthorizationToken in QSocketOptions 

    QSocketOptions options = new QSocketOptions().setAuthorizationToken("1234567890");
    QSocket qSocket = new QSocket(options, context);

If you need finer control over the endpoint then the setHost, setWsPort and setWssPort methods can be employed.

**Connecting**

In order to send and receive messages you need to connect to QSocket.

    QSocket qSocket = new QSocket(options, context);
    qSocket.connect();

**Reconnecting**

The connect method is also used to re-connect in case the connection has been lost, for example if an Android device loses reception. Note that the state of channel subscriptions will be preserved while disconnected and re-negotiated with the server once a connection is re-established.

**Disconnecting**

    qSocket.disconnect();
After disconnection the QSocket instance will release any internally allocated resources (threads and network connections)

**Example Android application using SocketManager library:**

    public class MainActivity extends AppCompatActivity {
        private QSocket qSocket;
        private final long startTime = System.currentTimeMillis();
        private Channel channel;
        private TextView dataTV;
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            dataTV = (TextView) findViewById(R.id.dataTV);
            QSocketOptions options = new QSocketOptions().setAuthorizationToken("1234567890").setEncrypted(false);
            qSocket = new QSocket(options, this);
        }
    
    
        public void connect(View view) {
            qSocket.connect(new ConnectionEventListener() {
                @Override
                public void onConnectionStateChange(ConnectionStateChange change) {
                    Log.i("WebsocketConnection", String.format("[%d] Connection state changed from [%s] to [%s]", timestamp(),
                            change.getPreviousState(), change.getCurrentState()));
                    doDisplay(String.format("[%d] Connection state changed from [%s] to [%s]", timestamp(),
                            change.getPreviousState(), change.getCurrentState()));
                }
    
                @Override
                public void onError(String message, String code, Exception e) {
                    Log.i("WebsocketConnection", String.format("[%d] An error was received with message [%s], code [%s], exception [%s]",
                            timestamp(), message, code, e));
                    doDisplay(String.format("[%d] An error was received with message [%s], code [%s], exception [%s]",
                            timestamp(), message, code, e));
                }
            }, ConnectionState.ALL);
        }
    
        public void disconnect(View view) {
            qSocket.disconnect();
        }
    
        public void subscribeChannel(View view) {
            channel = qSocket.subscribe("Channel B", new ChannelEventListener() {
                @Override
                public void onSubscriptionSucceeded(String channelName) {
                    Log.i("channelSubscription", String.format("[%d] Subscription to channel [%s] succeeded", timestamp(), channelName));
                    doDisplay(String.format("[%d] Subscription to channel [%s] succeeded", timestamp(), channelName));
                }
    
                @Override
                public void onEvent(String channelName, String eventName, String data) {
                    Log.i("ReceivedEventData:", String.format("[%d] Received event [%s] on channel [%s] with data [%s]", timestamp(),
                            eventName, channelName, data));
                    doDisplay(String.format("[%d] Received event [%s] on channel [%s] with data [%s]", timestamp(),
                            eventName, channelName, data));
                }
            });
        }
    
        public void unsubscribeChannel(View view) {
            qSocket.unsubscribe("Channel B", new ChannelUnsubscriptionEventListener() {
                @Override
                public void onUnsubscribed(String channelName) {
                    Log.i("channelUnsubscription", String.format("[%d] Unsubscription to channel [%s] succeeded", timestamp(), channelName));
                    doDisplay(String.format("[%d] Unsubscription to channel [%s] succeeded", timestamp(), channelName));
                }
            });
        }
    
        private long timestamp() {
            return System.currentTimeMillis() - startTime;
        }
    
        private void doDisplay(final String data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
    
                    dataTV.setText(data);
                }
            });
        }
    }
    
_Note;- Also you should include 'http://clojars.org/repo' in Default Library repositiry settings in Android studio to include library. This is because org.java-websocket:java-websocket:1.3.1 library that is used for websocket integration is hosted in clojars._