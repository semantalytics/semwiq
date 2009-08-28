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

package semwiq;

import at.jku.semwiq.mediator.Constants;
import at.jku.semwiq.mediator.conf.MediatorConfig;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * @author dorgon
 * 
 */
public class deployutils {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			if (args.length == 2 && args[0].equals("compile-rules")) {
//				Optimizer.compileRulesIntoFile(args[1]);
				throw new RuntimeException("Not implemented: compile-rules");
				
//			} else if (args[0].equals("clear-stats")) {
//				MediatorConfig conf = new MediatorConfig(Constants.DEFAULT_CONFIG_FILE);
//				Model m = conf.getDataSourceRegistryConfig().;
//				m.removeAll();
//				if (!m.listStatements().hasNext())
//					System.out.println("Statistics model successfully cleared.");
//				else
//					System.out.println("Statistics model couldn't be fully cleared.");
			}
		} else {
			System.out
					.println("Required arguments: command [arg1] ... [argn]\n"
							+ "   compile-rules [target-dir]\n"
							+ "       ... where target-dir is the directory where all the compiled classes go to, i.e. 'classes'\n"
							+ "   clear-stats\n");
			System.exit(-1);
		}
	}

}
