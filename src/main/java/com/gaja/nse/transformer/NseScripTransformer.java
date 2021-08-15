package com.gaja.nse.transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gaja.nse.utils.NseConstants;
import com.gaja.nse.vo.Scrip;
import com.gaja.nse.vo.ScripData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NseScripTransformer {

    private JoltTransformer joltTransformer;

    public NseScripTransformer(
            JoltTransformer joltTransformer) {
        this.joltTransformer = joltTransformer;
    }

    public <T> T transform(String input, Resource resource, TypeReference<T> typeReference) throws IOException {
        String specString = new BufferedReader(new InputStreamReader(resource.getInputStream())).lines().collect(Collectors.joining("\n"));
        return joltTransformer.transform(input, specString, typeReference);
    }
}
