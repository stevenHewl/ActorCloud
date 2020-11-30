package io.emqx.pulsar.functions;

import io.emqx.stream.common.Constants;
import io.emqx.stream.common.IRule;
import io.emqx.stream.common.JsonParser;
import io.emqx.stream.common.Rule;
import org.apache.pulsar.client.api.schema.Field;
import org.apache.pulsar.client.api.schema.GenericRecord;
import org.apache.pulsar.functions.api.Context;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
class AbstractRuleFunction {

  protected IRule rule;

  @SuppressWarnings("SameReturnValue")
  public Void process(List<Map<String, Object>> inputs, Context context) throws Exception {
    Logger logger = context.getLogger();

    if (rule == null) {
      logger.info("init rule");
      Map<String, Object> payload = context.getUserConfigMap();
      logger.info("AbstractRuleFunction.process.payload:" + JsonParser.toJson(payload));
      rule = new Rule(payload, logger);
    }
    try {
      List<Map<String, Object>> resultMap = rule.apply(inputs); // select输出的结果
      if (resultMap != null && !resultMap.isEmpty()) {
        rule.getActions().forEach(map -> map.forEach((key, value) -> {
          if (key.toLowerCase().equals("webhook")){
            Map<String, Object> alerts = JsonParser.parseRule(JsonParser.toJson(value));
            alerts.put("tenantID", inputs.get(0).get("tenantID"));
            alerts.put("deviceID", inputs.get(0).get("deviceID"));
            value = JsonParser.toJson(alerts);
            logger.info("value new:" + value);
          } else {
            value = JsonParser.toJson(value);
          }
          String actionResult;
          actionResult = String.format("{\"action\":%s,\"values\":%s}", value, JsonParser.toJson(resultMap));
          String actionRule = "__acaction_" + key;
          context.publish(actionRule, actionResult);
          logger.info("publish from function {} for rule {} to topic {} : {}", context.getFunctionId(), key, actionRule, actionResult);
        }));
      } else {
        logger.info("publish nothing");
      }
    }catch(Exception exp){
      logger.error("exception happens for input " + inputs, exp);
    }
    return null;
  }

  protected Map<String, Object> topicMessageToMap(String topicMessage, Logger logger){
    logger.info("49topicMessageToMap.topicMessage:" + topicMessage);
    String[] messages = topicMessage.split(Constants.MESSAGE_SEPERATOR);
    String topic = messages[0];
    String input = messages[1];
    long ts = Long.parseLong(messages[2]);
    String[] infos = topic.substring(1).split("/", 5);
    if (infos.length != 5) {
      logger.error("Invalid topic: " + topic);
      return null;
    }
    String tenantID = infos[1];
    String deviceID = infos[3];
    Map<String, Object> mqttMessage = JsonParser.parseMqttMessage(input);
    if(mqttMessage == null) {
      logger.error("invalid mqttmessage: {}", input);
      return null;
    }
    mqttMessage.put(Constants.TOPIC_FIELD, topic);
    mqttMessage.put(Constants.MESSAGE_TIMESTAMP, ts);
    mqttMessage.put("tenantID", tenantID);
    mqttMessage.put("deviceID", deviceID);
    mqttMessage.forEach((key, value) -> logger.info("{\"{}\": \"{}\"}", key, value));
    return mqttMessage;
  }

  protected Map<String, Object> genericRecordToMap(GenericRecord record) {
    Map<String, Object> resultMap = new HashMap<>();
    for (Field field : record.getFields()) {
      resultMap.put(field.getName(), record.getField(field));
    }
    return resultMap;
  }
}
