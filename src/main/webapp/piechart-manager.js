import {ChartUtil} from './chart-util.js';

class PieChartManager {
  constructor(containerId, COLS) {
    this.container = document.getElementById(containerId);
    if (this.container == null) {
      throw new ReferenceError(`Div with ID "${containerId}" could not be found`);
    }
    this.pieChart = this.createNewPieChart('chart-1');

    // Since this is a reference it will automatically be updated as COLS is updated in dashboard manager.
    this.COLS = COLS;
  }

  update(data, selectorState) {
    ChartUtil.removeAllChildren(this.pieChart);
    this.configurePieChart(this.pieChart, data, selectorState, 1);
  }

  draw() {
    this.pieChart.draw();
    this.drawChildren(this.pieChart);
  }

  drawChildren(chart) {
    while (chart.getOption('childChart') != null) {
      chart = chart.getOption('childChart');
      chart.draw();
    }
  }

  /* Create a (sub)PieChart with the appropriate data and event handlers */
  configurePieChart(pieChart, baseData, selectorState, depth, parent) {
    // Filter relevant entries from baseData based on which slice (if any) was selected
    let filtered = ChartUtil.filterDataFromParent(baseData, depth - 2, parent);
    this.setChartTitle(pieChart, parent, selectorState, depth);

    // Perform the aggregation and set the result as the pieChart's data
    let result = google.visualization.data.group(
        filtered,
        [depth - 1],
        [{'column': this.COLS.DEVICE_COUNT, 'aggregation': google.visualization.data.sum, 'type': 'number'}]);
    pieChart.setView({'columns': [0, 1]});
    pieChart.setDataTable(result);

    this.makeAccessible(pieChart, depth, this.isLastAggregation(selectorState.length, depth));
    if (!this.isLastAggregation(selectorState.length, depth)) {
      // We only want one event listener at a time, so we must remove/overwrite the previous one.
      ChartUtil.addOverwriteableEvent(
        pieChart,
        'select',
        this.onSliceSelect.bind(this, pieChart, filtered, selectorState, depth + 1));
    } else {
      ChartUtil.addOverwriteableEvent(
        pieChart,
        'select',
        () => {
          let selectedRow = ChartUtil.filterDataFromParent(filtered, depth - 1, pieChart);

          let selectedValues =
              [...Array(selectorState.length).keys()].map(index => selectedRow.getValue(0, index));
          let deviceIds = selectedRow.getValue(0, this.COLS.DEVICE_ID);
          // deviceIds is a serialized string so we can't directly get number of devices from it
          let devicesCount = selectedRow.getValue(0, this.COLS.DEVICE_COUNT);

          document.dispatchEvent(new CustomEvent(
            'displayBulkUpdateMenu',
            {detail:
                {deviceIds: deviceIds,
                selectedValues: selectedValues,
                selectedFields: selectorState,
                devicesCount: devicesCount}}));
        });
    }
  }

  onSliceSelect(parent, baseData, selectorState, depth) {
    // Remove all current sub pie charts because a new slice has been selected and
    // new sub pie charts will be generated
    ChartUtil.removeAllChildren(parent);

    this.createSubPieChart(parent, baseData, selectorState, depth);
  }

  createSubPieChart(parent, baseData, selectorState, depth) {
    const id = 'chart-' + depth;
    let pieChart = this.createNewDivWithPieChart(id, this.calcChartDiameter(depth));

    this.configurePieChart(pieChart, baseData, selectorState, depth, parent);
    parent.setOption('childChart', pieChart);

    this.draw();
  }

  makeAccessible(chart, depth, isLastChart) {
    google.visualization.events.addOneTimeListener(chart, 'ready', () => {
      let message = null;
      if (isLastChart) {
        message = 'Activate to bulk update.';
      } else {
        message = 'Activate to create a sub-aggregation pie chart.';
      }

      let container = document.getElementById('chart-' + depth);
      let slices = [...container.getElementsByTagName('g')];
      slices = slices.slice(1, -1); // The first and last elements are not slices.
      for (let [index, slice] of slices.entries()) {
        let value = chart.getDataTable().getValue(index, depth - 1);
        let percentContainer = slice.getElementsByTagName('text')[0];
        let percent = percentContainer == null ? '< 5%' : percentContainer.innerHTML;
        slice.setAttribute('role', 'button');
        slice.setAttribute('aria-label', `${message} ${value}: ${percent}`);
        slice.setAttribute('tabindex', 0);
      }
    });
  }

  show() {
    this.container.classList.remove('chart-hidden');
  }

  hide() {
    this.container.classList.add('chart-hidden');
  }

  setChartTitle(chart, parent, selectorState, depth){
    if (parent != null) {
      chart.setOption('title',
          parent.getOption('title') + ' > ' + selectorState[depth - 2] + ': ' +
              ChartUtil.getSelectedValue(parent));
    } else {
      chart.setOption('title', 'Devices by ' + selectorState[0]);
    }
  }

  calcChartDiameter(depth){
    const base = 350; //pixels;
    const decay = 0.9;

    return base * Math.pow(decay, depth-1);
  }

  isLastAggregation(aggregationsDesired, aggregationsDone) {
    return aggregationsDesired - aggregationsDone <= 0;
  }

  createNewDivWithPieChart(container, diameter) {
    const chartContainer = document.getElementById('chart');
    const subChartContainer = document.createElement('div');
    subChartContainer.setAttribute('id', container);
    chartContainer.appendChild(subChartContainer);

    return this.createNewPieChart(container, diameter);
  }

  createNewPieChart(container, diameter) {
    if (diameter == null) {
      diameter = 350; // pixels
    }
    return new google.visualization.ChartWrapper({
        'chartType': 'PieChart',
        'containerId': container,
        'options': {
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
}

export {PieChartManager};
