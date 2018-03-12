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

package com.societegenerale.commons.amqp.auto.configuration;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore
public class SpringBootAmqpAutoConfigurationTest {

  @MockBean
  private RabbitAdmin rabbitAdmin;

  @Test
  public void checkRabbitMqAutoConfigurationDeclaration() {
    Mockito.verify(rabbitAdmin, Mockito.times(5)).declareExchange(Mockito.any(Exchange.class));
    Mockito.verify(rabbitAdmin, Mockito.times(6)).declareQueue(Mockito.any(Queue.class));
    Mockito.verify(rabbitAdmin, Mockito.times(6)).declareBinding(Mockito.any(Binding.class));
  }
}
