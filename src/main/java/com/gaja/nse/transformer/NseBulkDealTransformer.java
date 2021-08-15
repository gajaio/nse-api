package com.gaja.nse.transformer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.gaja.nse.vo.BulkDeal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class NseBulkDealTransformer {
    private Resource bulkDealSpec;
    private JoltTransformer joltTransformer;

    @Autowired
    public NseBulkDealTransformer(@Value("classpath:bulkDealResponseTransform.json") Resource bulkDealSpec, JoltTransformer joltTransformer) {
        this.bulkDealSpec = bulkDealSpec;
        this.joltTransformer = joltTransformer;
    }

    public List<BulkDeal> transform(String input) throws IOException {
        List<BulkDeal> deals = joltTransformer.transform(input, bulkDealSpec.getInputStream(), new TypeReference<List<BulkDeal>>() {
        });
        return deals==null?new ArrayList<>(0):deals;
    }
}
