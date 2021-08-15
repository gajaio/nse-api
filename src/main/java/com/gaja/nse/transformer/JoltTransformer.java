package com.gaja.nse.transformer;

import com.bazaarvoice.jolt.Chainr;
import com.bazaarvoice.jolt.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JoltTransformer implements Transformer {
    private ObjectMapper mapper;
    private Map<String, Chainr> chainrMap = new ConcurrentHashMap<>();

    public JoltTransformer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <U extends InputStream, R> R transform(U input, U spec, TypeReference<R> typeReference) throws IOException {
        Chainr chainr = getChainr(spec, typeReference);
        return mapper.readValue(
                JsonUtils.toJsonString(chainr.transform(JsonUtils.jsonToObject(input))),
                typeReference
        );
    }

    private <U extends InputStream, R> Chainr getChainr(U spec, TypeReference<R> typeReference) {
        Chainr chainr = chainrMap.get(typeReference.getType().getTypeName());
        if (chainr == null) {
            chainr = Chainr.fromSpec(JsonUtils.jsonToList(spec));
            chainrMap.put(typeReference.getType().getTypeName(), chainr);
        }
        return chainr;
    }

    private <R> Chainr getChainr1(String spec, TypeReference<R> typeReference) {
        Chainr chainr = chainrMap.get(typeReference.getType().getTypeName());
        if (chainr == null) {
            chainr = Chainr.fromSpec(JsonUtils.jsonToList(spec));
            chainrMap.put(typeReference.getType().getTypeName(), chainr);
        }
        return chainr;
    }

    @Override
    public <R> R transform(String input, String specJson, TypeReference<R> typeReference) throws IOException {
        Chainr chainr = getChainr1(specJson, typeReference);
        return mapper.readValue(
                JsonUtils.toJsonString(chainr.transform(JsonUtils.jsonToObject(input))),
                typeReference
        );
    }

    @Override
    public <U extends InputStream, R> R transform(String input, U specJson, TypeReference<R> typeReference) throws IOException {
        return transform(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)),
                specJson,
                typeReference);
    }
}
