package com.gaja.nse.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@ConfigurationProperties(prefix = "nse")
@Setter
@NoArgsConstructor
public class NseServiceProperties {
    private String home;
    private String archive;

    public String getHome() {
        return home!=null?home:"https://www.nseindia.com/";
    }

    public String getArchive() {
        return archive!=null?archive:"https://archives.nseindia.com/";
    }
}
