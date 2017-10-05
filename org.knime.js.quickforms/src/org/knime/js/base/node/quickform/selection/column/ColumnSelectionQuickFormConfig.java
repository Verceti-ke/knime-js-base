/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME GmbH, Konstanz, Germany
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
 * ---------------------------------------------------------------------
 *
 * History
 *   Jun 13, 2014 (winter): created
 */
package org.knime.js.base.node.quickform.selection.column;

import java.util.Arrays;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.base.dialog.selection.single.SingleSelectionComponentFactory;
import org.knime.js.base.node.quickform.QuickFormFlowVariableConfig;

/**
 * The config for the column selection quick form node.
 *
 * @author Patrick Winter, KNIME.com AG, Zurich, Switzerland
 */
public class ColumnSelectionQuickFormConfig extends QuickFormFlowVariableConfig<ColumnSelectionQuickFormValue> {

    private static final String CFG_POSSIBLE_COLUMNS = "possibleColumns";

    private static final String[] DEFAULT_POSSIBLE_COLUMNS = new String[0];

    private String[] m_possibleColumns = DEFAULT_POSSIBLE_COLUMNS;

    private static final String CFG_TYPE = "type";

    private static final String DEFAULT_TYPE = SingleSelectionComponentFactory.DROPDOWN;

    private String m_type = DEFAULT_TYPE;

    private static final String CFG_LIMIT_NUMBER_VIS_OPTIONS = "limit_number_visible_options";
    private static final boolean DEFAULT_LIMIT_NUMBER_VIS_OPTIONS = false;
    private boolean m_limitNumberVisOptions = DEFAULT_LIMIT_NUMBER_VIS_OPTIONS;
    private static final String CFG_NUMBER_VIS_OPTIONS = "number_visible_options";
    private static final Integer DEFAULT_NUMBER_VIS_OPTIONS = 10;
    private Integer m_numberVisOptions = DEFAULT_NUMBER_VIS_OPTIONS;

    /**
     * @return the possibleColumns
     */
    String[] getPossibleColumns() {
        return m_possibleColumns;
    }

    /**
     * @param possibleColumns the possibleColumns to set
     */
    void setPossibleColumns(final String[] possibleColumns) {
        m_possibleColumns = possibleColumns;
    }

    /**
     * @return the type
     */
    String getType() {
        return m_type;
    }

    /**
     * @param type the type to set
     */
    void setType(final String type) {
        m_type = type;
    }

    /**
     * @return the limitNumberVisOptions
     */
    public boolean getLimitNumberVisOptions() {
        return m_limitNumberVisOptions;
    }

    /**
     * @param limitNumberVisOptions the limitNumberVisOptions to set
     */
    public void setLimitNumberVisOptions(final boolean limitNumberVisOptions) {
        m_limitNumberVisOptions = limitNumberVisOptions;
    }

    /**
     * @return the numberVisOptions
     */
    public Integer getNumberVisOptions() {
        return m_numberVisOptions;
    }

    /**
     * @param numberVisOptions the numberVisOptions to set
     */
    public void setNumberVisOptions(final Integer numberVisOptions) {
        m_numberVisOptions = numberVisOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSettings(final NodeSettingsWO settings) {
        super.saveSettings(settings);
        settings.addStringArray(CFG_POSSIBLE_COLUMNS, m_possibleColumns);
        settings.addString(CFG_TYPE, m_type);
        settings.addBoolean(CFG_LIMIT_NUMBER_VIS_OPTIONS, m_limitNumberVisOptions);
        settings.addInt(CFG_NUMBER_VIS_OPTIONS, m_numberVisOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadSettings(settings);
        m_possibleColumns = settings.getStringArray(CFG_POSSIBLE_COLUMNS);
        m_type = settings.getString(CFG_TYPE);

        // added with 3.3
        m_limitNumberVisOptions = settings.getBoolean(CFG_LIMIT_NUMBER_VIS_OPTIONS, DEFAULT_LIMIT_NUMBER_VIS_OPTIONS);
        m_numberVisOptions = settings.getInt(CFG_NUMBER_VIS_OPTIONS, DEFAULT_NUMBER_VIS_OPTIONS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsInDialog(final NodeSettingsRO settings) {
        super.loadSettingsInDialog(settings);
        m_possibleColumns = settings.getStringArray(CFG_POSSIBLE_COLUMNS, DEFAULT_POSSIBLE_COLUMNS);
        m_type = settings.getString(CFG_TYPE, DEFAULT_TYPE);

        // added with 3.3
        m_limitNumberVisOptions = settings.getBoolean(CFG_LIMIT_NUMBER_VIS_OPTIONS, DEFAULT_LIMIT_NUMBER_VIS_OPTIONS);
        m_numberVisOptions = settings.getInt(CFG_NUMBER_VIS_OPTIONS, DEFAULT_NUMBER_VIS_OPTIONS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ColumnSelectionQuickFormValue createEmptyValue() {
        return new ColumnSelectionQuickFormValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", ");
        sb.append("possibleColumns=");
        sb.append(Arrays.toString(m_possibleColumns));
        sb.append(", ");
        sb.append("type=");
        sb.append(m_type);
        sb.append(", ");
        sb.append("m_limitNumberVisOptions=");
        sb.append(m_limitNumberVisOptions);
        sb.append(", ");
        sb.append("m_numberVisOptions=");
        sb.append(m_numberVisOptions);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode())
                .append(m_possibleColumns)
                .append(m_type)
                .append(m_limitNumberVisOptions)
                .append(m_numberVisOptions)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ColumnSelectionQuickFormConfig other = (ColumnSelectionQuickFormConfig)obj;
        return new EqualsBuilder().appendSuper(super.equals(obj))
                .append(m_possibleColumns, other.m_possibleColumns)
                .append(m_type, other.m_type)
                .append(m_limitNumberVisOptions, other.m_limitNumberVisOptions)
                .append(m_numberVisOptions, other.m_numberVisOptions)
                .isEquals();
    }

}
