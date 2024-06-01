package net.certiv.fluent.dt.core.lang.md;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.certiv.common.diff.Differ;
import net.certiv.common.util.test.CommonTestBase;
import net.certiv.fluent.dt.core.lang.MdSupport;
import net.certiv.fluent.dt.core.lang.md.gen.MdParser.PageContext;

class LinkParseTest extends CommonTestBase {

	static final boolean FORCE = false;
	static final MdSupport MS = new MdSupport();

	@BeforeEach
	void setup() {
		MS.setup();
	}

	@Test
	void linkSimpleTest() {
		String name = "link_simple";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkAutoTest() {
		String name = "link_auto";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkDefTest() {
		String name = "link_def";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkParaTest() {
		String name = "link_para";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkFootnoteTest() {
		String name = "link_footnote";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkImageTest() {
		String name = "link_image";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkInlineTest() {
		String name = "link_inline";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkRefTest() {
		String name = "link_ref";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkRelTest() {
		String name = "link_rel";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkSelfTest() {
		String name = "link_self";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkTitleTest() {
		String name = "link_title";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}

	@Test
	void linkAllTest() {
		String name = "link_all";
		String src = name + ".md";
		String tgt = name + ".tree.txt";

		CommonTokenStream ts = MS.createMdTokenStream(getClass(), src, true);
		PageContext page = MS.createMdParserTree(ts);
		String tree = MS.renderTree(name, page);
		writeResource(getClass(), tgt, tree, FORCE);
		String txt = loadResource(getClass(), tgt);
		Differ.diff(name, txt, tree).sdiff(true, 200).out();

		assertEquals(txt, tree);
	}
}
