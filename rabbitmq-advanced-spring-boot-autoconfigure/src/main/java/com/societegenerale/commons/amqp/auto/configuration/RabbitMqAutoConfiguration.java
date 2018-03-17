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
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@Import(RabbitMqConfiguration.class)
@EnableRabbit
@ConditionalOnProperty(prefix = "rabbitmq.auto-config", name = "enabled", matchIfMissing = true)
@Slf4j
public class RabbitMqAutoConfiguration {

  private RabbitConfig rabbitConfig;

  private RabbitAdmin rabbitAdmin;

  private Map<String, Exchange> exchangeMap;

  private Map<String, Queue>queueMap;

  @Autowired
  public RabbitMqAutoConfiguration(RabbitConfig rabbitConfig, RabbitAdmin rabbitAdmin) {
    this.rabbitConfig=rabbitConfig;
    this.rabbitAdmin = rabbitAdmin;
    this.exchangeMap = new LinkedHashMap<>();
    this.queueMap = new LinkedHashMap<>();
  }

  @PostConstruct
  public void init() {
    Exchange deadLetterExchange = loadDeadLetterExchangeConfig();
    loadExchangeConfigs();
    loadQueueConfigs(deadLetterExchange);
    loadBindingConfigs();
    loadReQueueConfig();
  }

  private Exchange loadDeadLetterExchangeConfig() {
    ExchangeConfig deadLetterExchangeConfig =  (rabbitConfig.getDeadLetterConfig() != null && rabbitConfig.getDeadLetterConfig().getDeadLetterExchange() != null) ?
       rabbitConfig.getDeadLetterConfig().getDeadLetterExchange():ExchangeConfig.builder().name("DEFAULT-DEAD-LETTER-EXCHANGE.DLQ").type(ExchangeTypes.TOPIC).build();
    Exchange deadLetterExchange = deadLetterExchangeConfig.buildExchange(rabbitConfig.getDefaultExchange());
    rabbitAdmin.declareExchange(deadLetterExchange);
    log.info("Auto configuring dead letter exchange: Key = {} , DeadLetterExchange = {{}}", deadLetterExchange.getName(), deadLetterExchange);
    return deadLetterExchange;
  }

  private void loadExchangeConfigs() {
    if (!CollectionUtils.isEmpty(rabbitConfig.getExchanges())) {
      log.info("Auto configuring exchange...");
      for (Map.Entry<String, ExchangeConfig> entry : rabbitConfig.getExchanges().entrySet()) {
        Exchange exchange = entry.getValue().buildExchange(rabbitConfig.getDefaultExchange());
        exchangeMap.put(entry.getKey(), exchange);
        rabbitAdmin.declareExchange(exchange);
        log.info("Auto configuring exchange: Key = {} , Exchange = {{}}", entry.getKey(), exchange);
      }
    }
  }

  private void loadQueueConfigs(Exchange deadLetterExchange) {
    if (!CollectionUtils.isEmpty(rabbitConfig.getQueues())) {
      log.info("Auto configuring queue...");
      for (Map.Entry<String, QueueConfig> entry : rabbitConfig.getQueues().entrySet()) {
        Queue queue = entry.getValue().buildQueue(rabbitConfig.getDefaultQueue(), rabbitConfig.getDeadLetterConfig());
        queueMap.put(entry.getKey(), queue);
        rabbitAdmin.declareQueue(queue);
        log.info("Auto configuring queue: Key = {} , Queue = {{}}", entry.getKey(), queue);
        if (entry.getValue().getDeadLetterEnabled()) {
          Queue deadLetterQueue = entry.getValue().buildDeadLetterQueue(rabbitConfig.getDefaultQueue(), rabbitConfig.getDeadLetterConfig());
          rabbitAdmin.declareQueue(deadLetterQueue);
          log.info("Auto configuring dead letter queue: Key = {} , DeadLetterQueue = {{}}", deadLetterQueue.getName(), deadLetterQueue);
          Binding deadLetterBinding = BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(deadLetterQueue.getName()).noargs();
          rabbitAdmin.declareBinding(deadLetterBinding);
          log.info("Auto configuring dead letter binding: Key = {{}:{}} , DeadLetterBinding = {{}}", deadLetterExchange.getName(), deadLetterQueue.getName(), deadLetterBinding);
        }
      }
    }
  }

  private void loadBindingConfigs() {
    if (!CollectionUtils.isEmpty(rabbitConfig.getBindings())) {
      log.info("Auto configuring binding...");
      for (Map.Entry<String, BindingConfig> entry : rabbitConfig.getBindings().entrySet()) {
        Exchange exchange = exchangeMap.get(entry.getValue().getExchange());
        Queue queue = queueMap.get(entry.getValue().getQueue());
        Binding binding = entry.getValue().bind(exchange, queue);
        rabbitAdmin.declareBinding(binding);
        log.info("Auto configuring binding: Key = {} , Binding = {{}}", entry.getKey(), binding);
      }
    }
  }

  private void loadReQueueConfig() {
    if(rabbitConfig.getReQueueConfig()!=null && rabbitConfig.getReQueueConfig().isEnabled()) {
      ReQueueConfig reQueueConfig = rabbitConfig.getReQueueConfig();

      Exchange exchange = reQueueConfig.getExchange().buildExchange(rabbitConfig.getDefaultExchange());
      rabbitAdmin.declareExchange(exchange);
      log.info("Auto configuring exchange: Key = {} , Exchange = {{}}", exchange.getName(), exchange);

      Queue queue = reQueueConfig.getQueue().buildQueue(rabbitConfig.getDefaultQueue(), rabbitConfig.getDeadLetterConfig());
      rabbitAdmin.declareQueue(queue);
      log.info("Auto configuring queue: Key = {} , Queue = {{}}", queue.getName(), queue);

      Binding binding = BindingBuilder.bind(queue).to(exchange).with(reQueueConfig.getRoutingKey()).noargs();
      rabbitAdmin.declareBinding(binding);
      log.info("Auto configuring binding: Key = {} , Binding = {{}}", "requeueBinding", binding);

    }
  }

}
