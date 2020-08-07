class DashboardManager {
  constructor() {
    this.data = this.initData();

    this.dashboard = new google.visualization.Dashboard(document.getElementById('dashboard'));
    this.aggregationSelector = createNewAggregationSelector();
    this.tableManager = new TableManager();
    this.pieChart = createNewPieChart('piechart-container');

    this.tableManager.setDataTable(this.data);
    this.pieChart.setDataTable(this.data);

    this.pieChartDiv = document.getElementById('chart');

    google.visualization.events.addListener(
        this.aggregationSelector, 'statechange', this.updateAndDrawData.bind(this));

    this.updateModal = new Modal('update-modal', true);

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

      this.pieChartDiv.classList.remove('chart-hidden');
    } else {
      await this.updateNormal();

      this.pieChartDiv.classList.add('chart-hidden');
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

    // Get fields we are aggregating by and convert from user-displayed name to API name.
    const queryStringVals =
        selectorState.map(displayName => getAnnotatedFieldFromDisplay(displayName).API);
    await (fetch(`/aggregate?aggregationField=${queryStringVals.join()}`)
        .then(response => response.json())
        .then(response => {
            let results = response.response;
            for (let entry of results) {
              const row =
                  selectorState.map(displayName => entry[getAnnotatedFieldFromDisplay(displayName).API]);
              row.push(entry.count);

              if (entry.deviceIds == null) {
                entry.deviceIds = ['abc', 'def', 'ghi', 'jkl', 'mno'];
              }
              row.push(JSON.stringify(entry.deviceIds));

              this.data.addRow(row);
            }
    }));

    this.tableManager.updateAggregation(this.data);

    // Setup pie chart
    removeAllChildren(this.pieChart);
    this.configurePieChart(this.pieChart, this.data, selectorState, 1);
  }

  /* Setup data for standard table view */
  async updateNormal() {
    // TODO: use real data
    this.data = this.initData();
    this.tableManager.updateNormal(this.data);
  }

  /* Create a (sub)PieChart with the appropriate data and event handlers */
  configurePieChart(pieChart, baseData, selectorState, depth, parent) {
    // Filter relevant entries from baseData based on which slice (if any) was selected
    let filtered = filterDataFromParent(baseData, depth, parent);

    setChartTitle(pieChart, parent, selectorState, depth);

    // Perform the aggregation and set the result as the pieChart's data
    let result = google.visualization.data.group(
        filtered,
        [depth - 1],
                    // Devices count is second to last column
        [{'column': filtered.getNumberOfColumns() - 2, 'aggregation': google.visualization.data.sum, 'type': 'number'}]);
    pieChart.setView({'columns': [0, 1]});
    pieChart.setDataTable(result);

    if (!isLastAggregation(selectorState.length, depth)) {
      // We only want one event listener at a time, so we must remove/overwrite the previous one.
      addOverwriteableChartEvent(
        pieChart,
        'select',
        this.onSliceSelect.bind(this, pieChart, filtered, selectorState, depth + 1));
    } else {
      addOverwriteableChartEvent(
        pieChart,
        'select',
        () => {
          let selectedRow = filterDataFromParent(filtered, depth + 1, pieChart);

          let selectedValues =
              [...Array(selectorState.length).keys()].map(index => selectedRow.getValue(0, index));
          let deviceIds = selectedRow.getValue(0, selectedRow.getNumberOfColumns() - 1);
          // deviceIds is a serialized string so we can't directly get number of devices from it
          let devicesCount = selectedRow.getValue(0, selectedRow.getNumberOfColumns() - 2);

          let bodyElements = this.createModalBody(deviceIds, selectedValues, devicesCount);
          this.updateModal.setHeader('Perform Bulk Update');
          this.updateModal.setBody(bodyElements);
          this.updateModal.show();
        });
    }
  }

  onSliceSelect(parent, baseData, selectorState, depth) {
    // Remove all current sub pie charts because a new slice has been selected and
    // new sub pie charts will be generated
    removeAllChildren(parent);

    this.createSubPieChart(parent, baseData, selectorState, depth);
  }

  createSubPieChart(parent, baseData, selectorState, depth) {
    const id = 'chart-' + depth;
    let pieChart = createNewDivWithPieChart(id, calcChartDiameter(depth));

    this.configurePieChart(pieChart, baseData, selectorState, depth, parent);
    parent.childChart = pieChart;

    this.draw();
  }

  isAggregating() {
    // If no aggregation tags are selected, we are not currently aggregating
    return this.aggregationSelector.getState().selectedValues.length != 0;
  }

  createModalWarning(devicesCount, selectedValues) {
    let div = document.createElement('div');

    let p1 = document.createElement('p');
    p1.innerHTML  = `Warning: You are about to update <b>${devicesCount}</b> devices with the following properties:`;

    let ul = document.createElement('ul');
    for (let i = 0; i < selectedValues.length; i++) {
      let li = document.createElement('li');

      const aggregationField = this.aggregationSelector.getState().selectedValues[i];
      const aggregationFieldValue = selectedValues[i];

      li.innerHTML = `<b>${aggregationField}</b>: ${aggregationFieldValue}`;

      ul.appendChild(li);
    }

    let p2 = document.createElement('p');
    p2.innerText = 'This is a non-reversible action.  To continue, enter the desired values and press submit.';

    div.appendChild(p1);
    div.appendChild(ul);
    div.appendChild(p2);

    return div;
  }

  createModalForm(deviceIds, selectedValues) {
    let form = document.createElement('form');
    form.setAttribute('method', 'POST');
    form.setAttribute('action', '/update');

    for (let i = 0; i < selectedValues.length; i++) {
      const aggregationField = this.aggregationSelector.getState().selectedValues[i];
      const aggregationFieldValue = selectedValues[i];

      let label = document.createElement('label');
      label.innerHTML = aggregationField;
      label.classList.add('update-label');

      let input = document.createElement('input');
      input.setAttribute('type', 'text');
      input.setAttribute('value', aggregationFieldValue);
      input.setAttribute('name', getAnnotatedFieldFromDisplay(aggregationField).API);
      input.classList.add('update-input');

      let container = document.createElement('div');
      container.appendChild(label);
      container.appendChild(input);

      form.appendChild(container);
    }

    let devicesInput = document.createElement('input');
    devicesInput.setAttribute('type', 'hidden');
    devicesInput.setAttribute('value', deviceIds);
    devicesInput.setAttribute('name', 'deviceIds');
    form.appendChild(devicesInput);

    let submit = document.createElement('input');
    submit.setAttribute('type', 'submit');
    form.appendChild(submit);

    return form;
  }

  createModalBody(deviceIds, selectedValues, devicesCount) {
    let warning = this.createModalWarning(devicesCount, selectedValues);
    let form = this.createModalForm(deviceIds, selectedValues);
    return [warning, form];
  }

  draw() {
    if (!this.drawnControls) {
      this.aggregationSelector.draw();
      this.drawnControls = true;
    }

    this.tableManager.draw();
    if (this.isAggregating()) {
      this.pieChart.draw();
      this.drawChildren(this.pieChart);
    }
  }

  drawChildren(chart) {
    while (chart.childChart != null) {
      chart = chart.childChart;
      chart.draw();
    }
  }
};

function isLastAggregation(aggregationsDesired, aggregationsDone) {
  return aggregationsDesired - aggregationsDone <= 0;
}

function filterDataFromParent(baseData, depth, parent) {
  let filtered = null;
  if (chartHasSelection(parent)) {
    // The pieChart is being created from slice of parent, filter its data accordingly.
    filtered = new google.visualization.DataView(baseData);
    filtered.setRows(baseData.getFilteredRows([{'column': depth - 2, 'value': getChartSelectedValue(parent)}]));
  } else {
    filtered = baseData;
  }
  return filtered;
}

function addOverwriteableChartEvent(chart, eventType, func) {
  if (chart.listener != null) {
    google.visualization.events.removeListener(chart.listener);
    chart.listener = null;
  }

  chart.listener = google.visualization.events.addListener(chart, eventType, func);
}

function chartHasSelection(chart) {
  return chart != null && chart.getChart().getSelection().length > 0;
}

function getChartSelectedValue(chart) {
  let cell = chart.getChart().getSelection()[0];
  const row = cell.row == null ? 0 : cell.row;
  const col = cell.col == null ? 0 : cell.col;

  return chart.getDataTable().getValue(row, col);
}

function removeAllChildren(chart) {
  let curr = chart.childChart;
  while (curr != null) {
    let temp = curr.childChart;
    curr.getChart().clearChart();
    curr = temp;
  }
  chart.childChart = null;
}

function setChartTitle(chart, parent, selectorState, depth){
  if (parent != null) {
    chart.setOption('title', parent.getOption('title') + ' > ' + selectorState[depth - 2] + ': '+ getChartSelectedValue(parent));
  } else {
    chart.setOption('title', 'Devices by ' + selectorState[0]);
  }
}

function calcChartDiameter(depth){
  const base = 350; //pixels;
  const decay = 0.9;

  return base * Math.pow(decay, depth-1);
}

function createNewPieChart(container, diameter) {
  if (diameter == null) {
    diameter = 350; // pixels
  }
  return new google.visualization.ChartWrapper({
      'chartType': 'PieChart',
      'containerId': container,
      'options': {
          'title': 'Devices by Location',
          'legend': 'none',
          // This needs to be hardcoded because the div moves which causes 100% to be innacurate
          'width': diameter,
          'height': diameter + 75, // room for title
          'chartArea': {
              'left': '5', 'width': '100%', 'height': '90%'
          }
      },
      'view': {'columns': [0, 1]}
  });
}

function createNewDivWithPieChart(container, diameter) {
  const chartContainer = document.getElementById('chart');
  const subChartContainer = document.createElement('div');
  subChartContainer.setAttribute('id', container);
  chartContainer.appendChild(subChartContainer);

  return createNewPieChart(container, diameter);
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
