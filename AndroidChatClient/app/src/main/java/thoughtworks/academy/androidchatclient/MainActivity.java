package thoughtworks.academy.androidchatclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String QUEUE_NAME = "hello";
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView) findViewById(R.id.chat_text);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                String chatMsg = bundle.getString("chat");
                textView.setText(chatMsg);
            }
        };

        new Thread(new Runnable() {

            @Override
            public void run() {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost("192.168.33.19");
                try {
                    Connection connection = factory.newConnection();
                    Channel channel = connection.createChannel();
                    channel.queueDeclare(QUEUE_NAME, false, false, false, null);

                    QueueingConsumer consumer  = new QueueingConsumer(channel);
                    channel.basicConsume(QUEUE_NAME, true, consumer);

                    while(true) {
                        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                        String message = new String(delivery.getBody());

                        Message msg = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("chat", message);
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();




    }
}
