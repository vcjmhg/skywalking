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

package org.apache.skywalking.apm.testcase.akka.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import lombok.extern.log4j.Log4j2;
import org.apache.skywalking.apm.testcase.akka.actor.HasSenderActor;
import org.apache.skywalking.apm.testcase.akka.actor.NoSenderActor;
import org.apache.skywalking.apm.testcase.akka.actor.StartActor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/case")
@Log4j2
public class CaseController {

    private static final String SUCCESS = "Success";

    @RequestMapping("/akka-2.x-scenario")
    @ResponseBody
    public String testcase() {
        testActor();
        return SUCCESS;
    }

    @Autowired
    private ActorSystem system;

    private void testActor() {
        log.info("start create actor");
        final ActorRef noSenderActor = system.actorOf(NoSenderActor.props(), "noSenderActor");
        final ActorRef hasSenderActor = system.actorOf(HasSenderActor.props(), "hasSenderActor");
        final ActorRef startActor = system.actorOf(StartActor.props(), "FirstStartActor");
        log.info("start tell message!");
        noSenderActor.tell("start", startActor);
        hasSenderActor.tell("start", startActor);
//        //stop noSenderActor
//        noSenderActor.tell("stop", startActor);
//        hasSenderActor.tell("stop", startActor);
    }

    @RequestMapping("/healthCheck")
    @ResponseBody
    public String healthCheck() {
        // your codes
        return SUCCESS;
    }

}
