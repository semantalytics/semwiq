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
package at.jku.semwiq.endpoint;

import java.io.File;

/**
 * @author dorgon, Andreas Langegger, al@jku.at
 *
 */
public class Filesystem {
	
	/**
	 * deep-create directory
	 */
	public static void makeDirectory(File dir) {
		if (!dir.exists()) {
			File parent = dir.getParentFile();
			if (parent != null)
				makeDirectory(parent);
			dir.mkdir();
		}
	}

	/**
	 * deep-delete directory
	 * @param tdbLoc
	 */
	public static void deleteDirectory(File dir) {
		File[] files = dir.listFiles();
		for (File f : files) {
			if (f.isDirectory()) {
				deleteDirectory(f);
			} else
				f.delete();
		}
		dir.delete();
	}
}
