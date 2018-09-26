/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ------------------------------------------------------------------------
 *
 * History
 *   05.05.2014 (Christian Albrecht, KNIME AG, Zurich, Switzerland): created
 */
package org.knime.js.base.node.ui;

import java.awt.Color;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.knime.core.node.util.rsyntaxtextarea.guarded.GuardedDocument;
import org.knime.core.node.util.rsyntaxtextarea.guarded.GuardedSection;
import org.knime.core.node.util.rsyntaxtextarea.guarded.GuardedSectionsFoldParser;
import org.knime.js.base.node.css.editor.autocompletion.KnimeCssLanguageSupport;
import org.knime.js.base.node.css.editor.guarded.CssSnippetDocument;

/**
 *
 * @author Christian Albrecht, KNIME AG, Zurich, Switzerland, University of Konstanz
 */
@SuppressWarnings("serial")
public class CSSSnippetTextArea extends RSyntaxTextArea {

    /**
     *
     */
    public CSSSnippetTextArea() {
        super(20,60);
        boolean parserInstalled = FoldParserManager.get().getFoldParser(
            SYNTAX_STYLE_CSS) instanceof GuardedSectionsFoldParser;
        if (!parserInstalled) {
            FoldParserManager.get().addFoldParserMapping(SYNTAX_STYLE_CSS,new GuardedSectionsFoldParser());
        }
        setDocument(new CssSnippetDocument());
        setCodeFoldingEnabled(true);
        setSyntaxEditingStyle(SYNTAX_STYLE_CSS);
        setAntiAliasingEnabled(true);

        KnimeCssLanguageSupport cssLangSup = new KnimeCssLanguageSupport();
        cssLangSup.install(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getForegroundForToken(final Token t) {
        if (isInGuardedSection(t.getOffset())) {
            return Color.gray;
        } else {
            return super.getForegroundForToken(t);
        }
    }

    /**
     * Returns true when offset is within a guarded section.
     *
     * @param offset the offset to test
     * @return true when offset is within a guarded section.
     */
    private boolean isInGuardedSection(final int offset) {
        GuardedDocument doc = (GuardedDocument)getDocument();

        for (String name : doc.getGuardedSections()) {
            GuardedSection gs = doc.getGuardedSection(name);
            if (gs.contains(offset)) {
                return true;
            }
        }
        return false;
    }
}
