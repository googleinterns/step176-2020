class DashboardManager {
  constructor() {
    this.data = this.initData();

    this.dashboard = new google.visualization.Dashboard(document.getElementById('dashboard'));
    this.aggregationSelector = createNewAggregationSelector();
    this.table = createNewTable()
    this.pieChart = createNewPieChart();

    google.visualization.events.addListener(
        this.aggregationSelector, 'statechange', this.updateAndDrawData.bind(this));
  }

  /* Get the initial data to be shown to the user and populate the main datatable*/
  initData() {
    let data = new google.visualization.DataTable();
    data.addColumn('string', 'Serial Number');
    data.addColumn('string', 'Status');
    data.addColumn('string', 'Asset ID');
    data.addColumn('string', 'User');
    data.addColumn('string', 'Location');

    data.addRow(['SN12345', 'Provisioned', '1e76c3', 'James', 'Texas']);
    data.addRow(['SN54321', 'Provisioned', 'a9f27d', 'Justin', 'Alaska']);

    return data;
  }

  async updateAndDrawData() {
    this.data = new google.visualization.DataTable();

    if (this.isAggregating()) {
      // Setup data for aggregated table/chart view
      this.data.addColumn('string', 'Aggregation Field Value');
      this.data.addColumn('number', 'Devices');

      await (fetch(`/aggregate?aggregationField=${this.aggregationSelector.getState().selectedValues[0]}`)
          .then(response => response.json())
          .then(data => {
              for (let [key, val] of Object.entries(data)) {
                this.data.addRow([key, val]);
              }
      }));
    } else {
      // Setup data for standard table view
      this.data.addColumn('string', 'Serial Number');
      this.data.addColumn('string', 'Status');
      this.data.addColumn('string', 'Asset ID');
      this.data.addColumn('string', 'User');
      this.data.addColumn('string', 'Location');

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
      this.data.addRow(['SN12345', 'Provisioned', '1e76c3', 'James', 'Texas']);
      this.data.addRow(['SN54321', 'Provisioned', 'a9f27d', 'Justin', 'Alaska']);
    }

    this.draw();
  }

  isAggregating() {
    // If no aggregation tags are selected, we are not currently aggregating
    return this.aggregationSelector.getState().selectedValues.length != 0;
  }

  draw() {
    this.dashboard.draw(this.data);

    this.table.setDataTable(this.data);

    this.table.draw();
    this.aggregationSelector.draw();

    if (this.isAggregating()) {
      this.pieChart.setDataTable(this.data);
      this.pieChart.draw();
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

function createNewPieChart() {
  return new google.visualization.ChartWrapper({
      'chartType': 'PieChart',
      'containerId': 'piechart-container',
      'options': {
          'title': 'Devices by Location',
          'legend': 'none',
          'width': '100%',
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
  aggregationOptions.addRows([['annotatedAssetId'], ['annotatedLocation'], ['annotatedUser']]);

  return new google.visualization.ControlWrapper({
      'controlType': 'CategoryFilter',
      'containerId': 'aggregation-input',
      'dataTable': aggregationOptions,
      'options': {
            'filterColumnIndex': '0',
            'ui': {
                'label': 'Aggregate By...',
                'selectedValuesLayout': 'aside'
            }
        }
  });
}
