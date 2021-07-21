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
 *
 */

package org.apache.skywalking.apm.plugin.zeebe.client;

import io.zeebe.client.api.response.ActivatedJob;
import io.zeebe.client.impl.ZeebeObjectMapper;
import java.lang.reflect.Method;
import java.util.Map;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * restore the ContextCarrier from zeebeClient
 */
public class JobHandlerInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(final EnhancedInstance objInst,
            final Method method,
            final Object[] allArguments,
            final Class<?>[] argumentsTypes,
            final MethodInterceptResult result) throws Throwable {
        ActivatedJob job = (ActivatedJob) allArguments[1];
        final Map<String, Object> variablesAsMap = job.getVariablesAsMap();
        // recover ContextCarrier from request
        Object obj = variablesAsMap.get(CommonKeys.SW_ZEEBE_KEY);
        ContextCarrier contextCarrier = new ZeebeObjectMapper()
                .convertValue(obj, ContextCarrier.class);
        if (contextCarrier == null) {
            return;
        }
        //create a EntrySpan to connect
        final AbstractSpan entrySpan = ContextManager.createEntrySpan("JobWorker/" + job.getType(), contextCarrier);
        //        entrySpan.setComponent(ComponentsDefine.ZEEBE_JOBWORKER);
        SpanLayer.asRPCFramework(entrySpan);
    }

    @Override
    public Object afterMethod(final EnhancedInstance objInst,
            final Method method,
            final Object[] allArguments,
            final Class<?>[] argumentsTypes,
            final Object ret) throws Throwable {
        if (ContextManager.isActive()) {
            ContextManager.stopSpan();
        }
        return ret;
    }

    @Override
    public void handleMethodException(final EnhancedInstance objInst,
            final Method method,
            final Object[] allArguments,
            final Class<?>[] argumentsTypes,
            final Throwable t) {
        if (ContextManager.isActive()) {
            ContextManager.activeSpan().log(t);
        }

    }
}
