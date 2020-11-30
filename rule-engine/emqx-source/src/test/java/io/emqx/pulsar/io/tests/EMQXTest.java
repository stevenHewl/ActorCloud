package io.emqx.pulsar.io.tests;

import io.emqx.pulsar.io.ActorcloudSink;
import io.emqx.pulsar.io.EMQXSource;
import org.apache.pulsar.functions.api.Record;
import org.springframework.boot.SpringApplication;
import org.testng.Assert;
import java.util.HashMap;
import java.util.Map;

public class EMQXTest {

  public static void main(String[] args) {
    SpringApplication.run(EMQXTest.class, args);

    EMQXSource source = new EMQXSource();

    Map<String, Object> config = new HashMap<>();
    config.put("brokerUrl", "tcp://192.168.10.131:1883");
    config.put("inputTopics", "$share/group1/#");
    config.put("ruleId", "__emqx_all");
    config.put("userName","19ccd51edebc100aa26056126aa025a0eef0");
    config.put("password","12345678");

    try {
      source.open(config, null);
      Record<String> message = source.read();
      while (message == null) {
        Thread.sleep(5000);
        message = source.read();
      }
      //Assert.assertEquals("new", message.getValue());

      actorcloudSink(message);

      // Disconnect the client from the server
      //source.close();
    } catch (Exception es){
      es.printStackTrace();
    }
  }

  private static void actorcloudSink(Record<String> message){
    ActorcloudSink actorcloudSink = new ActorcloudSink();

    Map<String, Object> config = new HashMap<>();
    config.put("jdbcUrl", "jdbc:postgresql://192.168.10.131:5432/actorcloud");
    config.put("userName", "actorcloud");
    config.put("password", "public");
    config.put("tableName","device_events");
    String[] strarr = {"topic","msgTime","tenantID","deviceID","data","dataType","streamID","responseResult"};
    config.put("columns", strarr);

    try {
      actorcloudSink.open(config, null);
      actorcloudSink.write(message);
    } catch (Exception es){
      es.printStackTrace();
    }
  }

}
