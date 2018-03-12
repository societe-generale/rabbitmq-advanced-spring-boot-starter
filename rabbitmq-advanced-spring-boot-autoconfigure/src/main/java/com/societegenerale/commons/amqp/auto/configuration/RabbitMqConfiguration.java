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

import com.societegenerale.commons.amqp.core.config.RabbitConfig;
import com.societegenerale.commons.amqp.core.processor.CorrelationPostProcessor;
import com.societegenerale.commons.amqp.core.processor.DefaultCorrelationDataPostProcessor;
import com.societegenerale.commons.amqp.core.processor.DefaultCorrelationPostProcessor;
import com.societegenerale.commons.amqp.core.processor.InfoHeaderMessagePostProcessor;
import com.societegenerale.commons.amqp.core.recoverer.DeadLetterMessageRecoverer;
import com.societegenerale.commons.amqp.core.requeue.AutoReQueueScheduler;
import com.societegenerale.commons.amqp.core.requeue.ReQueueConsumer;
import com.societegenerale.commons.amqp.core.requeue.policy.ReQueuePolicy;
import com.societegenerale.commons.amqp.core.requeue.policy.impl.ThresholdReQueuePolicy;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.CorrelationDataPostProcessor;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;


@Configuration
@ConditionalOnProperty(prefix = "rabbitmq.auto-config", name = "enabled", matchIfMissing = true)
@EnableScheduling
public class RabbitMqConfiguration {

  @Autowired(required = false)
  private Tracer tracer;

  @Bean
  public RabbitConfig rabbitConfig() {
    return new RabbitConfig();
  }

  @Bean
  @ConditionalOnMissingBean(MessageConverter.class)
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  @ConditionalOnMissingBean(MessageRecoverer.class)
  public MessageRecoverer messageRecoverer() {
    return new DeadLetterMessageRecoverer();
  }

  @Bean
  public MessagePostProcessor headerMessagePostProcessor(RabbitConfig rabbitConfig) {
    InfoHeaderMessagePostProcessor infoHeaderMessagePostProcessor = new InfoHeaderMessagePostProcessor();
    infoHeaderMessagePostProcessor.setHeaders(rabbitConfig.getInfoHeaders());
    return infoHeaderMessagePostProcessor;
  }

  @Bean
  @ConditionalOnMissingBean(CorrelationDataPostProcessor.class)
  public CorrelationDataPostProcessor correlationDataPostProcessor(CorrelationPostProcessor correlationPostProcessor) {
    return new DefaultCorrelationDataPostProcessor(correlationPostProcessor);
  }

  @Bean
  @ConditionalOnMissingBean(CorrelationPostProcessor.class)
  public CorrelationPostProcessor correlationPostProcessor() {
    return new DefaultCorrelationPostProcessor(tracer);
  }

  @Bean
  @ConditionalOnProperty(prefix = "rabbitmq.auto-config", name = "re-queue-config.enabled", matchIfMissing = true)
  public ReQueueConsumer reQueueConsumer() {
    return new ReQueueConsumer();
  }

  @Bean
  @ConditionalOnProperty(prefix = "rabbitmq.auto-config", name = "re-queue-config.auto-requeue-enabled")
  public AutoReQueueScheduler autoReQueueScheduler() {
    return new AutoReQueueScheduler();
  }

  @Bean
  @ConditionalOnMissingBean(ReQueuePolicy.class)
  public ReQueuePolicy reQueuePolicy() {
    return new ThresholdReQueuePolicy();
  }

  @Bean
  @ConditionalOnMissingBean(RabbitAdmin.class)
  public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
    return new RabbitAdmin(connectionFactory);
  }

}
