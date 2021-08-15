package com.gaja.nse.transformer;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.InputStream;

public interface Transformer {
    <U extends InputStream, R> R transform(U input, U spec, TypeReference<R> typeReference) throws IOException;
    <R> R transform(String input, String specJson, TypeReference<R> typeReference) throws IOException;
    <U extends InputStream, R> R transform(String input, U specJson, TypeReference<R> typeReference) throws IOException;
}
