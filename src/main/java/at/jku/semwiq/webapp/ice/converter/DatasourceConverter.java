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
package at.jku.semwiq.webapp.ice.converter;

import java.util.ArrayList;
import java.util.Iterator;

import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.servlet.http.HttpServletRequest;

import at.jku.semwiq.webapp.ice.handler.InformationHandler;
import at.jku.semwiq.webapp.ice.model.Datasource;


public class DatasourceConverter implements Converter {

	public Object getAsObject(FacesContext context, UIComponent component, String uri) {
		// TODO Auto-generated method stub
		if(uri == null) {
			return null;
		}
		else if(uri.equals("")) {
			return null;
		}
		else {
	 		ExternalContext ec = context.getExternalContext();
	 		HttpServletRequest request = (HttpServletRequest)ec.getRequest();
	 		InformationHandler ih = (InformationHandler)request.getAttribute("informationHandler");
//	 		System.out.println("hugo");
//	 		System.out.println(ih.getTest());
	 		ArrayList temp = (ArrayList) ih.getDatasourceList();
	 		for (Iterator iter=temp.iterator(); iter.hasNext();) {
	 			Datasource ds = (Datasource) iter.next();
	 			if(ds.getSemwiq_informationURI().equalsIgnoreCase(uri)) {
	 				System.out.println(ds.getSemwiq_informationEndpoint());
	 				return ds;
	 			}
	 		}
			return null;
		}
	}

	public String getAsString(FacesContext context, UIComponent component, Object datasource) {
		// TODO Auto-generated method stub
		if(datasource == null) {
			return null;
		}
		else if(datasource instanceof Datasource) {
			return ((Datasource) datasource).getSemwiq_informationURI();
		}
		else if(datasource instanceof String) {
			return (String) datasource;
		}
		else {
			throw new ConverterException("Typesecurity: Unexpected Type " + datasource.getClass() + ". (Group expected)");
		}
	}
	
	public static String getAsString(Datasource ds) {
		return ds.getSemwiq_informationURI();
	}

}
