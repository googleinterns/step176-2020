import {BulkUpdateModal} from './bulk-update-modal.js';
import {Loading} from './loading.js';
import {PieChartManager} from './piechart-manager.js'
import {TableManager} from './table-manager.js';
import {AnnotatedFields, getAnnotatedFieldFromDisplay} from './fields.js';

class DashboardManager {
  constructor() {
    this.data = this.initData();

    // Updated as the table changes
    this.COLS = {'DEVICE_ID': 0, 'DEVICE_COUNT': 0};

    this.dashboard = new google.visualization.Dashboard(document.getElementById('dashboard'));
    this.aggregationSelector = createNewAggregationSelector();
    this.tableManager = new TableManager();
    this.pieChartManager = new PieChartManager(/*ContainerId:*/ 'chart', this.COLS);

    this.tableManager.setDataTable(this.data);

    google.visualization.events.addListener(
        this.aggregationSelector, 'statechange', this.updateAndDrawData.bind(this));

    document.addEventListener('bulkUpdate', (e) => {
      let row = e.detail;
      let deviceIds = this.data.getValue(row, this.COLS.DEVICE_ID);
      let selectedValues =
          [...Array(this.aggregationSelector.getState().selectedValues.length).keys()]
          .map(index => this.data.getValue(row, index));
      let devicesCount = this.data.getValue(row, this.COLS.DEVICE_COUNT);

      document.dispatchEvent(new CustomEvent(
        'displayBulkUpdateMenu',
        {detail:
            {deviceIds: deviceIds,
            selectedValues: selectedValues,
            selectedFields: this.aggregationSelector.getState().selectedValues,
            devicesCount: devicesCount}}));
    }, false);

    this.updateModal = new BulkUpdateModal('update-modal');

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
    data.addRow(['SNABCDE', 'Provisioned', '71ec9a', 'Jake', 'Missouri']);

    return data;
  }

  async updateAndDrawData() {
    this.data = new google.visualization.DataTable();

    if (this.isAggregating()) {
      await this.updateAggregation();

      this.pieChartManager.show();
    } else {
      await this.updateNormal();

      this.pieChartManager.hide();
    }

    this.draw();
  }

  /* Setup data for aggregated table/chart view */
  async updateAggregation() {
    let selectorState = this.aggregationSelector.getState().selectedValues;

    // Create appropriate columns for tables depending on what was selected
    for (let value of selectorState) {
      this.data.addColumn('string', value);
    }
    this.data.addColumn('number', 'Devices');
    this.data.addColumn('string', 'deviceIds');
    this.data.addColumn('string', '');

    let aggregationLoader = new Loading(this.fetchAndPopulateAggregation.bind(this), false);
    await aggregationLoader.load();

    this.COLS.DEVICE_ID = this.data.getNumberOfColumns() - 2;
    this.COLS.DEVICE_COUNT = this.data.getNumberOfColumns() - 3;

    this.tableManager.updateAggregation(this.data);
    this.pieChartManager.update(this.data, selectorState);
  }

  async fetchAndPopulateAggregation() {
    let selectorState = this.aggregationSelector.getState().selectedValues;

    // Get fields we are aggregating by and convert from user-displayed name to API name.
    const queryStringVals =
        selectorState.map(displayName => getAnnotatedFieldFromDisplay(displayName).API);
    await (fetch(`/aggregate?aggregationField=${queryStringVals.join()}`)
        .then(response => response.json())
        .then(response => {
            let results = response.response;
            for (let [index, entry] of results.entries()) {
              const row =
                  selectorState.map(displayName => entry[getAnnotatedFieldFromDisplay(displayName).API]);
              row.push(entry.count);

              // TODO: remove fake data after the server-side is updated to provide real data.
              if (entry.deviceIds == null) {
                entry.deviceIds = ['abc', 'def', 'ghi', 'jkl', 'mno'];
              }
              row.push(JSON.stringify(entry.deviceIds));

              row.push(`<button onclick="document.dispatchEvent(
                  new CustomEvent( \'bulkUpdate\', {detail: ${index} }))">Update Devices</button>`);

              this.data.addRow(row);
            }
    }));
  }

  /* Setup data for standard table view */
  async updateNormal() {
    // TODO: use real data
    this.data = this.initData();
    this.tableManager.updateNormal(this.data);
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

    this.tableManager.draw();
    if (this.isAggregating()) {
      this.pieChartManager.draw();
    }
  }
};

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

export {DashboardManager};
