package com.fortis.test;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;
import java.util.Scanner;

public class Produce {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "172.20.11.115:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);

        Scanner sc = new Scanner(System.in);
        boolean run = true;
        while (run) {
            String line = sc.nextLine().trim();
            if ("exit".equals(line)) {
                run = false;
                continue;
            }
            int off = line.indexOf(" ");
            String key, value;
            if (off != -1) {
                key = line.substring(0, off);
                value = line.substring(off + 1);
            } else {
                key = line;
                value = "";
            }
            if("0".equals(key)){
                producer.send(new ProducerRecord<>("doctor-state", key, value));
                System.out.println("doctor-state        "+"->"+value);
            }else if("1".equals(key)){
                producer.send(new ProducerRecord<>("doctor-change-notice", key, value));
                System.out.println("doctor-change-notice"+"->"+value);
            }
        }

        producer.close();
    }
}
