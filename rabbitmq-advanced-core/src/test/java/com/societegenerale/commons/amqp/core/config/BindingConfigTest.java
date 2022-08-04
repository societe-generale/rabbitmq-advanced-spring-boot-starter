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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
public class BindingConfigTest {

    private BindingConfig bindingConfig;

    private BindingConfig expectedBindingConfig;

    private String exchaneg = "exchaneg";

    private String queue = "queue";

    private String routingKey = "routingKey";

    @Test
    public void bindingConfigEqualsTest() {
        bindingConfig = BindingConfig.builder().build();
        expectedBindingConfig = BindingConfig.builder().build();
        assertTrue(bindingConfig.equals(expectedBindingConfig));
        bindingConfig.setDefaultConfigApplied(false);
        expectedBindingConfig.setDefaultConfigApplied(true);
        assertTrue(bindingConfig.equals(expectedBindingConfig));
    }

    @Test
    public void bindingConfigeHashcodeTest() {
        bindingConfig = BindingConfig.builder().build();
        expectedBindingConfig = BindingConfig.builder().build();
        assertEquals(bindingConfig.hashCode(), expectedBindingConfig.hashCode());
        bindingConfig.setDefaultConfigApplied(false);
        expectedBindingConfig.setDefaultConfigApplied(true);
        assertEquals(bindingConfig.hashCode(), expectedBindingConfig.hashCode());
    }

    @Test
    public void defaultBindingConfigTest() {
        bindingConfig = BindingConfig.builder().build();
        assertNull(bindingConfig.getExchange());
        assertNull(bindingConfig.getQueue());
        assertNull(bindingConfig.getRoutingKey());
        assertFalse(bindingConfig.isDefaultConfigApplied());
        assertNotNull(bindingConfig.getArguments());
        assertTrue(CollectionUtils.isEmpty(bindingConfig.getArguments()));
    }

    @Test
    public void bindingConfigValidationSuccessTest(CapturedOutput outputCapture) {
        bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build();
        assertTrue(bindingConfig.validate());
        assertTrue(outputCapture.getOut().contains(String.format("Binding configuration validated successfully for Binding '%s'", bindingConfig)));
    }

    @Test
    public void bindingConfigWithoutExchnageAndValidationFailTest(CapturedOutput outputCapture) {
        bindingConfig = BindingConfig.builder().exchange(null).queue(queue).routingKey(routingKey).build();
        assertFalse(bindingConfig.validate());
        assertTrue(outputCapture.getOut().contains(String.format("Invalid Exchange : Exchange must be provided for a binding")));
    }

    @Test
    public void bindingConfigWithoutQueueAndValidationFailTest(CapturedOutput outputCapture) {
        bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(null).routingKey(routingKey).build();
        assertFalse(bindingConfig.validate());
        assertTrue(outputCapture.getOut().contains(String.format("Invalid Queue : Queue must be provided for a binding")));
    }

    @Test
    public void bindingConfigNoExchangeAndNoQueueAndValidationFailTest(CapturedOutput outputCapture) {
        bindingConfig = BindingConfig.builder().exchange(null).queue(null).routingKey(routingKey).build();
        assertFalse(bindingConfig.validate());
        assertTrue(outputCapture.getOut().contains(String.format("Invalid Exchange : Exchange must be provided for a binding")));
        assertTrue(outputCapture.getOut().contains(String.format("Invalid Queue : Queue must be provided for a binding")));
    }


    @Test
    public void createBindingWithExchangeAndQueueTest() {
        Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.DIRECT).build().buildExchange(ExchangeConfig.builder().build());
        Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);

        Binding binding = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build().bind(bindingExchange, bindingQueue);

        expectedBindingConfig = BindingConfig.builder()
                .exchange(exchaneg).queue(queue).routingKey(routingKey).arguments(new HashMap<>())
                .build();
        assertBinding(binding, expectedBindingConfig);
    }

    @Test
    public void createBindingWithHeaderExchangeAndArgumentsTest() {
        Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.HEADERS).build().buildExchange(ExchangeConfig.builder().build());
        Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
        BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).argument("key", "value").build();
        Binding actualBinding = bindingConfig.bind(bindingExchange, bindingQueue);
        assertBinding(actualBinding, bindingConfig);
    }

    @Test
    public void createBindingWithHeaderExchangeAndNoArgumentsTest() {
        assertThrows(RabbitmqConfigurationException.class, () -> {
            Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.HEADERS).build().buildExchange(ExchangeConfig.builder().build());
            Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
            BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build();
            bindingConfig.bind(bindingExchange, bindingQueue);
        });
    }

    @Test
    public void createBindingWithNonHeaderExchangeAndRoutingKeyTest() {
        Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.DIRECT).build().buildExchange(ExchangeConfig.builder().build());
        Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
        BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build();
        Binding actualBinding = bindingConfig.bind(bindingExchange, bindingQueue);
        assertBinding(actualBinding, bindingConfig);
    }

    @Test
    public void createBindingWithNonHeaderExchangeAndNoRoutingKeyTest() {
        assertThrows(RabbitmqConfigurationException.class, () -> {
            Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.DIRECT).build().buildExchange(ExchangeConfig.builder().build());
            Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
            BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(null).build();
            bindingConfig.bind(bindingExchange, bindingQueue);
        });
    }

    private void assertBinding(Binding binding, BindingConfig bindingConfig) {
        assertEquals(binding.getExchange(), bindingConfig.getExchange());
        assertEquals(binding.getDestination(), bindingConfig.getQueue());
        assertEquals(binding.getDestinationType(), DestinationType.QUEUE);
        assertEquals(binding.getArguments(), bindingConfig.getArguments());
    }

}
