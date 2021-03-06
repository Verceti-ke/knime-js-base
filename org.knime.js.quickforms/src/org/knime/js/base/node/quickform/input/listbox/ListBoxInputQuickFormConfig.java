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
 *   Jun 12, 2014 (winter): created
 */
package org.knime.js.base.node.quickform.input.listbox;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.js.base.node.quickform.QuickFormFlowVariableConfig;

/**
 * The config for the list box input quick form node.
 *
 * @author Patrick Winter, KNIME AG, Zurich, Switzerland
 */
public class ListBoxInputQuickFormConfig extends QuickFormFlowVariableConfig<ListBoxInputQuickFormValue> {

    private static final String CFG_REGEX = "regex";
    private static final String DEFAULT_REGEX = "";
    private String m_regex = DEFAULT_REGEX;
    private static final String CFG_ERROR_MESSAGE = "error_message";
    private static final String DEFAULT_ERROR_MESSAGE = "";
    private String m_errorMessage = DEFAULT_ERROR_MESSAGE;
    private static final String CFG_SEPARATOR = "separator";
    static final String DEFAULT_SEPARATOR = "\\n";
    private String m_separator = null;
    private static final String CFG_SEPARATE_EACH_CHARACTER = "separate_each_character";
    private static final boolean DEFAULT_SEPARATE_EACH_CHARACTER = false;
    private boolean m_separateEachCharacter = DEFAULT_SEPARATE_EACH_CHARACTER;
    private static final String DEFAULT_SEPARATOR_REGEX = "";
    private String m_separatorRegex = DEFAULT_SEPARATOR_REGEX;
    private static final String CFG_OMIT_EMPTY = "omit_empty";
    private static final boolean DEFAULT_OMIT_EMPTY = true;
    private boolean m_omitEmpty = DEFAULT_OMIT_EMPTY;

    private static final String CFG_NUMBER_VIS_OPTIONS = "number_visible_options";
    private static final Integer DEFAULT_NUMBER_VIS_OPTIONS = 5;
    private Integer m_numberVisOptions = DEFAULT_NUMBER_VIS_OPTIONS;

    /**
     * @return the regex
     */
    String getRegex() {
        return m_regex;
    }

    /**
     * @param regex the regex to set
     */
    void setRegex(final String regex) {
        m_regex = regex;
    }

    /**
     * @return the errorMessage
     */
    String getErrorMessage() {
        return m_errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    void setErrorMessage(final String errorMessage) {
        m_errorMessage = errorMessage;
    }

    /**
     * @return the separator
     */
    String getSeparator() {
        return m_separator;
    }

    /**
     * @param separator the separator to set
     */
    void setSeparator(final String separator) {
        m_separator = separator;
    }

    /**
     * @return the separateEachCharacter
     */
    boolean getSeparateEachCharacter() {
        return m_separateEachCharacter;
    }

    /**
     * @param separateEachCharacter the separateEachCharacter to set
     */
    void setSeparateEachCharacter(final boolean separateEachCharacter) {
        m_separateEachCharacter = separateEachCharacter;
    }

    /**
     * @return the separatorRegex
     */
    public String getSeparatorRegex() {
        return m_separatorRegex;
    }

    /**
     * @param separatorRegex the separatorRegex to set
     */
    public void setSeparatorRegex(final String separatorRegex) {
        m_separatorRegex = separatorRegex;
    }

    /**
     * @return the omitEmpty
     */
    boolean getOmitEmpty() {
        return m_omitEmpty;
    }

    /**
     * @param omitEmpty the omitEmpty to set
     */
    void setOmitEmpty(final boolean omitEmpty) {
        m_omitEmpty = omitEmpty;
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
        settings.addString(CFG_REGEX, m_regex);
        settings.addString(CFG_ERROR_MESSAGE, m_errorMessage);
        settings.addString(CFG_SEPARATOR, m_separator);
        settings.addBoolean(CFG_SEPARATE_EACH_CHARACTER, m_separateEachCharacter);
        settings.addBoolean(CFG_OMIT_EMPTY, m_omitEmpty);
        settings.addInt(CFG_NUMBER_VIS_OPTIONS, m_numberVisOptions);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        super.loadSettings(settings);
        m_regex = settings.getString(CFG_REGEX);
        m_errorMessage = settings.getString(CFG_ERROR_MESSAGE);
        m_separator = settings.getString(CFG_SEPARATOR);
        m_omitEmpty = settings.getBoolean(CFG_OMIT_EMPTY);

        // added with 3.1
        m_separateEachCharacter = settings.getBoolean(CFG_SEPARATE_EACH_CHARACTER, DEFAULT_SEPARATE_EACH_CHARACTER);

        // added with 3.3
        m_numberVisOptions = settings.getInt(CFG_NUMBER_VIS_OPTIONS, DEFAULT_NUMBER_VIS_OPTIONS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadSettingsInDialog(final NodeSettingsRO settings) {
        super.loadSettingsInDialog(settings);
        m_regex = settings.getString(CFG_REGEX, DEFAULT_REGEX);
        m_errorMessage = settings.getString(CFG_ERROR_MESSAGE, DEFAULT_ERROR_MESSAGE);
        m_separator = settings.getString(CFG_SEPARATOR, DEFAULT_SEPARATOR);
        m_omitEmpty = settings.getBoolean(CFG_OMIT_EMPTY, DEFAULT_OMIT_EMPTY);

        // added with 3.1
        m_separateEachCharacter = settings.getBoolean(CFG_SEPARATE_EACH_CHARACTER, DEFAULT_SEPARATE_EACH_CHARACTER);

        // added with 3.3
        m_numberVisOptions = settings.getInt(CFG_NUMBER_VIS_OPTIONS, DEFAULT_NUMBER_VIS_OPTIONS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ListBoxInputQuickFormValue createEmptyValue() {
        return new ListBoxInputQuickFormValue();
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
        sb.append(", ");
        sb.append("separator=");
        sb.append(m_separator);
        sb.append(", ");
        sb.append("separateEachCharacter=");
        sb.append(m_separateEachCharacter);
        sb.append(", ");
        sb.append("separatorRegex=");
        sb.append(m_separatorRegex);
        sb.append(", ");
        sb.append("omitEmpty=");
        sb.append(m_omitEmpty);
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
                .append(m_regex)
                .append(m_errorMessage)
                .append(m_separator)
                .append(m_separateEachCharacter)
                .append(m_separatorRegex)
                .append(m_omitEmpty)
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
        ListBoxInputQuickFormConfig other = (ListBoxInputQuickFormConfig)obj;
        return new EqualsBuilder().appendSuper(super.equals(obj))
                .append(m_regex, other.m_regex)
                .append(m_errorMessage, other.m_errorMessage)
                .append(m_separator, other.m_separator)
                .append(m_separateEachCharacter, other.m_separateEachCharacter)
                .append(m_separatorRegex, other.m_separatorRegex)
                .append(m_omitEmpty, other.m_omitEmpty)
                .append(m_numberVisOptions, other.m_numberVisOptions)
                .isEquals();
    }

}
