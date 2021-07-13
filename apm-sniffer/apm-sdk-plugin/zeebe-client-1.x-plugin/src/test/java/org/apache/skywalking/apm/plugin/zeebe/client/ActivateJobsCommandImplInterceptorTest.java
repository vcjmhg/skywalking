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

package org.apache.skywalking.apm.plugin.zeebe.client;

import junit.framework.TestCase;
import org.springframework.test.util.ReflectionTestUtils;

public class ActivateJobsCommandImplInterceptorTest extends TestCase {
    String plain = "";
    String normal = "{\"key\":\"value\"}";

    public void testCombinVariables() {
        assertEquals(invokeCombinVariables(normal, plain), normal);
        assertEquals(invokeCombinVariables(plain, normal), normal);
        assertEquals(invokeCombinVariables(normal, normal), "{\"key\":\"value\",\"key\":\"value\"}");
        assertEquals(invokeCombinVariables(plain, plain), plain);
    }

    private String invokeCombinVariables(String first, String second) {
        final String methodName = "combinVariables";
        final ActivateJobsCommandImplInterceptor activateJobsCommandImplInterceptor = new ActivateJobsCommandImplInterceptor();
        return ReflectionTestUtils.invokeMethod(activateJobsCommandImplInterceptor, methodName, new String[] {
            first,
            second
        });
    }

}