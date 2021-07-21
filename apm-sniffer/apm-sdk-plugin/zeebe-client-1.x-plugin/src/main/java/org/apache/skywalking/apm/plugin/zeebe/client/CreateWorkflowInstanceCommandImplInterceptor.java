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

import io.zeebe.client.impl.ZeebeObjectMapper;
import io.zeebe.gateway.protocol.GatewayOuterClass;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;

/**
 * get an instance of ContextCarrier and add it to the request header for cross-thread
 */
public class CreateWorkflowInstanceCommandImplInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(final EnhancedInstance objInst,
            final Method method,
            final Object[] allArguments,
            final Class<?>[] argumentsTypes,
            final MethodInterceptResult result) throws Throwable {
        //set request header
        GatewayOuterClass.CreateWorkflowInstanceRequest request =
                (GatewayOuterClass.CreateWorkflowInstanceRequest) allArguments[0];
        if (request == null) {
            return;
        }
        ContextCarrier carrier = new ContextCarrier();
        //creat LocalSpan
        AbstractSpan span = ContextManager.createExitSpan(request.getBpmnProcessId(), carrier, "/createInstance");
        //        span.setComponent(ComponentsDefine.ZEEBE_CLIENT);
        SpanLayer.asRPCFramework(span);
        span.setOperationName("Zeebe/" + request.getBpmnProcessId());
        span.start();
        ContextManager.stopSpan();
        ContextManager.inject(carrier);
        //change request header
        final ZeebeObjectMapper mapper = new ZeebeObjectMapper();
        final String originVariables = request.getVariables();
        Map<String, Object> variablesMap = new HashMap<>();
        if (!originVariables.isEmpty()) {
            variablesMap = mapper.fromJsonAsMap(originVariables);
        }

        variablesMap.put(CommonKeys.SW_ZEEBE_KEY, carrier);
        String combinedVariables = mapper.toJson(variablesMap);
        allArguments[0] = request.toBuilder().setVariables(combinedVariables).build();
    }

    @Override
    public Object afterMethod(final EnhancedInstance objInst,
            final Method method,
            final Object[] allArguments,
            final Class<?>[] argumentsTypes,
            final Object ret) throws Throwable {
        //if first argument is null, can't resolve
        if (allArguments[0] == null) {
            return ret;
        }
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
            AbstractSpan span = ContextManager.activeSpan();
            span.log(t);
        }
    }
}
