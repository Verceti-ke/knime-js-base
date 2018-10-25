package org.knime.dynamic.js.base.grouped;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.knime.base.node.preproc.autobinner3.AutoBinner;
import org.knime.base.node.preproc.autobinner3.AutoBinnerLearnSettings;
import org.knime.base.node.preproc.autobinner3.AutoBinnerLearnSettings.BinNaming;
import org.knime.base.node.preproc.autobinner3.AutoBinnerLearnSettings.EqualityMethod;
import org.knime.base.node.preproc.autobinner3.AutoBinnerLearnSettings.Method;
import org.knime.base.node.preproc.autobinner3.AutoBinnerLearnSettings.OutputFormat;
import org.knime.base.node.preproc.autobinner3.AutoBinnerLearnSettings.PrecisionMode;
import org.knime.base.node.preproc.autobinner.apply.AutoBinnerApply;
import org.knime.base.node.preproc.autobinner.pmml.PMMLPreprocDiscretize;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.def.StringCell;
import org.knime.core.data.sort.BufferedDataTableSorter;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelInteger;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.port.PortObject;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.dynamic.js.v30.DynamicJSConfig;
import org.knime.js.core.JSONDataTable;
import org.knime.js.core.JSONDataTable.Builder;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * @author Christian Albrecht, KNIME GmbH, Konstanz, Germany
 *
 */
public class BinningProcessor extends GroupedProcessor {
    
    private String m_binColumn;

    @Override
    public Object[] processInputObjects(PortObject[] inObjects, ExecutionContext exec, DynamicJSConfig config)
        throws Exception {
        final BufferedDataTable table = (BufferedDataTable)inObjects[0];
        SettingsModelString catCol = (SettingsModelString)config.getModel("cat");
        String binMethod = ((SettingsModelString)config.getModel("bin_method")).getStringValue();
        int numberBins = ((SettingsModelInteger)config.getModel("num_bins")).getIntValue();
        String binEqual = ((SettingsModelString)config.getModel("bin_equal")).getStringValue();
        String binQuantiles = ((SettingsModelString)config.getModel("bin_quantiles")).getStringValue();
        String binNaming = ((SettingsModelString)config.getModel("bin_naming")).getStringValue();
        boolean forceInteger = ((SettingsModelBoolean)config.getModel("bin_force_int")).getBooleanValue();
        String numberFormat = ((SettingsModelString)config.getModel("num_format")).getStringValue();
        String outputFormat = ((SettingsModelString)config.getModel("out_format")).getStringValue();
        int formatPrecision = ((SettingsModelInteger)config.getModel("format_precision")).getIntValue();
        String formatPrecisionMode = ((SettingsModelString)config.getModel("format_prec_mode")).getStringValue();
        String roundingMode = ((SettingsModelString)config.getModel("format_rounding")).getStringValue();

        //Check bin column settings
        m_binColumn = catCol.getStringValue();
        if (m_binColumn == null) {
            throw new IllegalArgumentException("No column selected for binning.");
        }
        int columnIndex = table.getDataTableSpec().findColumnIndex(m_binColumn);
        if (columnIndex < 0) {
            throw new IllegalArgumentException("Index for binning column with name " + m_binColumn + " not found.");
        }

        //Create bin column with user settings
        AutoBinnerLearnSettings binnerSettings = new AutoBinnerLearnSettings();
        binnerSettings.setFilterConfiguration(new DataColumnSpecFilterConfiguration("filter", new InputFilter<DataColumnSpec>() {
            
            @Override
            public boolean include(DataColumnSpec name) {
                return name.getName().equals(m_binColumn);
            }
        }));
        Method method = binMethod.equals("Sample\u00A0quantiles") ? Method.sampleQuantiles : Method.fixedNumber;
        binnerSettings.setMethod(method);
        if (method == Method.fixedNumber) {
            binnerSettings.setBinCount(numberBins);
            EqualityMethod eq = binEqual.equals("width") ? EqualityMethod.width : EqualityMethod.frequency;
            binnerSettings.setEqualityMethod(eq);
        } else {
            String[] splittedQuantiles = binQuantiles.split(",");
            double[] dQuantiles = Arrays.stream(splittedQuantiles).mapToDouble(v -> Double.parseDouble(v.trim())).toArray();
            binnerSettings.setSampleQuantiles(dQuantiles);
        }
        BinNaming naming = BinNaming.numbered;
        if (binNaming.equals("Borders")) {
            naming = BinNaming.edges;
        } else if (binNaming.equals("Midpoints")) {
            naming = BinNaming.midpoints;
        }
        binnerSettings.setBinNaming(naming);
        binnerSettings.setIntegerBounds(forceInteger);
        binnerSettings.setAdvancedFormatting(numberFormat.equals("Advanced\u00A0formatting"));
        if (binnerSettings.getAdvancedFormatting()) {
            OutputFormat format = OutputFormat.Plain;
            if (outputFormat.equals("Standard\u00A0String")) {
                format = OutputFormat.Standard;
            } else if (outputFormat.equals("Engineering\u00A0String")) {
                format = OutputFormat.Engineering;
            }
            binnerSettings.setOutputFormat(format);
            binnerSettings.setPrecision(formatPrecision);
            PrecisionMode precMode = formatPrecisionMode.equals("Significant\u00A0figures") ? PrecisionMode.Significant
                : PrecisionMode.Decimal;
            binnerSettings.setPrecisionMode(precMode);
            binnerSettings.setRoundingMode(RoundingMode.valueOf(roundingMode));
        }
        AutoBinner binner = new AutoBinner(binnerSettings, table.getDataTableSpec());
        BufferedDataTable inData = binner.calcDomainBoundsIfNeccessary(table, exec, Arrays.asList(m_binColumn));
        PMMLPreprocDiscretize op = binner.execute(inData, exec.createSubExecutionContext(0.25));
        List<String> binnedNames = op.getConfiguration().getNames();
        assert binnedNames.size() == 1;
        String binnedColName = binnedNames.get(0);
        List<String> orderedBinNames = op.getConfiguration().getDiscretize(binnedColName).getBins().stream()
            .map(e -> e.getBinValue()).collect(Collectors.toList());
        AutoBinnerApply applier = new AutoBinnerApply();
        BufferedDataTable outData = applier.execute(op, table, exec.createSubExecutionContext(0.25));

        //Group table with bin column according to user settings on GroupedProcessor
        catCol.setStringValue(binnedColName);
        Object[] grouped =
            super.processInputObjects(new PortObject[]{outData}, exec.createSubExecutionContext(0.5), config);
        BufferedDataTable groupedTable = (BufferedDataTable)grouped[0];
        catCol.setStringValue(m_binColumn);
        
        //Make sure bins are sorted correctly in output table
        final int binIndex =
            Arrays.asList(groupedTable.getDataTableSpec().getColumnNames()).indexOf(binnedColName);
        Comparator<DataRow> comp = new Comparator<DataRow>() {

            @Override
            public int compare(DataRow o1, DataRow o2) {
                final DataCell c1 = o1.getCell(binIndex);
                final DataCell c2 = o2.getCell(binIndex);
                if (!c1.isMissing() && !c2.isMissing()) {
                    int bin1 = orderedBinNames.indexOf(((StringCell)c1).getStringValue());
                    int bin2 = orderedBinNames.indexOf(((StringCell)c2).getStringValue());
                    return Integer.compare(bin1, bin2);
                } else if (c1.isMissing() && !c2.isMissing()) {
                    return 1;
                } else if (!c1.isMissing() && c2.isMissing()) {
                    return -1;
                } else {
                    return 0;
                }
            }
        };
        BufferedDataTableSorter sorter = new BufferedDataTableSorter(groupedTable, comp);
        sorter.setSortInMemory(true);
        BufferedDataTable sortedTable = sorter.sort(exec.createSubExecutionContext(0.01));
        Builder builder = JSONDataTable.newBuilder()
                .setDataTable(sortedTable)
                .setFirstRow(1)
                .setMaxRows(Math.toIntExact(sortedTable.size()));
        BinningResult res = new BinningResult();
        res.setTable(builder.build(exec.createSubExecutionContext(0.01)));
        res.setBinnedColumn(binnedColName);
        return new Object[]{res};
    }
    
    @JsonAutoDetect
    private static final class BinningResult {
        
        private JSONDataTable m_table;
        private String m_binnedColumn;
        
        public JSONDataTable getTable() {
            return m_table;
        }
        
        public void setTable(JSONDataTable table) {
            m_table = table;
        }
        
        public String getBinnedColumn() {
            return m_binnedColumn;
        }
        
        public void setBinnedColumn(String binnedColumn) {
            m_binnedColumn = binnedColumn;
        }
        
    }

}
