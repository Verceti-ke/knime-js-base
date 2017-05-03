/*
# TODO:
* check donut hole / zoom
  * is this because of artificial root element?
* label nicht über donut hole
* formatter: no trailing zeros
* check shitty node
*/

(sunburst_namespace = function() {

  var view = {};
  var _representation, _value;
  var knimeTable1, knimeTable2;
  var _data = {};
  var uniqueLabels;
  var nodes;
  var selectedRows = [];
  var highlitedPath;
  var rowKey2leaf = {};
  var currentFilter = null;
  var _colorMap;
  var mouseMode = "highlite";
  var totalSize;

  var layoutContainer;
  var MIN_HEIGHT = 300, MIN_WIDTH = 400;

  var rootNodeName = "root";
  var nullNodeName = "?";

  var innerLabelStyles = ['sum', 'percentage'];

  view.init = function(representation, value) {
    debugger;
    _representation = representation;
    _value = value;

    // Load data from port 1 into knime table.
    knimeTable1 = new kt();
    knimeTable1.setDataTable(_representation.inObjects[0]);

    if (_value.options.mouseMode) {
      mouseMode = _value.options.mouseMode;
    }
    if (_value.options.selectedRows) {
      selectedRows = _value.options.selectedRows;
    }
    if (_value.options.highlitedPath) {
      highlitedPath = _value.options.highlitedPath;
    }

    transformData();
    setColors();
    drawControls();
    drawChart();
    toggleFilter();

    if (_representation.warnMessage != "") {
      knimeService.setWarningMessage(_representation.warnMessage, "representation_warnMessage");
    }
   
    if (_value.options.subscribeSelection) {
      knimeService.subscribeToSelection(knimeTable1.getTableId(), selectionChanged);
    }

    outputSelectionColumn();

    // CHECK: What does this actually do?
    if (parent !==undefined && parent.KnimePageLoader !==undefined) {
      parent.KnimePageLoader.autoResize(window.frameElement.id);
    }
  };

  // Transform data from first port into a hierarchical structure suitable
  // for a partition layout.
  var transformData = function() {
    // Get indices for path columns and frequency column.
    function indexOf(column) {
      return knimeTable1.getColumnNames().indexOf(column);
    }
    var pathColumns = _representation.options.pathColumns.map(indexOf);
    var freqColumn = indexOf(_representation.options.freqColumn);

    // Check which rows are included by the filter/selection.
    var includedRows = knimeTable1.getRows().filter(function(row) {
      var includedInFilter = !currentFilter || knimeTable1.isRowIncludedInFilter(row.rowKey, currentFilter);
      var includedInSelection = !_value.options.showSelectedOnly || selectedRows.length == 0 || selectedRows.indexOf(row.rowKey) != -1;
      return includedInFilter && includedInSelection;
    });
    
    // Get all unique labels from path columns.
    var notNull = function(value) { return value !== null; };
    var accumulate = function(accumulator, array) { return accumulator.concat(array); };
    var onlyUnique = function(value, index, self) { return self.indexOf(value) === index; };

    uniqueLabels = pathColumns
      .map(function(columnId) {
        var uniqueLabelsOfColumn = includedRows.map(function(row) {
          return row.data[columnId];
        })
        .filter(notNull)
        .filter(onlyUnique);

        return uniqueLabelsOfColumn;
      })
      .reduce(accumulate, [])
      .filter(onlyUnique);


    // make sure that reserved names do not collide whith user given classes
    while (uniqueLabels.indexOf(rootNodeName) > -1) {
      rootNodeName += "_";
    }
    while (uniqueLabels.indexOf(nullNodeName) > -1) {
      nullNodeName += "_";
    }

    var id = 0;

    // Initialize _data object
    _data = {
      id: id++,
      name: rootNodeName,
      children: [],
      active: false,
      highlited: false,
      selected: false
    };



    var missingSizeCount = 0;
    var missingPathCount = 0;

    // Create hierarchical structure.
    for (var i = 0; i < includedRows.length; i++) {

      var size = includedRows[i].data[freqColumn];
      if (size === null || isNaN(size)) {
        missingSizeCount++;
        size = 0;
      }
      size = Math.abs(size);

      // get array of path elements from current row
      var parts = pathColumns.map(function(col) { return includedRows[i].data[col]; });
      // Remove trailing nulls
      while(parts[parts.length-1] === null) {
        parts.pop();
      }

      if (parts.length === 0) {
        missingPathCount++;
      }

      // Loop over path elements,
      // append to hierarchical structure
      var currentNode = _data;
      for (var j = 0; j < parts.length; j++) {
        var children = currentNode["children"];
        if (parts[j] === null) {
          var nodeName = nullNodeName;
        } else {
          var nodeName = parts[j];
        }
        
        var childNode;
        if (j + 1 < parts.length) {
          // Not yet at the end of the sequence; move down the tree.
          var foundChild = false;
          for (var k = 0; k < children.length; k++) {
            if (children[k]["name"] === nodeName) {
              childNode = children[k];
              foundChild = true;
              break;
            }
          }
          // If we don't already have a child node for this branch, create it.
          if (!foundChild) {
            childNode = {
              id: id++,
              name: nodeName,
              children: [],
              active: false,
              highlited: false,
              selected: false
            };
            children.push(childNode);
          }
          currentNode = childNode;
        } else {
          // Reached the end of the sequence; create a leaf node.
          childNode = {
            id: id++,
            name: nodeName,
            size: size,
            children: [],
            active: false,
            highlited: false,
            selected: false,
            rowKey: includedRows[i].rowKey
          };
          children.push(childNode);

          // Add id of leaf to [row -> leaf]-data-structure. 
          rowKey2leaf[includedRows[i].rowKey] = childNode;
        }
      }
    }

    if (missingPathCount > 0) {
      knimeService.setWarningMessage(missingPathCount + " rows are not display because of missing path.", "missingPathCount");
    }
    if ((_representation.options.freqColumn != null) && (missingSizeCount > 0) ) {
      knimeService.setWarningMessage(missingSizeCount + " have a missing numeric value. The value defaults to zero.", "missingSizeCount");
    }
  };

  var setColors = function() {
    var useCustomColors = (_representation.inObjects[1] != null) && (_representation.inObjects[1].labels != null);
    var showWarning = (_representation.inObjects[1] != null) && (_representation.inObjects[1].labels == null);

    if (showWarning) {
      knimeService.setWarningMessage("Your color model does not provide a 'label' attribute.", "colormodel");
    }

    if (useCustomColors) {
      // TODO: check if nominal
      var colors = _representation.inObjects[1].colors;
      var labels = _representation.inObjects[1].labels;
      var colorMap = {}
      for (var i = 0; i < colors.length; i++) {
        colorMap[labels[i]] = colors[i];
      }
    } else {
      if (uniqueLabels.length <= 10) {
        var scale = d3.scale.category10();
      } else {
        var scale = d3.scale.category20();
      }

      var colorMap = {};
      uniqueLabels.forEach(function(label) { colorMap[label] = scale(label); });
    }

    _colorMap = function(label) {
      if (label === rootNodeName || label === nullNodeName) {
        return "#FFFFFF";
      } else {
        if (colorMap.hasOwnProperty(label)) {
          return colorMap[label];
        } else {
          return "#000000";
        }
      }
    }
    _colorMap.entries = d3.entries(colorMap);
    _colorMap.keys = d3.keys(colorMap);
  };

  var updateChart = function() {
    transformData();
    setColors();
    drawChart();
  };

  var updateTitles = function(updateChart) {
    d3.select("#title").text(this.value);
    d3.select("#subtitle").text(_value.options.subtitle);

    if (updateChart) {
       drawChart();
    }
  };

  var drawChart = function() {
    // Remove earlier chart.
    d3.select("#layoutContainer").remove();

    /*
     * Parse some options.
     */
    var optFullscreen = _representation.options.svg.fullscreen && _representation.runningInView;
    var isTitle = _value.options.title !== "" || _value.options.subtitle !== "";

    d3.selectAll("html, body")
      .style({
        "width": "100%",
        "height": "100%",
        "margin": "0",
        "padding": "0"
      });

    var body = d3.select("body");
    body.style({
      "font-family": "sans-serif",
      "font-size": "12px",
      "font-weight": "400",
    })

    // Determine available witdh and height.
    if (optFullscreen) {
      var width = "100%";

      if (isTitle || !_representation.options.enableViewControls) {
        knimeService.floatingHeader(true);
        var height = "100%";
      } else {
        knimeService.floatingHeader(false);
        var height = "calc(100% - " + knimeService.headerHeight() + "px)"
      }

    } else {
      var width = _representation.options.svg.width + 'px';
      var height = _representation.options.svg.height + 'px';
    }

    layoutContainer = body.append("div")
      .attr("id", "layoutContainer")
      .style({
        "width": width,
        "height": height,
        "min-width": MIN_WIDTH + "px",
        "min-height": MIN_HEIGHT + "px",
        "position": "absolute"
      });

    // create div container to hold svg
    var svgContainer = layoutContainer.append("div")
      .attr("id", "svgContainer")
      .style({
        "min-width": MIN_WIDTH + "px",
        "min-height": MIN_HEIGHT + "px",
        "box-sizing": "border-box",
        "overflow": "hidden",
        "margin": "0",
        "width": "100%",
        "height": "100%"
      });
  
    // Create the SVG object
    svg = svgContainer.append("svg")
      .attr("id", "svg")
      .style({
        "font-family": "sans-serif",
        "font-size": "12px",
        "font-weight": "400",
      });

    // set width / height of svg
    if (optFullscreen) {
      // TODO CHECK: Do I really need computedHeight/computedWidth ?
      var boundingRect = svgContainer.node().getBoundingClientRect();
      var svgWidth = boundingRect.width;
      var svgHeight = boundingRect.height;
    } else {
      var svgWidth = _representation.options.svg.width;
      var svgHeight = _representation.options.svg.height;
    }
    svg
      .style("width", svgWidth + "px")
      .style("height", svgHeight + "px")
      .attr("width", svgWidth)
      .attr("height", svgHeight);

    // Title
    svg.append("text")
      .attr("id", "title")
      .attr("font-size", 24)
      .attr("x", 20)
      .attr("y", 30)
      .text(_value.options.title);

    // Subtitle
    svg.append("text")
      .attr("id", "subtitle")
      .attr("font-size", 12)
      .attr("x", 20)
      .attr("y", 46)
      .text(_value.options.subtitle);


    // Compute plotting options
    var margin = {
      top : isTitle ? 60 : 10,
      left : 10,
      bottom : 10,
      right : 10
    };

    var plottingSurface = svg.append("g")
      .attr("id", "plottingSurface")
      .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    var w = Math.max(50, svgWidth - margin.left - margin.right);
    var h = Math.max(50, svgHeight - margin.top - margin.bottom);

    if (_data.children.length == 0) {
      svg.append("text")
        .attr("text-anchor", "middle")
        .attr("alignment-baseline", "central")
        .attr("x", w/2)
        .attr("y", h/2)
        .attr("id", "errorMsg")
        .text("Error: No data available")

    } else {
      var options = {
        legend: _value.options.legend,
        breadcrumb: _value.options.breadcrumb,
        zoomable: _representation.options.zoomable,
        donutHole: _value.options.donutHole,
        aggregationType: _value.options.aggregationType,
        filterSmallNodes: _value.options.filterSmallNodes
      };

      drawSunburst(_data, plottingSurface, w, h, options);
    }
    
    // Set resize handler
    if (optFullscreen) {
      var win = document.defaultView || document.parentWindow;
      win.onresize = resize;
    }
  };

  var drawSunburst = function(data, plottingSurface, width, height, options) {
    var marginTop = options.breadcrumb ? 40 : 0;
    var marginLeft = options.legend ? 85 : 0;

    // Dimensions of sunburst.
    var radius = Math.min(width - marginLeft, height - marginTop) / 2;

    // Breadcrumb dimensions: width, height, spacing, width of tip/tail.
    var b = { w: 100, h: 30, s: 3, t: 10 };

    // The partition layout returns a rectengular hierarchical layout in
    // a cartesian coordinate space. That is, nodes of the tree get the
    // attributes x, y, dx, dy (dx,dy = extent of node position).
    // In its original form the layout has a size of 1x1.
    // x maps the node's x and dx attribute to an angle.
    // y maps the node's y and dy attribute to vector length.
    var x = d3.scale.linear()
        .range([0, 2 * Math.PI]);

    var y = d3.scale.sqrt()
        .range([0, radius]);

    var partition = d3.layout.partition()
        .value(
          _representation.options.freqColumn == null
          ? function(d) { return 1; }
          : function(d) { return d.size; }
        )

    // Create list of segment objects with cartesian orientation from data.
    if (options.filterSmallNodes) {
      // For efficiency, filter nodes to keep only those large enough to see.
      nodes = partition.nodes(data)
        .filter(function(d) {
          return (d.dx > _representation.options.filteringThreshold);
        });
    } else {
      nodes = partition.nodes(data);
    }

    // Functions to map cartesian orientation of partition layout into radial
    // orientation of sunburst chart.
    var arc = d3.svg.arc()
        .startAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x))); })
        .endAngle(function(d) { return Math.max(0, Math.min(2 * Math.PI, x(d.x + d.dx))); })

    // Set display of donut hole depending on donut-hole-configuration and zoom-configuration.
    if (options.donutHole) {
      arc
        .innerRadius(function(d) { return Math.max(0, y(d.y)); })
        .outerRadius(function(d) { return Math.max(0, y(d.y + d.dy)); });
    } else {
      var rootSegmentExtent = nodes[0].dy;
      arc
        .innerRadius(function(d) { 
          var notZoomed = !(_representation.options.zoomable && _value.options.zoomedPath!=null)
          return Math.max(0, y(d.y - notZoomed * rootSegmentExtent));
        })
        .outerRadius(function(d) {
          var notZoomed = !(_representation.options.zoomable && _value.options.zoomedPath!=null)
          return Math.max(0, y(d.y + d.dy - notZoomed * rootSegmentExtent));
        });
    }

    // create new group for the sunburst plot (not legend, not breadcrumb)
    var sunburstGroup = plottingSurface.append("g")
        .attr("transform", "translate(" + ((width - marginLeft) / 2) + "," + ((height + marginTop) / 2) + ")")
        .attr("id", "sunburstGroup");

    // Bounding circle underneath the sunburst, to make it easier to detect
    // when the mouse leaves the plottingSurface g.
    sunburstGroup.append("svg:circle")
        .attr("r", radius)
        .style("opacity", 0);


    var path = sunburstGroup.selectAll("path")
        .data(nodes)
      .enter().append("path")
        .attr("d", arc)
        .attr("fill-rule", "evenodd")
        .attr("fill", function(d) { return _colorMap(d.name); })
        .attr("stroke",function(d) { return d.selected ? "black" : "white" })
        .attr("stroke-width", 1)
        .on("mouseover", mouseover)
        .on("click", click);

    // Basic setup of page elements.
    if (options.breadcrumb) {
      initializeBreadcrumbTrail(plottingSurface);
    }

    if (options.legend) {
      drawLegend(plottingSurface, options.breadcrumb, b.h);
    }

    // add explanation in the middle of the circle
    var explanation = sunburstGroup.append("g")
      .attr("id", "explanation")
      .style({
        "position": "absolute",
        "top": "260px",
        "left": "305px",
        "width": "140px",
        "text-align": "center",
        "color": "#666",
        "z-index": "-1",
      })
      .style("display", (options.donutHole && !options.zoom) ? "initial" : "none");

    explanation.append("text")
      .attr("id", "percentage")
      .attr("text-anchor", "middle")
      .attr("alignment-baseline", "middle")
      .style("font-size", "2.5em");
    explanation.append("text")
      .attr("id", "explanationText")
      .attr("text-anchor", "middle")
      .attr("alignment-baseline", "middle")
      .attr("y", 30)
      .style("font-size", "1.8em")
      .style("font-weight", "lighter");

    // Get total size of the tree = value of root node from partition.
    totalSize = path.node().__data__.value;
    
    // Set highliting
    if (_representation.options.highliting && mouseMode != "zoom" && highlitedPath != null) {
      var d = getNodeFromPath(highlitedPath); // nodes.filter(function(node) { return node.id == highlitedPath })[0];
      if (d != null) {
        highlite(d);
      }
    }

    // Set selection
    if (!_value.options.showSelectedOnly && selectedRows.length > 0 ) {
      selectedRows.forEach(function(rowKey) { rowKey2leaf[rowKey].selected = true; });
      renderSelection();
    }

    // Set zoom
    if (_value.options.zoomedPath && _representation.options.zoomable) {
      if (_value.options.breadcrumb) {
        updateBreadcrumb(d);
        toggleBreadCrumb(true);
      }

      var zoomNode = getNodeFromPath(_value.options.zoomedPath);
      path.transition()
        .duration(0)
        .attrTween("d", arcTweenZoom(zoomNode));
    }

    // Add the mouseleave handler to the bounding circle.
    sunburstGroup.on("mouseleave", mouseleave);

    // Handle clicks on sunburst segments
    function click(d) {
      if (mouseMode == "zoom") {
        clearHighliting();
        zoom(d);
      } else if (mouseMode == "select"){
        select(d);
        if (_value.options.showSelectedOnly) {
          highlitedPath = null;
          updateChart();
        } 
      } else {
        clearHighliting();
        setPropAllNodes('active', false);
        highlite(d);
      }
    }

    // Handle mouseover on sunburst segments
    function mouseover(d) {
      if ((mouseMode == "highlite") && highlitedPath == null) {
        // set sunburst segment properties
        setPropAllNodes('active', false);
        setPropsBackward(d, 'active', true);
        sunburstGroup.selectAll("path")
          .style("opacity", function(d) { return (d.active || d.highlited) ? 1 : 0.3; });

        updateStatisticIndicators(d);
        toggleBreadCrumb(true);
        toggleInnerLabel(true);
      }
    }
    
    // Handle mouseleave on sunburst segments
    function mouseleave(d) {
      if ((mouseMode == "highlite") && highlitedPath == null) {
        // set sunburst segment properties
        setPropAllNodes('active', true);
        sunburstGroup.selectAll("path")
          .style("opacity", function(d) { return ((highlitedPath == null) || d.highlited) ? 1 : 0.3; });

        toggleBreadCrumb(false);
        toggleInnerLabel(false);
      }
    }

    // Highliting one node and it's ancestors, show inner label / breadcrumb.
    function highlite(node) {
      highlitedPath = getUniquePathToNode(node);
      setPropAllNodes('active', false);
      setPropAllNodes('highlited', false);
      setPropsBackward(node, 'highlited', true);
      sunburstGroup.selectAll("path")
        .style("opacity", function(d) { return d.highlited ? 1 : 0.3; });

      updateStatisticIndicators(node);
      toggleBreadCrumb(true);
      toggleInnerLabel(true);
    }

    function select(node) {
      if (d3.event.shiftKey) {
        if (node.selected) {
          // Remove elements from selection.
          setPropsBackward(node, "selected", false);
          var leafs = setPropsForward(node, "selected", false);
          var rowKeys = leafs.map(function(leaf) { return leaf.rowKey; });
          for (var i = 0; i < rowKeys.length; i++) {
            var index = selectedRows.indexOf(rowKeys[i]);
            if (index > -1) {
              selectedRows.splice(index, 1);
            }
          }

          if (_value.options.publishSelection) {
            knimeService.removeRowsFromSelection(knimeTable1.getTableId(), rowKeys, selectionChanged);
          }
        } else {
          // Add element to selection.
          var leafs = setPropsForward(node, 'selected', true);
          addNodeToSelectionBackward(node);
          var rowKeys = leafs.map(function(leaf) { return leaf.rowKey; });
          for (var i = 0; i < rowKeys.length; i++) {
            var index = selectedRows.indexOf(rowKeys[i]);
            if (index == -1) {
              selectedRows.push(rowKeys[i]);
            }
          }

          if (_value.options.publishSelection) {
            knimeService.addRowsToSelection(knimeTable1.getTableId(), rowKeys, selectionChanged);
          }
        }
      } else {
        // Set selection.
        setPropAllNodes('selected', false);
        var leafs = setPropsForward(node, 'selected', true);
        addNodeToSelectionBackward(node);
        selectedRows =  leafs.map(function(leaf) { return leaf.rowKey; });

        if (_value.options.publishSelection) {
          knimeService.setSelectedRows(knimeTable1.getTableId(), selectedRows, selectionChanged);
        }
      }
      renderSelection();
    }

    // Restore everything to full opacity when moving off the visualization.
    clearHighliting = function(d) {
      highlitedPath = null;
      setPropAllNodes('highlited', false);

      sunburstGroup.selectAll("path")
        .style("opacity", 1);

      toggleBreadCrumb(false);
      toggleInnerLabel(false);
    }

    // Draw border around all selected segments.
    renderSelection = function() {
      if (_value.options.showSelectedOnly) {
        sunburstGroup.selectAll("path")
          .attr("stroke-width", 1)
          .attr("stroke", "white");
      } else {
        sunburstGroup.selectAll("path")
          .attr("stroke-width", function(d) {
            return d.selected ? 2 : 1;
          })
          .attr("stroke",function(d) {
            return d.selected ? "black" : "white";
          });
      }

    }

    clearSelection = function() {
      selectedRows = [];
      setPropAllNodes('selected', false);
      renderSelection();
      if (_value.options.publishSelection) {
        knimeService.setSelectedRows(knimeTable1.getTableId(), [], selectionChanged);
      }
    }

    // Traverse through tree and add nodes to selection.
    addNodeToSelectionBackward = function(node) {
      node.selected = true;
      var parent = node.parent;
      while (parent != null) {
        var allChildrenSelected = parent.children.every(function(child) { return child.selected; });
        if (allChildrenSelected) {
          parent.selected = true;
        } else {
          break;
        }
        parent = parent.parent;
      }
    }

    var zoom = function(d) {
      if (_value.options.donutHole && (d.name != rootNodeName)) {
        path.transition()
          .duration(750)
          .attrTween("d", arcTweenZoomWithHole(d));
      } else {
        path.transition()
          .duration(750)
          .attrTween("d", arcTweenZoom(d));
      }


      if (_value.options.breadcrumb) {
        updateBreadcrumb(d);
        toggleBreadCrumb(true);
      }

      if (d.name === rootNodeName) {
        delete _value.options.zoomedPath;
      } else {
        _value.options.zoomedPath = getUniquePathToNode(d);
      }
    }

    resetZoom = function() {
      zoom(_data);
    }

    // When zooming: interpolate the scales.
    function arcTweenZoom(d) {
      var xd = d3.interpolate(x.domain(), [d.x, d.x + d.dx]),
          yd = d3.interpolate(y.domain(), [d.y, 1]),
          yr = d3.interpolate(y.range(), [d.y ? 20 : 0, radius]);
      return function(d, i) {
        return i
            ? function(t) { return arc(d); }
            : function(t) { x.domain(xd(t)); y.domain(yd(t)).range(yr(t)); return arc(d); };
      };
    }

    // When zooming: interpolate the scales.
    function arcTweenZoomWithHole(d) {
      var rootRadius = d3.scale.sqrt().range([0, radius])(nodes[0].dy );
      var xd = d3.interpolate(x.domain(), [d.x, d.x + d.dx]),
          // d is the zoomed node. d.y is the new minimal "starting value" in cartesia system
          yd = d3.interpolate(y.domain(), [d.y, 1]),
          // This is the vector length range in the zoomed state
          yr = d3.interpolate(y.range(), [rootRadius, radius]);
          // We need clamping here
      return function(d, i) {
        return i
            // t has values between 0 and 1
            // 0: start animation, 1: stop animation
            // this function should then give
            // for t=0 the layout before animation
            // for t=1 the layout after animation
            ? function(t) { return arc(d); }
            : function(t) { debugger; x.domain(xd(t)); y.domain(yd(t)).range(yr(t)).clamp(true); return arc(d); };
            // TODO: set clamping at different position
      };
    }

    // Updates inner label and breadcrumb
    function updateStatisticIndicators(d) {
      if (_value.options.innerLabelStyle === "percentage") {
        var statistic = (100 * d.value / totalSize).toPrecision(3);
        var statisticString = statistic + "%";
        if (statistic < 0.1) {
          statisticString = "< 0.1%";
        }
      } else {
        var statistic = d.value;
        var statisticString = d3.format(".4s")(statistic);
      }

      // set inner label and breadcrumb
      updateInnerLabel(statisticString);
      updateBreadcrumb(d, statisticString);
    }

    function updateInnerLabel(statisticString) {
      d3.select("#percentage")
        .text(statisticString);

      d3.select("#explanationText")
        .text(_value.options.innerLabelText);
    }

    // Update the breadcrumb trail to show the current sequence and percentage.
    function updateBreadcrumb(d, statisticString) {
      // Get Ancestors
      var nodeArray = [];
      var current = d;
      while (current.parent) {
        nodeArray.unshift(current);
        current = current.parent;
      }
 
      // Data join; key function combines name and depth (= position in sequence).
      var g = d3.select("#trail")
          .selectAll("g")
          .data(nodeArray, function(d) { return d.name + d.depth; });

      // Add breadcrumb and label for entering nodes.
      var entering = g.enter().append("svg:g");

      entering.append("svg:polygon")
          .attr("points", breadcrumbPoints)
          .attr("fill", function(d) { return _colorMap(d.name); })
          .attr("stroke", function(d) { return d.name === nullNodeName ? "black" : "none"; });

      entering.append("svg:text")
          .attr("x", (b.w + b.t) / 2)
          .attr("y", b.h / 2)
          .attr("width", b.w)
          .attr("dy", "0.35em")
          .attr("text-anchor", "middle")
          .text(function(d) { return d.name; })
          .each(wrap);

      // Set position for entering and updating nodes.
      g.attr("transform", function(d, i) {
        return "translate(" + i * (b.w + b.s) + ", 0)";
      });

      // Remove exiting nodes.
      g.exit().remove();

      // Now move and update the percentage at the end.
      d3.select("#trail").select("#endlabel")
          .attr("x", (nodeArray.length + 0.5) * (b.w + b.s))
          .attr("y", b.h / 2)
          .attr("dy", "0.35em")
          .attr("text-anchor", "middle")
          .text(statisticString)

      // Make the breadcrumb trail visible, if it's hidden.
      d3.select("#trail")
          .style("visibility", "");
    }

    // Generate a string that describes the points of a breadcrumb polygon.
    function breadcrumbPoints(d, i) {
      var points = [];
      points.push("0,0");
      points.push(b.w + ",0");
      points.push(b.w + b.t + "," + (b.h / 2));
      points.push(b.w + "," + b.h);
      points.push("0," + b.h);
      if (i > 0) { // Leftmost breadcrumb; don't include 6th vertex.
        points.push(b.t + "," + (b.h / 2));
      }
      return points.join(" ");
    }

    // Show/hide inner label
    function toggleInnerLabel(visible) {
      if (_value.options.innerLabel && _value.options.donutHole) {
        d3.select("#explanation")
          .style("display", visible ? "initial" : "none");
      }
    }

    // Show/hide inner breadcrumb
    function toggleBreadCrumb(visible) {
      if (_value.options.breadcrumb)  {
        d3.select("#trail")
          .style("display", visible ? "initial" : "none");
      }
    }

    // Travers through tree and set property of nodes.
    function setPropsForward(start, prop, val) {
      var stack = [start];
      var leafs = [];

      while (stack.length > 0) {
        start = stack.pop();
        if (prop != null && val != null) {
          start[prop] = val;
        }

        if (start.children.length === 0) {
          leafs.push(start);
        } else {
          for (var i = 0; i < start.children.length; i++) {
            stack.push(start.children[i]);
          }
        }
      }

      return leafs;
    }

    // Travers through tree and set property of nodes.
    function setPropsBackward(start, prop, val) {
      while (start) {
        start[prop] = val;
        start = start.parent;
      }
    }

    function setPropAllNodes(prop, val) {
      for (var i = 0; i < nodes.length; i++) {
        nodes[i][prop] = val;
      }
    }

    function getUniquePathToNode(d) {
      var sequence = [] ;
      var parent = d;
      while (parent != null) {
        sequence.unshift(parent.name);
        parent = parent.parent;
      }
      var path = {sequence: sequence, isLeaf: d.children.length == 0};
      return path;
    }

    function getNodeFromPath(path) {
      var current = nodes[0];
      for (var i = 1; i < path.sequence.length-1; i++) {
        current = current.children
          .filter(function(child) { return child.name == path.sequence[i]; })[0];
      }
      var node = current.children
        .filter(function(child) { return child.name == path.sequence[path.sequence.length-1]; })
        .filter(function(child) { return (child.children.length == 0) == path.isLeaf; })[0];

      return node;
    }

    function initializeBreadcrumbTrail(plottingSurface) {
      // Add the svg area.
      var trail = plottingSurface.append("svg:svg")
          .attr("width", width)
          .attr("height", 50)
          .attr("id", "trail")

      // Add the label at the end, for the percentage.
      trail.append("svg:text")
        .attr("id", "endlabel")
        .style("fill", "#000");
    }

    function drawLegend(plottingSurface, breadcrumb, breadcrumbHeight) {
      var entries = uniqueLabels.map(function(label) {
        return { key: label, value: _colorMap(label) };
      }); 

      // Dimensions of legend item: width, height, spacing.
      var li = {
        w: 100, h: 15, s: 6, r: 6
      };

      var legend = plottingSurface.append("g")
          .attr("width", li.w)
          .attr("height", entries.length * (li.h + li.s))
          .attr("transform", "translate(" + (width - li.w) + ", " + (breadcrumb * breadcrumbHeight + 10) + ")");

      var g = legend.selectAll("g")
          .data(entries)
          .enter().append("svg:g")
          .attr("transform", function(d, i) {
                  return "translate(0," + i * (li.h + li.s) + ")";
               });


      g.append("svg:circle")
          .attr("cx", 0)
          .attr("cy", 0.5 * (li.h - li.r))
          .attr("r", li.r)
          .style("fill", function(d) { return d.value; });

      g.append("svg:text")
          .attr("x", li.r + 5)
          .attr("y", li.r)
          .attr("width", li.w)
          .attr("font-size", 12)
          .attr("dy", "0.35em")
          .text(function(d) { return d.key; })
          .each(wrap);
    }

    // Wrap text if too long.
    function wrap() {
      var self = d3.select(this),
        textLength = self.node().getComputedTextLength(),
        text = self.text(),
        width = self.attr("width");
      while (textLength+5 > width && text.length > 0) {
        text = text.slice(0, -1);
        self.text(text + '...');
        textLength = self.node().getComputedTextLength();
      }
    } 
  };

  var drawControls = function() {
    if (!knimeService || !_representation.options.enableViewControls) {
		  // TODO: error handling?
		  return;
	  }

    if (_representation.options.displayFullscreenButton) {
      knimeService.allowFullscreen();
    }

   	knimeService.addNavSpacer();


    // Reset controlls.
    if (_representation.options.zoomable) {
      knimeService.addButton('zoom-reset-button', 'search-minus', 'Reset Zoom', function() {
        resetZoom();
      });
    }
    if (_representation.options.selection) {
      knimeService.addButton('selection-reset-button', 'minus-square-o', 'Reset Selection', function() {
        clearSelection();
        if (_value.options.showSelectedOnly) {
          updateChart();
        }
      });
    }
    if (_representation.options.highliting) {
      knimeService.addButton('highlite-reset-button', 'star-o', 'Reset Highlite', function() {
        clearHighliting();
      });
    }

   	knimeService.addNavSpacer();

    if (mouseMode == null) {
      mouseMode = "highlite";
    }

    function toggleButton() {
      var targetID = "mouse-mode-" + mouseMode;
      d3.selectAll("#knime-service-header .service-button")
        .classed("active", function() {
          return targetID == this.getAttribute("id");
        });
     }

    // mouse mode controlls.
    if (_representation.options.zoomable) {
      knimeService.addButton('mouse-mode-zoom', 'search', 'Mouse Mode "Zoom"', function() {
        mouseMode = "zoom";
     	  toggleButton();
      });
    }
    if (_representation.options.selection) {
      knimeService.addButton('mouse-mode-select', 'check-square-o', 'Mouse Mode "Select"', function() {
        mouseMode = "select";
        if (_value.options.showSelectedOnly) {
          highlitedPath = null;
          updateChart();
        }
     	  toggleButton();
      });
    }
    if (_representation.options.highliting) {
      knimeService.addButton('mouse-mode-highlite', 'star', 'Mouse Mode "Highlite"', function() {
        mouseMode = "highlite";
     	  toggleButton();
      });
    }
    toggleButton();

    // Title / Subtitle configuration
    var titleEdit = _representation.options.enableTitleEdit;
    var subtitleEdit = _representation.options.enableSubtitleEdit;
  	if (titleEdit || subtitleEdit) {
  	  if (titleEdit) {
  	    var chartTitleText = knimeService.createMenuTextField(
  	        'chartTitleText', _value.options.title, function() {
  	      if (_value.options.title != this.value) {
  	        _value.options.title = this.value;
  	        updateTitles(true);
  	      }
  	    }, true);
  	    knimeService.addMenuItem('Chart Title:', 'header', chartTitleText);
  	  }
  	  if (subtitleEdit) {
  	    var chartSubtitleText = knimeService.createMenuTextField(
  	        'chartSubtitleText', _value.options.subtitle,
  	        function() {
  	        	if (_value.options.subtitle != this.value) {
  	        		_value.options.subtitle = this.value;
  	        		updateTitles(true);
  	        	}
  	        }, true);
  	    knimeService.addMenuItem('Chart Subtitle:', 'header', chartSubtitleText, null, knimeService.SMALL_ICON);
  	  }
  	}

    // Filter-small-nodes configuration
    var filterSmallNodesToggle = _representation.options.filterSmallNodesToggle;
    if (filterSmallNodesToggle) {
      knimeService.addMenuDivider();

      var filterSmallCheckbox = knimeService.createMenuCheckbox(
          'filterSmallNodesCheckbox', _value.options.filterSmallNodes, function() {
            _value.options.filterSmallNodes = this.checked;
            drawChart();
          });
      knimeService.addMenuItem('Filter out small nodes:', 'search', filterSmallCheckbox);
    }

    // Legend / Breacdcrumb
    var legendToggle = _representation.options.legendToggle;
    var breadcrumbToggle = _representation.options.breadcrumbToggle;
    if (legendToggle || breadcrumbToggle) {
      knimeService.addMenuDivider();

      if (legendToggle) {
        var legendCheckbox = knimeService.createMenuCheckbox(
            'legendCheckbox', _value.options.legend,
            function() {
              _value.options.legend = this.checked;
              drawChart();
            });
        knimeService.addMenuItem('Legend:', 'info-circle', legendCheckbox);
      }

      if (breadcrumbToggle) {
        var breadcrumbCheckbox = knimeService.createMenuCheckbox(
                'breadcrumbCheckbox', _value.options.breadcrumb,
                function() {
                  _value.options.breadcrumb = this.checked;
                  drawChart();
                  if (this.checked && highlitedPath != null) {
                    toggleBreadCrumb(true);
                  }
                });

        knimeService.addMenuItem('Breadcrumb:', 'ellipsis-h', breadcrumbCheckbox);
      }
    }

    // Donut hole configuration
    var donutHoleToggle = _representation.options.donutHoleToggle;
    if (donutHoleToggle) {
      knimeService.addMenuDivider();

      var donutHoleCheckbox = knimeService.createMenuCheckbox(
          'donutHoleCheckbox', _value.options.donutHole, function() {
            _value.options.donutHole = this.checked;
            drawChart();
          });
      knimeService.addMenuItem('Donut hole:', 'search', donutHoleCheckbox);
    }

    // Inner label configuration
    var innerLabelToggle = _representation.options.innerLabelToggle;
    var innerLabelStyleSelect = _representation.options.innerLabelStyleSelect;
    var enableInnerLabelEdit = _representation.options.enableInnerLabelEdit;
    if (innerLabelToggle || innerLabelStyleSelect || enableInnerLabelEdit) {
      knimeService.addMenuDivider();

      if (innerLabelToggle) {
        var innerLabelCheckbox = knimeService.createMenuCheckbox(
            'innerLabelCheckbox', _value.options.innerLabel,
            function() {
              _value.options.innerLabel = this.checked;
              toggleInnerLabel(true);
            });
        knimeService.addMenuItem('Inner Label:', 'dot-circle-o', innerLabelCheckbox);
      }

      if (innerLabelStyleSelect) {
        var innerLabelStyleSelector =
          knimeService.createMenuSelect('innerLabelStyleSelector', _value.options.innerLabelStyle, innerLabelStyles, function() {
            _value.options.innerLabelStyle = this.options[this.selectedIndex].value;
            drawChart();
          });
        knimeService.addMenuItem('Inner Label Style:', 'percent', innerLabelStyleSelector);
      }

      if (enableInnerLabelEdit) {
  	    var innerLabelText = knimeService.createMenuTextField(
  	        'innerLabelText', _value.options.innerLabelText, function() {
    	        _value.options.innerLabelText = this.value;
    	        drawChart();
  	        }, true);
  	    knimeService.addMenuItem('Inner Label Text:', 'header', innerLabelText);
  	  }
    }

    // show selection only
    if (_representation.options.selection && _representation.options.showSelectedOnlyToggle) {
      knimeService.addMenuDivider();
      var showSelectedOnlyCheckbox = knimeService.createMenuCheckbox('showSelectedOnlyCheckbox', _value.showSelectedOnly, function() {
        _value.options.showSelectedOnly = this.checked;
        if (this.checked) {
          highlitedPath = null;
        }
        highlitedPath = null;
        updateChart();
      });
      knimeService.addMenuItem('Show selected rows only', 'filter', showSelectedOnlyCheckbox);
    }
    
    if (knimeService.isInteractivityAvailable()) {
      // Selection / Filter configuration
      var publishSelectionToggle = _representation.options.publishSelectionToggle;
      var subscribeSelectionToggle = _representation.options.subscribeSelectionToggle;
      var subscribeFilterToggle = _representation.options.subscribeFilterToggle;
      if (publishSelectionToggle || subscribeSelectionToggle || subscribeFilterToggle) {
          knimeService.addMenuDivider();

          if (publishSelectionToggle) {
              var pubSelIcon = knimeService.createStackedIcon('check-square-o', 'angle-right', 'faded left sm', 'right bold');
              var pubSelCheckbox = knimeService.createMenuCheckbox('publishSelectionCheckbox', _value.options.publishSelection, function() {
                  if (this.checked) {
                      _value.options.publishSelection = true;
                  } else {
                      _value.options.publishSelection = false;
                  }
              });

              knimeService.addMenuItem('Publish selection', pubSelIcon, pubSelCheckbox);
          }

          if (subscribeSelectionToggle) {
              var subSelIcon = knimeService.createStackedIcon('check-square-o', 'angle-double-right', 'faded right sm', 'left bold');
              var subSelCheckbox = knimeService.createMenuCheckbox('subscribeSelectionCheckbox', _value.options.subscribeSelection, function() {
                  if (this.checked) {
                      _value.options.subscribeSelection = true;
                      knimeService.subscribeToSelection(knimeTable1.getTableId(), selectionChanged);
                  } else {
                      _value.options.subscribeSelection = false;
                      knimeService.unsubscribeSelection(knimeTable1.getTableId(), selectionChanged);
                  }
              });
              knimeService.addMenuItem('Subscribe to selection', subSelIcon, subSelCheckbox);
          }

          if (subscribeFilterToggle) {
              var subFilIcon = knimeService.createStackedIcon('filter', 'angle-double-right', 'faded right sm', 'left bold');
              var subFilCheckbox = knimeService.createMenuCheckbox('subscribeFilterCheckbox', _value.options.subscribeFilter, function() {
                _value.options.subscribeSelection = this.checked;
                toggleFilter();
              });
              knimeService.addMenuItem('Subscribe to filter', subFilIcon, subFilCheckbox);
          }
      }
    }
  }

  var selectionChanged = function(data) {
    if (data.changeSet) {
      if (data.changeSet.removed) {
        for (var i = 0; i < data.changeSet.removed.length; i++) {
          var removedKey = data.changeSet.removed[i];
          var parent = rowKey2leaf[removedKey];
          while (parent != null) {
            parent.selected = false;
            parent = parent.parent;
          }
          var index = selectedRows.indexOf(removedKey);
          if (index > -1) {
            selectedRows.splice(index, 1);
          }           
        }
      }
      if (data.changeSet.added) {
        for (var i = 0; i < data.changeSet.added.length; i++) {
          var addedKey = data.changeSet.added[i];
          var leaf = rowKey2leaf[addedKey];
          addNodeToSelectionBackward(leaf);
          var index = selectedRows.indexOf(addedKey);
          if (index == -1) {
            selectedRows.push(addedKey);
          }           
        }
      }
    } else if (data.reevaluate) {
      selectedRows = knimeService.getAllRowsForSelection(knimeTable1.getTableId());
      setPropAllNodes("selected", false);
      for (var i = 0; i < selectedRows.length; i++) {
        var leaf = rowKey2leaf[rowKey];
        addNodeToSelectionBackward(leaf);
      }
    }

    if (_value.options.showSelectedOnly) {
      highlitedPath = null;
      updateChart();
    }
    renderSelection();
  };

  var toggleFilter = function() {
    if (_value.options.subscribeFilter) {
      knimeService.subscribeToFilter(
        knimeTable1.getTableId(), filterChanged, knimeTable1.getFilterIds()
      );
    } else {
      knimeService.unsubscribeFilter(knimeTable1.getTableId(), filterChanged);
    }
  };

  var filterChanged = function(filter) {
    currentFilter = filter;
    highlitedPath = null;
    transformData();
    drawChart();
  };

  var resize = function(event) {
    drawChart();
  };

  var outputSelectionColumn = function() {
    if (_representation.options.selection) {
      _value.outColumns.selection = {};
      // set selected = false for every row
      knimeTable1.getRows().forEach(function(row) {
        _value.outColumns.selection[row.rowKey] = false;
      });
      // set selected = true for every selected row
      selectedRows.forEach(function(rowKey) {
        _value.outColumns.selection[rowKey] = true;
      });
    }
  };

  view.validate = function() {
    return true;
  };

  view.getComponentValue = function() {
    outputSelectionColumn();

    // Save mousemode unless its default mode.
    _value.options.mouseMode = mouseMode;
    if (_value.options.mouseMode == "highlite") {
      delete _value.options.mouseMode;
    }
    _value.options.selectedRows = selectedRows;
    if (_value.options.selectedRows.length == 0) {
      delete _value.options.selectedRows;
    }
    _value.options.highlitedPath = highlitedPath;
    if (_value.options.highlitedPath == null) {
      delete _value.options.highlitedPath;
    }

    return _value;
  };

  view.getSVG = function() {
    var svgElement = d3.select("svg")[0][0];
    // Return the SVG as a string.
    return (new XMLSerializer()).serializeToString(svgElement);
  };

  return view;
}());
