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
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ConfigurationProperties
public class QueueConfig extends AbstractConfig {

  /**
   * name of a queue. Its a mandatory property
   */
  private String name;

  /**
   * If true we are declaring a durable queue (the queue will survive a server restart)
   */
  private Boolean durable;

  /**
   * If true set auto delete for the queue
   */
  private Boolean autoDelete;

  /**
   * If true set 'exclusive' for the queue
   */
  private Boolean exclusive;

  /**
   * If true for auto configuring the dead letter queue
   */
  private Boolean deadLetterEnabled;

  /**
   * Arguments for the queue
   */
  @Singular
  private Map<String, Object> arguments;

  public boolean validate() {
    if (StringUtils.isEmpty(getName())) {
      log.error("Invalid Queue Configuration : Name must be provided for a queue");
      return false;
    }
    log.info("Queue configuration validated successfully for queue '{}'", getName());
    return true;
  }

  public QueueConfig applyDefaultConfig(QueueConfig defaultQueueConfig) {
    log.debug("Applying DefaultQueueConfig on the current QueueConfig :: QueueConfig = {{}} , DefaultQueueConfig = {{}}",
        this, defaultQueueConfig);
    setDurable(getDefaultConfig(getName(), "durable", getDurable(), defaultQueueConfig.getDurable(), Boolean.FALSE));
    setAutoDelete(getDefaultConfig(getName(), "autoDelete", getAutoDelete(), defaultQueueConfig.getAutoDelete(), Boolean.FALSE));
    setExclusive(getDefaultConfig(getName(), "exclusive", getExclusive(), defaultQueueConfig.getExclusive(), Boolean.FALSE));
    setDeadLetterEnabled(getDefaultConfig(getName(), "deadLetterEnabled", getDeadLetterEnabled(),
        defaultQueueConfig.getDeadLetterEnabled(), Boolean.FALSE));
    setArguments(loadArguments(getArguments(), defaultQueueConfig.getArguments()));
    setDefaultConfigApplied(true);
    log.info("DefaultQueueConfig applied on the current ExchangeConfig :: ExchangeConfig = {{}} , DefaultQueueConfig = {{}}",
        this, defaultQueueConfig);
    return this;
  }

  public Queue buildQueue(QueueConfig defaultQueueConfig, DeadLetterConfig deadLetterConfig) {
    if (!isDefaultConfigApplied()) {
      applyDefaultConfig(defaultQueueConfig);
    }
    Queue queue = new Queue(getName(), getDurable(), getExclusive(), getAutoDelete(), getArguments());
    if (Boolean.TRUE.equals(getDeadLetterEnabled())) {
      if (deadLetterConfig == null || deadLetterConfig.getDeadLetterExchange() == null) {
        throw new RabbitmqConfigurationException(
            String.format("Invalid configuration %s : DeadLetterConfig/DeadLetterExchange must be provided when deadLetterEnabled=true for queue %s.",
                getName(), getName()));
      }
      queue.getArguments().put("x-dead-letter-exchange", deadLetterConfig.getDeadLetterExchange().getName());
      queue.getArguments().put("x-dead-letter-routing-key", deadLetterConfig.createDeadLetterQueueName(getName()));
    }
    return queue;
  }

  public Queue buildDeadLetterQueue(QueueConfig defaultQueueConfig, DeadLetterConfig deadLetterConfig) {
    if (!isDefaultConfigApplied()) {
      applyDefaultConfig(defaultQueueConfig);
    }
    return new Queue(deadLetterConfig.createDeadLetterQueueName(getName()), getDurable(), getExclusive(), getAutoDelete(), getArguments());
  }

}
