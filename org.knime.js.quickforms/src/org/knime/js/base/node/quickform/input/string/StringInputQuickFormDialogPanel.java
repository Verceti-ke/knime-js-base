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
 *   Oct 14, 2013 (Patrick Winter, KNIME AG, Zurich, Switzerland): created
 */
package org.knime.js.base.node.quickform.input.string;

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.knime.core.node.InvalidSettingsException;
import org.knime.js.base.node.quickform.QuickFormDialogPanel;
import org.knime.js.base.node.quickform.QuickFormNodeDialog;

/**
 * The sub node dialog panel for the string input quick form node.
 *
 * @author Patrick Winter, KNIME AG, Zurich, Switzerland
 */
@SuppressWarnings("serial")
public class StringInputQuickFormDialogPanel extends QuickFormDialogPanel<StringInputQuickFormValue> {

    private JTextComponent m_component;


    /**
     * @param representation The dialog representation
     *
     */
    public StringInputQuickFormDialogPanel(final StringInputQuickFormRepresentation representation) {
        super(representation.getDefaultValue());
        if (representation.getEditorType().equals(StringInputQuickFormConfig.EDITOR_TYPE_SINGLE_LINE_STRING)) {
            m_component = new JTextField(QuickFormNodeDialog.DEF_TEXTFIELD_WIDTH);
        } else {
            // Multi-line editor
            m_component = new JTextArea(5, QuickFormNodeDialog.DEF_TEXTFIELD_WIDTH);
        }
        m_component.setText(representation.getDefaultValue().getString());
        setComponent(m_component);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringInputQuickFormValue createNodeValue() throws InvalidSettingsException {
        StringInputQuickFormValue value = new StringInputQuickFormValue();
        value.setString(m_component.getText());
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadNodeValue(final StringInputQuickFormValue value) {
        super.loadNodeValue(value);
        if (value != null) {
            m_component.setText(value.getString());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(final boolean enabled) {
        super.setEnabled(enabled);
        m_component.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void resetToDefault() {
        m_component.setText(getDefaultValue().getString());
    }

}
