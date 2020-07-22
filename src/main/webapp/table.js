google.charts.load('current', {'packages':['table', 'corechart']});
google.charts.setOnLoadCallback(drawAllCharts);

function drawTable() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Serial Number');
  data.addColumn('string', 'Status');
  data.addColumn('string', 'Asset ID');
  data.addColumn('string', 'User');
  data.addColumn('string', 'Location');

  data.addRow(['SN12345', 'Provisioned', '1e76c3', 'James', 'Texas']);
  data.addRow(['SN54321', 'Provisioned', 'a9f27d', 'Justin', 'Alaska']);

  const options = {
    'title': 'Sample Table',
    'width': '100%',
    'height': '100%'
  };

  const table = new google.visualization.Table(document.getElementById('table-container'));
  table.draw(data, options);

}

function drawPieChart() {
  const data = new google.visualization.DataTable();
  data.addColumn('string', 'Aggregation Field Value');
  data.addColumn('number', 'Devices');

  data.addRow(['California', 75]);
  data.addRow(['New Jersey', 50]);
  data.addRow(['Missouri', 10]);
  data.addRow(['', 2]);

  const options = {
    'title': 'Devices by Location',
    'width': '100%',
    'height': '50%',
    'chartArea': {
        'left': '5', 'width': '100%', 'height': '90%'
    }
  };

  const chart = new google.visualization.PieChart(document.getElementById('piechart-container'));
  chart.draw(data, options);

}

function drawAllCharts() {
  drawTable();
  drawPieChart();
}
