package com.fortis.test;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.util.Collections;
import java.util.Properties;
import java.util.Scanner;

public class Consumer {
    static boolean run = true;

    public static void main(String[] args) throws InterruptedException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.20.11.115:9092");
        props.put("group.id", "xc");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("max.poll.records", "200");


        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<>(props);
        kafkaConsumer.subscribe(Collections.singletonList("test001"));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (run ) {
                    ConsumerRecords<String, String> records = kafkaConsumer.poll(2000);
                    for (ConsumerRecord<String, String> record : records) {
                        System.out.println(record.key() + "->" + record.value());
                    }
                }
            }
        });
        thread.start();

        Scanner sc = new Scanner(System.in);
        String line;
        for (;;){
            line = sc.nextLine();
            if("exit".equals(line)){
                break;
            }
        }
        run = false;
        thread.join();
        kafkaConsumer.close();
    }
}
