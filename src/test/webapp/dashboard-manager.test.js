import {DashboardManager} from '../../main/webapp/dashboard-manager.js';
import {stubGoogleAPIs} from './stubs/google-charts.js';
import {ChartUtil} from '../../main/webapp/chart-util.js';
import {Loading} from '../../main/webapp/loading.js';
import {PieChartManager} from '../../main/webapp/piechart-manager.js';
import {TableManager} from '../../main/webapp/table-manager.js';
import {createAndAddToDOM, createElementWithId} from './testutils.js'
jest.mock('../../main/webapp/loading.js');
jest.mock('../../main/webapp/piechart-manager.js');
jest.mock('../../main/webapp/table-manager.js');

stubGoogleAPIs();

global.fetch = jest.fn((URL) => {
  if (URL == '/aggregate?aggregationField=annotatedUser') {
    return Promise.resolve({
      json: () => {return Promise.resolve(
        {"response":
          [
            {"annotatedUser":"user","count":1,"deviceIds":["0"], "serialNumbers":["SN0"]},
            {"annotatedUser":"Jane","count":2,"deviceIds":["1","2"], "serialNumbers":["SN1", "SN2"]}
          ]
        }
      )}
    });
  }
});

beforeEach(() => {
  document.body.innerHTML = "";
  let pieChartContainer = createAndAddToDOM('div', 'chart');
  let pageLeftButton = createAndAddToDOM('buttton', 'page-left');
  let pageRightButton = createAndAddToDOM('buttton', 'page-right');
  let pageDisplay = createAndAddToDOM('span', 'page-number');
  let pageSizeSelect = createAndAddToDOM('select', 'page-size-select');

  jest.clearAllMocks();
});

test('Constructor registers appropriate event listeners', () => {
  let bulkUpdateListener = jest.spyOn(document, 'addEventListener');
  let aggregationListener = jest.spyOn(google.visualization.events, 'addListener');

  let dashboard = new DashboardManager();

  expect(bulkUpdateListener.mock.calls[0][0]).toBe('bulkUpdate');

  expect(aggregationListener.mock.calls.length).toBe(2);
  expect(aggregationListener.mock.calls[0][0]).toBe(dashboard.aggregationSelector);
  expect(aggregationListener.mock.calls[0][1]).toBe('statechange');
});

test('Only draws aggregation selector once', () => {
  let dashboard = new DashboardManager();
  let aggregationSelectorDraw = jest.spyOn(dashboard.aggregationSelector, 'draw');

  for (let i = 0; i < 10; i++) {
    dashboard.draw();
  }

  expect(aggregationSelectorDraw.mock.calls.length).toBe(1);
});

test('Updating aggregation view', async () => {
  let dashboard = new DashboardManager();
  let selectedValues = ['User'];
  // device count, button, ids, serial numbers, and row number columns are always present
  let numOfCols = selectedValues.length + 5;

  jest.spyOn(dashboard.aggregationSelector, 'getState')
      .mockImplementation(() => {return {selectedValues: selectedValues}});
  jest.spyOn(dashboard.data, 'getNumberOfColumns')
      .mockImplementation(() => {return numOfCols;});


  let setupTableColumns = jest.spyOn(dashboard.data, 'addColumn');
  await dashboard.updateAggregation();

  expect(setupTableColumns.mock.calls.length).toBe(numOfCols);

  expect(Loading.mock.calls.length).toBe(1);
  // JSON.stringify workaround to solve this issue: https://github.com/facebook/jest/issues/8475
  expect(JSON.stringify(Loading.mock.calls[0][0])).toEqual(JSON.stringify(dashboard.fetchAndPopulateAggregation.bind(dashboard)));

  expect(dashboard.COLS.ROW_NUMBER).toBe(numOfCols - 1);
  expect(dashboard.COLS.SERIAL_NUMBERS).toBe(numOfCols - 2);
  expect(dashboard.COLS.DEVICE_IDS).toBe(numOfCols - 3);
  expect(dashboard.COLS.DEVICE_COUNT).toBe(numOfCols - 5);

  expect(PieChartManager.mock.instances[0].update.mock.calls[0]).toEqual([dashboard.data, selectedValues]);
  expect(TableManager.mock.instances[0].updateAggregation.mock.calls[0]).toEqual([dashboard.data]);
});

test('Fetching and Populating Aggregation Data', async () => {
  let dashboard = new DashboardManager();
  jest.spyOn(dashboard.aggregationSelector, 'getState')
      .mockImplementation(() => {return {selectedValues: ['User']}});

  let addData = jest.spyOn(dashboard.data, 'addRow');

  await dashboard.fetchAndPopulateAggregation();

  expect(addData.mock.calls.length).toBe(2);
  testAggregationAddRow(addData, 0, 'user', 1, "[\"0\"]", "[\"SN0\"]");
  testAggregationAddRow(addData, 1, 'Jane', 2, "[\"1\",\"2\"]", "[\"SN1\",\"SN2\"]");
});


// TODO: add tests for initData, updateNormal, etc. after oauth gets merged in
// because they will likely undergo significant changes


// Corresponds to the data given for aggregation in global.fetch mock.
function testAggregationAddRow(addRow, row, user, count, ids, serialNumbers) {
  expect(addRow.mock.calls[row][0][0]).toBe(user);
  expect(addRow.mock.calls[row][0][1]).toBe(count);
  expect(addRow.mock.calls[row][0][2]).toContain('button');
  expect(addRow.mock.calls[row][0][3]).toBe(ids);
  expect(addRow.mock.calls[row][0][4]).toContain(serialNumbers);
}
