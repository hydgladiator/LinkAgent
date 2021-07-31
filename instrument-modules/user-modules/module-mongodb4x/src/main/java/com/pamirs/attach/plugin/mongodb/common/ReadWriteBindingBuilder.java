package com.pamirs.attach.plugin.mongodb.common;

import com.mongodb.client.ClientSession;
import com.mongodb.internal.binding.ReadWriteBinding;

/**
 * @author xiaobin.zfb|xiaobin@shulie.io
 * @since 2020/9/27 6:17 下午
 */
public interface ReadWriteBindingBuilder {
    ReadWriteBinding build(final ClientSession session, final boolean ownsSession, ReadWriteBinding readWriteBinding);

    boolean isSupported(Class clazz);
}
