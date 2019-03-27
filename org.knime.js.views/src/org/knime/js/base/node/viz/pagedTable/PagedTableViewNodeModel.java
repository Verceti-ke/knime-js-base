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
 * History
 *   23.04.2014 (Christian Albrecht, KNIME AG, Zurich, Switzerland): created
 */
package org.knime.js.base.node.viz.pagedTable;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DirectAccessTable.UnknownRowCountException;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.cache.WindowCacheTable;
import org.knime.core.data.cache.WindowCacheTableTransformationExecutor.WindowCacheTableTansformationExecutorBuilder;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.property.filter.FilterModel;
import org.knime.core.data.property.filter.FilterModelNominal;
import org.knime.core.data.property.filter.FilterModelRange;
import org.knime.core.data.sort.TableSortInformation;
import org.knime.core.data.sort.TableSortInformation.ColumnSortInformation;
import org.knime.core.data.sort.TableSortInformation.MissingValueSortStrategy;
import org.knime.core.data.sort.TableSortInformation.SortDirection;
import org.knime.core.data.transform.DataTableFilterInformation;
import org.knime.core.data.transform.DataTableNominalFilterInformation;
import org.knime.core.data.transform.DataTableRangeFilterInformation;
import org.knime.core.data.transform.DataTableSearchFilterInformation;
import org.knime.core.data.transform.DataTableSearchFilterInformation.DataTableSearchFilterFormatter;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.interactive.ViewRequestHandlingException;
import org.knime.core.node.port.PortObject;
import org.knime.js.base.node.viz.pagedTable.PagedTableViewRequest.Column;
import org.knime.js.base.node.viz.pagedTable.PagedTableViewRequest.Order;
import org.knime.js.base.node.viz.pagedTable.PagedTableViewRequest.Search;
import org.knime.js.core.JSONDataTable;
import org.knime.js.core.JSONDataTable.Builder;
import org.knime.js.core.JSONViewRequestHandler;
import org.knime.js.core.node.table.AbstractTableNodeModel;
import org.knime.js.core.selections.json.JSONTableSelection;
import org.knime.js.core.selections.json.RangeSelection;
import org.knime.js.core.selections.json.SelectionElement;
import org.knime.js.core.settings.table.TableRepresentationSettings;
import org.knime.js.core.settings.table.TableSettings;

/**
 * Node model implementation for the Table View (JavaScript) node
 *
 * @author Christian Albrecht, KNIME.com GmbH, Konstanz, Germany
 */
public class PagedTableViewNodeModel extends AbstractTableNodeModel<PagedTableViewRepresentation, PagedTableViewValue>
    implements JSONViewRequestHandler<PagedTableViewRequest, PagedTableViewResponse> {

    private static NodeLogger LOGGER = NodeLogger.getLogger(PagedTableViewNodeModel.class);

    private static int SMALL_TABLE = 1000;

    private WindowCacheTable m_cache;

    private WindowCacheTable m_transformedCache;

    private PagedTableViewRequest m_lastRequest;

    /**
     * @param viewName The name of the interactive view
     */
    protected PagedTableViewNodeModel(final String viewName) {
        super(viewName, new PagedTableViewConfig());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getJavascriptObjectID() {
        return "org.knime.js.base.node.viz.pagedTable";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedTableViewRepresentation createEmptyViewRepresentation() {
        return new PagedTableViewRepresentation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedTableViewValue createEmptyViewValue() {
        return new PagedTableViewValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedTableViewRepresentation getViewRepresentation() {
        PagedTableViewRepresentation rep = super.getViewRepresentation();
        if (m_cache == null && rep.getSettings().getEnableLazyLoading()) {
            initializeCache(rep);
        }
        return rep;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected PortObject[] performExecute(final PortObject[] inObjects, final ExecutionContext exec) throws Exception {
        BufferedDataTable out = (BufferedDataTable)inObjects[0];
        synchronized (getLock()) {
            PagedTableViewRepresentation viewRepresentation = getViewRepresentation();
            TableRepresentationSettings settings = m_config.getSettings().getRepresentationSettings();
            m_table = (BufferedDataTable)inObjects[0];
            double tableCreationFraction = 0.5;
            if (settings.getEnableLazyLoading() && settings.getEnableSelection()) {
                tableCreationFraction = 0.05;
            } else if (!settings.getEnableLazyLoading() && !settings.getEnableSelection()) {
                tableCreationFraction = 1.0;
            }
            if (viewRepresentation.getSettings().getTable() == null) {
                JSONDataTable jsonTable = createJSONTableFromBufferedDataTable(m_table,
                    exec.createSubExecutionContext(tableCreationFraction));
                viewRepresentation.getSettings().setTable(jsonTable);
                copyConfigToRepresentation();
            }
            if (m_cache == null && settings.getEnableLazyLoading()) {
                initializeCache(viewRepresentation);
            }

            if (settings.getEnableSelection() && !settings.getEnableLazyLoading()) {
                PagedTableViewValue viewValue = getViewValue();
                List<String> selectionList = null;
                if (viewValue != null) {
                    if (viewValue.getSettings().getSelection() != null) {
                        selectionList = Arrays.asList(viewValue.getSettings().getSelection());
                    }
                }
                ColumnRearranger rearranger = createColumnAppender(m_table.getDataTableSpec(), selectionList);
                out = exec.createColumnRearrangeTable(m_table, rearranger,
                    exec.createSubExecutionContext(1 - tableCreationFraction));
            }
            viewRepresentation.getSettings()
                .setSubscriptionFilterIds(getSubscriptionFilterIds(m_table.getDataTableSpec()));
        }
        exec.setProgress(1);
        return new PortObject[]{out};
    }

    private void initializeCache(final PagedTableViewRepresentation rep) {
        if (rep == null || rep.getSettings() == null) {
            return;
        }
        TableRepresentationSettings settings = rep.getSettings();
        List<String> includedColumns = new ArrayList<String>();
        if (settings.getTable() != null) {
            includedColumns.addAll(Arrays.asList(settings.getTable().getSpec().getColNames()));
        }
        if (m_table != null && m_table.getSpec() != null) {
            m_table.getSpec().forEach(colSpec -> {
                // retain columns with color, size, shape of filter handlers set
                if (colSpec.getColorHandler() != null || colSpec.getSizeHandler() != null
                    || colSpec.getShapeHandler() != null || colSpec.getFilterHandler().isPresent()) {
                    if (!includedColumns.contains(colSpec.getName())) {
                        includedColumns.add(colSpec.getName());
                    }
                }
            });
        }
        m_cache = new WindowCacheTable(m_table, includedColumns.stream().toArray(String[]::new));
        int maxPageSize = settings.getInitialPageSize();
        if (settings.getEnablePageSizeChange()) {
            maxPageSize = Arrays.stream(settings.getAllowedPageSizes()).max().getAsInt();
        }
        // we either take the default cache size (500), or if larger, 5 times the largest page size
        // that is available for selection in the view
        m_cache.setCacheSize(Math.max(5 * maxPageSize, WindowCacheTable.DEFAULT_CACHE_SIZE));
        m_transformedCache = m_cache;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
        (new TableSettings()).loadSettings(settings);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void performReset() {
        m_cache = null;
        m_transformedCache = null;
        m_lastRequest = null;
        super.performReset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedTableViewRequest createEmptyViewRequest() {
        return new PagedTableViewRequest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PagedTableViewResponse handleRequest(final PagedTableViewRequest request, final ExecutionMonitor exec)
        throws ViewRequestHandlingException, InterruptedException, CanceledExecutionException {
        exec.checkCanceled();
        PagedTableViewResponse response = new PagedTableViewResponse(request);
        try {
            synchronized (getLock()) {
                exec.checkCanceled();
                double remainingProgress = 0.95;
                final Order[] order = request.getOrder();
                if (order != null && order.length > 0) {
                    if (sortingChanged(request, m_lastRequest)) {
                        double sortProgress = request.getStart() < SMALL_TABLE ? 0.9 : 0.5;
                        ExecutionMonitor sortMonitor = exec.createSubProgress(sortProgress);
                        remainingProgress -= sortProgress;
                        exec.setMessage("Sorting...");
                        m_transformedCache = sort(order, sortMonitor);
                        m_lastRequest = null;
                    }
                } else if (m_lastRequest != null && !Arrays.deepEquals(m_lastRequest.getOrder(), order)) {
                    m_transformedCache = m_cache;
                    m_lastRequest = null;
                }

                if (needsNewFilter(request, m_lastRequest)) {
                    // evaluated all potential filter rules, if any changed a new transformation builder is needed
                    WindowCacheTableTansformationExecutorBuilder transformationBuilder =
                        WindowCacheTableTansformationExecutorBuilder.newBuilder();

                    // global search filter
                    Search search = request.getSearch();
                    if (search != null && StringUtils.isNotEmpty(search.getValue())) {
                        addSearch(search, null, transformationBuilder);
                    }

                    // search filter on individual columns
                    Column[] columns = request.getColumns();
                    if (columns != null) {
                        for (Column column : columns) {
                            if (column != null && column.getSearch() != null
                                && StringUtils.isNotEmpty(column.getSearch().getValue())) {
                                addSearch(column.getSearch(), column.getName(), transformationBuilder);
                            }
                        }
                    }

                    // filter rules from interactive events
                    JSONTableSelection filter = request.getFilter();
                    if (filter != null) {
                        SelectionElement[] elements = filter.getElements();
                        if (elements != null) {
                            for (SelectionElement element : elements) {
                                // the following call makes sure the model is well formed and contains only 1 filter
                                FilterModel model = element.createFilterModel();
                                String colName = ((RangeSelection)element).getColumns()[0].getColumnName();
                                DataTableSpec spec = m_table.getDataTableSpec();
                                DataTableFilterInformation filterInfo = null;
                                if (model instanceof FilterModelNominal) {
                                    filterInfo =
                                        new DataTableNominalFilterInformation(spec, colName, (FilterModelNominal)model);
                                } else if (model instanceof FilterModelRange) {
                                    filterInfo =
                                        new DataTableRangeFilterInformation(spec, colName, (FilterModelRange)model);
                                }
                                if (filterInfo != null) {
                                    transformationBuilder.filter(filterInfo);
                                }
                            }
                        }
                    }

                    // execute filters
                    m_transformedCache =
                            (WindowCacheTable)transformationBuilder.build().execute(m_transformedCache, null);
                    try {
                        // try to get a row count for small tables immediately
                        if (m_cache.getRowCount() < SMALL_TABLE) {
                            m_transformedCache.getRows(m_cache.getRowCount() - 1, 1, null);
                        }
                    } catch (UnknownRowCountException | IndexOutOfBoundsException e) { /* ignore */ }
                    // TODO: count rows for larger filtered tables

                }

                m_lastRequest = request;
                ExecutionMonitor cacheProgress = exec.createSubProgress(remainingProgress);
                exec.setMessage("Caching rows...");
                List<DataRow> rows;
                try {
                    rows = m_transformedCache.getRows(request.getStart(), request.getLength(), cacheProgress);
                } catch (IndexOutOfBoundsException e) {
                    rows = new ArrayList<DataRow>(0);
                }
                exec.checkCanceled();
                Builder tableBuilder = getJsonDataTableBuilder(m_table);
                tableBuilder.setDataRows(rows.stream().toArray(DataRow[]::new));
                tableBuilder.setFirstRow(request.getStart() + 1);
                tableBuilder.setMaxRows(request.getLength());
                try {
                    tableBuilder.setPartialTableRows(m_cache.getRowCount(), m_transformedCache.getRowCount());
                } catch (UnknownRowCountException e) {
                    tableBuilder.setPartialTableRows(m_cache.getRowCount(), -1);
                }
                exec.setMessage("Serializing response...");
                response.setTable(tableBuilder.build(exec.createSubProgress(0.05)));
            }
        } catch (CanceledExecutionException e) {
            // request was cancelled, no need for special treatment
            throw e;
        } catch (Exception e) {
            // wrap all other exceptions for proper error handling
            LOGGER.error("Table request could not be processed: " + e.getMessage(), e);
            response.setError(e.getMessage());
            throw new ViewRequestHandlingException(e);
        }
        return response;
    }

    private static boolean needsNewFilter(final PagedTableViewRequest newRequest,
        final PagedTableViewRequest previousRequest) {
        if (previousRequest == null) {
            return true;
        }
        return searchChanged(newRequest, previousRequest) || filterChanged(newRequest, previousRequest)
            || columnSearchChanged(newRequest, previousRequest);
    }

    private static boolean sortingChanged(final PagedTableViewRequest newRequest,
        final PagedTableViewRequest previousRequest) {
        if (previousRequest == null) {
            return true;
        }
        return !Arrays.deepEquals(previousRequest.getOrder(), newRequest.getOrder());
    }

    private static boolean searchChanged(final PagedTableViewRequest newRequest,
        final PagedTableViewRequest previousRequest) {
        if (previousRequest == null) {
            return true;
        }
        return !Objects.equals(previousRequest.getSearch(), newRequest.getSearch());
    }

    private static boolean filterChanged(final PagedTableViewRequest newRequest,
        final PagedTableViewRequest previousRequest) {
        if (previousRequest == null) {
            return true;
        }
        return !Objects.equals(previousRequest.getFilter(), newRequest.getFilter());
    }

    private static boolean columnSearchChanged(final PagedTableViewRequest newRequest,
        final PagedTableViewRequest previousRequest) {
        if (previousRequest == null || previousRequest.getColumns() == null) {
            return true;
        }
        for (int i = 0; i < previousRequest.getColumns().length; i++) {
            Column prevCol = previousRequest.getColumns()[i];
            Column newCol = newRequest.getColumns()[i];
            if (!Objects.equals(prevCol.getSearch(), newCol.getSearch())) {
                return true;
            }
        }
        return false;
    }

    private WindowCacheTable sort(final Order[] order, final ExecutionMonitor exec) throws CanceledExecutionException {
        TableSortInformation sort = new TableSortInformation();
        sort.setMissingValueStrategy(MissingValueSortStrategy.LAST);
        for (Order o : order) {
            SortDirection dir = o.getDir().equalsIgnoreCase("asc") ? SortDirection.ASCENDING : SortDirection.DESCENDING;
            String colName = m_table.getDataTableSpec().getColumnSpec(o.getColumn()) == null ? null : o.getColumn();
            sort.addColumn(new ColumnSortInformation(colName, dir, colName == null));
        }
        WindowCacheTableTansformationExecutorBuilder builder =
            WindowCacheTableTansformationExecutorBuilder.newBuilder();
        builder.sort(sort);
        return (WindowCacheTable)builder.build().execute(m_cache, exec);
    }

    private void addSearch(final Search search, final String colName,
        final WindowCacheTableTansformationExecutorBuilder transformationBuilder) throws CanceledExecutionException {
        int[] colIndices = null;
        final DataTableSpec spec = m_table.getDataTableSpec();
        PagedTableViewRepresentation rep = getViewRepresentation();
        Map<Integer, DataTableSearchFilterFormatter> formatters = new HashMap<Integer, DataTableSearchFilterFormatter>();
        if (rep != null) {
            if (rep.getSettings().getTable() != null) {
                String[] colNames = rep.getSettings().getTable().getSpec().getColNames();
                if (colName != null) {
                    colNames = new String[] {colName};
                }
                colIndices = new int[colNames.length];
                for (int col = 0; col < colNames.length; col++) {
                    colIndices[col] = spec.findColumnIndex(colNames[col]);
                }
            }
            if (rep.getSettings().getEnableGlobalNumberFormat() && colIndices != null) {
                for (int col : colIndices) {
                    if (spec.getColumnSpec(col).getType().isCompatible(DoubleValue.class)) {
                        formatters.put(col,
                            new LimitDecimalSearchFormatter(rep.getSettings().getGlobalNumberFormatDecimals()));
                    }
                }
            }
        }
        DataTableSearchFilterInformation filter = new DataTableSearchFilterInformation(search.getValue(),
            search.isRegex(), !search.isRegex(), formatters, m_table.getDataTableSpec(), colIndices);
        transformationBuilder.filter(filter);
    }

    private static final class LimitDecimalSearchFormatter implements DataTableSearchFilterFormatter {

        private final DecimalFormat m_pattern;

        /**
         *
         */
        public LimitDecimalSearchFormatter(final int decimals) {
            m_pattern = (DecimalFormat)NumberFormat.getInstance(Locale.US);
            m_pattern.setMinimumFractionDigits(decimals);
            m_pattern.setMaximumFractionDigits(decimals);
            m_pattern.setRoundingMode(RoundingMode.HALF_UP);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String format(final DataCell cell) {
            return m_pattern.format(((DoubleValue)cell).getDoubleValue());
        }

    }
}
