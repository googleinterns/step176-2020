class TableManager {
  constructor() {
    this.table = this.createNewTable();
    this.currPage = 0;
    this.pageSize = 1;

    this.pageLeft = document.getElementById('page-left');
    this.pageRight = document.getElementById('page-right');
    this.pageNumber = document.getElementById('page-number');

    google.visualization.events.addOneTimeListener(this.table, 'ready', () => {
      google.visualization.events.addListener(
          this.table.getChart(), 'page', this.onPageChange.bind(this));
    });

    this.pageLeft.onclick = () => {
      google.visualization.events.trigger(this.table.getChart(), 'page', {'page': -1});
    };
    this.pageRight.onclick = () => {
      google.visualization.events.trigger(this.table.getChart(), 'page', {'page': 1});
    };
  }

  draw() {
    this.table.draw();

    this.pageLeft.disabled = this.currPage == 0;
    this.pageRight.disabled = !this.hasNextPage();
    this.pageNumber.innerText = this.currPage + 1;
  }

  updateAggregation(dataTable) {
    this.currPage = 0;
    // Google charts API has a bug where setting pageSize will cause the table to be
    // paginated, regardles of if the 'page' property is set to 'disable'.  Thus, we
    // must remove the pageSize property entirely.
    let currOptions = this.table.getOptions();

    delete currOptions['pageSize'];
    currOptions['page'] = 'disable';

    this.table.setOptions(currOptions);
    this.setDataTable(dataTable);
  }

  updateNormal(dataTable) {
    this.currPage = 0;

    this.table.setOption('page', 'event');
    this.table.setOption('pageSize', 1);
    this.setDataTable(dataTable);
  }

  setDataTable(dataTable) {
    this.table.setDataTable(dataTable);
  }

  hasNextPage() {
    return true;
  }

  onPageChange(properties) {
    const pageDelta = properties['page']; // 1 or -1
    const newPage = this.currPage + pageDelta;

    this.currPage = newPage;
    this.draw();
  }

  createNewTable() {
    return new google.visualization.ChartWrapper({
        'chartType': 'Table',
        'containerId': 'table-container',
        'options': {
            'title': 'Sample Table',
            'page': 'event',
            'pageSize': 1,
            'width': '100%',
            'allowHtml': 'true',
            'cssClassNames': {
                'headerRow': 'table-header-row',
                'tableRow': 'table-row',
                'oddTableRow': 'table-row',
                'tableCell': 'table-cell'
            }
        }
    });
  }
}
