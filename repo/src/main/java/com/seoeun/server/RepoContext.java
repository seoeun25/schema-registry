package com.seoeun.server;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RepoContext {

    private static final String SITE_XML = "repo.conf";
    private static Logger LOG = LoggerFactory.getLogger(RepoContext.class);
    private static RepoContext context;
    private Properties properties;

    private RepoContext() {
        initConfig();
    }

    public static RepoContext getContext() {
        if (context == null) {
            context = new RepoContext();
        }
        return context;
    }

    private void initConfig() {

        properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("repo-default.conf"));
        } catch (Exception e) {
            LOG.info("Fail to load repo-default.conf");
        }

        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(SITE_XML));
            for (String key: properties.stringPropertyNames()) {
                LOG.info(key + " = " + properties.get(key));
            }
        } catch (Exception e) {
            LOG.info("Can not find config file {0}, Using default-config", SITE_XML);
        }

    }

    public String getConfig(String name) {
        return properties.getProperty(name);
    }

    @VisibleForTesting
    public void setConfig(String name, String value) {
        properties.put(name, value);
    }


}
