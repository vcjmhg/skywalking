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
public class ActivateJobsCommandImplInterceptor implements InstanceMethodsAroundInterceptor {

    @Override
    public void beforeMethod(final EnhancedInstance objInst,
                             final Method method,
                             final Object[] allArguments,
                             final Class<?>[] argumentsTypes,
                             final MethodInterceptResult result) throws Throwable {
        //set request header
        GatewayOuterClass.CreateWorkflowInstanceRequest request = (GatewayOuterClass.CreateWorkflowInstanceRequest) allArguments[0];
        if (request == null) {
            return;
        }
        ContextCarrier carrier = new ContextCarrier();
        //creat LocalSpan
        AbstractSpan span = ContextManager.createExitSpan(request.getBpmnProcessId(), carrier, "/zeebe/no/peer");
        //        span.setComponent(ComponentsDefine.ZEEBE_CLIENT);
        SpanLayer.asRPCFramework(span);
        span.setOperationName("Zeebe/" + request.getBpmnProcessId());
        span.start();
        ContextManager.stopSpan();
        ContextManager.inject(carrier);
        //serialize ContextCarrier
        final String variablesString = new ZeebeObjectMapper()
            .toJson(
                new HashMap<String, ContextCarrier>() {{
                    put("__skywaling_zeebe_header__", carrier);
                }});
        //change request header
        final String originVariables = request.getVariables();
        String combinedVariables = combinVariables(variablesString, originVariables);
        allArguments[0] = request.toBuilder().setVariables(combinedVariables).build();
    }

    private String combinVariables(final String carrierString, final String originVariables) {
        StringBuilder sb = new StringBuilder();
        // in case originVariables is null
        if ("".equals(originVariables)) {
            sb.append(carrierString);
        } else if ("".equals(carrierString)) {
            sb.append(originVariables);
        } else {
            sb.append(originVariables, 0, originVariables.length() - 1)
              .append(",")
              .append(carrierString.substring(1));
        }
        return sb.toString();
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
