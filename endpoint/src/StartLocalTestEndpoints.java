import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.hp.hpl.jena.util.FileUtils;

import at.jku.semwiq.endpoint.JosekiInstance;
import at.jku.semwiq.endpoint.daemon.EndpointDaemonImpl;
import at.jku.semwiq.rmi.CommonConstants;
import at.jku.semwiq.rmi.EndpointMetadata;
import at.jku.semwiq.rmi.SemWIQInterfaceException;
import at.jku.semwiq.rmi.SpawnedEndpointMetadata;

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

/**
 * @author dorgon
 *
 */
public class StartLocalTestEndpoints {

	/**
	 * @param args
	 * @throws SemWIQInterfaceException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, SemWIQInterfaceException {
		JosekiInstance endpoint0 = JosekiInstance.createInstance(new SpawnedEndpointMetadata(CommonConstants.HOSTNAME, 8909, "sparql"),
				new FileInputStream("testing/data1.n3"), "file:testing/data1", "N3");
		JosekiInstance endpoint1 = JosekiInstance.createInstance(new SpawnedEndpointMetadata(CommonConstants.HOSTNAME, 8900, "sparql/endpoint8900"),
				new FileInputStream("testing/data1.n3"), "file:testing/data1", "N3");
		JosekiInstance endpoint2 = JosekiInstance.createInstance(new SpawnedEndpointMetadata(CommonConstants.HOSTNAME, 8901, "sparql/endpoint8901"),
				new FileInputStream("testing/data2.n3"), "file:testing/data2", "N3");
	}

}
