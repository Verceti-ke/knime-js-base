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
 *   Apr 17, 2014 ("Patrick Winter"): created
 */
package org.knime.js.base.dialog.selection.multiple;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.EtchedBorder;

import org.apache.commons.lang.ArrayUtils;

/**
 *
 * @author "Patrick Winter", KNIME AG, Zurich, Switzerland
 */
public class ListComponent implements MultipleSelectionsComponent {

    private static final int MIN_WIDTH = 200;

    private JList<String> m_list;

    private DefaultListModel<String> m_listModel;

    /**
     * Create ListComponent.
     */
    ListComponent() {
        m_listModel = new DefaultListModel<String>();
        m_list = new JList<String>(m_listModel);
        m_list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        m_list.setBorder(new EtchedBorder());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChoices(final String[] choices) {
        m_listModel.removeAllElements();
        m_list.setPreferredSize(null);
        for (String choice : choices) {
            m_listModel.addElement(choice);
        }
        if (m_list.getPreferredSize().width < MIN_WIDTH) {
            m_list.setPreferredSize(new Dimension(MIN_WIDTH, m_list.getPreferredSize().height));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent getComponent() {
        return m_list;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("deprecation")
    @Override
    public String[] getSelections() {
        return (Arrays.asList(m_list.getSelectedValues())).toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelections(final String[] selections) {
        List<Integer> indices = new ArrayList<Integer>(selections.length);
        List<String> selectionsList = Arrays.asList(selections);
        ListModel<String> model = m_list.getModel();
        for (int i = 0; i < model.getSize(); i++) {
            if (selectionsList.contains(model.getElementAt(i))) {
                indices.add(i);
            }
        }
        m_list.setSelectedIndices(ArrayUtils.toPrimitive(indices.toArray(new Integer[indices.size()])));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setEnabled(final boolean enabled) {
        m_list.setEnabled(enabled);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return m_list.isEnabled();
    }

}
