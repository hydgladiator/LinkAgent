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
package com.shulie.instrument.simulator.core.inject.util.classreading;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by xiaobin on 2017/1/19.
 */
public class ClassLoadingChecker {

    private final Set<String> loadClass = new HashSet<String>();

    public boolean isFirstLoad(String className) {
        if (className == null) {
            throw new NullPointerException("className must not be null");
        }
        if (this.loadClass.add(className)) {
            // first load
            return true;
        }
        // already exist
        return false;
    }
}
