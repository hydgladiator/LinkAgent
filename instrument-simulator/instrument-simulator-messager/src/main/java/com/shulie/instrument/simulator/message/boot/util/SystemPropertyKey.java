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
package com.shulie.instrument.simulator.message.boot.util;

/**
 * 系统 property 的 key
 *
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2020/12/9 4:25 下午
 */
public enum SystemPropertyKey {

    JAVA_VERSION("java.version"),
    JAVA_RUNTIME_VERSION("java.runtime.version"),
    JAVA_RUNTIME_NAME("java.runtime.name"),
    JAVA_SPECIFICATION_VERSION("java.specification.version"),
    JAVA_CLASS_VERSION("java.class.version"),
    JAVA_VM_NAME("java.vm.name"),
    JAVA_VM_VERSION("java.vm.version"),
    JAVA_VM_INFO("java.vm.info"),
    JAVA_VM_SPECIFICATION_VERSION("java.vm.specification.version"),
    SUN_JAVA_COMMAND("sun.java.command"),
    OS_NAME("os.name");

    private final String key;

    SystemPropertyKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }
}

