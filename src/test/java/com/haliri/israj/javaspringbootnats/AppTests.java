package com.haliri.israj.javaspringbootnats;

import com.haliri.israj.javaspringbootnats.util.NatsUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AppTests {

    @Test
    public void contextLoads() throws IOException, InterruptedException, TimeoutException, ExecutionException {

        System.out.println("------ Pub Sub ------");
        NatsUtil.pubsub();
        System.out.println("------ Pub Sub ------");

//        follow this tutor https://nats.io/documentation/additional_documentation/nats-queueing/ then uncomment this code bwloq
        System.out.println("------ Queue ------");
        NatsUtil.queue();
        System.out.println("------ Queue ------");

        System.out.println("------ Request Reply Separate ------");
        NatsUtil.reply();
        NatsUtil.request();
        System.out.println("------ Request Reply Separate ------");

        System.out.println("------ Request Reply ------");
        NatsUtil.requestReply();
        System.out.println("------ Request Reply ------");

        System.out.println("------ Stream 1 ------");
        NatsUtil.stream();
        System.out.println("------ Stream 1 ------");

        System.out.println("------ Stream 2 ------");
        NatsUtil.streams2();
        System.out.println("------ Stream 2 ------");
    }

}
