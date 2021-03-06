/**
 * Copyright 2021 Shulie Technology, Co.Ltd
 * Email: shulie@shulie.io
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shulie.instrument.module.config.fetcher.config;

import com.shulie.instrument.module.config.fetcher.config.event.ConfigEventEnum;
import com.shulie.instrument.module.config.fetcher.config.event.ConfigListener;
import com.shulie.instrument.module.config.fetcher.config.event.FIELDS;
import com.shulie.instrument.module.config.fetcher.config.resolver.ConfigResolver;
import com.shulie.instrument.simulator.api.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author shiyajian
 * @since 2020-08-11
 */
public abstract class AbstractConfig<T> implements IConfig<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfig.class);

    protected boolean isInit;

    protected ConfigResolver resolver;

    protected Class<T> type;

    public AbstractConfig(ConfigResolver resolver) {
        this.isInit = false;
        this.resolver = resolver;
    }

    @Override
    public boolean isInit() {
        return isInit;
    }

    protected void loaded() {
        isInit = true;
    }

    protected <F> boolean isChanged(F source, F target) {
        return true;
    }

    @Override
    public void init() {
        this.resolver.resolve(this);
        this.loaded();
    }

    @Override
    public void triggerFetch() {
        this.resolver.triggerFetch(this);
    }

    @Override
    public void destroy() {
        if (this.resolver != null) {
            this.resolver.destroy();
        }
    }

    protected <V> void change(FIELDS field, V newValue) {
        // ???????????????????????????????????????
        if (newValue == null) {
            return;
        }
        // ??????????????????????????????????????????????????????
        Boolean change = field.compareIsChangeAndSet(this, newValue);
        if (change) {
            LOGGER.info("config changed???" + field.getDesc());
            // ??????????????????????????????
            // ?????????????????????????????? init  ??? clear ??????
            String listenerKey = ConfigManager.getListenerKey(type.getName(), field.getFileName(), ConfigEventEnum.CHANGE.name());
            List<ConfigListener> configListeners = ConfigManager.getInstance().getListenerHolder().get(listenerKey);
            if (CollectionUtils.isNotEmpty(configListeners)) {
                for (ConfigListener configListener : configListeners) {
                    configListener.onEvent(newValue);
                }
            }
        }
    }
}
