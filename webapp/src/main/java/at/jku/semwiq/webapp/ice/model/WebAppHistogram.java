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
package at.jku.semwiq.webapp.ice.model;

import java.util.ArrayList;
import java.util.List;

import at.jku.rdfstats.hist.ComparableDomainHistogram;

/**
 * @author thomas
 *
 */
public class WebAppHistogram {
//	
//	private Histogram regHis;
//	private long[] hisBins;
//	private long hisMaxBin;
//	private int hisHeight;
//	private String propertyName;
//	private String propertyLink;
//	
//	private String compareMin;
//	private String compareMax;
//	// indicates if min/max should be rendered
//	private boolean render;
//	
//
//	/**
//	 * @param hisBins
//	 * @param hisMaxBin
//	 * @param regHis
//	 */
//	public WebAppHistogram(long[] hisBins,  Histogram regHis, String propertyName, String propertyLink, boolean render) {
//		this.hisBins = hisBins;
//		this.regHis = regHis;
//		this.hisHeight = 140;
//		this.hisMaxBin = findMaxBin();
//		this.propertyName = propertyName;
//		this.propertyLink = propertyLink;
//		this.render = render;
//		init();
//	}
//
//	/**
//	 * @param hisBins
//	 * @param hisHeight
//	 * @param hisMaxBin
//	 * @param regHis
//	 */
//	public WebAppHistogram(long[] hisBins, int hisHeight, Histogram regHis, String propertyName, String propertyLink, boolean render) {
//		this.hisBins = hisBins;
//		if (hisHeight < 140) {
//			this.hisHeight = 140;
//		}
//		else {
//			this.hisHeight = hisHeight;
//		}
//		this.regHis = regHis;
//		this.hisMaxBin = findMaxBin();
//		this.propertyName = propertyName;
//		this.propertyLink = propertyLink;
//		this.render = render;
//		init();
//	}
//
//	public Histogram getRegHis() {
//		return regHis;
//	}
//	
//	public void setRegHis(Histogram regHis) {
//		this.regHis = regHis;
//	}
//	
//	public long[] getHisBins() {
//		return hisBins;
//	}
//	
//	public void setHisBins(long[] hisBins) {
//		this.hisBins = hisBins;
//	}
//	
//	public long getHisMaxBin() {
//		return hisMaxBin;
//	}
//	
//	public void setHisMaxBin(long hisMaxBin) {
//		this.hisMaxBin = hisMaxBin;
//	}
//	
//	public int getHisHeight() {
//		return hisHeight;
//	}
//	
//	public void setHisHeight(int hisHeight) {
//		this.hisHeight = hisHeight;
//	}
//	
//	public String getPropertyName() {
//		return propertyName;
//	}
//
//	public void setPropertyName(String propertyName) {
//		this.propertyName = propertyName;
//	}
//
//	public String getPropertyLink() {
//		return propertyLink;
//	}
//
//	public void setPropertyLink(String propertyLink) {
//		this.propertyLink = propertyLink;
//	}
//	
//	public boolean isRender() {
//		return render;
//	}
//
//	public void setRender(boolean render) {
//		this.render = render;
//	}
//	
//	public String getCompareMin() {
//		return compareMin;
//	}
//
//	public void setCompareMin(String compareMin) {
//		this.compareMin = compareMin;
//	}
//
//	public String getCompareMax() {
//		return compareMax;
//	}
//
//	public void setCompareMax(String compareMax) {
//		this.compareMax = compareMax;
//	}
//	
//	
//	// --- program logic --- //
//	
//	private void init() {
//		if(this.isRender()) {
//			this.setCompareMin(((Object) ((ComparableDomainHistogram)regHis).getMin()).toString());
//			this.setCompareMax(((Object) ((ComparableDomainHistogram)regHis).getMax()).toString());
//		}
//	}
//
//	private long findMaxBin() {
//		long max=0;
//		for (int i=0; i<hisBins.length; i++) {
//			if (hisBins[i]>max) {
//				max = hisBins[i];
//			}
//		}
//		return max;
//	}
//	
//	public List getGraphData() {
//		List tmp = new ArrayList();
//		for (int i=0; i<hisBins.length; i++) {
//			double[] temp = new double[1];
//			temp[0] = (double) hisBins[i];
////			System.out.println("hisBin"+i+": "+hisBins[i]);
//			tmp.add(temp);
//		}
//		return tmp;
//	}
//		
//	public List getGraphLabels() {
//		List tmp = new ArrayList();
//		for (int i=0; i<hisBins.length; i++) {
//			tmp.add(new Integer(i).toString());
//		}
//		return tmp;
//	}
//	

}
