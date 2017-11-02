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
 * History:
 * 3-July-2012: created
 *
 */
package org.knime.quickform.nodes.in.selection.value;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.quickform.AbstractQuickFormValueInConfiguration;

/**
 * Configuration to column selection input node.
 * @author Dominik Morent, KNIME AG, Zurich, Switzerland
 * @since 2.6
 */
final class ValueFilterInputQuickFormValueInConfiguration
    extends AbstractQuickFormValueInConfiguration {
    private String m_column;
    private String[] m_values;
    private boolean m_lockColumn;

    /** @return the value */
    String[] getValues() {
        return m_values;
    }
    /** @param values the values to set */
    void setValues(final String[] values) {
        if (values != null) {
            m_values = values.clone();
        } else {
            m_values = new String[0];
        }
    }

    /**
     * @return the column
     */
    public String getColumn() {
        return m_column;
    }
    /**
     * @param column the column to set
     */
    public void setColumn(final String column) {
        m_column = column;
    }

    /**
     * @return the lockColumn
     */
    public boolean getLockColumn() {
        return m_lockColumn;
    }

    /**
     * @param lockColumn the lockColumn to set
     */
    public void setLockColumn(final boolean lockColumn) {
        m_lockColumn = lockColumn;
    }

    /** {@inheritDoc} */
    @Override
    public void saveValue(final NodeSettingsWO settings) {
        settings.addString("column", m_column);
        settings.addStringArray("values", m_values);
        settings.addBoolean("lockColumn", m_lockColumn);
    }

    /** {@inheritDoc} */
    @Override
    public void loadValueInModel(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        m_column = settings.getString("column");
        m_values = settings.getStringArray("values");
        m_lockColumn = settings.getBoolean("lockColumn", false);
    }

    /** {@inheritDoc} */
    @Override
    public void loadValueInDialog(final NodeSettingsRO settings) {
        m_column = settings.getString("column", (String) null);
        m_values = settings.getStringArray("values", (String) null);
        m_lockColumn = settings.getBoolean("lockColumn", false);
    }
}
