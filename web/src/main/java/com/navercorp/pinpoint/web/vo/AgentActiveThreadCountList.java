/*
 *
 *  * Copyright 2014 NAVER Corp.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package com.navercorp.pinpoint.web.vo;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadCountRes;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author Taejin Koo
 */
@JsonSerialize(using = AgentActiveThreadCountListSerializer.class)
public class AgentActiveThreadCountList {

    private final List<AgentActiveThreadCount> agentActiveThreadRepository;

    public AgentActiveThreadCountList(int initialCapacity) {
        agentActiveThreadRepository = new ArrayList<AgentActiveThreadCount>(initialCapacity);
    }

    public void add(AgentActiveThreadCount agentActiveThreadStatus) {
        agentActiveThreadRepository.add(agentActiveThreadStatus);
    }

    public List<AgentActiveThreadCount> getAgentActiveThreadRepository() {
        return agentActiveThreadRepository;
    }


    @Override
    public String toString() {
        return "AgentActiveThreadCountList{" +
                "agentActiveThreadRepository=" + agentActiveThreadRepository +
                '}';
    }
}

class AgentActiveThreadCountListSerializer extends JsonSerializer<AgentActiveThreadCountList>
{
    @Override
    public void serialize(AgentActiveThreadCountList agentActiveThreadStatusList, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
        List<AgentActiveThreadCount> agentActiveThreadRepository = agentActiveThreadStatusList.getAgentActiveThreadRepository();

        jgen.writeStartObject();

        for (AgentActiveThreadCount agentActiveThread : agentActiveThreadRepository) {
            jgen.writeFieldName(agentActiveThread.getAgentId());
            jgen.writeStartObject();

            TRouteResult routeResult = agentActiveThread.getRouteResult(TRouteResult.UNKNOWN);
            jgen.writeNumberField("code", routeResult.getValue());
            jgen.writeStringField("message", routeResult.name());

            TCmdActiveThreadCountRes activeThreadCount = agentActiveThread.getActiveThreadCount();
            long timeStamp = System.currentTimeMillis();
            if (activeThreadCount != null) {
                timeStamp = activeThreadCount.getTimeStamp();

                if (activeThreadCount.getActiveThreadCountSize() >= 4) {
                    List<Integer> values = activeThreadCount.getActiveThreadCount();

                    jgen.writeFieldName("status");
                    jgen.writeStartArray();
                    jgen.writeNumber(values.get(0));
                    jgen.writeNumber(values.get(1));
                    jgen.writeNumber(values.get(2));
                    jgen.writeNumber(values.get(3));
                    jgen.writeEndArray();
                }
            }
            jgen.writeNumberField("timeStamp", timeStamp);

            jgen.writeEndObject();
        }

        jgen.writeEndObject();
    }
}
