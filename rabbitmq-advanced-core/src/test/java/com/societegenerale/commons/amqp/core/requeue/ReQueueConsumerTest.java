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

package com.societegenerale.commons.amqp.core.requeue;

import com.societegenerale.commons.amqp.core.requeue.policy.ReQueuePolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.*;

/**
 * Created by Anand Manissery on 7/13/2017.
 */

public class ReQueueConsumerTest {

  private static final long TIME_OUT = 3000L;
  private ReQueueConsumer reQueueConsumer;

  private RabbitTemplate rabbitTemplate;

  private ReQueuePolicy reQueuePolicy;

  private Message message;

  private ReQueueMessage reQueueMessage;

  @BeforeEach
  public void setUp() {
    rabbitTemplate = mock(RabbitTemplate.class);
    reQueuePolicy = mock(ReQueuePolicy.class);
    reQueueConsumer = new ReQueueConsumer(rabbitTemplate, reQueuePolicy, TIME_OUT);

    MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().setHeader("x-original-queue", "dummy-queue").build();
    message = MessageBuilder.withBody("DummyMessage".getBytes()).andProperties(messageProperties).build();

    reQueueMessage = ReQueueMessage.builder()
        .deadLetterQueue("dummy-queue.dlq")
        .messageCount(2)
        .build();
  }

  @Test
  public void reQueueMessageUntilTheMessageCountWhenAllMessageCanRequeue() {
    when(rabbitTemplate.receive("dummy-queue.dlq", TIME_OUT)).thenReturn(message);
    when(reQueuePolicy.canReQueue(message)).thenReturn(true);
    reQueueConsumer.onMessage(reQueueMessage);
    verify(rabbitTemplate, times(2)).send("dummy-queue", message);
  }

  @Test
  public void shouldNotReQueueMessageFromDealLetterQueueWhenMessageCanNotRequeue() {
    when(rabbitTemplate.receive("dummy-queue.dlq", TIME_OUT)).thenReturn(message);
    when(reQueuePolicy.canReQueue(message)).thenReturn(false);
    reQueueConsumer.onMessage(reQueueMessage);
    verify(rabbitTemplate, times(2)).send("dummy-queue.dlq", message);
  }

  @Test
  public void shouldNotReQueueIfTheMessageIsNull() {
    when(rabbitTemplate.receive(anyString(), anyLong())).thenReturn(null);
    reQueueConsumer.onMessage(reQueueMessage);
    verify(rabbitTemplate, never()).send(anyString(), any(Message.class));
  }

  @Test
  public void shouldNotReQueueWhenRequeuePolicyIsNull() {
    //Given
    reQueueConsumer = new ReQueueConsumer(rabbitTemplate, null, TIME_OUT);
    //And
    when(rabbitTemplate.receive("dummy-queue.dlq", 3000L)).thenReturn(message);
    //When
    reQueueConsumer.onMessage(reQueueMessage);
    //Then
    verify(rabbitTemplate, times(2)).send("dummy-queue.dlq", message);
  }
}
