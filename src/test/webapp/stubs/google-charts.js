let stubGoogleAPIs = function () {
    return window.google = {
        visualization: {
            ColumnChart: function() {
              return {
                  draw: function () { },
                  getSelection: function () { }
              }
            },
            //https://developers.google.com/chart/interactive/docs/reference#DataTable
            DataTable: function () {
                return {
                    addColumn: function (description_object) { },
                    addRow: function (opt_cellArray) { },
                    addRows: function (numOrArray) { },
                    clone: function() { },
                    getColumnId: function (columnIndex) { },
                    getColumnLabel: function (columnIndex) { },
                    getColumnPattern: function (columnIndex) { },
                    getColumnProperties: function (columnIndex) { },
                    getColumnProperty: function (columnIndex, name) { },
                    getColumnRange: function (columnIndex) { },
                    getColumnRole: function (columnIndex) { },
                    getColumnType: function (columnIndex) { },
                    getDistinctValues: function (columnIndex) { },
                    getFilteredRows: function (filters) { },
                    getFormattedValue: function (rowIndex, columnIndex) { },
                    getNumberOfColumns: function() { },
                    getNumberOfRows: function() { },
                    getProperties: function (rowIndex, columnIndex) {},
                    getProperty: function(rowIndex, columnIndex, name) {},
                    getRowProperties: function(rowIndex) {},
                    getRowProperty: function() {},
                    getSortedRows: function(sortedColumns) {},
                    getTableProperties: function () { },
                    getTableProperty: function (name) { },
                    getValue: function(rowIndex, columnIndex) {}
                }
            },

            //https://developers.google.com/chart/interactive/docs/reference#dataview-class
            DataView: function() {
                return {
                    setRows: function(rows) { }
                }
            },

            Dashboard: function() {
                return {}
            },
            //https://developers.google.com/chart/interactive/docs/reference#chartwrapperobject
            ChartWrapper: function () {
                return {
                    draw: function() { },
                    getChart: function() { },
                    getDataTable: function() { },
                    getOption: function(option) { },
                    getOptions: function() { return {}; },
                    setDataTable: function(dataTable) { },
                    setOption: function(optionName, value) { },
                    setOptions: function(options) { },
                    setView: function(view) { }
                }
            },
            //https://developers.google.com/chart/interactive/docs/reference#charteditor-class
            ChartEditor: {},
            data: {
                group: function(dataTable, carryoverCols, groupFunctionCols) { },
                sum: function() { }
            },
            ControlWrapper: function() {
                return {
                    draw: function() { },
                    getState: function() { return {selectedValues: []}; },
                };
            },
            events : {
                addListener: function(chart, eventType, callback) { return 10; },
                addOneTimeListener: function(chart, eventType, callback) {},
                removeListener: function(listenerId) { }
            }

        }
    };
};

export {stubGoogleAPIs};
