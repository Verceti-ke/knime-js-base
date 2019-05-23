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
package org.knime.js.base.node.configuration.input.string;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.dialog.DialogNodePanel;
import org.knime.js.base.node.configuration.AbstractDialogNodeRepresentation;

/**
 * The dialog representation of the string configuration node
 *
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 */
public class StringDialogNodeRepresentation
    extends AbstractDialogNodeRepresentation<StringDialogNodeValue, StringDialogNodeConfig> {

    private final String m_regex;
    private final String m_errorMessage;
    private final String m_editorType;
    private final int m_multilineEditorWidth;
    private final int m_multilineEditorHeight;

    /**
     * @param currentValue The value currently used by the node
     * @param config The config of the node
     */
    public StringDialogNodeRepresentation(final StringDialogNodeValue currentValue,
        final StringDialogNodeConfig config) {
        super(currentValue, config);
        m_regex = config.getRegex();
        m_errorMessage = config.getErrorMessage();
        m_editorType = config.getEditorType();
        m_multilineEditorWidth = config.getMultilineEditorWidth();
        m_multilineEditorHeight = config.getMultilineEditorHeight();
    }

    /**
     * @return the regex
     */
    public String getRegex() {
        return m_regex;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return m_errorMessage;
    }

    /**
     * @return the editorType
     */
    public String getEditorType() {
        return m_editorType;
    }

    /**
     * @return the multilineEditorWidth
     */
    public int getMultilineEditorWidth() {
        return m_multilineEditorWidth;
    }

    /**
     * @return the multilineEditorHeight
     */
    public int getMultilineEditorHeight() {
        return m_multilineEditorHeight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DialogNodePanel<StringDialogNodeValue> createDialogPanel() {
        StringConfigurationPanel panel = new StringConfigurationPanel(this);
        fillDialogPanel(panel);
        return panel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append(", ");
        sb.append("regex=");
        sb.append(m_regex);
        sb.append(", ");
        sb.append("errorMessage=");
        sb.append(m_errorMessage);
        sb.append("editorType=");
        sb.append(m_editorType);
        sb.append("multilineEditorWidth=");
        sb.append(m_multilineEditorWidth);
        sb.append("multilineEditorHeight=");
        sb.append(m_multilineEditorHeight);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode())
                .append(m_regex)
                .append(m_errorMessage)
                .append(m_editorType)
                .append(m_multilineEditorWidth)
                .append(m_multilineEditorHeight)
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
        StringDialogNodeRepresentation other = (StringDialogNodeRepresentation)obj;
        return new EqualsBuilder().appendSuper(super.equals(obj))
                .append(m_regex, other.m_regex)
                .append(m_errorMessage, other.m_errorMessage)
                .append(m_editorType, other.m_editorType)
                .append(m_multilineEditorWidth, other.m_multilineEditorWidth)
                .append(m_multilineEditorHeight, other.m_multilineEditorHeight)
                .isEquals();
    }

}