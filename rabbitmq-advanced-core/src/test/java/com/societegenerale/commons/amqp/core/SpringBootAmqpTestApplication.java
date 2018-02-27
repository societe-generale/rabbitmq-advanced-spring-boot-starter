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

package com.societegenerale.commons.amqp.core;

import com.societegenerale.commons.amqp.core.config.RabbitConfig;
import com.societegenerale.commons.amqp.core.recoverer.DeadLetterMessageRecoverer;
import com.societegenerale.commons.amqp.core.recoverer.handler.MessageExceptionHandler;
import com.societegenerale.commons.amqp.core.recoverer.handler.impl.LogMessageExceptionHandler;
import com.societegenerale.commons.amqp.core.requeue.AutoReQueueScheduler;
import com.societegenerale.commons.amqp.core.requeue.ReQueueConsumer;
import com.societegenerale.commons.amqp.core.requeue.policy.impl.ThresholdReQueuePolicy;
import org.mockito.Mockito;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {RabbitAutoConfiguration.class})
public class SpringBootAmqpTestApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootAmqpTestApplication.class, args);
  }

  @Bean
  public RabbitConfig rabbitConfig() {
    return new RabbitConfig();
  }

  @Bean
  public MessageRecoverer deadLetterMessageRecoverer() {
    return new DeadLetterMessageRecoverer();
  }

  @Bean
  public ThresholdReQueuePolicy thresholdReQueuePolicy() {
    return new ThresholdReQueuePolicy();
  }

  @Bean
  public RabbitTemplate rabbitTemplate() {
    return Mockito.mock(RabbitTemplate.class);
  }

  @Bean
  public MessageExceptionHandler logMessageExceptionHandler() {
    return new LogMessageExceptionHandler();
  }

  @Bean
  public AutoReQueueScheduler autoReQueueScheduler() {
    return new AutoReQueueScheduler();
  }

  @Bean
  public ReQueueConsumer reQueueConsumer() {
    return new ReQueueConsumer();
  }
}
