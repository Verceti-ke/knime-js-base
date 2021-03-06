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
 */
package org.knime.js.base.node.quickform.selection.value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.knime.core.data.DataTable;
import org.knime.core.data.DataTableSpec;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.port.PortObjectSpec;
import org.knime.core.node.port.PortType;
import org.knime.core.node.port.flowvariable.FlowVariablePortObject;
import org.knime.core.node.port.flowvariable.FlowVariablePortObjectSpec;
import org.knime.core.node.web.ValidationError;
import org.knime.js.base.node.quickform.QuickFormNodeModel;

/**
 * The model for the value selection quick form node.
 *
 * @author Patrick Winter, KNIME AG, Zurich, Switzerland
 */
public class ValueSelectionQuickFormNodeModel
        extends QuickFormNodeModel
        <ValueSelectionQuickFormRepresentation,
        ValueSelectionQuickFormValue,
        ValueSelectionQuickFormConfig> {

    /** Creates a new value selection node model.
     * @param viewName the view name*/
    public ValueSelectionQuickFormNodeModel(final String viewName) {
        super(new PortType[]{BufferedDataTable.TYPE},
                new PortType[]{FlowVariablePortObject.TYPE}, viewName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavascriptObjectID() {
        return "org_knime_js_base_node_quickform_selection_value";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSelectionQuickFormValue createEmptyViewValue() {
        return new ValueSelectionQuickFormValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObjectSpec[] configure(final PortObjectSpec[] inSpecs)
            throws InvalidSettingsException {
        updateValues((DataTableSpec)inSpecs[0]);
        createAndPushFlowVariable();
        return new PortObjectSpec[]{FlowVariablePortObjectSpec.INSTANCE};
    }

    /** {@inheritDoc} */
    @Override
    protected PortObject[] execute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        updateValues(((DataTable)inObjects[0]).getDataTableSpec());
        createAndPushFlowVariable();
        return new PortObject[]{FlowVariablePortObject.INSTANCE};
    }

    /**
     * Updates the values in the config.
     *
     * @param spec The input specs
     */
    private void updateValues(final DataTableSpec spec) {
        getConfig().setFromSpec(spec);
    }

    /**
     * Pushes the current values as flow variable.
     *
     * @throws InvalidSettingsException If the current values are not available in the input
     */
    private void createAndPushFlowVariable() throws InvalidSettingsException {
        ValueSelectionQuickFormValue rValue = getRelevantValue();
        String column = rValue.getColumn();
        String value = rValue.getValue();
        Map<String, List<String>> possibleValues = getConfig().getPossibleValues().entrySet().stream()//
            .filter(e -> !e.getValue().isEmpty())//
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (possibleValues.isEmpty()) {
            throw new InvalidSettingsException("The input table does not contain any column with domain information.");
        }
        if (!possibleValues.containsKey(column)) {
            String warning = "";
            if (!StringUtils.isEmpty(column)) {
                if (getConfig().getLockColumn()) {
                    throw new InvalidSettingsException(
                        "Locked column '" + column + "' is not part of the table spec anymore or its domain is empty.");
                }
                warning = "Column '" + column + "' is not part of the table spec anymore or its domain is empty.\n";
            }
            warning += "Auto-guessing default column and value.";
            column = possibleValues.keySet().iterator().next();
            value = possibleValues.get(column).get(0);
            setWarningMessage(warning);
        }
        final List<String> values = possibleValues.get(column);
        if (!values.contains(value)) {
            setWarningMessage("The selected value '" + value + "' is not among the possible values in the column '"
                + column + "'.\nAuto-guessing new default value.");
            value = values.get(0);
        }
        String variableName = getConfig().getFlowVariableName();
        pushFlowVariableString(variableName + " (column)", rValue.getColumn());
        switch (getConfig().getColumnType()) {
            case Integer:
                pushFlowVariableInt(variableName, Integer.parseInt(value));
                break;
            case Double:
                pushFlowVariableDouble(variableName, Double.parseDouble(value));
                break;
            default:
                pushFlowVariableString(variableName, value);
                break;
        }
    }

    /** {@inheritDoc} */
    @Override
    public ValidationError validateViewValue(final ValueSelectionQuickFormValue viewContent) {
        if (viewContent.getColumn() == null) {
            return new ValidationError("Selected column(s) cannot be empty.");
        }
        if (viewContent.getValue() == null) {
            return new ValidationError("Selected value(s) cannot be empty.");
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void copyValueToConfig() {
        getConfig().getDefaultValue().setColumn(getViewValue().getColumn());
        getConfig().getDefaultValue().setValue(getViewValue().getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ValueSelectionQuickFormConfig createEmptyConfig() {
        return new ValueSelectionQuickFormConfig();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ValueSelectionQuickFormRepresentation getRepresentation() {
        return new ValueSelectionQuickFormRepresentation(getRelevantValue(), getConfig());
    }

}
