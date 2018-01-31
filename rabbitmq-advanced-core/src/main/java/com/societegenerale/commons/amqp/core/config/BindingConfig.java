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
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ConfigurationProperties
public class BindingConfig extends AbstractConfig {

  /**
   * Exchange to bind. Provide the exchange key from the exchange configuration
   */
  private String exchange;

  /**
   * Queue to bind. Provide the queue key from the queue configuration
   */
  private String queue;

  /**
   * Routing Key for exchange and queue binding.
   */
  private String routingKey;

  /**
   * Arguments for the bindings
   */
  @Singular
  private Map<String, Object> arguments;

  @Override
  public boolean validate() {
    boolean valid = true;
    if (StringUtils.isEmpty(getExchange())) {
      log.error("Invalid Exchange : Exchange must be provided for a binding");
      valid = false;
    }
    if (StringUtils.isEmpty(getQueue())) {
      log.error("Invalid Queue : Queue must be provided for a binding");
      valid = false;
    }
    if (valid) {
      log.info("Binding configuration validated successfully for Binding '{}'", this);
    }
    return valid;
  }

  public Binding bind(Exchange exchange, Queue queue) {
    if (ExchangeTypes.HEADERS.getValue().equals(exchange.getType()) && CollectionUtils.isEmpty(getArguments())) {
      throw new RabbitmqConfigurationException(String
          .format("Invalid Arguments : Arguments must be provided for a header exchange for binding {%s}", this));
    } else if (StringUtils.isEmpty(getRoutingKey())) {
      throw new RabbitmqConfigurationException(String
          .format("Invalid RoutingKey : RoutingKey must be provided for a non header exchange for binding {%s}", this));
    }
    return BindingBuilder.bind(queue).to(exchange).with(getRoutingKey()).and(getArguments());
  }

}
