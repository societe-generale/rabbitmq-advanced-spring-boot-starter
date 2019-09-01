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

package com.societegenerale.commons.amqp.core.processor;

import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by anand on 02/07/17.
 */
public class DefaultCorrelationDataPostProcessorTest {

  private DefaultCorrelationDataPostProcessor defaultCorrelationDataPostProcessor;

  private Message message;

  @Before
  public void setUp() {
    defaultCorrelationDataPostProcessor = new DefaultCorrelationDataPostProcessor(new DefaultCorrelationPostProcessor(null));
    message = MessageBuilder.withBody("DummyMessage".getBytes()).build();
  }

//  @Test
//  public void postProcessWithNoCorrelationDataTest() {
//    CorrelationData correlationData = defaultCorrelationDataPostProcessor.postProcess(message, null);
//    assertNotNull(message.getMessageProperties().getHeaders().get("correlation-id"));
//    assertNotNull(correlationData);
//    assertNotNull(correlationData.getId());
//    assertThat(message.getMessageProperties().getHeaders().get("correlation-id"),
//        is(correlationData.getId()));
//  }

//  @Test
//  public void postProcessWithNoCorrelationDataIdTest() {
//    CorrelationData correlationData = defaultCorrelationDataPostProcessor.postProcess(message, new CorrelationData(null));
//    assertNotNull(message.getMessageProperties().getHeaders().get("correlation-id"));
//    assertNotNull(correlationData);
//    assertNotNull(correlationData.getId());
//    assertThat(message.getMessageProperties().getHeaders().get("correlation-id"),
//        is(correlationData.getId()));
//  }

  @Test
  public void postProcessWithCorrelationDataTest() {
    CorrelationData inputCorrelationData = new CorrelationData("my-correlation-id");
    CorrelationData correlationData = defaultCorrelationDataPostProcessor.postProcess(message, inputCorrelationData);
    assertNotNull(message.getMessageProperties().getHeaders().get("correlation-id"));
    assertNotNull(correlationData);
    assertNotNull(correlationData.getId());
    assertThat(message.getMessageProperties().getHeaders().get("correlation-id"),
        is(inputCorrelationData.getId()));
    assertThat(correlationData.getId(),
        is(inputCorrelationData.getId()));
  }


}
