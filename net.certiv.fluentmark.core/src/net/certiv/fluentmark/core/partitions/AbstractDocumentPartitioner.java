package net.certiv.fluentmark.core.partitions;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;


public abstract class AbstractDocumentPartitioner implements IFluentDocumentPartitioner {
	
	private IPartitionTokenScanner partitionScanner;
	
	protected final IPartitionTokenScanner getPartitionScanner() {
		if (partitionScanner == null) {
			partitionScanner = createPartitionScanner();
		}
		return partitionScanner;
	}
	
	protected final IDocumentPartitioner createDocumentPartitioner() {
		IPartitionTokenScanner scanner = getPartitionScanner();
		return new FastPartitioner(scanner, getLegalContentTypes());
	}
	
	protected abstract IPartitionTokenScanner createPartitionScanner();
	
	public abstract String[] getLegalContentTypes();
	
	@Override
	public final void setupDocumentPartitioner(IDocument document) {
		if (document == null) {
			throw new IllegalArgumentException();
		}
		
		IDocumentPartitioner partitioner = FluentPartitioningTools.getDocumentPartitioner(document, getSupportedPartitioning());
		
		if (partitioner == null) {
			partitioner = createDocumentPartitioner();
			FluentPartitioningTools.setupDocumentPartitioner(document, partitioner, getSupportedPartitioning());
		}
	}

	@Override
	public final ITypedRegion[] computePartitioning(IDocument document) {
		return FluentPartitioningTools.computePartitions(document, getSupportedPartitioning());
	}

}
