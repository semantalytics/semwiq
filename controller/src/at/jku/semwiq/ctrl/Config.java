/**
 * Copyright 2007-2008 Institute for Applied Knowledge Processing, Johannes Kepler University Linz
 *  
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.jku.semwiq.ctrl;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import at.jku.semwiq.rmi.DaemonRegistry;
import at.jku.semwiq.rmi.InterfaceUtils;

/**
 * @author dorgon
 *
 */
public class Config {
	private final List<DaemonRegistry> registries;
	
	public static Config create(String configFile) throws SemWIQControllerException {
		try {
			Config cfg = new Config();
			if (configFile != null) {
				BufferedReader reader = new BufferedReader(new FileReader(configFile));
				String str;
				while ((str = reader.readLine()) != null)
					cfg.addRegistry(InterfaceUtils.getDaemonRegistry(str));
			}
			return cfg;
		} catch (Exception e) {
			throw new SemWIQControllerException("Failed to read config file: " + configFile, e);
		}
	}
	
	public Config() {
		registries = new ArrayList<DaemonRegistry>();
	}
	
	public List<DaemonRegistry> getRegistries() {
		return registries;
	}
	
	public void addRegistry(DaemonRegistry reg) {
		registries.add(reg);
	}
}
