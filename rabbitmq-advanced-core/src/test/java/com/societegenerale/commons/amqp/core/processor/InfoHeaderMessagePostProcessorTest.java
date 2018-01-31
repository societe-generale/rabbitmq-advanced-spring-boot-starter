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
import org.mockito.Mockito;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.core.env.Environment;

import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Created by Anand Manissery on 02/07/17.
 */
public class InfoHeaderMessagePostProcessorTest {

  private InfoHeaderMessagePostProcessor infoHeaderMessagePostProcessor;

  private Message message;

  private Environment environment;

  @Before
  public void setUp() {
    environment = Mockito.mock(Environment.class);
    Mockito.when(environment.getProperty("spring.application.name", String.class)).thenReturn("my-application-name");
    infoHeaderMessagePostProcessor = new InfoHeaderMessagePostProcessor();
    infoHeaderMessagePostProcessor.setEnvironment(environment);
    infoHeaderMessagePostProcessor.getHeaders().put("info-key", "info-value");
    infoHeaderMessagePostProcessor.setEnvironment(environment);
    message = MessageBuilder.withBody("DummyMessage".getBytes()).build();
  }

  @Test
  public void addInfoHeadersToMessage() {
    infoHeaderMessagePostProcessor.postProcessMessage(message);
    Map<String, Object> headers = (Map<String, Object>) message.getMessageProperties().getHeaders().get("info");
    assertThat(headers.get("info-key"), is(equalTo("info-value")));
    assertThat(headers.get("spring-application-name"), is(equalTo("my-application-name")));
    assertNotNull(headers.get("execution-time"));
  }

  @Test
  public void addDefaultHeadersToMessage() {
    infoHeaderMessagePostProcessor.getHeaders().clear();
    infoHeaderMessagePostProcessor.postProcessMessage(message);
    Map<String, Object> headers = (Map<String, Object>) message.getMessageProperties().getHeaders().get("info");
    assertThat(headers.get("spring-application-name"), is(equalTo("my-application-name")));
    assertNotNull(headers.get("execution-time"));
  }

}
