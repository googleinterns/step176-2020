import {TableManager} from '../../main/webapp/table-manager.js';
import {stubGoogleAPIs} from './stubs/google-charts.js';
import {createAndAddToDOM} from './testutils.js';

const CONTAINER_ID = 'table-container';

stubGoogleAPIs();

global.fetch = jest.fn((URL) => {
  if (URL.toString() == window.location.href + 'devices?maxDeviceCount=10&pageToken=') {
    return Promise.resolve({
      json: () => {return Promise.resolve(
        {'chromeosdevices':
            [{'serialNumber': 'SN123',
            'status': 'Provisioned',
            'annotatedAssetId': 'ID123',
            'annotatedUser': 'user',
            'annoatedLocation': 'location'}],
        'nextPageToken': 'token'}
      )}
    });
  }
});

beforeEach(() => {
  document.body.innerHTML = "";
  let pageLeftButton = createAndAddToDOM('buttton', 'page-left');
  let pageRightButton = createAndAddToDOM('buttton', 'page-right');
  let pageDisplay = createAndAddToDOM('span', 'page-number');
  let pageSizeSelect = createAndAddToDOM('select', 'page-size-select');
  let tableContainer = createAndAddToDOM('div', CONTAINER_ID);

  jest.clearAllMocks();
});

test('Builds correct endpoint URL', () => {
  let manager = new TableManager(CONTAINER_ID);
  let url = manager.buildRequestURL();
  let expected = window.location.href +
      `devices?maxDeviceCount=${manager.pageSize}&pageToken=${manager.nextPageToken}`;

  expect(url.toString()).toEqual(expected);
});

test('Adding data to table when aggregating throws error', async () => {
  // Make sure the async assertions get called
  expect.assertions(1);
  let manager = new TableManager(CONTAINER_ID);
  manager.aggregating = true;

  try {
    await manager.addDataToTable();
  } catch (e) {
    expect(e.name).toEqual('Error');
  }
});

test('Adding data to table does not overwrite previous entries', async () => {
  let manager = new TableManager(CONTAINER_ID);
  manager.aggregating = false;

  // By using the managers current data table we can ensure it is not swapped out for a
  // new blank data table, meaning the existing data is persisted.
  let dataAdder = jest.spyOn(manager.baseDataTable, 'addRow');

  await manager.addDataToTable();

  expect(dataAdder.mock.calls.length).toBe(1);
});

test('UI is drawn correctly', () => {
  let manager = new TableManager(CONTAINER_ID);

  // With page set to 1 and hasNext page returning true, we expect both arrows to be enabled
  manager.currPage = 1;
  let hasNextPage = jest.spyOn(manager, 'hasNextPage').mockImplementationOnce(() => {return true});

  manager.draw();

  expect(document.getElementById('page-left').disabled).toBeFalsy();
  expect(document.getElementById('page-right').disabled).toBeFalsy();
  expect(document.getElementById('page-number').innerText).toBe(manager.currPage + 1);

  manager.currPage = 0;
  hasNextPage.mockImplementationOnce(() => {return false});
  manager.draw();
  expect(document.getElementById('page-left').disabled).toBeTruthy();
  expect(document.getElementById('page-right').disabled).toBeTruthy();
});

test('Resets data correctly', async () => {
  // Create a manager and give it some non-default state;
  let manager = new TableManager(CONTAINER_ID);
  manager.currPage = 7;
  manager.nextPageToken = '123';

  let dataConstructor = jest.spyOn(google.visualization, 'DataTable');
  await manager.resetNormalData();

  // A new table was created, the current page was reset to 0, and the next page token
  // was reset to the default value (i.e. is no longer equal to its old value)
  expect(dataConstructor.mock.calls.length).toBe(1);
  expect(manager.currPage).toBe(0);
  expect(manager.nextPageToken).not.toEqual('123');
});

test('Page change respects bounds', () => {
  let manager = new TableManager(CONTAINER_ID);
  manager.onPageChange({'pageDelta': -1});

  // Should ignore the request to go to a lower page and stay on 0.
  expect(manager.currPage).toBe(0);
});

test('Sets data table properly in aggregation view', () => {
  let manager = new TableManager(CONTAINER_ID);
  let newTable = new google.visualization.DataTable();
  manager.aggregating = true;
  let tableSetter = jest.spyOn(manager.table, 'setDataTable');

  manager.setDataTable(newTable);

  // When aggregating the table should be set to be the table that was passed in.
  expect(tableSetter.mock.calls.length).toBe(1);
  expect(tableSetter.mock.calls[0][0]).toBe(newTable);
});

test('Sets data table properly in normal view', () => {
  let manager = new TableManager(CONTAINER_ID);
  manager.aggregating = false;
  let tableSetter = jest.spyOn(manager.table, 'setDataTable');
  let view = new google.visualization.DataView();
  let viewConstructor = jest.spyOn(google.visualization, 'DataView').mockImplementation(() => {
    return view;
  });

  manager.setDataTable();

  // When in normal view, a new DataView should be created and set as the new table view.
  expect(tableSetter.mock.calls.length).toBe(1);
  expect(viewConstructor.mock.calls.length).toBe(1);
  expect(tableSetter.mock.calls[0][0]).toBe(view);
});
