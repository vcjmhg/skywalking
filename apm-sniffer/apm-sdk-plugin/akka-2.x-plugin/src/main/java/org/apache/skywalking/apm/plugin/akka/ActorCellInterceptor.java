/*
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

package org.apache.skywalking.apm.plugin.akka;

import akka.actor.ActorRef;
import akka.dispatch.Envelope;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.logging.api.ILog;
import org.apache.skywalking.apm.agent.core.logging.api.LogManager;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

import java.lang.reflect.Method;

public class ActorCellInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, MethodInterceptResult result) throws Throwable {
        if (allArguments[0] == null) {
            //illegal args,can't trace ignore
            return;
        }
        Envelope messageHandler = (Envelope) allArguments[0];
        if (!(messageHandler.message() instanceof MessageHelper)) {
            return;
        }
        final ILog log = LogManager.getLogger(ActorCellInterceptor.class);
        MessageHelper messageHelper = (MessageHelper) messageHandler.message();
        // reset message
        allArguments[0] = messageHelper.getMessage();
        ActorRef sender = messageHandler.sender();
        log.info("Receive message===>" + allArguments[0].toString() + " from===> " + sender.toString());
        ContextCarrier contextCarrier = new ContextCarrier();
        CarrierItem next = contextCarrier.items();
        while (next.hasNext()) {
            next = next.next();
            next.setHeadValue(messageHelper.getTraceMessage().get(next.getHeadKey()));
        }
        AbstractSpan span = ContextManager.createEntrySpan(sender.toString(), contextCarrier);
        //set component
//        span.setComponent(ComponentsDefine.AKKA);
        //set URL
        Tags.URL.set(span, sender.toString());
        SpanLayer.asRPCFramework(span);

//        span.prepareForAsync();
        //just for test
        span.tag("akka-message", messageHelper.getMessage().toString());
        ContextManager.stopSpan();
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Object ret) throws Throwable {
//        ((AbstractSpan) objInst.getSkyWalkingDynamicField()).asyncFinish();
        return null;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes, Throwable t) {

    }
}
