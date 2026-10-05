package ru.yandex.practicum.filmorate.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;


import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GenreDeserializer extends JsonDeserializer<Set<Long>> {
    @Override
    public Set<Long> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        Set<Long> result = new HashSet<>();
        JsonNode node = p.getCodec().readTree(p);

        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isValueNode()) {
                    result.add(item.asLong());
                } else if (item.has("id")) {
                    result.add(item.get("id").asLong());
                }
            }
        }
        return result;
    }
}
