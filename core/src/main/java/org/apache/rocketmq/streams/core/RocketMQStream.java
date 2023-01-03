package org.apache.rocketmq.streams.core;
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.rocketmq.streams.core.metadata.StreamConfig;
import org.apache.rocketmq.streams.core.running.WorkerThread;
import org.apache.rocketmq.streams.core.topology.TopologyBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class RocketMQStream {
    private static final Logger logger = LoggerFactory.getLogger(RocketMQStream.class.getName());
    private final TopologyBuilder topologyBuilder;
    private final Properties properties;
    private List<WorkerThread> workerThreads = new ArrayList<>();

    public RocketMQStream(TopologyBuilder topologyBuilder, Properties properties) {
        this.topologyBuilder = topologyBuilder;
        this.properties = properties;
    }


    public void start() {
        if (workerThreads.size() != 0) {
            return;
        }

        //启动线程
        try {
            int threadNum = StreamConfig.STREAMS_PARALLEL_THREAD_NUM;
            for (int i = 0; i < threadNum; i++) {
                WorkerThread thread = new WorkerThread(topologyBuilder, this.properties);
                thread.start();
                workerThreads.add(thread);
            }
        } catch (Throwable t) {
            logger.error("start RocketMQStream error.");
            throw new RuntimeException(t);
        }
    }

    public void stop() {
        for (WorkerThread thread : workerThreads) {
            thread.shutdown();
        }
        workerThreads.clear();
    }
}