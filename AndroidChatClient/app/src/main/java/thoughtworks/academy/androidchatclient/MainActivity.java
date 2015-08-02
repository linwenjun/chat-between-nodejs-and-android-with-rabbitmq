package thoughtworks.academy.androidchatclient;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {

    private static final String QUEUE_NAME = "hello";

    ListView chatListView;
    Button fetchButton;

    ArrayList<String> chatList;
    ChatViewAdapter adapter;
    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        adapter = new ChatViewAdapter();

        chatList = new ArrayList<>();
        chatList.add("abc1");


        chatListView = (ListView) findViewById(R.id.chat_list);
        fetchButton = (Button) findViewById(R.id.fetch_button);

        chatListView.setAdapter(adapter);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Bundle bundle = msg.getData();
                String chatMsg = bundle.getString("chat");
                chatList.add(chatMsg);
                adapter.notifyDataSetChanged();
                Log.i("Receive from server", chatMsg);
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

    class ChatViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return chatList.size();
        }

        @Override
        public Object getItem(int i) {
            return chatList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if(null == view) {
                view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.layout_chat, null);
            }

            TextView chatContent = (TextView)view.findViewById(R.id.chat_text);
            chatContent.setText(chatList.get(i));

            return view;
        }
    }
}
