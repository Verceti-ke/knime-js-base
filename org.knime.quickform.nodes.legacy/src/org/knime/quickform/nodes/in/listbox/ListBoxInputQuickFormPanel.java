/*
 * ------------------------------------------------------------------------
 *  Copyright by KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
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
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
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
 *   Jun 22, 2011 (wiswedel): created
 */
package org.knime.quickform.nodes.in.listbox;

import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.quickform.QuickFormConfigurationPanel;
import org.knime.core.util.node.quickform.AbstractQuickFormElement;
import org.knime.core.util.node.quickform.in.AbstractQuickFormInElement;
import org.knime.core.util.node.quickform.in.StringListPasteboxInputQuickFormInElement;
import org.knime.quickform.nodes.in.QuickFormInNodeDialogPane;

/**
 * Panel shown in meta node dialogs, displaying string of values.
 *
 * @author Thomas Gabriel, KNIME.com, Zurich, Switzerland
 * @since 2.6
 */
public class ListBoxInputQuickFormPanel extends
    QuickFormConfigurationPanel
        <ListBoxInputQuickFormValueInConfiguration> {

    private final JTextArea m_valueField;

    /** Constructors, inits fields calls layout routines.
     * @param cfg underlying configuration object
     */
    ListBoxInputQuickFormPanel(final ListBoxInputQuickFormInConfiguration cfg) {
        super(new FlowLayout(FlowLayout.LEFT));
        String labelString = cfg.getLabel();
        JLabel label = new JLabel(labelString);
        label.setToolTipText(cfg.getDescription());
        add(label);
        m_valueField = new JTextArea(5, QuickFormInNodeDialogPane.DEF_TEXTFIELD_WIDTH);
        add(new JScrollPane(m_valueField));
        m_valueField.setToolTipText(cfg.getDescription());
        loadValueConfig(cfg.getValueConfiguration());
    }


    /** {@inheritDoc} */
    @Override
    public void saveSettings(
            final ListBoxInputQuickFormValueInConfiguration config)
            throws InvalidSettingsException {
        config.setValue(m_valueField.getText());
    }


    /** {@inheritDoc} */
    @Override
    public void loadSettings(
            final ListBoxInputQuickFormValueInConfiguration config) {
        loadValueConfig(config);
    }


    private void loadValueConfig(
            final ListBoxInputQuickFormValueInConfiguration config) {
        m_valueField.setText(config.getValue());
    }

    /** {@inheritDoc} */
    @Override
    public void updateQuickFormInElement(final AbstractQuickFormInElement e) throws InvalidSettingsException {
        StringListPasteboxInputQuickFormInElement cast =
                AbstractQuickFormElement.cast(StringListPasteboxInputQuickFormInElement.class, e);
        cast.setValue(m_valueField.getText());
    }

}
