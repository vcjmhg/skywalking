/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH under
 * one or more contributor license agreements. See the NOTICE file distributed
 * with this work for additional information regarding copyright ownership.
 * Licensed under the Zeebe Community License 1.0. You may not use this file
 * except in compliance with the Zeebe Community License 1.0.
 */

package test.apache.skywalking.testcase.zeebe.client.job;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.ZeebeClientBuilder;
import io.zeebe.client.api.worker.JobWorker;
import java.time.Duration;
import lombok.Setter;

public final class JobWorkerCreator {
    @Setter
    private static boolean isStop = false;

    //主要是为了方便在url请求中调用
    public JobWorker createJobWorker(String broker, String jobType) {
        isStop = false;

        final ZeebeClientBuilder builder =
            ZeebeClient.newClientBuilder().gatewayAddress(broker).usePlaintext();

        try (final ZeebeClient client = builder.build()) {
            FirstJobHandler jobHandler = new FirstJobHandler();
            try (final JobWorker workerRegistration =
                     client
                         .newWorker()
                         .jobType(jobType)
                         .handler(jobHandler)
                         .timeout(Duration.ofSeconds(10))
                         .open()) {
                System.out.println("Job worker opened and receiving jobs.");
                while (!isStop) {
                }
                return workerRegistration;
            }
        }
    }
}
