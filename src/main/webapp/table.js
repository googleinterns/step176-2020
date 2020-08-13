google.charts.load('current', {'packages':['table', 'corechart', 'controls']});
google.charts.setOnLoadCallback(dashboardInit);

async function dashboardInit() {
  let dashboard = new DashboardManager();
  await dashboard.updateNormal();
  dashboard.draw();
}
