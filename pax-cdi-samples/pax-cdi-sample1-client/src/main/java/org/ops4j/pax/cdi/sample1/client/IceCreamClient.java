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
package org.ops4j.pax.cdi.sample1.client;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

import org.ops4j.pax.cdi.api.OsgiService;
import org.ops4j.pax.cdi.api.OsgiServiceProvider;
import org.ops4j.pax.cdi.api.event.ServiceAdded;
import org.ops4j.pax.cdi.sample1.IceCreamService;

@OsgiServiceProvider
public class IceCreamClient {

    @Inject
    @OsgiService(timeout = 2000, dynamic = true, filter = "(flavour=chocolate)")
    private IceCreamService iceCreamService;

    @Inject
    @OsgiService(timeout = 2000, dynamic = true)
    private Instance<IceCreamService> iceCreamServices;


    public void onInit(@Observes @Initialized(ApplicationScoped.class) Object object) {
        System.out.println("initialized application scope");
    }

    public void onShutdown(@Observes @Destroyed(ApplicationScoped.class) Object object) {
        System.out.println("destroyed application scope");
    }

    public void onInit(@Observes @ServiceAdded BeanManager manager) {
        System.out.println("registered BeanManager");
    }

    public String getFlavour() {
        return iceCreamService.getFlavour();
    }

    public List<String> getAllFlavours() {
        List<String> flavours = new ArrayList<String>();
        for (IceCreamService service : iceCreamServices) {
            String flavour = service.getFlavour();
            flavours.add(flavour);
        }
        return flavours;
    }
}
