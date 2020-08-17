import {ChartUtil} from '../../main/webapp/chart-util.js';
import {stubGoogleAPIs} from './stubs.js';

stubGoogleAPIs();

test('filterDataFromParent with no selection returns original data.', () => {
  const originalData = new google.visualization.DataTable();
  const filterCol = 2;
  const parent = new google.visualization.ChartWrapper();

  // Set the util to assume nothing has been selected, then try to filter
  jest.spyOn(ChartUtil, 'hasSelection').mockImplementationOnce(() => {return false;});
  const result = ChartUtil.filterDataFromParent(originalData, filterCol, parent);

  expect(originalData).toBe(result);
});

test('filterDataFromParent with selection calls google charts filter', () => {
  const originalData = new google.visualization.DataTable();
  const filterCol = 2;
  const parent = new google.visualization.ChartWrapper();

  // Set the util to assume something has been selected
  const selectedVal = 'test';
  jest.spyOn(ChartUtil, 'hasSelection').mockImplementationOnce(() => {return true;});
  jest.spyOn(ChartUtil, 'getSelectedValue').mockImplementationOnce(() => {return selectedVal;});

  const dataViewConstructor = jest.spyOn(google.visualization, 'DataView');
  const dataFilter = jest.spyOn(originalData, 'getFilteredRows');

  const result = ChartUtil.filterDataFromParent(originalData, filterCol, parent);

  // DataView constructor was called once with originalData passed in
  expect(dataViewConstructor.mock.calls.length).toBe(1);
  expect(dataViewConstructor.mock.calls[0][0]).toBe(originalData);

  // originalData.getFilteredRows was called once with correct filter args
  expect(dataFilter.mock.calls.length).toBe(1);
  expect(dataFilter.mock.calls[0][0]).toStrictEqual([{'column': filterCol, 'value': selectedVal}]);
});

test('addOverwriteableEvent removes previously added events', () => {
  const chart = new google.visualization.ChartWrapper();
  const eventType = 'select'
  const callback = () => {};
  const newCallback = () => {return 2;};

  const adder = jest.spyOn(google.visualization.events, 'addListener');
  const remover = jest.spyOn(google.visualization.events, 'removeListener');

  ChartUtil.addOverwriteableEvent(chart, eventType, callback);
  expect(adder.mock.calls.length).toBe(1);
  expect(adder.mock.calls[0]).toEqual([chart, eventType, callback]);
  expect(remover.mock.calls.length).toBe(0);

  ChartUtil.addOverwriteableEvent(chart, eventType, newCallback);
  expect(adder.mock.calls.length).toBe(2);
  expect(adder.mock.calls[1]).toEqual([chart, eventType, newCallback]);
  expect(remover.mock.calls.length).toBe(1);
});

test('getSelectedValue with null input', () => {
  const chart = new google.visualization.ChartWrapper();
  const data = [[1, 2], [3, 4]];
  const selectedData = [null, 1];

  // mock functions of datatable/chartwrapper
  const getSelection = () => {return [{'row': selectedData[0], 'col': selectedData[1]}]};
  const getValue = (row, col) => {return data[row][col];};
  jest.spyOn(chart, 'getChart').mockImplementationOnce(() => {return {getSelection: getSelection}});
  jest.spyOn(chart, 'getDataTable').mockImplementationOnce(() => {return {getValue: getValue}});

  // The null input should have been treated as a 0.
  const result = ChartUtil.getSelectedValue(chart);
  expect(result).toBe(data[0][selectedData[1]])
});
