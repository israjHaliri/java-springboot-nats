package com.haliri.israj.javaspringbootnats.util;

import com.haliri.israj.javaspringbootnats.App;
import io.nats.client.*;
import io.nats.streaming.AckHandler;
import io.nats.streaming.StreamingConnection;
import io.nats.streaming.StreamingConnectionFactory;
import io.nats.streaming.SubscriptionOptions;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.*;

public class NatsUtil {

    public static void pubsub() throws IOException, InterruptedException {
        Connection nc = Nats.connect("nats://localhost:4222");

        App.getLogger(NatsUtil.class).info("NATS sending message");

        Subscription sub = nc.subscribe("subject");
        Subscription sub2 = nc.subscribe("subject");

        nc.publish("subject", "hello world".getBytes(StandardCharsets.UTF_8));

        Message msg = sub.nextMessage(Duration.ofMillis(5000));
        Message msg2 = sub2.nextMessage(Duration.ofMillis(5000));

        String response = new String(msg.getData(), StandardCharsets.UTF_8);

        App.getLogger(NatsUtil.class).info("NATS response message : {}", response);
    }

    public static void request() {
        try {
            Connection nc = Nats.connect("nats://localhost:4222");
            Future<Message> replyFuture = nc.request("subject", "ini pesan yang di kirim".getBytes(StandardCharsets.UTF_8));
            Message reply = replyFuture.get(5, TimeUnit.SECONDS);

            App.getLogger(NatsUtil.class).info("Request msg {}", new String(reply.getData(), StandardCharsets.UTF_8));

            nc.close();

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void reply() {

        try {
            Connection nc = Nats.connect("nats://localhost:4222");

            Dispatcher d = nc.createDispatcher((msg) -> {
                App.getLogger(NatsUtil.class).info("Received message {} on subject {}, replying to {}",
                        new String(msg.getData(), StandardCharsets.UTF_8),
                        msg.getSubject(), msg.getReplyTo());
                nc.publish(msg.getReplyTo(), msg.getData());
            });
            d.subscribe("subject");

            Dispatcher d2 = nc.createDispatcher((msg) -> {
                App.getLogger(NatsUtil.class).info("Received message reply {} on subject {}, replying to {}",
                        new String(msg.getData(), StandardCharsets.UTF_8),
                        msg.getSubject(), msg.getReplyTo());
                nc.publish(msg.getReplyTo(), msg.getData());
            });
            d2.subscribe("subject");

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void queue() {
        try {
            Connection nc = Nats.connect("nats://localhost:4222");
            Subscription sub = nc.subscribe("subject-name", "queue-name");
            nc.flush(Duration.ofSeconds(3));

            nc.publish("subject-name", "queue msg".getBytes(StandardCharsets.UTF_8));

            for (int i = 0; i < 1; i++) {
                Message msg = sub.nextMessage(Duration.ofSeconds(2));

                App.getLogger(NatsUtil.class).info("Received message queue {} on subject {}",
                        new String(msg.getData(), StandardCharsets.UTF_8),
                        msg.getSubject());
            }

            nc.close();

        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public static void requestReply() throws IOException, InterruptedException, TimeoutException, ExecutionException {
        Connection nc = Nats.connect();
        String message = "Hello";
        //reply
        CountDownLatch latch = new CountDownLatch(1); // dispatcher runs callback in another thread

        System.out.println();
        Dispatcher d = nc.createDispatcher((msg) -> {
            System.out.printf("Received message \"%s\" on subject \"%s\", replying to %s\n",
                    new String(msg.getData(), StandardCharsets.UTF_8),
                    msg.getSubject(), msg.getReplyTo());
            nc.publish(msg.getReplyTo(), msg.getData());
            latch.countDown();
        });
        d.subscribe("subject");

        //request
        Future<Message> replyFuture = nc.request("subject", message.getBytes(StandardCharsets.UTF_8));
        Message reply = replyFuture.get(5, TimeUnit.SECONDS);

        System.out.println();
        System.out.printf("Received reply \"%s\" on subject \"%s\"\n",
                new String(reply.getData(), StandardCharsets.UTF_8),
                reply.getSubject());
        System.out.println();


        nc.closeDispatcher(d); // This isn't required, closing the connection will do it
        nc.close();
    }

    public static void stream() throws IOException, InterruptedException, TimeoutException {
        String clientId = UUID.randomUUID().toString();

        StreamingConnectionFactory cf = new StreamingConnectionFactory("test-cluster", clientId);
        Connection conn = Nats.connect("nats://localhost:4333");
        cf.setNatsConnection(conn);

        StreamingConnection sc = cf.createConnection();

        AckHandler ackHandler = new AckHandler() {
            public void onAck(String guid, Exception err) {
                if (err != null) {
                    App.getLogger(NatsUtil.class).info("Error publishing msg id {} {}", guid, err.getMessage());
                } else {
                    App.getLogger(NatsUtil.class).info("Received ack for msg id {}", guid);
                }
            }
        };

        sc.publish("stream", "israj-haliri".getBytes(), ackHandler);

        final CountDownLatch doneSignal = new CountDownLatch(1);

        io.nats.streaming.Subscription sub = sc.subscribe("stream", m -> {
            App.getLogger(NatsUtil.class).info("Received a message: {}", new String(m.getData()));
            doneSignal.countDown();
        }, new SubscriptionOptions.Builder().startWithLastReceived().build());

        doneSignal.await();

        sub.unsubscribe();

        sc.close();
    }

    public static void streams2() throws IOException, InterruptedException, TimeoutException {
        String clientId = UUID.randomUUID().toString();

        System.out.println("Starting NATS Example Streaming Client " + clientId);
        // Create NATS connection
        Connection conn = Nats.connect("nats://localhost:4333");
        // Create a Streaming Connection Factory with NATS connection
        StreamingConnectionFactory connFactory = new StreamingConnectionFactory("test-cluster", clientId);
        connFactory.setMaxPubAcksInFlight(25);
        connFactory.setNatsConnection(conn);

        // Create Streaming connection with Connection Factory
        StreamingConnection streamingConn = connFactory.createConnection();

        final CountDownLatch latch = new CountDownLatch(1);
        for (int i = 0; i < 3; i++) {
            System.out.println("PUBLISHING: "  +i);
            streamingConn.publish("stream", Integer.toString(i).getBytes());
            Thread.sleep(1_000);
        }
        streamingConn.subscribe("stream",  m -> {
            System.out.println("RECEIVED: " + new String(m.getData()));
            latch.countDown();
        }, new SubscriptionOptions.Builder().startWithLastReceived().build());

        latch.await();

        streamingConn.close();
    }
}
