/*
 * This work is made available under the terms of the Eclipse Public License (EPL) Version 1.0.
 * The EPL 1.0 accompanies this distribution.
 * 
 * You may obtain a copy of the License at
 * https://www.eclipse.org/org/documents/epl-v10.html
 * 
 * Copyright © 2022-2023 Advantest Europe GmbH. All rights reserved.
 */
package net.certiv.fluentmark.ui.validation;

import org.eclipse.core.resources.IFile;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;

import java.util.ArrayList;

import net.certiv.fluentmark.ui.Log;

public class JavaCodeMemberResolver {
	
	public ICompilationUnit findJavaCompilationUnitFor(IFile javaFile) {
		IJavaElement element = JavaCore.create(javaFile);
		
		if (element == null || !(element instanceof ICompilationUnit)) {
			return null;
		}
		
		return (ICompilationUnit) element;
	}
	
	public IMember findJavaMember(IFile javaFile, String memberReference) {
		if (javaFile == null || memberReference == null || memberReference.isBlank()) {
			return null;
		}
		
		ICompilationUnit compilationUnit = findJavaCompilationUnitFor(javaFile);
		
		if (compilationUnit == null) {
			return null;
		}
		
		IType primaryType = compilationUnit.findPrimaryType();
		
		if (primaryType == null) {
			return null;
		}
		
		if (memberReference.contains("(")) {
			// memberReference is in this case the method signature, e.g. "doSomething(String, boolean, int)"
			return findMethod(primaryType, memberReference);
		} else {
			// memberReference is in this case the field name
			return primaryType.getField(memberReference);
		}
	}
	
	private IMethod findMethod(IType parentType, String methodSignature) {
		String[] signatureParts = methodSignature.split("[\\(\\)]");
		String methodName = signatureParts[0];
		String methodParametersPart = signatureParts.length > 1 ? signatureParts[1] : "";
		//String[] methodParameters = methodParametersPart.split(",\\s*");
		String[] methodParameters = parseMethodParameters(methodParametersPart);
		int numParams = methodParameters.length;
		
		try {
			for (IMethod method: parentType.getMethods()) {
				if (method.getElementName().equals(methodName)
						&& method.getNumberOfParameters() == numParams) {
					
					String[] parameterTypes = method.getParameterTypes();
					boolean equalParameterTypes = true;
					for (int i = 0; equalParameterTypes && i < parameterTypes.length; i++) {
						String simpleType = Signature.getSignatureSimpleName(parameterTypes[i]);
						if (!methodParameters[i].equals(simpleType)) {
							equalParameterTypes = false;
						}
					}
					
					if (equalParameterTypes) {
						return method;
					}
				}
			}
		} catch (JavaModelException e) {
			Log.log(IStatus.WARNING, -1, "Could not access methods in type " + parentType.getFullyQualifiedName(), e);
		}
		
		return null;
	}
	
	/*
	 * Parses parameter type lists like
	 * (int, boolean, Character[], List<Map<K,V>>)
	 * 
	 * Such list are common in method signatures as the are used in Javadoc links, e.g.
	 * {@link ClassName#getSomething(int, boolean, Character[], List<Map<K,V>>)}
	 * 
	 * We use the same syntax in Markdown to point to a certain method in a Java class.
	 * 
	 * String.split(",") does not work with string like in the example above, since commas
	 * are also contained in parameter types like List<Map<K,V>>. Thus, we have to
	 * do a more complicated parsing of the parameter list.
	 */
	private String[] parseMethodParameters(String methodParametersList) {
		ArrayList<String> parameters = new ArrayList<>();
		int pos = 0;
		int begin = 0;
		int countTypeParamBrackets = 0;
		String param;
		
		while (pos < methodParametersList.length()) {
			char currentChar = methodParametersList.charAt(pos);
			
			if (currentChar == ','
					&& countTypeParamBrackets == 0) {
				if (pos == 0) {
					return null;
				}
				
				param = methodParametersList.substring(begin, pos);
				parameters.add(param);
				begin = pos + 1;
			} else if (currentChar == ' '
					&& countTypeParamBrackets == 0) {
				begin = pos +1;
			} else if (currentChar == '<') {
				countTypeParamBrackets++;
			} else if (currentChar == '>') {
				countTypeParamBrackets--;
			}
			
			if (countTypeParamBrackets < 0) {
				return null;
			}
			
			pos++;
		}
		
		if (begin < methodParametersList.length()) {
			param = methodParametersList.substring(begin, methodParametersList.length());
			parameters.add(param);
		}
		
		return parameters.toArray(new String[parameters.size()]);
	}

}
