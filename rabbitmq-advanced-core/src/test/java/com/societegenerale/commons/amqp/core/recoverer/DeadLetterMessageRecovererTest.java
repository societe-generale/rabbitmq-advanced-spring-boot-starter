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

package com.societegenerale.commons.amqp.core.recoverer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by Anand Manissery on 7/13/2017.
 */

@SpringBootTest
public class DeadLetterMessageRecovererTest {

    @Autowired
    private AmqpTemplate errorTemplate;

    @Autowired
    private DeadLetterMessageRecoverer deadLetterMessageRecoverer;

    private Message message;

    private RuntimeException cause;

    @BeforeEach
    public void setUp() {
        MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().build();
        message = MessageBuilder.withBody("DummyMessage".getBytes()).andProperties(messageProperties).build();
        cause = new RuntimeException("Some Exception", new RuntimeException("Some Root Cause"));
    }

    @Test
    public void deadLetterMessageRecovererTest() {
        doNothing().when(errorTemplate).send(anyString(), anyString(), any(Message.class));
        deadLetterMessageRecoverer.recover(message, cause);
        verify(errorTemplate, times(1)).send(anyString(), anyString(), any(Message.class));
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        assertTrue(headers.containsKey("x-exception-stacktrace"));
        assertTrue(headers.containsKey("x-exception-message"));
        assertTrue(headers.containsKey("x-exception-root-cause-message"));
        assertTrue(headers.containsKey("x-original-exchange"));
        assertTrue(headers.containsKey("x-original-routingKey"));
        assertTrue(headers.containsKey("x-original-queue"));
        assertTrue(headers.containsKey("x-recover-time"));
        assertTrue(headers.containsKey("x-dead-letter-exchange"));
        assertTrue(headers.containsKey("x-dead-letter-queue"));
        assertEquals(headers.get("x-exception-message"), "RuntimeException: Some Exception");
        assertEquals(headers.get("x-exception-root-cause-message"), "RuntimeException: Some Root Cause");
    }

}
