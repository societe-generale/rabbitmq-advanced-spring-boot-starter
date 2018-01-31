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

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Getter
@Setter
@Slf4j
public abstract class AbstractConfig {

  private boolean defaultConfigApplied;

  protected <T> T getDefaultConfig(String name, String property, T currentValue, T defaultConfigValue, T defaultValue) {
    T value = getDefaultConfig(currentValue, defaultConfigValue);
    if (null == value) {
      log.warn("'{}' : '{}' : No '{}' configuration provided. Applying default value {} : {} ", getClass().getName(), name, property, property, defaultValue);
    }
    return value != null ? value : defaultValue;
  }

  protected <T> T getDefaultConfig(T currentValue, T defaultConfigValue) {
    return currentValue != null ? currentValue : defaultConfigValue;
  }

  protected Map<String, Object> loadArguments(Map<String, Object> currentArguments, Map<String, Object> defaultArguments) {
    Map<String, Object> arguments = new HashMap<>();
    if (defaultArguments != null) {
      arguments.putAll(defaultArguments);
    }
    if (currentArguments != null) {
      arguments.putAll(currentArguments);
    }
    return arguments;
  }

  public abstract boolean validate();
}
