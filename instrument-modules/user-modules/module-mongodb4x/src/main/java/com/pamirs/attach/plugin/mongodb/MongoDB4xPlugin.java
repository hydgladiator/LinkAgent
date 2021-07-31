package com.pamirs.attach.plugin.mongodb;

import com.pamirs.attach.plugin.mongodb.interceptor.DelegateOperationExecutorInterceptor;
import com.shulie.instrument.simulator.api.ExtensionModule;
import com.shulie.instrument.simulator.api.ModuleInfo;
import com.shulie.instrument.simulator.api.ModuleLifecycleAdapter;
import com.shulie.instrument.simulator.api.instrument.EnhanceCallback;
import com.shulie.instrument.simulator.api.instrument.InstrumentClass;
import com.shulie.instrument.simulator.api.instrument.InstrumentMethod;
import com.shulie.instrument.simulator.api.listener.Listeners;
import org.kohsuke.MetaInfServices;

/**
 * @author angju
 * @date 2020/8/5 18:58
 */
@MetaInfServices(ExtensionModule.class)
@ModuleInfo(id = "mongodb4x", version = "1.0.0", author = "angju@shulie.io", description = "mongdb4x 数据库")
public class MongoDB4xPlugin extends ModuleLifecycleAdapter implements ExtensionModule {

    @Override
    public void onActive() throws Throwable {

        //4.0.5
        enhanceTemplate.enhance(this, "com.mongodb.client.internal.MongoClientDelegate$DelegateOperationExecutor", new EnhanceCallback() {
            @Override
            public void doEnhance(InstrumentClass target) {

                InstrumentMethod instrumentMethod_1 = target.getDeclaredMethod("execute",
                        "com.mongodb.internal.operation.ReadOperation", "com.mongodb.ReadPreference",
                        "com.mongodb.ReadConcern", "com.mongodb.client.ClientSession");

                InstrumentMethod instrumentMethod_2 = target.getDeclaredMethod("execute",
                        "com.mongodb.internal.operation.WriteOperation", "com.mongodb.ReadConcern",
                        "com.mongodb.client.ClientSession");

                instrumentMethod_1.addInterceptor(Listeners.of(DelegateOperationExecutorInterceptor.class));
                instrumentMethod_2.addInterceptor(Listeners.of(DelegateOperationExecutorInterceptor.class));
            }
        });
    }

}
