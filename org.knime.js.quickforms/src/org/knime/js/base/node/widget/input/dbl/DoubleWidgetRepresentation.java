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
 *   22 May 2019 (albrecht): created
 */
package org.knime.js.base.node.widget.input.dbl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.js.base.node.widget.AbstractWidgetNodeRepresentation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The representation for the double widget node
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
@JsonAutoDetect
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class DoubleWidgetRepresentation
    extends AbstractWidgetNodeRepresentation<DoubleWidgetValue, DoubleWidgetConfig> {

    private final boolean m_useMin;
    private final boolean m_useMax;
    private final double m_min;
    private final double m_max;

    @JsonCreator
    private DoubleWidgetRepresentation(@JsonProperty("label") final String label,
        @JsonProperty("description") final String description, @JsonProperty("required") final boolean required,
        @JsonProperty("defaultValue") final DoubleWidgetValue defaultValue,
        @JsonProperty("currentValue") final DoubleWidgetValue currentValue,
        @JsonProperty("usemin") final boolean useMin, @JsonProperty("usemax") final boolean useMax,
        @JsonProperty("min") final double min, @JsonProperty("max") final double max) {
        super(label, description, required, defaultValue, currentValue);
        m_useMin = useMin;
        m_useMax = useMax;
        m_min = min;
        m_max = max;
    }

    /**
     * @param currentValue the value currently used by the node
     * @param config the config of the node
     */
    public DoubleWidgetRepresentation(final DoubleWidgetValue currentValue, final DoubleWidgetConfig config) {
        super(currentValue, config);
        m_useMin = config.isUseMin();
        m_useMax = config.isUseMax();
        m_min = config.getMin();
        m_max = config.getMax();
    }

    /**
     * @return the useMin
     */
    public boolean isUseMin() {
        return m_useMin;
    }

    /**
     * @return the useMax
     */
    public boolean isUseMax() {
        return m_useMax;
    }

    /**
     * @return the min
     */
    public double getMin() {
        return m_min;
    }

    /**
     * @return the max
     */
    public double getMax() {
        return m_max;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", ");
        sb.append("useMin=");
        sb.append(m_useMin);
        sb.append(", ");
        sb.append("useMax=");
        sb.append(m_useMax);
        sb.append(", ");
        sb.append("min=");
        sb.append(m_min);
        sb.append(", ");
        sb.append("max=");
        sb.append(m_max);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .appendSuper(super.hashCode())
            .append(m_useMin)
            .append(m_useMax)
            .append(m_min)
            .append(m_max)
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
        DoubleWidgetRepresentation other = (DoubleWidgetRepresentation)obj;
        return new EqualsBuilder()
            .appendSuper(super.equals(obj))
            .append(m_useMin, other.m_useMin)
            .append(m_useMax, other.m_useMax)
            .append(m_min, other.m_min)
            .append(m_max, other.m_max)
            .isEquals();
    }

}
