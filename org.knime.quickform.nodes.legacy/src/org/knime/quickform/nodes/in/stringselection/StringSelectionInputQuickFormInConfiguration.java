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
 * History:
 * 24-Febr-2011: created
 *
 */
package org.knime.quickform.nodes.in.stringselection;

import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.quickform.AbstractQuickFormConfiguration;

/**
 * Configuration to string (one of multiple choices) input node.
 * @author Peter Ohl, KNIME AG, Zurich, Switzerland
 */
final class StringSelectionInputQuickFormInConfiguration
    extends AbstractQuickFormConfiguration
        <StringSelectionInputQuickFormValueInConfiguration> {

    private String[] m_choices = new String[0];

    /**
     * @return the choices out of which the value should be selected
     */
    String[] getChoices() {
        return m_choices.clone();
    }

    /**
     * @param choices out of which the value should be selected
     */
    void setChoices(final String[] choices) {
        if (choices == null) {
            m_choices = new String[0];
        } else {
            m_choices = choices.clone();
        }
    }

    /** Save config to argument.
     * @param settings To save to.
     */
    @Override
    public void saveSettingsTo(final NodeSettingsWO settings) {
        super.saveSettingsTo(settings);
        settings.addStringArray("choices", m_choices);
    }

    /** Load config in model.
     * @param settings To load from.
     * @throws InvalidSettingsException If that fails for any reason.
     */
    @Override
    public void loadSettingsInModel(final NodeSettingsRO settings)
        throws InvalidSettingsException {
        super.loadSettingsInModel(settings);
        m_choices = settings.getStringArray("choices");
        if (m_choices == null) {
            m_choices = new String[0];
        }
    }

    /** Load settings in dialog, init defaults if that fails.
     * @param settings To load from.
     */
    @Override
    public void loadSettingsInDialog(final NodeSettingsRO settings) {
        super.loadSettingsInDialog(settings);
        m_choices = settings.getStringArray("choices", new String[0]);
    }

    /** {@inheritDoc} */
    @Override
    public StringSelectionInputQuickFormPanel createController() {
        return new StringSelectionInputQuickFormPanel(this);
    }

    /** {@inheritDoc} */
    @Override
    public StringSelectionInputQuickFormValueInConfiguration
        createValueConfiguration() {
        return new StringSelectionInputQuickFormValueInConfiguration();
    }

}
