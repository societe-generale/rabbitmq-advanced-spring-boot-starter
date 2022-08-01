/*
 * Copyright 2017-2018, Société Générale All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.societegenerale.commons.amqp.core.requeue.policy.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Anand Manissery on 7/14/2017.
 */

@SpringBootTest
public class ThresholdReQueuePolicyTest {

    @Autowired
    private ThresholdReQueuePolicy thresholdReQueuePolicy;

    private Message message;

    @BeforeEach
    public void setUp() {
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
        message = MessageBuilder.withBody("DummyMessage".getBytes()).andProperties(messageProperties).build();
    }

    @Test
    public void shouldReQueueForAnyNewMessage() throws Exception {
        assertTrue(thresholdReQueuePolicy.canReQueue(message));
        assertEquals(message.getMessageProperties().getHeaders().get("x-requeue-count"), 1);
    }

    @Test
    public void shouldReQueueRequeueCountIsLessThanThreshold() throws Exception {
        message.getMessageProperties().getHeaders().put("x-requeue-count", 1);
        assertTrue(thresholdReQueuePolicy.canReQueue(message));
        assertEquals(message.getMessageProperties().getHeaders().get("x-requeue-count"), 2);
    }

    @Test
    public void shouldNotReQueueRequeueCountIsEqualToThreshold() throws Exception {
        message.getMessageProperties().getHeaders().put("x-requeue-count", 3);
        assertFalse(thresholdReQueuePolicy.canReQueue(message));
        assertEquals(message.getMessageProperties().getHeaders().get("x-requeue-count"), 3);
    }

    @Test
    public void shouldNotReQueueRequeueCountIsGreaterThanOrEqualToThreshold() throws Exception {
        message.getMessageProperties().getHeaders().put("x-requeue-count", 3);
        assertFalse(thresholdReQueuePolicy.canReQueue(message));
        assertEquals(message.getMessageProperties().getHeaders().get("x-requeue-count"), 3);
    }

}