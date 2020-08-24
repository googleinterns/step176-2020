class TableManager {
  constructor(containerId) {
    this.container = document.getElementById(containerId);
    this.table = this.createNewTable(containerId);
    this.baseDataTable = new google.visualization.DataTable();
    this.currPage = 0;
    this.pageSize = 10;
    this.aggregating = false;

    this.pageLeft = document.getElementById('page-left');
    this.pageRight = document.getElementById('page-right');
    this.pageNumber = document.getElementById('page-number');
    this.pageSizeSelect = document.getElementById('page-size-select');

    google.visualization.events.addOneTimeListener(this.table, 'ready', () => {
      google.visualization.events.addListener(
          this.table.getChart(), 'page', this.onPageChange.bind(this));
    });

    google.visualization.events.addListener(this.table, 'ready', () => {
      let table = this.container.getElementsByTagName('table')[0];
      let tableHeaders = table.getElementsByTagName('th');
      for (let [index, th] of Object.entries(tableHeaders)) {
        let idPrefix = 'tableHeaderSpan';
        let mainSpan = document.createElement('span');

        let textLabel = document.createElement('span');
        textLabel.innerText = th.innerText;
        textLabel.setAttribute('id', idPrefix + (2 * index));
        textLabel.classList.add('hidden');

        let otherSpan = document.createElement('span');
        otherSpan.setAttribute('aria-labelledby', idPrefix + (2 * index + 1));
        otherSpan.setAttribute('role', 'button');
        otherSpan.setAttribute('tabindex', 0);

        let displayTextLabel = document.createElement('span');
        displayTextLabel.setAttribute('aria-hidden', 'true');
        displayTextLabel.innerText = th.innerText;

        let purposeLabel = document.createElement('span');
        purposeLabel.innerText = 'Activate to sort by column.';
        purposeLabel.classList.add('hidden');
        purposeLabel.setAttribute('id', idPrefix + (2 * index + 1));

        th.innerHTML = '';

        otherSpan.append(displayTextLabel, purposeLabel);
        mainSpan.append(textLabel, otherSpan);

        th.appendChild(mainSpan);

        th.setAttribute('aria-labelledby', idPrefix + (2 * index));
        th.removeAttribute('aria-label');
        th.removeAttribute('tabindex');

        th.setAttribute('role', 'columnheader');

        // TODO: add aria-sort
      }
    });

    this.pageLeft.onclick = () => {
      google.visualization.events.trigger(this.table.getChart(), 'page', {'page': -1});
    };
    this.pageRight.onclick = () => {
      google.visualization.events.trigger(this.table.getChart(), 'page', {'page': 1});
    };
    this.pageSizeSelect.onchange = () => {
      this.onPageSizeChange(parseInt(this.pageSizeSelect.value));
    };
  }

  draw() {
    this.table.draw();
    this.pageNumber.innerText = this.currPage + 1;

    this.drawArrows();
  }

  drawArrows() {
    if (this.aggregating) {
      this.pageLeft.disabled = true;
      this.pageRight.disabled = true;
    } else {
      this.pageLeft.disabled = this.currPage == 0;
      this.pageRight.disabled = !this.hasNextPage();
    }
  }

  updateAggregation(dataTable) {
    this.currPage = 0;
    this.aggregating = true;
    // Google charts API has a bug where setting pageSize will cause the table to be
    // paginated, regardles of if the 'page' property is set to 'disable'.  Thus, we
    // must remove the pageSize property entirely.
    let currOptions = this.table.getOptions();

    delete currOptions['pageSize'];
    currOptions['page'] = 'disable';

    this.table.setOptions(currOptions);
    this.setTableView(dataTable.getNumberOfColumns());
    this.setDataTable(dataTable);
  }

  updateNormal(dataTable) {
    this.currPage = 0;
    this.aggregating = false;

    this.table.setOption('page', 'event');
    this.table.setOption('pageSize', this.pageSize);
    this.setTableView(dataTable.getNumberOfColumns());
    this.setDataTable(dataTable);
  }

  setDataTable(dataTable) {
    if (dataTable != null) {
      this.baseDataTable = dataTable;
    }

    if (this.aggregating) {
      this.table.setDataTable(this.baseDataTable);
    } else {
      let view = new google.visualization.DataView(this.baseDataTable);
      const startIndex = this.currPage * this.pageSize;
      const endIndex = Math.min(
          this.currPage * this.pageSize + this.pageSize - 1,
          this.baseDataTable.getNumberOfRows() - 1);
      view.setRows(startIndex, endIndex);
      this.table.setDataTable(view);
    }
  }

  setTableView(numOfCols) {
    /*
     * Aggregation table has Field 1 | ... | Field n | deviceCount | deviceIds | Button
     * Normal view has Field 1 | ... | Field n
     * In the first case we want to hide only deviceIds; in the second case we show everything
     */
    let viewableCols = [...Array(numOfCols - 2).keys()];
    if (!this.aggregating) {
      viewableCols.push(numOfCols - 2);
    }
    viewableCols.push(numOfCols - 1);

    this.table.setView({'columns': viewableCols});
  }

  hasNextPage() {
    return (this.currPage + 1) * this.pageSize < this.baseDataTable.getNumberOfRows();
  }

  onPageChange(properties) {
    const pageDelta = properties['page']; // 1 or -1
    const newPage = this.currPage + pageDelta;

    if (newPage < 0) {
      return;
    }

    this.currPage = newPage;
    // TODO: Eventually this should use a server-side pagination endpoint to request material
    this.setDataTable();

    this.draw();
  }

  onPageSizeChange(newPageSize) {
    this.pageSize = newPageSize;
    this.table.setOption('pageSize', newPageSize);
    this.currPage = 0;

    // TODO: Eventually this should use a server-side pagination endpoint to request material
    this.setDataTable();

    this.draw();
  }

  createNewTable(containerId) {
    return new google.visualization.ChartWrapper({
        'chartType': 'Table',
        'containerId': containerId,
        'options': {
            'title': 'Sample Table',
            'page': 'event',
            'pageSize': 10,
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

export {TableManager};
