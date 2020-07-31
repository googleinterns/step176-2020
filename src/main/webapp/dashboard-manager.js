class DashboardManager {
  constructor() {
    this.data = this.initData();

    this.dashboard = new google.visualization.Dashboard(document.getElementById('dashboard'));
    this.aggregationSelector = createNewAggregationSelector();
    this.table = createNewTable();
    this.pieChart = createNewPieChart('piechart-container');

    this.table.setDataTable(this.data);
    this.pieChart.setDataTable(this.data);

    this.pieChartDiv = document.getElementById('chart');

    google.visualization.events.addListener(
        this.aggregationSelector, 'statechange', this.updateAndDrawData.bind(this));

    this.drawnControls = false;
  }

  /* Get the initial data to be shown to the user and populate the main datatable*/
  initData() {
    let data = new google.visualization.DataTable();
    data.addColumn('string', 'Serial Number');//this is fake data
    data.addColumn('string', 'Status');//TODO: integrate in real data
    data.addColumn('string', 'Asset ID');
    data.addColumn('string', 'User');
    data.addColumn('string', 'Location');

    // TODO: Use real data pulled from server.
    data.addRow(['SN12345', 'Provisioned', '1e76c3', 'James', 'Texas']);
    data.addRow(['SN54321', 'Provisioned', 'a9f27d', 'Justin', 'Alaska']);

    return data;
  }

  async updateAndDrawData() {
    this.data = new google.visualization.DataTable();

    if (this.isAggregating()) {
      await this.updateAggregation();

      this.pieChartDiv.classList.remove('chart-hidden');
    } else {
      await this.updateNormal();

      this.pieChartDiv.classList.add('chart-hidden');
    }

    this.draw();
  }

  async updateAggregation() {
    /* Setup data for aggregated table/chart view */
    let selectorState = this.aggregationSelector.getState().selectedValues;

    // Create appropriate columns for tables depending on what was selected
    for (let value of selectorState) {
      this.data.addColumn('string', value);
    }
    this.data.addColumn('number', 'Devices');

    const queryString = selectorState.map(displayName => getAnnotatedFieldFromDisplay(displayName).API);
    await (fetch(`/aggregate?aggregationField=${queryString.join()}`)
        .then(response => response.json())
        .then(response => {
            let results = response.response;
            for (let entry of results) {
              const row = selectorState.map(displayName => entry[getAnnotatedFieldFromDisplay(displayName).API]);
              row.push(entry.count);

              this.data.addRow(row);
            }
    }));

    this.table.setDataTable(this.data);

    let curr = this.pieChart.childChart;
    while (curr != null) {
      let temp = curr.childChart;
      curr.getChart().clearChart();
      curr = temp;
    }
    this.pieChart.childChart = null;
    this.configurePieChart(this.pieChart, this.data, selectorState, 1);
  }

  async updateNormal() {
    // Setup data for standard table view
    this.data = this.initData();
    /* TODO: once the server can send a list of devices, we can get real data here.
    await (fetch('/devices')
        .then(response => response.json())
        .then(deviceJsons => {
            for (let device of deviceJsons) {
              this.data.addRow([
                  device.serialNumber,
                  device.status,
                  device.assetId,
                  device.user,
                  device.location]);
            }
    }));
    */
    this.table.setDataTable(this.data);
  }

  configurePieChart(pieChart, base_data, selectorState, depth, parent) {
    let filtered = null;
    if (parent != undefined && parent.getChart().getSelection().length != 0) {
      filtered = new google.visualization.DataView(base_data);
      let filter = parent.getChart().getSelection();
      console.log(parent.getDataTable().toJSON());
      console.log(base_data.toJSON());
      console.log('---------------');
      filtered.setRows(base_data.getFilteredRows([{'column': depth - 2, 'value': parent.getDataTable().getValue(filter[0].row, 0)}]));
    } else {
      filtered = base_data;
    }

    let result = google.visualization.data.group(
        filtered,
        [depth - 1], // [0, 1, ..., depth-1]
        [{'column': filtered.getNumberOfColumns() - 1, 'aggregation': google.visualization.data.sum, 'type': 'number'}]);

    pieChart.setView({'columns': [0, 1]});
    pieChart.setDataTable(result);

    if (selectorState.length - depth > 0) {
      // Create a new pie chart based on the selected slice
      if (pieChart.listener != undefined) {
        google.visualization.events.removeListener(pieChart.listener);
        pieChart.listener = undefined;
      }
      pieChart.listener = google.visualization.events.addListener(
          pieChart, 'select', this.createSubPieChart.bind(this, pieChart, filtered, selectorState, depth + 1));
    } else {
      // TODO: Allow bulk updating the selected slice
    }

  }

  createSubPieChart(parent, base_data, selectorState, depth) {
    const chartContainer = document.getElementById('chart');

    const subChartContainer = document.createElement('div');
    const id = 'chart-' + depth;
    subChartContainer.setAttribute('id', id);

    chartContainer.appendChild(subChartContainer);

    let pieChart = createNewPieChart(id);
    console.log("Selection at depth ", depth, ": ", parent.getChart().getSelection());
    this.configurePieChart(pieChart, base_data, selectorState, depth, parent);
    parent.childChart = pieChart;
    this.draw();
  }

  isAggregating() {
    // If no aggregation tags are selected, we are not currently aggregating
    return this.aggregationSelector.getState().selectedValues.length != 0;
  }

  draw() {
    if (!this.drawnControls) {
      this.aggregationSelector.draw();
      this.drawnControls = true;
    }

    this.table.draw();
    if (this.isAggregating()) {
      this.pieChart.draw();

      let pieChart = this.pieChart;
      while (pieChart.childChart != null) {
        pieChart = pieChart.childChart;
        pieChart.draw();
      }
    }
  }
};




function createNewTable() {
  return new google.visualization.ChartWrapper({
      'chartType': 'Table',
      'containerId': 'table-container',
      'options': {
          'title': 'Sample Table',
          'width': '100%',
          'allowHtml': 'true',
          'cssClassNames': {
              'headerRow': 'table-header-row',
              'tableRow': 'table-row',
              'oddTableRow': 'table-row',
              'tableCell': 'table-cell'
          }
      }
  });
}

function createNewPieChart(container) {
  return new google.visualization.ChartWrapper({
      'chartType': 'PieChart',
      'containerId': container,
      'options': {
          'title': 'Devices by Location',
          'legend': 'none',
          // This needs to be hardcoded because the div moves which causes 100% to be innacurate
          'width': 350,
          'height': '50%',
          'chartArea': {
              'left': '5', 'width': '100%', 'height': '95%'
          }
      },
      'view': {'columns': [0, 1]}
  });
}

function createNewAggregationSelector() {
  const aggregationOptions = new google.visualization.DataTable();
  aggregationOptions.addColumn('string', 'Aggregation Options');
  aggregationOptions.addRows([
      [AnnotatedFields.ASSET_ID.DISPLAY],
      [AnnotatedFields.LOCATION.DISPLAY],
      [AnnotatedFields.USER.DISPLAY]]);

  return new google.visualization.ControlWrapper({
      'controlType': 'CategoryFilter',
      'containerId': 'aggregation-input',
      'dataTable': aggregationOptions,
      'options': {
            'filterColumnIndex': '0',
            'ui': {
                'label': 'Aggregate By...',
                'selectedValuesLayout': 'aside',
                'sortValues': false
            }
        }
  });
}
