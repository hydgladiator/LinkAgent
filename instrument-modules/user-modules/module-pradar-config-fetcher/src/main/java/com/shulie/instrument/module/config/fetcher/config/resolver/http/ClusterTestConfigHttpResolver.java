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
package com.shulie.instrument.module.config.fetcher.config.resolver.http;


import com.alibaba.fastjson.JSON;
import com.pamirs.pradar.PradarSwitcher;
import com.pamirs.pradar.common.HttpUtils;
import com.pamirs.pradar.pressurement.agent.event.impl.ClusterTestSwitchOffEvent;
import com.pamirs.pradar.pressurement.agent.event.impl.ClusterTestSwitchOnEvent;
import com.pamirs.pradar.pressurement.agent.shared.service.ErrorReporter;
import com.pamirs.pradar.pressurement.agent.shared.service.EventRouter;
import com.pamirs.pradar.pressurement.base.util.PropertyUtil;
import com.shulie.instrument.module.config.fetcher.config.event.FIELDS;
import com.shulie.instrument.module.config.fetcher.config.impl.ClusterTestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author shiyajian
 * @since 2020-08-11
 */
public class ClusterTestConfigHttpResolver extends AbstractHttpResolver<ClusterTestConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterTestConfigHttpResolver.class.getName());

    public ClusterTestConfigHttpResolver(int interval, TimeUnit timeUnit) {
        super("cluster-test-config-fetch-scheduled", interval, timeUnit);
    }

    private static final String APP_PRESSURE_SWITCH_STATUS = "/api/application/center/app/switch/agent";
    private static final String APP_WHITE_LIST_SWITCH_STATUS = "/api/global/switch/whitelist";

    private static final String CLOSE = "CLOSED";
    private static final String SWITCHSTATUS = "switchStatus";

    private AtomicBoolean pradarSwitchProcessing = new AtomicBoolean(false);

    @Override
    public ClusterTestConfig fetch() {
        // ????????????
        String troControlWebUrl = PropertyUtil.getTroControlWebUrl();
        ClusterTestConfig clusterTestConfig = new ClusterTestConfig(this);
        /**
         * ???????????????????????????
         * premain???????????? close Listener???open Listener??????0
         * ??????????????????????????????????????????????????????????????????????????????????????????
         */
        getApplicationSwitcher(troControlWebUrl, clusterTestConfig);
        // ?????????????????????
        getWhiteListSwitcher(troControlWebUrl, clusterTestConfig);
        return clusterTestConfig;
    }

    @Override
    public ClusterTestConfig fetch(FIELDS... fields) {
        String troControlWebUrl = PropertyUtil.getTroControlWebUrl();
        ClusterTestConfig clusterTestConfig = new ClusterTestConfig(this);
        if (fields == null || fields.length == 0) {
            return null;
        }
        for (FIELDS field : fields) {
            switch (field) {
                case GLOBAL_SWITCHON:
                    /**
                     * ???????????????????????????
                     * premain???????????? close Listener???open Listener??????0
                     * ??????????????????????????????????????????????????????????????????????????????????????????
                     */
                    getApplicationSwitcher(troControlWebUrl, clusterTestConfig);
                    break;
                case WHITE_LIST_SWITCHON:
                    // ?????????????????????
                    getWhiteListSwitcher(troControlWebUrl, clusterTestConfig);
                    break;
            }
        }

        return clusterTestConfig;
    }


    /**
     * ???????????????
     */
    private void getWhiteListSwitcher(String troWebUrl, ClusterTestConfig clusterTestConfig) {

        try {
            if (null == troWebUrl || "".equals(troWebUrl)) {
                troWebUrl = PropertyUtil.getTroControlWebUrl();
            }

            String url = troWebUrl + APP_WHITE_LIST_SWITCH_STATUS;
            HttpUtils.HttpResult httpResult = HttpUtils.doGet(url);
            if (!httpResult.isSuccess()) {
                LOGGER.warn("SIMULATOR: [FetchConfig] White list switcher status response error. status: {}, result: {}! tro url is {}",
                        httpResult.getStatus(), httpResult.getResult(), url);
                clusterTestConfig.setWhiteListSwitchOn(PradarSwitcher.whiteListSwitchOn());
                return;
            }
            Map<String, Object> resultMap = JSON.parseObject(httpResult.getResult());
            Map<String, Object> map = (Map<String, Object>) resultMap.get("data");
            if (map != null && map.get(SWITCHSTATUS) != null) {
                String status = (String) map.get(SWITCHSTATUS);
                clusterTestConfig.setWhiteListSwitchOn(!CLOSE.equals(status));
                ClusterTestConfig.getWhiteListSwitcher = Boolean.TRUE;
            }
        } catch (Throwable e) {
            LOGGER.warn("SIMULATOR: [FetchConfig] Admin console Init Failure!", e);
//            clusterTestConfig.setWhiteListSwitchOn(Boolean.TRUE);
            clusterTestConfig.setWhiteListSwitchOn(PradarSwitcher.whiteListSwitchOn());

        }
    }

    /**
     * ?????????????????????
     *
     * @param troWebUrl
     * @return ?????????????????????????????????????????????
     */
    private void getApplicationSwitcher(String troWebUrl, ClusterTestConfig clusterTestConfig) {

        try {
            if (null == troWebUrl || "".equals(troWebUrl)) {
                troWebUrl = PropertyUtil.getTroControlWebUrl();
            }

            String url = troWebUrl + APP_PRESSURE_SWITCH_STATUS;
            HttpUtils.HttpResult httpResult = HttpUtils.doGet(url);
            if (!httpResult.isSuccess()) {
                LOGGER.warn(String.format("SIMULATOR: [FetchConfig] Admin console http response error. status: %s, result: %s! tro url is %s",
                        httpResult.getStatus(), httpResult.getResult(), url));
                clusterTestConfig.setGlobalSwitchOn(PradarSwitcher.clusterTestSwitchOn());
                return;
            }
            Map<String, Object> resultMap = JSON.parseObject(httpResult.getResult());
            Map<String, Object> map = (Map<String, Object>) resultMap.get("data");
            if (map != null && map.get(SWITCHSTATUS) != null) {
                String status = (String) map.get(SWITCHSTATUS);
                clusterTestConfig.setGlobalSwitchOn(!CLOSE.equals(status));
                ClusterTestConfig.getApplicationSwitcher = Boolean.TRUE;
            }
        } catch (Throwable e) {
            LOGGER.warn("SIMULATOR: [FetchConfig] Admin console Init Failure!", e);
//            clusterTestConfig.setGlobalSwitchOn(Boolean.FALSE);
            clusterTestConfig.setGlobalSwitchOn(PradarSwitcher.clusterTestSwitchOn());
        }
    }

    /**
     * ??????????????????,??????????????????????????????????????????
     */
    private void pradarSwitchOpen() {
        try {
            ErrorReporter.getInstance().clear();
            ClusterTestSwitchOnEvent event = new ClusterTestSwitchOnEvent(this);
            boolean isSuccess = EventRouter.router().publish(event);
            if (!isSuccess) {
                PradarSwitcher.turnClusterTestSwitchOff();
            }
        } catch (Throwable e) {
            LOGGER.warn("SIMULATOR: [FetchConfig] switch do opened has error", e);
            /**
             * ????????????????????????
             */
            PradarSwitcher.turnClusterTestSwitchOff();
        } finally {
            pradarSwitchProcessing.set(false);
        }
    }

    /**
     * ??????????????????,??????????????????????????????????????????
     */
    private void pradarSwitchClosed() {
        try {
            //???????????????????????????
            ErrorReporter.getInstance().clear();
            ClusterTestSwitchOffEvent event = new ClusterTestSwitchOffEvent(this);
            boolean isSuccess = EventRouter.router().publish(event);
            if (!isSuccess) {
                PradarSwitcher.turnClusterTestSwitchOn();
            }
        } catch (Throwable e) {
            LOGGER.warn("SIMULATOR: [FetchConfig] switch do closed has error", e);
            /**
             * ?????????????????????????????????
             */
            PradarSwitcher.turnClusterTestSwitchOn();
        } finally {
            pradarSwitchProcessing.set(false);
        }
    }

}
