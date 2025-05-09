/*******************************************************************************
 * Copyright (c) 2016 - 2017 Certiv Analytics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package net.certiv.fluentmark.ui.spell;

import org.eclipse.osgi.util.NLS;

/**
 * Helper class to get NLSed messages.
 *
 * @since 2.1
 */
public final class TextEditorMessages extends NLS {

	private static final String BUNDLE_NAME = TextEditorMessages.class.getName();

	private TextEditorMessages() {
		// Do not instantiate
	}

	public static String AnnotationsConfigurationBlock_DASHED_BOX;
	public static String EditorsPlugin_additionalInfo_affordance;
	public static String EditorsPlugin_internal_error;
	public static String LinkedModeConfigurationBlock_DASHED_BOX;
	public static String TextEditorPreferencePage_displayedTabWidth;
	public static String TextEditorPreferencePage_enableWordWrap;
	public static String TextEditorPreferencePage_convertTabsToSpaces;
	public static String TextEditorPreferencePage_undoHistorySize;
	public static String TextEditorPreferencePage_printMarginColumn;
	public static String TextEditorPreferencePage_showLineNumbers;
	public static String TextEditorPreferencePage_highlightCurrentLine;
	public static String TextEditorPreferencePage_showPrintMargin;
	public static String TextEditorPreferencePage_color;
	public static String TextEditorPreferencePage_appearanceOptions;
	public static String TextEditorPreferencePage_lineNumberForegroundColor;
	public static String TextEditorPreferencePage_currentLineHighlighColor;
	public static String TextEditorPreferencePage_printMarginColor;
	public static String TextEditorPreferencePage_foregroundColor;
	public static String TextEditorPreferencePage_backgroundColor;
	public static String TextEditorPreferencePage_findScopeColor;
	public static String TextEditorPreferencePage_accessibility_disableCustomCarets;
	public static String TextEditorPreferencePage_accessibility_wideCaret;
	public static String TextEditorPreferencePage_accessibility_useSaturatedColorsInOverviewRuler;
	public static String TextEditorPreferencePage_showAffordance;
	public static String TextEditorPreferencePage_selectionForegroundColor;
	public static String TextEditorPreferencePage_selectionBackgroundColor;
	public static String TextEditorPreferencePage_systemDefault;
	public static String TextEditorPreferencePage_invalidInput;
	public static String TextEditorPreferencePage_invalidRange;
	public static String TextEditorPreferencePage_emptyInput;
	public static String TextEditorPreferencePage_colorsAndFonts_link;
	public static String TextEditorPreferencePage_Font_link;
	public static String QuickDiffConfigurationBlock_description;
	public static String QuickDiffConfigurationBlock_referenceProviderTitle;
	public static String QuickDiffConfigurationBlock_referenceProviderNoteMessage;
	public static String QuickDiffConfigurationBlock_referenceProviderNoteTitle;
	public static String QuickDiffConfigurationBlock_characterMode;
	public static String QuickDiffConfigurationBlock_showForNewEditors;
	public static String QuickDiffConfigurationBlock_showInOverviewRuler;
	public static String QuickDiffConfigurationBlock_colorTitle;
	public static String QuickDiffConfigurationBlock_changeColor;
	public static String QuickDiffConfigurationBlock_additionColor;
	public static String QuickDiffConfigurationBlock_deletionColor;
	public static String NewTextEditorAction_namePrefix;
	public static String AnnotationsConfigurationBlock_description;
	public static String AnnotationsConfigurationBlock_showInText;
	public static String AnnotationsConfigurationBlock_showInOverviewRuler;
	public static String AnnotationsConfigurationBlock_showInVerticalRuler;
	public static String AnnotationsConfigurationBlock_isNavigationTarget;
	public static String AnnotationsConfigurationBlock_annotationPresentationOptions;
	public static String AnnotationsConfigurationBlock_SQUIGGLES;
	public static String AnnotationsConfigurationBlock_PROBLEM_UNDERLINE;
	public static String AnnotationsConfigurationBlock_UNDERLINE;
	public static String AnnotationsConfigurationBlock_BOX;
	public static String AnnotationsConfigurationBlock_IBEAM;
	public static String AnnotationsConfigurationBlock_HIGHLIGHT;
	public static String AnnotationsConfigurationBlock_labels_showIn;
	public static String AnnotationsConfigurationBlock_color;
	public static String HyperlinkDetectorsConfigurationBlock_description;
	public static String HyperlinkDetectorTable_nameColumn;
	public static String HyperlinkDetectorTable_modifierKeysColumn;
	public static String HyperlinkDetectorTable_targetNameColumn;
	public static String SelectResourcesDialog_filterSelection;
	public static String SelectResourcesDialog_deselectAll;
	public static String SelectResourcesDialog_selectAll;
	public static String SelectResourcesDialog_noFilesSelected;
	public static String SelectResourcesDialog_oneFileSelected;
	public static String SelectResourcesDialog_nFilesSelected;
	public static String ConvertLineDelimitersAction_convert_all;
	public static String ConvertLineDelimitersAction_convert_text;
	public static String ConvertLineDelimitersAction_default_label;
	public static String ConvertLineDelimitersAction_dialog_title;
	public static String ConvertLineDelimitersToWindows_label;
	public static String ConvertLineDelimitersToUnix_label;
	public static String ConvertLineDelimitersAction_dialog_description;
	public static String ConvertLineDelimitersAction_nontext_selection;
	public static String ConvertLineDelimitersAction_show_only_text_files;
	public static String RemoveTrailingWhitespaceHandler_dialog_title;
	public static String RemoveTrailingWhitespaceHandler_dialog_description;
	public static String HyperlinksEnabled_label;
	public static String HyperlinkColor_label;
	public static String HyperlinkKeyModifier_label;
	public static String HyperlinkDefaultKeyModifier_label;
	public static String HyperlinkKeyModifier_error_modifierIsNotValid;
	public static String HyperlinkKeyModifier_error_shiftIsDisabled;
	public static String HyperlinkKeyModifier_delimiter;
	public static String HyperlinkKeyModifier_concatModifierStrings;
	public static String HyperlinkKeyModifier_insertDelimiterAndModifier;
	public static String HyperlinkKeyModifier_insertDelimiterAndModifierAndDelimiter;
	public static String HyperlinkKeyModifier_insertModifierAndDelimiter;
	public static String AccessibilityPreferencePage_accessibility_title;
	public static String SpellingConfigurationBlock_enable;
	public static String SpellingConfigurationBlock_combo_caption;
	public static String SpellingConfigurationBlock_info_no_preferences;
	public static String SpellingConfigurationBlock_error_not_installed;
	public static String SpellingConfigurationBlock_error_not_exist;
	public static String SpellingConfigurationBlock_error_title;
	public static String SpellingConfigurationBlock_error_message;

	static {
		NLS.initializeMessages(BUNDLE_NAME, TextEditorMessages.class);
	}

	public static String TextEditorDefaultsPreferencePage_carriageReturn;
	public static String TextEditorDefaultsPreferencePage_transparencyLevel;
	public static String TextEditorDefaultsPreferencePage_configureWhitespaceCharacterPainterProperties;
	public static String TextEditorDefaultsPreferencePage_enclosed;
	public static String TextEditorDefaultsPreferencePage_enrichHoverMode;
	public static String TextEditorDefaultsPreferencePage_enrichHover_immediately;
	public static String TextEditorDefaultsPreferencePage_enrichHover_afterDelay;
	public static String TextEditorDefaultsPreferencePage_enrichHover_disabled;
	public static String TextEditorDefaultsPreferencePage_enrichHover_onClick;
	public static String TextEditorDefaultsPreferencePage_ideographicSpace;
	public static String TextEditorDefaultsPreferencePage_leading;
	public static String TextEditorDefaultsPreferencePage_lineFeed;
	public static String TextEditorDefaultsPreferencePage_range_indicator;
	public static String TextEditorDefaultsPreferencePage_smartHomeEnd;
	public static String TextEditorDefaultsPreferencePage_warn_if_derived;
	public static String TextEditorDefaultsPreferencePage_showWhitespaceCharacters;
	public static String TextEditorDefaultsPreferencePage_showWhitespaceCharactersLinkText;
	public static String TextEditorDefaultsPreferencePage_showWhitespaceCharactersDialogInvalidInput;
	public static String TextEditorDefaultsPreferencePage_showWhitespaceCharactersDialogTitle;
	public static String TextEditorDefaultsPreferencePage_space;
	public static String TextEditorDefaultsPreferencePage_tab;
	public static String TextEditorDefaultsPreferencePage_textDragAndDrop;
	public static String TextEditorDefaultsPreferencePage_trailing;
	public static String LinkedModeConfigurationBlock_annotationPresentationOptions;
	public static String LinkedModeConfigurationBlock_SQUIGGLES;
	public static String LinkedModeConfigurationBlock_UNDERLINE;
	public static String LinkedModeConfigurationBlock_BOX;
	public static String LinkedModeConfigurationBlock_IBEAM;
	public static String LinkedModeConfigurationBlock_HIGHLIGHT;
	public static String LinkedModeConfigurationBlock_labels_showIn;
	public static String LinkedModeConfigurationBlock_color;
	public static String LinkedModeConfigurationBlock_linking_title;
}
