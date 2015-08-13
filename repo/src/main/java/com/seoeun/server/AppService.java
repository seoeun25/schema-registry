package com.seoeun.server;

import com.seoeun.AvroRepoException;

public interface AppService {
    public void start() throws AvroRepoException;

    public void shutdown() throws AvroRepoException;
}
