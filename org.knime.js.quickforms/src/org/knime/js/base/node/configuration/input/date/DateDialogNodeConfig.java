/*
 * ------------------------------------------------------------------------
 *
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
 * ---------------------------------------------------------------------
 *
 * History
 *   23 May 2019 (albrecht): created
 */
package org.knime.js.base.node.configuration.input.date;

import java.time.ZonedDateTime;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.base.node.base.date.DateNodeConfig;
import org.knime.js.base.node.base.date.GranularityTime;
import org.knime.js.base.node.configuration.LabeledFlowVariableDialogNodeConfig;
import org.knime.time.util.DateTimeType;

/**
 * The config for the date configuration node
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
public class DateDialogNodeConfig extends LabeledFlowVariableDialogNodeConfig<DateDialogNodeValue> {

    private final DateNodeConfig m_config;

    /**
     * Instantiate a new config object
     */
    public DateDialogNodeConfig() {
        m_config = new DateNodeConfig();
    }

    /**
     * @return the dateNodeConfig
     */
    public DateNodeConfig getDateNodeConfig() {
        return m_config;
    }

    /**
     * @return the showNow
     */
    public boolean isShowNowButton() {
        return m_config.isShowNowButton();
    }

    /**
     * @param showNow the showNow to set
     */
    public void setShowNowButton(final boolean showNow) {
        m_config.setShowNowButton(showNow);
    }

    /**
     * @return the granularity
     */
    public GranularityTime getGranularity() {
        return m_config.getGranularity();
    }

    /**
     * @param granularity the granularity to set
     */
    public void setGranularity(final GranularityTime granularity) {
        m_config.setGranularity(granularity);
    }

    /**
     * @return the useMin
     */
    public boolean isUseMin() {
        return m_config.isUseMin();
    }

    /**
     * @param useMin the useMin to set
     */
    public void setUseMin(final boolean useMin) {
        m_config.setUseMin(useMin);
    }

    /**
     * @return the useMax
     */
    public boolean isUseMax() {
        return m_config.isUseMax();
    }

    /**
     * @param useMax the useMax to set
     */
    public void setUseMax(final boolean useMax) {
        m_config.setUseMax(useMax);
    }

    /**
     * @return the useMinExecTime
     */
    public boolean isUseMinExecTime() {
        return m_config.isUseMinExecTime();
    }

    /**
     * @param useMinExecTime the useMinExecTime to set
     */
    public void setUseMinExecTime(final boolean useMinExecTime) {
        m_config.setUseMinExecTime(useMinExecTime);
    }

    /**
     * @return the useMaxExecTime
     */
    public boolean isUseMaxExecTime() {
        return m_config.isUseMaxExecTime();
    }

    /**
     * @param useMaxExecTime the useMaxExecTime to set
     */
    public void setUseMaxExecTime(final boolean useMaxExecTime) {
        m_config.setUseMaxExecTime(useMaxExecTime);
    }

    /**
     * @return the useDefaultExecTime
     */
    public boolean isUseDefaultExecTime() {
        return m_config.isUseDefaultExecTime();
    }

    /**
     * @param useDefaultExecTime the useDefaultExecTime to set
     */
    public void setUseDefaultExecTime(final boolean useDefaultExecTime) {
        m_config.setUseDefaultExecTime(useDefaultExecTime);
    }

    /**
     * @return the min
     */
    public ZonedDateTime getMin() {
        return m_config.getMin();
    }

    /**
     * @param min the min to set
     */
    public void setMin(final ZonedDateTime min) {
        m_config.setMin(min);
    }

    /**
     * @return the max
     */
    public ZonedDateTime getMax() {
        return m_config.getMax();
    }

    /**
     * @param max the max to set
     */
    public void setMax(final ZonedDateTime max) {
        m_config.setMax(max);
    }

    /**
     * @return the type
     */
    public DateTimeType getType() {
        return m_config.getType();
    }

    /**
     * @param withTime the withTime to set
     */
    public void setType(final DateTimeType withTime) {
        m_config.setType(withTime);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected DateDialogNodeValue createEmptyValue() {
        return new DateDialogNodeValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveSettings(final NodeSettingsWO settings) {
        super.saveSettings(settings);
        m_config.saveSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadSettings(settings);
        m_config.loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsInDialog(final NodeSettingsRO settings) {
        super.loadSettingsInDialog(settings);
        m_config.loadSettingsInDialog(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", ");
        sb.append(m_config.toString());
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(m_config)
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
        DateDialogNodeConfig other = (DateDialogNodeConfig)obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(m_config, other.m_config)
                .isEquals();
    }

}