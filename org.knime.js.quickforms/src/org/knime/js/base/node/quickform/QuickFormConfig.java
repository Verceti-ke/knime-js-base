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
package org.knime.js.base.node.quickform;

import java.util.regex.Pattern;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.dialog.DialogNode;
import org.knime.core.node.dialog.DialogNodeValue;
import org.knime.core.node.util.CheckUtils;
import org.knime.core.node.workflow.SubNodeContainer;

/**
 * Configuration of a quick form node.
 *
 * @author Patrick Winter, KNIME AG, Zurich, Switzerland
 * @param <VAL> The value implementation of the quick form node.
 */
@Deprecated
public abstract class QuickFormConfig <VAL extends DialogNodeValue> {

    private static final String CFG_LABEL = "label";
    private static final String CFG_DESCRIPTION = "description";
    private static final String CFG_HIDE_IN_WIZARD = "hideInWizard";
    private static final String CFG_HIDE_IN_DIALOG = "hideInDialog";
    private static final String CFG_DEFAULT_VALUE = "defaultValue";
    private static final String CFG_REQUIRED = "required";
    private static final String CFG_PARAMETER_NAME = "parameterName";
    private static final String CFG_CUSTOM_CSS = "customCSS";

    private static final String DEFAULT_LABEL = "Label";
    private static final String DEFAULT_DESCRIPTION = "Enter Description";
    private static final boolean DEFAULT_HIDE_IN_WIZARD = false;
    private static final boolean DEFAULT_HIDE_IN_DIALOG = false;
    private static final boolean DEFAULT_REQUIRED = true;
    private static final String DEFAULT_CUSTOM_CSS = "";

    private String m_label = DEFAULT_LABEL;
    private String m_description = DEFAULT_DESCRIPTION;
    private boolean m_hideInWizard = DEFAULT_HIDE_IN_WIZARD;
    private boolean m_hideInDialog = DEFAULT_HIDE_IN_DIALOG;
    private VAL m_defaultValue = createEmptyValue();
    private boolean m_required = DEFAULT_REQUIRED;
    private String m_parameterName = SubNodeContainer.getDialogNodeParameterNameDefault(getClass());
    private String m_customCSS = DEFAULT_CUSTOM_CSS;

    /**
     * @return the label
     */
    public String getLabel() {
        return m_label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(final String label) {
        this.m_label = label;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return m_description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.m_description = description;
    }

    /**
     * @return the hideInWizard
     */
    public boolean getHideInWizard() {
        return m_hideInWizard;
    }

    /**
     * @param hideInWizard the hideInWizard to set
     */
    public void setHideInWizard(final boolean hideInWizard) {
        m_hideInWizard = hideInWizard;
    }

    /**
     * @return the hideInDialog
     */
    public boolean getHideInDialog() {
        return m_hideInDialog;
    }

    /**
     * @param hideInDialog the hideInDialog to set
     */
    public void setHideInDialog(final boolean hideInDialog) {
        m_hideInDialog = hideInDialog;
    }

    /**
     * @return the required
     */
    public boolean getRequired() {
        return m_required;
    }

    /**
     * @param required the required to set
     */
    public void setRequired(final boolean required) {
        m_required = required;
    }

    /**
     * @return the parameterName
     */
    public String getParameterName() {
        return m_parameterName;
    }

    /**
     * @param s the parameterName to set
     * @param allowLegacyFormat if true it will allow the {@link DialogNode#PARAMETER_NAME_PATTERN_LEGACY} (backward
     *            compatible)
     * @throws InvalidSettingsException If null or invalid
     */
    public void setParameterName(final String s, final boolean allowLegacyFormat) throws InvalidSettingsException {
        CheckUtils.checkSettingNotNull(s, "Parameter name must not be null");
        Pattern pattern = allowLegacyFormat
                ? DialogNode.PARAMETER_NAME_PATTERN_LEGACY : DialogNode.PARAMETER_NAME_PATTERN;
        CheckUtils.checkSetting("".equals(s) || pattern.matcher(s).matches(),
            "Parameter name \"%s\" is invalid - only character, digits and a dash character are allowed", s);
        m_parameterName = s;
    }

    /**
     * @return the customCSS
     */
    public String getCustomCSS() {
        return m_customCSS;
    }

    /**
     * @param customCSS the customCSS to set
     */
    public void setCustomCSS(final String customCSS) {
        m_customCSS = customCSS;
    }

    /**
     * @param settings The settings to save to
     */
    public void saveSettings(final NodeSettingsWO settings) {
        NodeSettingsWO defaultValueSettings = settings.addNodeSettings(CFG_DEFAULT_VALUE);
        m_defaultValue.saveToNodeSettings(defaultValueSettings);
        settings.addString(CFG_LABEL, m_label);
        settings.addString(CFG_DESCRIPTION, m_description);
        settings.addBoolean(CFG_HIDE_IN_WIZARD, m_hideInWizard);
        settings.addBoolean(CFG_HIDE_IN_DIALOG, m_hideInDialog);
        settings.addBoolean(CFG_REQUIRED, m_required);
        settings.addString(CFG_PARAMETER_NAME, m_parameterName);

        //added with 3.6
        settings.addString(CFG_CUSTOM_CSS, m_customCSS);
    }

    /**
     * @param settings The settings to load from
     * @throws InvalidSettingsException If the settings are not valid
     */
    public void loadSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        NodeSettingsRO defaultValueSettings = settings.getNodeSettings(CFG_DEFAULT_VALUE);
        m_defaultValue = createEmptyValue();
        m_defaultValue.loadFromNodeSettings(defaultValueSettings);
        m_label = settings.getString(CFG_LABEL);
        m_description = settings.getString(CFG_DESCRIPTION);
        m_hideInWizard = settings.getBoolean(CFG_HIDE_IN_WIZARD);
        m_hideInDialog = settings.getBoolean(CFG_HIDE_IN_DIALOG);
        m_required = settings.getBoolean(CFG_REQUIRED);
        // added in 2.12 - "" is discouraged but OK
        setParameterName(settings.getString(CFG_PARAMETER_NAME, ""), true);

        //added with 3.6
        m_customCSS = settings.getString(CFG_CUSTOM_CSS, DEFAULT_CUSTOM_CSS);
    }

    /**
     * @param settings The settings to load from
     */
    public void loadSettingsInDialog(final NodeSettingsRO settings) {
        m_defaultValue = createEmptyValue();
        NodeSettingsRO defaultValueSettings;
        try {
            defaultValueSettings = settings.getNodeSettings(CFG_DEFAULT_VALUE);
            m_defaultValue.loadFromNodeSettingsInDialog(defaultValueSettings);
        } catch (InvalidSettingsException e) {
            // Stay with defaults
        }
        m_label = settings.getString(CFG_LABEL, DEFAULT_LABEL);
        m_description = settings.getString(CFG_DESCRIPTION, DEFAULT_DESCRIPTION);
        m_hideInWizard = settings.getBoolean(CFG_HIDE_IN_WIZARD, DEFAULT_HIDE_IN_WIZARD);
        m_hideInDialog = settings.getBoolean(CFG_HIDE_IN_DIALOG, DEFAULT_HIDE_IN_DIALOG);
        m_required = settings.getBoolean(CFG_REQUIRED, DEFAULT_REQUIRED);
        final String defaultParName = SubNodeContainer.getDialogNodeParameterNameDefault(getClass());
        String parName = settings.getString(CFG_PARAMETER_NAME, defaultParName);
        try {
            setParameterName(parName, true);
        } catch (InvalidSettingsException ise) {
            m_parameterName = defaultParName;
        }

        //added with 3.6
        m_customCSS = settings.getString(CFG_CUSTOM_CSS, DEFAULT_CUSTOM_CSS);
    }

    /**
     * @return the default value
     */
    public VAL getDefaultValue() {
        return m_defaultValue;
    }

    /**
     * Creates an instance of a value used for the default value of this config.
     *
     * @return Create a value instance
     */
    protected abstract VAL createEmptyValue();

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("label=");
        sb.append(m_label);
        sb.append(", ");
        sb.append("description=");
        sb.append(m_description);
        sb.append(", ");
        sb.append("hideInWizard=");
        sb.append(m_hideInWizard);
        sb.append(", ");
        sb.append("hideInDialog=");
        sb.append(m_hideInDialog);
        sb.append(", ");
        sb.append("paramaterName=");
        sb.append(m_parameterName);
        sb.append(", ");
        sb.append("defaultValue=");
        sb.append("{");
        sb.append(m_defaultValue);
        sb.append("}");
        sb.append(", ");
        sb.append("required=");
        sb.append(m_required);
        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(m_label)
                .append(m_description)
                .append(m_hideInWizard)
                .append(m_hideInDialog)
                .append(m_parameterName)
                .append(m_defaultValue)
                .append(m_required)
                .append(m_customCSS)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
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
        QuickFormConfig<VAL> other = (QuickFormConfig<VAL>)obj;
        return new EqualsBuilder()
                .append(m_label, other.m_label)
                .append(m_description, other.m_description)
                .append(m_hideInWizard, other.m_hideInWizard)
                .append(m_hideInDialog, other.m_hideInDialog)
                .append(m_parameterName, other.m_parameterName)
                .append(m_defaultValue, other.m_defaultValue)
                .append(m_required, other.m_required)
                .append(m_customCSS, other.m_customCSS)
                .isEquals();
    }

}
