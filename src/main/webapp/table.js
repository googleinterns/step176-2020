import {DashboardManager} from './dashboard-manager.js';
import {handleLogin} from './auth-flow.js';

google.charts.load('current', {'packages':['table', 'corechart', 'controls']});
google.charts.setOnLoadCallback(dashboardInit);

async function dashboardInit() {
  await handleLogin();
  let dashboard = new DashboardManager();
  await dashboard.updateNormal();
  dashboard.draw();
}
