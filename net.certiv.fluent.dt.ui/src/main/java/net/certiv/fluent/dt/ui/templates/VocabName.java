package net.certiv.fluent.dt.ui.templates;

import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import net.certiv.dsl.core.model.ICodeUnit;
import net.certiv.dsl.core.util.Chars;
import net.certiv.dsl.ui.editor.text.completion.DslTemplateContext;

public class VocabName extends TemplateVariableResolver {

	public VocabName() {
		super();
	}

	public VocabName(String type, String description) {
		super(type, description);
	}

	@Override
	protected boolean isUnambiguous(TemplateContext context) {
		return resolve(context) != null;
	}

	@Override
	protected String resolve(TemplateContext context) {
		ICodeUnit unit = ((DslTemplateContext) context).getSourceModule();
		String vocabName = null;
		if (unit != null) {
			vocabName = unit.getElementName();
			if (vocabName != null && vocabName.lastIndexOf(Chars.DOT) > 0) {
				vocabName = vocabName.substring(0, vocabName.lastIndexOf(Chars.DOT));
			}
		}
		return vocabName;
	}
}
