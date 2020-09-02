import {BulkUpdateModal} from './bulk-update-modal.js';
import {Loading} from './loading.js';
import {PieChartManager} from './piechart-manager.js'
import {TableManager} from './table-manager.js';
import {AnnotatedFields, getAnnotatedFieldFromDisplay} from './fields.js';

class DashboardManager {
  constructor() {
    this.data = new google.visualization.DataTable();

    // Updated as the table changes
    this.COLS = {'DEVICE_IDS': 0, 'DEVICE_COUNT': 0, 'SERIAL_NUMBERS': 0, 'ROW_NUMBER': 0};

    this.dashboard = new google.visualization.Dashboard(document.getElementById('dashboard'));
    this.aggregationSelector = createNewAggregationSelector();
    this.tableManager = new TableManager('table-container');
    this.pieChartManager = new PieChartManager(/*ContainerId:*/ 'chart', this.COLS);

    google.visualization.events.addListener(
        this.aggregationSelector, 'statechange', this.updateAndDrawData.bind(this));

    google.visualization.events.addListener(this.aggregationSelector, 'statechange', () => {
      let containerDiv = document.getElementById('aggregation-input');

      let selectedTags = containerDiv.getElementsByTagName('li');
      for (let selectedTag of selectedTags) {
        let divs = selectedTag.getElementsByTagName('div');
        divs[0].setAttribute('aria-label', 'Activate to undo aggregation by ' + divs[1].innerText);
      }
    });

    document.addEventListener('bulkUpdate', (e) => {
      let row = e.detail;
      let deviceIds = this.data.getValue(row, this.COLS.DEVICE_IDS);
      let serialNumbers = this.data.getValue(row, this.COLS.SERIAL_NUMBERS);
      let selectedFields = this.aggregationSelector.getState().selectedValues;
      let selectedValues =
          [...Array(selectedFields.length).keys()]
          .map(index => this.data.getValue(row, index));
      let devicesCount = this.data.getValue(row, this.COLS.DEVICE_COUNT);

      let updateModal = new BulkUpdateModal('update-modal');
      updateModal.populateAndShowModal(deviceIds, serialNumbers, selectedValues, selectedFields, devicesCount);
    }, false);

    document.addEventListener('refreshData', (e) => {
      this.updateAndDrawData();
    });

    this.drawnControls = false;
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
    this.data.addColumn('string', '');
    this.data.addColumn('string', 'deviceIds');
    this.data.addColumn('string', 'serialNumbers');
    this.data.addColumn('number', 'row');

    let aggregationLoader = new Loading(this.fetchAndPopulateAggregation.bind(this), false);
    await aggregationLoader.load();

    // Based on how we decided to organize the table-- see top of table-manager.js for details
    this.COLS.ROW_NUMBER = this.data.getNumberOfColumns() - 1;
    this.COLS.SERIAL_NUMBERS = this.data.getNumberOfColumns() - 2;
    this.COLS.DEVICE_IDS = this.data.getNumberOfColumns() - 3;
    this.COLS.DEVICE_COUNT = this.data.getNumberOfColumns() - 5;

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

              row.push(`<button aria-label="Activate to bulk update devices in row." onclick="document.dispatchEvent(
                  new CustomEvent( \'bulkUpdate\', {detail: ${index} }))">Update Devices</button>`);

              row.push(JSON.stringify(entry.deviceIds));
              row.push(JSON.stringify(entry.serialNumbers));
              row.push(index);

              this.data.addRow(row);
            }
    }));
  }

  /* Setup data for standard table view */
  async updateNormal() {
    await this.tableManager.updateNormal();
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
                'allowTyping': false,
                'caption': 'Select a field by which to aggregate',
                'label': 'Aggregate By...',
                'selectedValuesLayout': 'aside',
                'sortValues': false
            }
        }
  });
}


async function handleLogin() {
  fetch('/status').then(response => response.json()).then((isLoggedIn) => {
    if (!isLoggedIn) {
      window.location.replace("/login");
    }
  });
}

function authorizeCallback(authResult) {
  if (authResult['code']) {
    $('#signinButton').attr('style', 'display: none');

    const codeMsg = 'code=' + authResult['code'];
    const request = new Request('/authorize', {
                                                method: 'POST',
                                                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', },
                                                body: codeMsg});
    fetch(request).then(response => {
        console.log('auth code sent'); //TODO: handle failure case
        document.location.href = '/index.html';
    });
  } else {
    console.log('user is not authorized');
    window.location.href = '/authorize.html';
  }
}

export {DashboardManager};
