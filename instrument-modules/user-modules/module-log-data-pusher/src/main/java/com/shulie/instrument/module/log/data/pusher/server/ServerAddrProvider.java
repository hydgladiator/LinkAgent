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
package com.shulie.instrument.module.log.data.pusher.server;

/**
 * @Description
 * @Author xiaobin.zfb
 * @mail xiaobin@shulie.io
 * @Date 2020/8/7 7:47 下午
 */
public interface ServerAddrProvider {

    /**
     * 获取连接信息
     *
     * @return
     */
    ConnectInfo selectConnectInfo();

    /**
     * 上报异常的连接信息
     *
     * @param connectInfo
     */
    void errorConnectInfo(ConnectInfo connectInfo);
}
