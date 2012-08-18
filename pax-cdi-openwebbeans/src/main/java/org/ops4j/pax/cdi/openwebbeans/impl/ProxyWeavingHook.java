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
package org.ops4j.pax.cdi.openwebbeans.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A weaving hook which adds a dynamic package import for {@code javassist.util.proxy} for
 * all classes from bean bundles. This is required for Javassist proxies for managed beans
 * generated by OpenWebBeans.
 * 
 * @author Harald Wellmann
 */
public class ProxyWeavingHook implements WeavingHook {

	private static Logger log = LoggerFactory.getLogger(ProxyWeavingHook.class);

	@Override
	public void weave(WovenClass wovenClass) {
		Bundle bundle = wovenClass.getBundleWiring().getBundle();
		if (bundle.getHeaders().get("Pax-ManagedBeans") != null) {		
			log.debug("weaving {}", wovenClass.getClassName());
			wovenClass.getDynamicImports().add("javassist.util.proxy");
		}
	}
}