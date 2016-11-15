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
 */
package org.knime.js.base.node.quickform.selection.multiple;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.js.base.dialog.selection.multiple.MultipleSelectionsComponentFactory;
import org.knime.js.base.node.quickform.QuickFormNodeDialog;

/**
 * The dialog for the muliple selections quick form node.
 *
 * @author Patrick Winter, KNIME.com, Zurich, Switzerland
 */
@SuppressWarnings({"rawtypes", "unchecked", "deprecation" })
public class MultipleSelectionQuickFormNodeDialog extends QuickFormNodeDialog {

    private final JList m_defaultField;

    private final JTextArea m_possibleChoicesField;

    private final JComboBox m_type;

    private final JCheckBox m_limitNumberVisOptionsBox;
    private final JSpinner m_numberVisOptionSpinner;

    private MultipleSelectionQuickFormConfig m_config;

    /**
     * Constructors, inits fields calls layout routines.
     */
    MultipleSelectionQuickFormNodeDialog() {
        m_config = new MultipleSelectionQuickFormConfig();
        m_defaultField = new JList();
        m_defaultField.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_possibleChoicesField = new JTextArea();
        m_possibleChoicesField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(final DocumentEvent e) {
                refreshChoices();
            }
            @Override
            public void insertUpdate(final DocumentEvent e) {
                refreshChoices();
            }
            @Override
            public void changedUpdate(final DocumentEvent e) {
                refreshChoices();
            }
        });
        m_type = new JComboBox(MultipleSelectionsComponentFactory.listMultipleSelectionsComponents());
        m_limitNumberVisOptionsBox = new JCheckBox();
        m_numberVisOptionSpinner = new JSpinner(new SpinnerNumberModel(10, 1, Integer.MAX_VALUE, 1));
        createAndAddTab();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void fillPanel(final JPanel panelWithGBLayout, final GridBagConstraints gbc) {
        GridBagConstraints gbc2 = (GridBagConstraints)gbc.clone();
        gbc2.fill = GridBagConstraints.BOTH;
        gbc2.weighty = 1;
        Dimension prefSize = new Dimension(DEF_TEXTFIELD_WIDTH, 70);
        addPairToPanel("Selection Type: ", m_type, panelWithGBLayout, gbc);
        JScrollPane choicesPane = new JScrollPane(m_possibleChoicesField);
        choicesPane.setPreferredSize(prefSize);
        addPairToPanel("Possible Choices: ", choicesPane, panelWithGBLayout, gbc2);
        JScrollPane defaultPane = new JScrollPane(m_defaultField);
        defaultPane.setPreferredSize(prefSize);
        addPairToPanel("Default Values: ", defaultPane, panelWithGBLayout, gbc2);

        m_type.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                boolean enabled = MultipleSelectionsComponentFactory.LIST.equals(m_type.getSelectedItem()) || MultipleSelectionsComponentFactory.TWINLIST.equals(m_type.getSelectedItem());
                m_limitNumberVisOptionsBox.setEnabled(enabled);
                m_numberVisOptionSpinner.setEnabled(enabled && m_limitNumberVisOptionsBox.isSelected());
            }
        });
        addPairToPanel("Limit number of visible options: ", m_limitNumberVisOptionsBox, panelWithGBLayout, gbc);
        m_limitNumberVisOptionsBox.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                m_numberVisOptionSpinner.setEnabled(m_limitNumberVisOptionsBox.isSelected());
            }
        });
        addPairToPanel("Number of visible options: ", m_numberVisOptionSpinner, panelWithGBLayout, gbc);
    }

    /**
     * Refreshes the default and value fields based on changes in the current
     * choices, while keeping the selection.
     */
    private void refreshChoices() {
        refreshChoices(m_defaultField);
    }

    /**
     * Refreshes the given list based on changes in the current
     * choices, while keeping the selection.
     *
     * @param list The list that will be refreshed
     */
    private void refreshChoices(final JList list) {
        List<String> selections = (List)Arrays.asList(list.getSelectedValues());
        list.setListData(m_possibleChoicesField.getText().split("\n"));
        setSelections(list, selections);
    }

    /**
     * Sets the selections in the given list to the given selections.
     *
     * @param list The list where the selections will be applied
     * @param selections The new selections
     */
    private void setSelections(final JList list, final List<String> selections) {
        List<Integer> indices = new ArrayList<Integer>(selections.size());
        ListModel model = list.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (selections.contains(model.getElementAt(i))) {
                indices.add(i);
            }
        }
        list.setSelectedIndices(ArrayUtils.toPrimitive(indices.toArray(new Integer[indices.size()])));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadSettingsFrom(final NodeSettingsRO settings, final PortObjectSpec[] specs)
            throws NotConfigurableException {
        m_config.loadSettingsInDialog(settings);
        super.loadSettingsFrom(m_config);
        m_possibleChoicesField.setText(StringUtils.join(m_config.getPossibleChoices(), "\n"));
        m_type.setSelectedItem(m_config.getType());
        setSelections(m_defaultField, Arrays.asList(m_config.getDefaultValue().getVariableValue()));
        m_limitNumberVisOptionsBox.setSelected(m_config.getLimitNumberVisOptions());
        m_numberVisOptionSpinner.setValue(m_config.getNumberVisOptions());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void saveSettingsTo(final NodeSettingsWO settings) throws InvalidSettingsException {
        saveSettingsTo(m_config);
        m_config.getDefaultValue().setVariableValue(
            (Arrays.asList(m_defaultField.getSelectedValues())).toArray(new String[0]));
        String possibleChoices = m_possibleChoicesField.getText();
        m_config.setPossibleChoices(possibleChoices.isEmpty() ? new String[0] : possibleChoices.split("\n"));
        m_config.setType((String)m_type.getItemAt(m_type.getSelectedIndex()));
        m_config.setLimitNumberVisOptions(m_limitNumberVisOptionsBox.isSelected());
        m_config.setNumberVisOptions((Integer)m_numberVisOptionSpinner.getValue());
        m_config.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getValueString(final NodeSettingsRO settings) throws InvalidSettingsException {
        MultipleSelectionQuickFormValue value = new MultipleSelectionQuickFormValue();
        value.loadFromNodeSettings(settings);
        return StringUtils.join(value.getVariableValue(), ", ");
    }

}
