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

import com.societegenerale.commons.amqp.core.config.*;
import com.societegenerale.commons.amqp.core.config.ExchangeTypes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.CorrelationDataPostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;

import java.util.Map.Entry;

@Configuration
@Import(RabbitMqConfiguration.class)
@EnableRabbit
@Slf4j
@ConditionalOnProperty(prefix = "rabbitmq.auto-config", name = "enabled", matchIfMissing = true)
public class RabbitMqAutoConfiguration implements ApplicationContextAware {

  private ConfigurableApplicationContext applicationContext;

  @Autowired
  private RabbitConfig rabbitConfig;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    if (rabbitConfig != null) {
      loadRabbitConfig();
    }
    RabbitTemplate rabbitTemplate = this.applicationContext.getBean(RabbitTemplate.class);
    rabbitTemplate.setBeforePublishPostProcessors(applicationContext.getBeansOfType(MessagePostProcessor.class).values().toArray(new MessagePostProcessor[0]));
    rabbitTemplate.setCorrelationDataPostProcessor(applicationContext.getBean(CorrelationDataPostProcessor.class));
  }

  private void loadRabbitConfig() {
    Exchange deadLetterExchange = new CustomExchange("DEFAULT-DEAD-LETTER-EXCHANGE.DLQ", ExchangeTypes.TOPIC.getValue());
    deadLetterExchange = loadDeadLetterExchangeConfig(deadLetterExchange);
    loadExchangeConfigs();
    loadQueueConfigs(deadLetterExchange);
    loadBindingConfigs();
    loadReQueueConfig();
  }

  private void loadReQueueConfig() {
    if(rabbitConfig.getReQueueConfig()!=null) {
      ReQueueConfig reQueueConfig = rabbitConfig.getReQueueConfig();

      Exchange exchange = reQueueConfig.getExchange().buildExchange(rabbitConfig.getDefaultExchange());
      applicationContext.getBeanFactory().registerSingleton(exchange.getName(), exchange);
      log.info("Auto configuring exchange: Key = {} , Exchange = {{}}", exchange.getName(), exchange);

      Queue queue = reQueueConfig.getQueue().buildQueue(rabbitConfig.getDefaultQueue(), rabbitConfig.getDeadLetterConfig());
      applicationContext.getBeanFactory().registerSingleton(queue.getName(), queue);
      log.info("Auto configuring queue: Key = {} , Queue = {{}}", queue.getName(), queue);

      Binding binding = BindingBuilder.bind(queue).to(exchange).with(reQueueConfig.getRoutingKey()).noargs();
      applicationContext.getBeanFactory().registerSingleton("requeueBinding", binding);
      log.info("Auto configuring binding: Key = {} , Binding = {{}}", "requeueBinding", binding);

    }
  }

  private void loadBindingConfigs() {
    if (!CollectionUtils.isEmpty(rabbitConfig.getBindings())) {
      log.info("Auto configuring binding...");
      for (Entry<String, BindingConfig> entry : rabbitConfig.getBindings().entrySet()) {
        Exchange exchange = applicationContext.getBean(entry.getValue().getExchange(), Exchange.class);
        Queue queue = applicationContext.getBean(entry.getValue().getQueue(), Queue.class);
        Binding binding = entry.getValue().bind(exchange, queue);
        applicationContext.getBeanFactory().registerSingleton(entry.getKey(), binding);
        log.info("Auto configuring binding: Key = {} , Binding = {{}}", entry.getKey(), binding);
      }
    }
  }

  private void loadQueueConfigs(Exchange deadLetterExchange) {
    if (!CollectionUtils.isEmpty(rabbitConfig.getQueues())) {
      log.info("Auto configuring queue...");
      for (Entry<String, QueueConfig> entry : rabbitConfig.getQueues().entrySet()) {
        Queue queue = entry.getValue().buildQueue(rabbitConfig.getDefaultQueue(), rabbitConfig.getDeadLetterConfig());
        applicationContext.getBeanFactory().registerSingleton(entry.getKey(), queue);
        log.info("Auto configuring queue: Key = {} , Queue = {{}}", entry.getKey(), queue);
        if (entry.getValue().getDeadLetterEnabled()) {
          Queue deadLetterQueue = entry.getValue().buildDeadLetterQueue(rabbitConfig.getDefaultQueue(), rabbitConfig.getDeadLetterConfig());
          applicationContext.getBeanFactory().registerSingleton(deadLetterQueue.getName(), deadLetterQueue);
          log.info("Auto configuring dead letter queue: Key = {} , DeadLetterQueue = {{}}", deadLetterQueue.getName(), deadLetterQueue);
          Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(deadLetterQueue.getName()).noargs();
          String deadLetterBindingKey = new StringBuilder().append(deadLetterExchange.getName()).append(":").append(deadLetterQueue.getName()).toString();
          applicationContext.getBeanFactory().registerSingleton(deadLetterBindingKey, deadLetterBinding);
          log.info("Auto configuring dead letter binding: Key = {} , DeadLetterBinding = {{}}", deadLetterBindingKey, deadLetterBinding);
        }
      }
    }
  }

  private void loadExchangeConfigs() {
    if (!CollectionUtils.isEmpty(rabbitConfig.getExchanges())) {
      log.info("Auto configuring exchange...");
      for (Entry<String, ExchangeConfig> entry : rabbitConfig.getExchanges().entrySet()) {
        Exchange exchange = entry.getValue().buildExchange(rabbitConfig.getDefaultExchange());
        applicationContext.getBeanFactory().registerSingleton(entry.getKey(), exchange);
        log.info("Auto configuring exchange: Key = {} , Exchange = {{}}", entry.getKey(), exchange);
      }
    }
  }

  private Exchange loadDeadLetterExchangeConfig(Exchange defaultDeadLetterExchange) {
    if (rabbitConfig.getDeadLetterConfig() != null && rabbitConfig.getDeadLetterConfig().getDeadLetterExchange() != null) {
      Exchange deadLetterExchange = rabbitConfig.getDeadLetterConfig().getDeadLetterExchange().buildExchange(rabbitConfig.getDefaultExchange());
      applicationContext.getBeanFactory().registerSingleton(deadLetterExchange.getName(), deadLetterExchange);
      log.info("Auto configuring dead letter exchange: Key = {} , DeadLetterExchange = {{}}", deadLetterExchange.getName(), deadLetterExchange);
      return deadLetterExchange;
    }
    return defaultDeadLetterExchange;
  }
}
