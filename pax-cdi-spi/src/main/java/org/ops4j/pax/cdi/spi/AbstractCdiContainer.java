/*
 * Copyright 2012 Harald Wellmann.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.cdi.spi;

import static org.ops4j.pax.swissbox.core.ContextClassLoaderUtils.doWithClassLoader;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.Callable;

import javax.enterprise.inject.spi.BeanManager;

import org.ops4j.lang.Ops4jException;
import org.ops4j.pax.cdi.api.ContainerInitialized;
import org.ops4j.pax.cdi.api.ServicesPublished;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for {@link CdiContainer} implementations.
 * 
 * @author Harald Wellmann
 */
public abstract class AbstractCdiContainer implements CdiContainer {

    private static Logger log = LoggerFactory.getLogger(AbstractCdiContainer.class);

    private Bundle bundle;
    private CdiContainerType containerType;
    private ServiceRegistration<CdiContainer> registration;
    private boolean started;

    protected AbstractCdiContainer(CdiContainerType containerType, Bundle bundle) {
        this.containerType = containerType;
        this.bundle = bundle;
    }

    @Override
    public synchronized void start(Object environment) {
        if (!started) {
            log.info("Starting CDI container for bundle {}", getBundle());
            doStart(environment);
            BeanBundles.addBundle(getContextClassLoader(), getBundle());
            finishStartup();
            started = true;
        }
    }

    @Override
    public synchronized void stop() {
        if (started) {
            log.info("Stopping CDI container for bundle {}", getBundle());
            BeanBundles.removeBundle(getContextClassLoader(), getBundle());
            doStop();
            if (registration != null) {
                try {
                    registration.unregister();
                }
                // CHECKSTYLE:SKIP
                catch (Exception e) {
                    // Ignore
                }
            }
            started = false;
        }
    }

    protected abstract void doStart(Object environment);

    protected abstract void doStop();

    protected void finishStartup() {
        try {
            registration = doWithClassLoader(getContextClassLoader(),
                new Callable<ServiceRegistration<CdiContainer>>() {

                    @Override
                    public ServiceRegistration<CdiContainer> call() throws Exception {
                        BundleContext bc = bundle.getBundleContext();

                        // fire ContainerInitialized event
                        BeanManager beanManager = getBeanManager();
                        beanManager.fireEvent(new ContainerInitialized());
                        
                        // register CdiContainer service
                        Dictionary<String, Object> props = new Hashtable<String, Object>();
                        props.put("bundleId", bundle.getBundleId());
                        props.put("symbolicName", bundle.getSymbolicName());

                        ServiceRegistration<CdiContainer> reg = bc.registerService(CdiContainer.class, AbstractCdiContainer.this,
                            props);

                        // fire ServicesPublished event
                        beanManager.fireEvent(new ServicesPublished());
                        
                        return reg;
                    }
                });
        }
        // CHECKSTYLE:SKIP
        catch (Exception exc) {
            log.error("", exc);
            throw new Ops4jException(exc);
        }
    }

    public Bundle getBundle() {
        return bundle;
    }

    public CdiContainerType getContainerType() {
        return containerType;
    }
}
