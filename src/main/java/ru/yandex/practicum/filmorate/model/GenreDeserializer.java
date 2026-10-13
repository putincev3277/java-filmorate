package ru.yandex.practicum.filmorate.model;

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
        JsonNode node = p.getCodec().readTree(p);
        Set<Long> result = new HashSet<>();

        if (!node.isArray()) {
            return result;
        }

        for (JsonNode item : node) {
            Long id = null;

            if (item.isLong()) {
                id = item.asLong();
            } else if (item.has("id")) {
                JsonNode idNode = item.get("id");
                if (idNode.isLong() || idNode.isInt()) {
                    id = idNode.asLong();
                }
            }

            if (id != null) {
                result.add(id);
            }
        }
        return result;
    }
}

