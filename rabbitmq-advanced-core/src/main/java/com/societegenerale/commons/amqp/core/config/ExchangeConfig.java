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

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.CustomExchange;
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
public class ExchangeConfig extends AbstractConfig {

  /**
   * name of an exchange. Its a mandatory property
   */
  private String name;

  /**
   * type of an exchange (direct, topic, fanout, headers)
   * Default value will be <b>topic</b>
   */
  private ExchangeTypes type;

  /**
   * If true we are declaring a durable exchange (the exchange will survive a server restart)
   */
  private Boolean durable;

  /**
   * If true set auto delete for the exchange
   */
  private Boolean autoDelete;

  /**
   * If true set internal for the exchange
   */
  private Boolean internal;

  /**
   * If true set delayed for the exchange
   */
  private Boolean delayed;

  /**
   * Arguments for the exchange
   */
  @Singular
  private Map<String, Object> arguments;

  public boolean validate() {
    if (StringUtils.isEmpty(getName())) {
      log.error("Invalid Exchange Configuration : Name must be provided for an exchange");
      return false;
    }
    log.info("Exchange configuration validated successfully for exchange '{}'", getName());
    return true;
  }

  public ExchangeConfig applyDefaultConfig(ExchangeConfig defaultExchangeConfig) {
    log.debug("Applying DefaultExchangeConfig on the current ExchangeConfig :: ExchangeConfig = {{}} , DefaultExchangeConfig = {{}}", this, defaultExchangeConfig);
    setType(getDefaultConfig(getName(), "type", getType(), defaultExchangeConfig.getType(), ExchangeTypes.TOPIC));
    setDurable(getDefaultConfig(getName(), "durable", getDurable(), defaultExchangeConfig.getDurable(), Boolean.FALSE));
    setAutoDelete(getDefaultConfig(getName(), "autoDelete", getAutoDelete(), defaultExchangeConfig.getAutoDelete(), Boolean.FALSE));
    setInternal(getDefaultConfig(getName(), "internal", getInternal(), defaultExchangeConfig.getInternal(), Boolean.FALSE));
    setDelayed(getDefaultConfig(getName(), "delayed", getDelayed(), defaultExchangeConfig.getDelayed(), Boolean.FALSE));
    setArguments(loadArguments(getArguments(), defaultExchangeConfig.getArguments()));
    setDefaultConfigApplied(true);
    log.info("DefaultExchangeConfig applied on the current ExchangeConfig :: ExchangeConfig = {{}} , DefaultExchangeConfig = {{}}", this, defaultExchangeConfig);
    return this;
  }

  public AbstractExchange buildExchange(ExchangeConfig defaultExchangeConfig) {
    if (!isDefaultConfigApplied()) {
      applyDefaultConfig(defaultExchangeConfig);
    }
    AbstractExchange exchange = new CustomExchange(getName(), getType().getValue(), getDurable(), getAutoDelete(), getArguments());
    exchange.setInternal(getInternal());
    exchange.setDelayed(getDelayed());
    return exchange;
  }

}
