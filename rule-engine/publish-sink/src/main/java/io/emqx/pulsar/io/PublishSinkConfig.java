package io.emqx.pulsar.io;

import com.google.gson.Gson;
import lombok.*;
import lombok.experimental.Accessors;
import org.apache.pulsar.shade.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;


@Data
@Setter
@Getter
@EqualsAndHashCode
@ToString
@Accessors(chain = true)
public class PublishSinkConfig implements Serializable {
    private String url;
    private String username;
    private String password;

    public static PublishSinkConfig load(Map<String, Object> map) {
        //ObjectMapper mapper = new ObjectMapper();
        //return mapper.readValue(new ObjectMapper().writeValueAsString(map), PublishSinkConfig.class);
        return new Gson().fromJson(new Gson().toJson(map), PublishSinkConfig.class);
    }
}
