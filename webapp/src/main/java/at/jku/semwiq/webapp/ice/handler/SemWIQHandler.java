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
package at.jku.semwiq.webapp.ice.handler;

import java.io.OutputStream;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.jku.semwiq.mediator.Mediator;
import at.jku.semwiq.mediator.MediatorException;
import at.jku.semwiq.mediator.MediatorImpl;
import at.jku.semwiq.mediator.conf.ConfigException;
import at.jku.semwiq.mediator.conf.MediatorConfig;
import at.jku.semwiq.webapp.StartupListener;
import at.jku.semwiq.webapp.Webapp;

import com.icesoft.faces.async.render.RenderManager;
import com.icesoft.faces.async.render.Renderable;
import com.icesoft.faces.webapp.xmlhttp.PersistentFacesState;
import com.icesoft.faces.webapp.xmlhttp.RenderingException;

import edu.emory.mathcs.backport.java.util.concurrent.LinkedBlockingQueue;
import edu.emory.mathcs.backport.java.util.concurrent.ThreadPoolExecutor;
import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;


public class SemWIQHandler implements Renderable {
	
	private static final Logger log = LoggerFactory.getLogger(SemWIQHandler.class);

	private static final String MEDIATOR_INSTANCE = "semwiq.instance";
	
	// declarations
	private int percentComplete;
	private RenderManager renderManager;
	public void setRenderManager(RenderManager renderManager) {
        this.renderManager = renderManager;
        // Casting to HttpSession ruins it for portlets, in this case we only
        // need a unique reference so we use the object hash
        sessionId = FacesContext.getCurrentInstance().getExternalContext().getSession(false).toString();
        renderManager.getOnDemandRenderer(sessionId).add(this);
    }
	
	private InformationPopupHandler informationPopupHandler;
	public void setInformationPopupHandler(InformationPopupHandler informationPopupHandler) {
		this.informationPopupHandler = informationPopupHandler;
	}
	
	private String sessionId;
	private PersistentFacesState persistentFacesState;
	protected static ThreadPoolExecutor longRunningTaskThreadPool = new ThreadPoolExecutor(5, 15, 30, TimeUnit.SECONDS, new LinkedBlockingQueue(20));
	
	private boolean restartMessage=false;
	private String errorMessage;
	private OutputStream logStream;
	
	private boolean buttonEnabled = true;
	
	// constructor
	public SemWIQHandler() {
//		System.out.println("test ...");
		persistentFacesState = PersistentFacesState.getInstance();
	}
	
	// getters and setters
	public static Mediator getMediator() {
//		return (Mediator) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get(MEDIATOR_INSTANCE);
		return Webapp.fromServletContext((ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext());
	}

	public int getPercentComplete() {
		return percentComplete;
	}

	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}

	public MediatorConfig getMediatorConfiguration() {
		return getMediator().getConfig();
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public boolean isRestartMessage() {
		return restartMessage;
	}

	public void setRestartMessage(boolean restartMessage) {
		this.restartMessage = restartMessage;
	}
	
//	public OutputStream getLogStream() {
//		return logStream;
//	}

	public void setLogStream(OutputStream logStream) {
		this.logStream = logStream;
	}

	public boolean isButtonEnabled() {
		return !buttonEnabled;
	}

	public void setButtonEnabled(boolean buttonEnabled) {
		this.buttonEnabled = buttonEnabled;
	}

	// just for test purposes
	public String getTest() {
//		init();
		return "test";
	}
	
	
	
	// for the start/stop-buttons and progress bar
	public void startLongProcress(ActionEvent event) {
		this.setButtonEnabled(false);
        longRunningTaskThreadPool.execute(new LongOperationRunner(this, persistentFacesState, FacesContext.getCurrentInstance(), true));
    }
	
	// for the start/stop-buttons and progress bar
	public void stopLongProcress(ActionEvent event) {
		this.setButtonEnabled(false);
        longRunningTaskThreadPool.execute(new LongOperationRunner(this, persistentFacesState, FacesContext.getCurrentInstance(), false));
    }
	
	
	// for the start/stop-buttons and progress bar
    /**
     * Utility class to represent some server process that we want to monitor
     * using ouputProgress and server push.
     */
    protected class LongOperationRunner implements Runnable {
        PersistentFacesState state = null;
        private SemWIQHandler semwiq;
        private FacesContext context;
        // indicates whether semwiq should be started (true,startButton) or stopped (false, stopButton)	
        private boolean semwiqAction;

        public LongOperationRunner(SemWIQHandler semwiq, PersistentFacesState state, FacesContext context, boolean semwiqAction) {
            this.state = state;
            this.semwiq = semwiq;
            this.context = context;
            this.semwiqAction = semwiqAction;
        }

        /**
         * Routine that takes time and updates percentage as it runs.
         */
        public void run() {
        	Mediator mediator = Webapp.fromServletContext((ServletContext) context.getExternalContext().getContext());
        	
        	if (semwiqAction == true) {
        		if(mediator==null || !mediator.isReady()) {
        			setPercentComplete(10);
        			try {
        				mediator = new MediatorImpl();
        				// put new object into servlet context
        				Webapp.putIntoServletContext(mediator, (ServletContext) context.getExternalContext().getContext());
        				
        				while (!mediator.isReady()) {
        					Thread.sleep(300);
        					renderManager.getOnDemandRenderer(sessionId).requestRender();
        				}
        				setPercentComplete(100);
        			} catch (MediatorException e1) {
        				log.error(e1.getMessage(), e1);
        			} catch (ConfigException e2) {
        				log.error(e2.getMessage(), e2);
        			} catch (InterruptedException e) {
        				log.error(e.getMessage(), e);
        			} catch (IllegalStateException e) {
                        log.error("Error running progress thread.", e);
                    } finally {
                    	setButtonEnabled(true);
                    }
        			renderManager.getOnDemandRenderer(sessionId).requestRender();
        		}
        	}
        	else if (semwiqAction == false) {
        		if(mediator!=null && !mediator.isTerminated()) {
        			setPercentComplete(10);
        			try {
        				mediator.shutdown();
        				while (!mediator.isTerminated()) {
        					Thread.sleep(300);
        					renderManager.getOnDemandRenderer(sessionId).requestRender();
        				}
//        				setRestartMessage(false);
        				String extendInfo = "The SemWIQ mediator was shut down. Most of the features won't be available until the mediator will be started again!";
        				informationPopupHandler.addInformationDetails("SemWIQ Information", "SemWIQ resart necessary", extendInfo);
        				informationPopupHandler.openPopup();
        				setPercentComplete(100);
        			} catch (InterruptedException e) {
        				log.error(e.getMessage(), e);
        			} catch (IllegalStateException e) {
                        log.error("Error running progress thread.", e);
                    } finally {
                    	setButtonEnabled(true);
                    }
        			renderManager.getOnDemandRenderer(sessionId).requestRender();
        		}
        	}
        	setButtonEnabled(true);
        }
    }


	/* (non-Javadoc)
	 * @see com.icesoft.faces.async.render.Renderable#getState()
	 */
	public PersistentFacesState getState() {
		return persistentFacesState;
	}


	/* (non-Javadoc)
	 * @see com.icesoft.faces.async.render.Renderable#renderingException(com.icesoft.faces.webapp.xmlhttp.RenderingException)
	 */
	public void renderingException(RenderingException arg0) {
	}
}
