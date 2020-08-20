class ChartUtil {
  static filterDataFromParent(baseData, filterCol, parent) {
    let filtered = null;
    if (ChartUtil.hasSelection(parent)) {
      // The pieChart is being created from slice of parent, filter its data accordingly.
      filtered = new google.visualization.DataView(baseData);
      filtered.setRows(baseData.getFilteredRows([{'column': filterCol, 'value': ChartUtil.getSelectedValue(parent)}]));
    } else {
      filtered = baseData;
    }
    return filtered;
  }

  static addOverwriteableEvent(chart, eventType, func) {
    if (chart.listener != null) {
      google.visualization.events.removeListener(chart.listener);
    }

    chart.listener  = google.visualization.events.addListener(chart, eventType, func);
  }

  static hasSelection(chart) {
    return chart != null && chart.getChart().getSelection().length > 0;
  }

  static getSelectedValue(chart) {
    let cell = chart.getChart().getSelection()[0];
    const row = cell.row == null ? 0 : cell.row;
    const col = cell.col == null ? 0 : cell.col;

    return chart.getDataTable().getValue(row, col);
  }

  static removeAllChildren(chart) {
    let curr = chart.getOption('childChart');
    while (curr != null) {
      let temp = curr.getOption('childChart');
      curr.getChart().clearChart();
      curr = temp;
    }
    chart.setOption('childChart', null);
  }
}
