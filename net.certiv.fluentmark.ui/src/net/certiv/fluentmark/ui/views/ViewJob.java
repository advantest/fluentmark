package net.certiv.fluentmark.ui.views;

import org.eclipse.ui.IEditorInput;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import org.apache.commons.lang3.StringEscapeUtils;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.widgets.Display;

import java.net.URISyntaxException;

import java.io.IOException;

import java.math.BigDecimal;

import net.certiv.fluentmark.convert.Kind;
import net.certiv.fluentmark.core.util.FileUtils;
import net.certiv.fluentmark.core.util.Strings;
import net.certiv.fluentmark.ui.FluentUI;
import net.certiv.fluentmark.ui.Log;
import net.certiv.fluentmark.ui.editor.FluentEditor;
import net.certiv.fluentmark.ui.preferences.Prefs;

public class ViewJob extends Job {

	private static final String Render = "Fluent.set('%s');";
	private static final String CMD_SCROLL_TO = "Fluent.scrollTo('%s');";
	
	private String currentAnchorToScrollTo;
	private String previewContents;

	private enum State {
		NONE,
		LOAD,
		READY,
		TYPESET;
	}
	
	private boolean updateImagesOnly = false;
	
	void setAnchorForNextPageLoad(String anchor) {
		this.currentAnchorToScrollTo = anchor;
	}

	private ProgressListener watcher = new ProgressAdapter() {

		@Override
		public void completed(ProgressEvent event) {
			done();
		}
	};

	private FluentPreview view;
	private Browser browser;
	private IPreferenceStore store;
	private DoneFunction func;

	private State state = State.NONE;
	private long timer;

	private boolean mathjax;

	public ViewJob(FluentPreview view) {
		super("Preview");
		this.view = view;
		this.browser = view.getBrowser();
		this.store = FluentUI.getDefault().getPreferenceStore();

		load();
	}
	
	public String getHtmlViewContents() {
		return this.previewContents;
	}

	public boolean load() {
		return load(false);
	}

	public boolean load(boolean firebug) {
		this.previewContents = null;
		
		FluentEditor editor = view.getActiveFluentEditor();
		if (editor == null) {
			editor = view.getLatestActiveFluentEditor();
			if (editor == null) {
				return false;
			}
		}

		IEditorInput input = editor.getEditorInput();
		if (input == null) {
			return false;
		}

		state = State.LOAD;
		
		if (editor.useMathJax()) {
			mathjax = true;
			func = new DoneFunction(browser, "typeset");
		} else {
			mathjax = false;
			func = null;
		}
		
		browser.addProgressListener(watcher);
		
		timer = System.nanoTime();
		
		String content = editor.getHtml(Kind.VIEW);
		
		if (firebug) {
			String script;
			try {
				script = FileUtils.fromBundle("resources/html/firebug.html", FluentUI.PLUGIN_ID) + Strings.EOL;
				content = content.replaceFirst("</head>", script + "</head>");
			} catch (IOException | URISyntaxException e) {
				FluentUI.log(IStatus.ERROR, "Could not load firebug.html from bundle", e);
			}
		}
		
		browser.setText(content);
		
		return true;
	}

	public void update() {
		if (state != State.READY) {
			return;
		}
		
		switch (getState()) {
			case Job.WAITING:
			case Job.RUNNING:
				schedule(store.getInt(Prefs.VIEW_UPDATE_DELAY));
				break;
			default:
				schedule(SHORT);
				break;
		}
	}
	
	private void updateImagesOnly() {
		if (state != State.READY) {
			return;
		}
		
		this.updateImagesOnly = true;
		
		schedule(store.getInt(Prefs.VIEW_UPDATE_DELAY));
	}
	
	public void scrollTo(String anchor) {
		if (state != State.READY || browser == null || browser.isDisposed()) {
			return;
		}
		
		String script = String.format(CMD_SCROLL_TO, StringEscapeUtils.escapeEcmaScript(anchor));
		
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (browser != null && !browser.isDisposed()) {
					boolean ok = browser.execute(script);
					if (!ok) {
						Log.error(String.format("JavaScript execution (scroll to anchor %s) failed.", anchor));
					}
				}
			}
		});
	}
	
	/** The job to run when scheduled */
	@Override
	protected IStatus run(IProgressMonitor monitor) {
		if (view == null) {
			return Status.CANCEL_STATUS;
		}
		
		FluentEditor editor = view.getActiveFluentEditor();
		if (editor == null) {
			editor = view.getLatestActiveFluentEditor();
		}
		
		if (editor == null || browser == null || browser.isDisposed()) {
			return Status.CANCEL_STATUS;
		}
		
		if (updateImagesOnly) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (browser != null && !browser.isDisposed()) {
						boolean ok = browser.execute("Fluent.updateImages()");
						if (!ok) {
							Log.error("JavaScript execution (image zoom styling) failed.");
						}
					}
				}
			});
			
			updateImagesOnly = false;
			return Status.OK_STATUS;
		}
		
		timer = System.nanoTime();

		String html = editor.getHtml(Kind.UPDATE);
		this.previewContents = html;
		
		if (html.isEmpty()) {
			return Status.CANCEL_STATUS;
		}

		String script = String.format(Render, StringEscapeUtils.escapeEcmaScript(html));
		if (mathjax) state = State.READY;

		// execute script on UI thread
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				if (browser != null && !browser.isDisposed()) {
					
					boolean ok = browser.execute(script);
					if (ok) {
						// read the temporarily saved anchor if any
						if (currentAnchorToScrollTo != null) {
							scrollTo(currentAnchorToScrollTo);
							
							// we've consumed the anchor, remove it now
							currentAnchorToScrollTo = null;
						}
					} else {
						Log.error("JavaScript execution (set page contents) failed.");
					}
					
					// delay update of images' styling (zoom), otherwise the image are not found within JavaScript
					updateImagesOnly();
				}
			}
		});
		
		return Status.OK_STATUS;
	}

	protected void done() {
		switch (state) {
			case LOAD:
				result("ViewJob ready");
				state = State.READY;
				update();
				break;
			default:
				state = State.READY;
				break;
		}
	}

	protected void dispose() {
		if (func != null) {
			func.dispose();
			func = null;
		}
		if (browser != null && !browser.isDisposed()) {
			browser.removeProgressListener(watcher);
		}
	}

	private void result(String msg) {
		if (timer == 0) {
			Log.info(msg);
		} else {
			long elapsed = System.nanoTime() - timer;
			String value = BigDecimal.valueOf(elapsed, 6).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
			if (value.indexOf('.') > 3) {
				Log.info(String.format("%s %s (ms): ", msg, value));
			}
		}
		timer = 0;
	}

	// called on completion of MathJax typesetting
	class DoneFunction extends BrowserFunction {

		DoneFunction(Browser browser, String name) {
			super(browser, name);
		}

		@Override
		public Object function(Object[] args) {
			Log.info(String.format("typeset(%s", args));
			if (args.length > 0 && "End".equals(args[0])) {
				state = State.READY;
				done();
			}
			return null;
		}
	}
}
