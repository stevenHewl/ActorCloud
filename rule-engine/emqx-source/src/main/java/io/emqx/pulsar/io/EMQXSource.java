package io.emqx.pulsar.io;

import io.emqx.stream.common.Constants;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.functions.api.Record;
import io.emqx.pulsar.io.*;

import java.util.Optional;

// 用这个Source替换pulsar默认的source，接入消息，处理
@Slf4j
public class EMQXSource extends AbstractEMQXSource<String> {

  @Override
  protected void doConsume(String topic, String message, long time) {
    String value = String.format("%s%s%s%s%d", topic,
            Constants.MESSAGE_SEPERATOR, message, Constants.MESSAGE_SEPERATOR, time);
    log.info("EMQXSource received message: {} ", value);
    consume(new StringRecord(Optional.of(randomString()), value));
  }

  @Data
  static private class StringRecord implements Record<String> {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<String> key;
    private final String value;
  }
}