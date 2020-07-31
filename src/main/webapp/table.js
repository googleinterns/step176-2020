google.charts.load('current', {'packages':['table', 'corechart', 'controls']});
google.charts.setOnLoadCallback(dashboardInit);

function dashboardInit() {
  let dashboard = new DashboardManager();
  dashboard.initData();
  dashboard.draw();
}
