package net.certiv.fluentmark.core.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import net.certiv.fluentmark.core.FluentCore;
import net.certiv.fluentmark.core.partitions.IFluentDocumentPartitioner;
import net.certiv.fluentmark.core.util.FileUtils;


public class FileValidator {
	
	private final List<IFluentDocumentPartitioner> partitioners;
	private final List<ITypedRegionValidator> validators;
	private final IValidationResultConsumer validationResultConsumer;
	
	public FileValidator(List<IFluentDocumentPartitioner> partitioners, List<ITypedRegionValidator> validators,
			IValidationResultConsumer validationResultConsumer) {
		
		if (validationResultConsumer == null) {
			throw new IllegalArgumentException();
		}
		
		this.validationResultConsumer = validationResultConsumer;
		this.partitioners = partitioners;
		this.validators = validators;
	}
	
	public void performResourceValidation(IDocument document, IFile file, IProgressMonitor monitor) {
		if (file == null || monitor == null) {
			throw new IllegalArgumentException();
		}
		
		if (monitor.isCanceled()) {
			return;
		}
		
		// The file could have been deleted / moved after scheduling the marker calculation
		if (!file.exists()) {
			return;
		}
		
		List<ITypedRegionValidator> responsibleValidators = validators.stream()
			.filter(validator -> validator.isValidatorFor(file))
			.toList();
		
		if (responsibleValidators.isEmpty()) {
			return;
		}
		
		if (document == null) {
			// Sometimes file.exists() returns true, although the file does no longer exist => We refresh the file system state to avoid that.
			refreshFileStatus(file, monitor);
			
			if (!file.exists()) {
				return;
			}
			
			IDocument newDocument = readFile(file);
			
			if (newDocument == null) {
				return;
			}
			
			computePartitionsAndValidate(newDocument, file, monitor, responsibleValidators);
		} else {
			computePartitionsAndValidate(document, file, monitor, responsibleValidators);
		}
	}

	private void computePartitionsAndValidate(IDocument document, IFile file, IProgressMonitor monitor,
			List<ITypedRegionValidator> responsibleValidators) {
		
		Map<String,List<ITypedRegionValidator>> partitioningToValidatorsMap = mapValidatorsToPartitioning(responsibleValidators, file);
		
		for (String partitioning: partitioningToValidatorsMap.keySet()) {
			if (monitor.isCanceled()) {
				return;
			}
			
			ITypedRegion[] partitions = computePartitions(document, file, partitioning);
			
			if (partitions == null || (partitions.length == 0 && document.getLength() != 0)) {
				FluentCore.log(IStatus.WARNING, String.format("Could not calculate partitions for file %s.", file.getLocation().toString()));
				return;
			}
			
			for (ITypedRegionValidator validator : partitioningToValidatorsMap.get(partitioning)) {
				for (ITypedRegion region: partitions) {
					if (monitor.isCanceled()) {
						return;
					}
					
					if (validator.isValidatorFor(region, file) ) {
						validator.setValidationResultConsumer(validationResultConsumer);
						validator.validateRegion(region, document, file);
					}
				}
			}
		}
	}
	
	private void refreshFileStatus(IFile file, IProgressMonitor monitor) {
		if (file != null) {
			try {
				file.refreshLocal(IResource.DEPTH_ZERO, monitor);
			} catch (CoreException e) {
				FluentCore.log(IStatus.ERROR, "Could not refresh file status for file " + file.getLocation().toString(), e);
			}
		}
	}
	
	private IDocument readFile(IFile file) {
		try {
			String fileContents = FileUtils.readFileContents(file);
			return new Document(fileContents);
		} catch (Exception e) {
			FluentCore.log(IStatus.ERROR, "Failed reading / valdating file " + file.getLocation().toString(), e);
		}
		
		return null;
	}
	
	private Map<String,List<ITypedRegionValidator>> mapValidatorsToPartitioning(List<ITypedRegionValidator> validators, IFile file) {
		Map<String,List<ITypedRegionValidator>> partitioningToValidatorsMap = new HashMap<>();
		
		for (ITypedRegionValidator validator: validators) {
			String partitioning = validator.getRequiredPartitioning(file);
			
			if (partitioning == null || partitioning.isBlank()) {
				FluentCore.log(IStatus.ERROR, String.format("Validator %s requires illegal (empty) partitioning", validator.getClass().getName()));
				continue;
			}
			
			List<ITypedRegionValidator> validatorsForPartioning = partitioningToValidatorsMap.get(partitioning);
			if (validatorsForPartioning == null) {
				validatorsForPartioning = new ArrayList<>();
				partitioningToValidatorsMap.put(partitioning, validatorsForPartioning);
			}
			validatorsForPartioning.add(validator);
		}
		
		return partitioningToValidatorsMap;
	}
	
	private ITypedRegion[] computePartitions(IDocument document, IFile file, String partitioning) {
		Optional<IFluentDocumentPartitioner> partitonerOpt = partitioners.stream()
			.filter(partitioner -> partitioner.getSupportedPartitioning().equals(partitioning))
			.findFirst();
		
		if (partitonerOpt.isEmpty()) {
			FluentCore.log(IStatus.ERROR, "No document partitioner found for partioning \"" + partitioning + "\".");
			return null;
		}
		
		IFluentDocumentPartitioner partitioner = partitonerOpt.get();
		
		// TODO can we check if the partitioner we need is already set up?
		partitioner.setupDocumentPartitioner(document, file);
		
		return partitioner.computePartitioning(document, file);
	}

}
