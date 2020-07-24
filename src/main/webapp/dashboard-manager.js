class DashboardManager {
  constructor() {
    this.data = this.initData();

    this.dashboard = new google.visualization.Dashboard(document.getElementById('dashboard'));
    this.aggregationSelector = createNewAggregationSelector();
    this.table = createNewTable()
    this.pieChart = createNewPieChart();

    google.visualization.events.addListener(
        this.aggregationSelector, 'statechange', this.updateData.bind(this));
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

  updateData() {
    this.data = new google.visualization.DataTable();

    if (this.isAggregating()) {
      // Setup data for aggregated table/chart view
      this.data.addColumn('string', 'Aggregation Field Value');
      this.data.addColumn('number', 'Devices');

      this.data.addRow(['California', 75]);
      this.data.addRow(['New Jersey', 50]);
      this.data.addRow(['Missouri', 10]);
      this.data.addRow(['', 2]);

    } else {
      // Setup data for standard table view
      this.data.addColumn('string', 'Serial Number');
      this.data.addColumn('string', 'Status');
      this.data.addColumn('string', 'Asset ID');
      this.data.addColumn('string', 'User');
      this.data.addColumn('string', 'Location');

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
