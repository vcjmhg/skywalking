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
 *
 */

package org.apache.skywalking.apm.testcase.zeebe.client.controller;

import io.zeebe.client.ZeebeClient;
import io.zeebe.client.ZeebeClientBuilder;
import io.zeebe.client.api.response.DeploymentEvent;
import io.zeebe.client.api.worker.JobWorker;
import javax.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.skywalking.apm.testcase.zeebe.client.workflow.WorkflowInstanceCreator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import test.apache.skywalking.testcase.zeebe.client.job.JobWorkerCreator;

@RestController
@Log4j2
public class CaseController {

    private static final String SUCCESS = "Success";
    @Value("$jobType:foo")
    final String jobType = "foo";
    @Value("${broker.host:127.0.0.1:26500}")
    private final String broker = "9.134.234.223:8080";
    @Value("$bpmn.processID:demoProcess")
    private final String bpmnProcessId = "demoProcess";

    // Deploy workflow
    @PostConstruct
    public void setUp() {
        final ZeebeClientBuilder clientBuilder =
            ZeebeClient.newClientBuilder().gatewayAddress(broker).usePlaintext();

        try (final ZeebeClient client = clientBuilder.build()) {

            final DeploymentEvent deploymentEvent =
                client.newDeployCommand().addResourceFromClasspath(bpmnProcessId).send().join();

            log.info("Deployment created with key: " + deploymentEvent.getKey());
        }
    }

    @RequestMapping("/zeebe-client-1.x-scenario")
    @ResponseBody
    public String testcase() {
        // your codes
        return SUCCESS;
    }

    @RequestMapping("/healthCheck")
    @ResponseBody
    public String healthCheck() {
        // your codes
        return SUCCESS;
    }

    @RequestMapping("/startJobWorker")
    public String startJobWorker() {
        JobWorker worker = new JobWorkerCreator().createJobWorker(broker, jobType);
        return worker.toString();
    }

    @RequestMapping("/createWorkflowInstance")
    public String createWorkflowInstance() {

        return new WorkflowInstanceCreator().createWorkflowInstance(broker, bpmnProcessId).toString();
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }
}
