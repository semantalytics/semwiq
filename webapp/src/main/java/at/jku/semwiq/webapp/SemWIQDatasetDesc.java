package at.jku.semwiq.webapp;

import org.joseki.DatasetDesc;
import org.joseki.Service;

import at.jku.semwiq.mediator.dataset.SemWIQDataset;

import com.hp.hpl.jena.query.Dataset;

public class SemWIQDatasetDesc extends DatasetDesc {
	private final SemWIQDataset ds;
	
	public SemWIQDatasetDesc(SemWIQDataset ds) {
		super(null);
		this.ds = ds;
	}
	
	@Override
	public void initialize(Service arg0, String arg1) {
		// do nothing
	}
	
	@Override
	public Dataset acquireDataset() {
		return ds;
	}
	
}
