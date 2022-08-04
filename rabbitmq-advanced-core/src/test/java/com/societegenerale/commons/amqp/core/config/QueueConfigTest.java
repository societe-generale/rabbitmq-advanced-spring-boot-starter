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

package com.societegenerale.commons.amqp.core.config;


import com.societegenerale.commons.amqp.core.exception.RabbitmqConfigurationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
public class QueueConfigTest {

    private QueueConfig queueConfig;

    private QueueConfig expectedQueueConfig;

    private QueueConfig defaultQueueConfig;

    private DeadLetterConfig deadLetterConfig;

    private String queueName = "queue-1";

    @BeforeEach
    public void setUp() {
        queueConfig = QueueConfig.builder().build();
        expectedQueueConfig = QueueConfig.builder().durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(false).build();
        defaultQueueConfig = QueueConfig.builder().build();
        deadLetterConfig = DeadLetterConfig.builder().build();
    }

    @Test
    public void queueConfigEqualsTest() {
        queueConfig = QueueConfig.builder().build();
        expectedQueueConfig = QueueConfig.builder().build();
        assertTrue(queueConfig.equals(expectedQueueConfig));
        queueConfig.setDefaultConfigApplied(false);
        expectedQueueConfig.setDefaultConfigApplied(true);
        assertTrue(queueConfig.equals(expectedQueueConfig));
    }

    @Test
    public void queueConfigeHashcodeTest() {
        queueConfig = QueueConfig.builder().build();
        expectedQueueConfig = QueueConfig.builder().build();
        assertEquals(queueConfig.hashCode(), expectedQueueConfig.hashCode());
        queueConfig.setDefaultConfigApplied(false);
        expectedQueueConfig.setDefaultConfigApplied(true);
        assertEquals(queueConfig.hashCode(), expectedQueueConfig.hashCode());
    }

    @Test
    public void defaultQueueConfigWithoutDefaultConfigurationAppliedTest() {
        queueConfig = QueueConfig.builder().build();
        assertNull(queueConfig.getName());
        assertNull(queueConfig.getDurable());
        assertNull(queueConfig.getAutoDelete());
        assertNull(queueConfig.getExclusive());
        assertNull(queueConfig.getDeadLetterEnabled());
        assertFalse(queueConfig.isDefaultConfigApplied());
        assertNotNull(queueConfig.getArguments());
        assertTrue(CollectionUtils.isEmpty(queueConfig.getArguments()));
    }

    @Test
    public void defaultQueueConfigWithDefaultConfigurationAppliedTest() {
        queueConfig = QueueConfig.builder().build().applyDefaultConfig(defaultQueueConfig);
        expectedQueueConfig = QueueConfig.builder().durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(false).build();
        assertNull(queueConfig.getName());
        assertFalse(queueConfig.getDurable());
        assertFalse(queueConfig.getAutoDelete());
        assertFalse(queueConfig.getExclusive());
        assertFalse(queueConfig.getDeadLetterEnabled());
        assertNotNull(queueConfig.getArguments());
        assertTrue(queueConfig.isDefaultConfigApplied());
        assertTrue(CollectionUtils.isEmpty(queueConfig.getArguments()));
        assertEquals(queueConfig, expectedQueueConfig);
    }

    @Test
    public void queueConfigWithNameAndValidationSuccessTest(CapturedOutput outputCapture) {
        queueConfig = QueueConfig.builder().name(queueName).build();
        assertTrue(queueConfig.validate());
        assertEquals(outputCapture.getOut(), containsString(String.format("Queue configuration validated successfully for queue '%s'", queueName)));
    }

    @Test
    public void queueConfigWithoutNameAndValidationFailTest(CapturedOutput outputCapture) {
        queueConfig = QueueConfig.builder().build();
        assertFalse(queueConfig.validate());
        assertTrue(outputCapture.getOut().contains("Invalid Queue Configuration : Name must be provided for a queue"));
    }

    @Test
    public void queueConfigWithoutNameAndDefaultConfigurationWithNameAndValidationFailTest(CapturedOutput outputCapture) {
        defaultQueueConfig = QueueConfig.builder().name(queueName).build();
        queueConfig = QueueConfig.builder().build().applyDefaultConfig(defaultQueueConfig);
        assertFalse(queueConfig.validate());
        assertTrue(outputCapture.getOut().contains("Invalid Queue Configuration : Name must be provided for a queue"));
    }

    @Test
    public void queueConfigWithOnlyQueueNameAndDefaultConfigurationAppliedTest() {
        queueConfig = QueueConfig.builder().name(queueName).build().applyDefaultConfig(defaultQueueConfig);
        expectedQueueConfig = QueueConfig.builder().name(queueName).durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(false).build();
        assertEquals(queueConfig, expectedQueueConfig);
    }

    @Test
    public void queueConfigWithOnlyQueueNameAndNoDefaultConfigurationAppliedTest() {
        queueConfig = QueueConfig.builder().name(queueName).build();
        expectedQueueConfig = QueueConfig.builder().name(queueName).durable(null).autoDelete(null).exclusive(null).deadLetterEnabled(null).build();
        assertEquals(queueConfig, expectedQueueConfig);
    }

    @Test
    public void queueConfigWithOnlyQueueNameAndFewDefaultConfigurationTest() {
        defaultQueueConfig = QueueConfig.builder()
                .durable(true).autoDelete(true).exclusive(true).deadLetterEnabled(true).argument("key1", "value1")
                .build();

        queueConfig = QueueConfig.builder().name(queueName).build().applyDefaultConfig(defaultQueueConfig);

        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(true).autoDelete(true).exclusive(true).deadLetterEnabled(true)
                .argument("key1", "value1")
                .build();

        assertEquals(queueConfig, expectedQueueConfig);
    }

    @Test
    public void queueConfigByOverridingFromDefaultConfigurationTest() {
        defaultQueueConfig = QueueConfig.builder()
                .durable(true).autoDelete(true).exclusive(true).deadLetterEnabled(true).argument("key1", "value1")
                .build();

        queueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false)
                .argument("key1", "NEW_VALUE").argument("key2", "value2")
                .build()
                .applyDefaultConfig(defaultQueueConfig);

        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false).exclusive(true).deadLetterEnabled(true)
                .argument("key1", "NEW_VALUE").argument("key2", "value2")
                .build();

        assertEquals(queueConfig, expectedQueueConfig);
    }

    @Test
    public void createQueueWithNoDeadLetterAndDefaultConfigurationAppliedTest() {
        defaultQueueConfig = QueueConfig.builder().build();
        queueConfig = QueueConfig.builder().name(queueName).build();
        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(false).arguments(new HashMap<>())
                .build();
        Queue queue = queueConfig.buildQueue(defaultQueueConfig, null);
        assertQueue(queue, expectedQueueConfig);
    }

    @Test
    public void createQueueWithNoDeadLetterAndDefaultConfigurationPreAppliedTest() {
        defaultQueueConfig = QueueConfig.builder().build();
        queueConfig = QueueConfig.builder().name(queueName).build().applyDefaultConfig(defaultQueueConfig);
        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(false).arguments(new HashMap<>())
                .build();
        Queue queue = queueConfig.buildQueue(defaultQueueConfig, null);
        assertQueue(queue, expectedQueueConfig);
    }

    @Test
    public void createQueueWithFewDefaultConfigurationTest() {
        defaultQueueConfig = QueueConfig.builder()
                .durable(true).autoDelete(true).exclusive(true).deadLetterEnabled(false).argument("key1", "value1")
                .build();

        queueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false)
                .argument("key2", "value2").argument("key1", "NEW_VALUE")
                .build()
                .applyDefaultConfig(defaultQueueConfig);

        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false).exclusive(true).deadLetterEnabled(true)
                .argument("key2", "value2").argument("key1", "NEW_VALUE")
                .build();

        Queue queue = queueConfig.buildQueue(defaultQueueConfig, null);
        assertQueue(queue, expectedQueueConfig);
    }

    @Test
    public void createQueueWithDeadLetterAndDefaultDeadLetterConfig() {
        defaultQueueConfig = QueueConfig.builder().deadLetterEnabled(true).build();

        queueConfig = QueueConfig.builder().name(queueName).deadLetterEnabled(true).argument("key1", "value1").build()
                .applyDefaultConfig(defaultQueueConfig);

        deadLetterConfig = DeadLetterConfig.builder().deadLetterExchange(ExchangeConfig.builder().name("dead-letter-exchange").build())
                .build();

        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(true)
                .argument("key1", "value1")
                .argument("x-dead-letter-exchange", "dead-letter-exchange").argument("x-dead-letter-routing-key", queueName + ".DLQ")
                .build();

        Queue queue = queueConfig.buildQueue(defaultQueueConfig, deadLetterConfig);
        assertQueue(queue, expectedQueueConfig);
    }


    @Test
    public void createQueueWithDeadLetterAndDeadLetterConfig() {
        defaultQueueConfig = QueueConfig.builder().deadLetterEnabled(true).build();

        queueConfig = QueueConfig.builder().name(queueName).deadLetterEnabled(true).argument("key1", "value1").build()
                .applyDefaultConfig(defaultQueueConfig);

        deadLetterConfig = DeadLetterConfig.builder().deadLetterExchange(ExchangeConfig.builder().name("dead-letter-exchange").build())
                .queuePostfix(".dlq-new")
                .build();

        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(true)
                .argument("key1", "value1")
                .argument("x-dead-letter-exchange", "dead-letter-exchange").argument("x-dead-letter-routing-key", queueName + ".dlq-new")
                .build();

        Queue queue = queueConfig.buildQueue(defaultQueueConfig, deadLetterConfig);
        assertQueue(queue, expectedQueueConfig);
    }


    @Test
    public void createQueueWithDeadLetterAndNoDeadLetterConfig() {
        Assertions.assertThrows(RabbitmqConfigurationException.class, () -> {
            defaultQueueConfig = QueueConfig.builder().deadLetterEnabled(true).build();

            queueConfig = QueueConfig.builder().name(queueName).build();

            Queue queue = queueConfig.buildQueue(defaultQueueConfig, null);
            assertQueue(queue, expectedQueueConfig);
        });
    }

    @Test
    public void createQueueWithNoDeadLetterAndNoDeadLetterConfig() {
        defaultQueueConfig = QueueConfig.builder().deadLetterEnabled(false).build();

        queueConfig = QueueConfig.builder().name(queueName).build();

        expectedQueueConfig = QueueConfig.builder()
                .name(queueName).durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(false)
                .build();

        Queue queue = queueConfig.buildQueue(defaultQueueConfig, null);
        assertQueue(queue, expectedQueueConfig);
    }

    @Test
    public void createDeadLetterQueueWithDefaultConfigurationAppliedTest() {
        defaultQueueConfig = QueueConfig.builder().deadLetterEnabled(true).build();
        queueConfig = QueueConfig.builder().name(queueName).build();
        deadLetterConfig = DeadLetterConfig.builder().deadLetterExchange(ExchangeConfig.builder().name("dead-letter-exchange").build())
                .queuePostfix(".dlq-new")
                .build();
        expectedQueueConfig = QueueConfig.builder()
                .name(queueName + ".dlq-new").durable(false).autoDelete(false).exclusive(false).arguments(new HashMap<>())
                .build();
        Queue queue = queueConfig.buildDeadLetterQueue(defaultQueueConfig, deadLetterConfig);
        assertQueue(queue, expectedQueueConfig);
    }

    @Test
    public void createDeadLetterQueueWithDefaultConfigurationPreAppliedTest() {
        defaultQueueConfig = QueueConfig.builder().deadLetterEnabled(true).build();
        queueConfig = QueueConfig.builder().name(queueName).build().applyDefaultConfig(defaultQueueConfig);
        deadLetterConfig = DeadLetterConfig.builder().deadLetterExchange(ExchangeConfig.builder().name("dead-letter-exchange").build())
                .queuePostfix(".dlq-new")
                .build();
        expectedQueueConfig = QueueConfig.builder()
                .name(queueName + ".dlq-new").durable(false).autoDelete(false).exclusive(false).deadLetterEnabled(true).arguments(new HashMap<>())
                .build();
        Queue queue = queueConfig.buildDeadLetterQueue(defaultQueueConfig, deadLetterConfig);
        assertQueue(queue, expectedQueueConfig);
    }

    private void assertQueue(Queue queue, QueueConfig queueConfig) {
        assertEquals(queue.getName(), queueConfig.getName());
        assertEquals(queue.isDurable(), queueConfig.getDurable());
        assertEquals(queue.isAutoDelete(), queueConfig.getAutoDelete());
        assertEquals(queue.isExclusive(), queueConfig.getExclusive());
        assertEquals(queue.getArguments(), queueConfig.getArguments());
    }

}
