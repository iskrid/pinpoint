/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.navercorp.pinpoint.plugin.redis.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Group;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.redis.CommandContext;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;

/**
 * RedisConnection(nBase-ARC client) constructor interceptor - trace endPoint
 * 
 * @author jaehong.kim
 *
 */
@Group(value = RedisConstants.REDIS_SCOPE, executionPolicy = ExecutionPolicy.INTERNAL)
public class ProtocolSendCommandAndReadMethodInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;
    private InterceptorGroup interceptorGroup;

    public ProtocolSendCommandAndReadMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorGroup interceptorGroup) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorGroup = interceptorGroup;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
            if (invocation != null && invocation.getAttachment() != null) {
                final CommandContext commandContext = (CommandContext) invocation.getAttachment();
                if (methodDescriptor.getMethodName().equals("sendCommand")) {
                    commandContext.setWriteBeginTime(System.currentTimeMillis());
                } else {
                    commandContext.setReadBeginTime(System.currentTimeMillis());
                }
                logger.debug("Set command context {}", commandContext);
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, methodDescriptor.getClassName(), methodDescriptor.getMethodName(), "", args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
            if (invocation != null && invocation.getAttachment() != null) {
                final CommandContext commandContext = (CommandContext) invocation.getAttachment();
                if (methodDescriptor.getMethodName().equals("sendCommand")) {
                    commandContext.setWriteEndTime(System.currentTimeMillis());
                    commandContext.setWriteFail(throwable != null);
                } else {
                    commandContext.setReadEndTime(System.currentTimeMillis());
                    commandContext.setReadFail(throwable != null);
                }
                logger.debug("Set command context {}", commandContext);
            }
        } catch (Throwable t) {
            logger.warn("Failed to AFTER process. {}", t.getMessage(), t);
        }
    }
}