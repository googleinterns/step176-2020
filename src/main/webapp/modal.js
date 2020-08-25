class Modal {
  constructor(id, blocking, closeable) {
    this.container = document.createElement('div');
    this.container.setAttribute('id', id);
    this.container.classList.add('modal', 'hidden');
    document.body.appendChild(this.container);

    this.blockingDiv = null;
    if (blocking) {
      this.blockingDiv = this.addBlockingBackgroundDiv();
    }

    // By default, modals are closeable
    this.closeable = closeable == null ? true : closeable;

    this.header = this.createAndAddDiv('modal-header');
    this.body = this.createAndAddDiv('modal-body');
    this.footer = this.createAndAddDiv('modal-footer');

    this.onResize = () => {
      this.center();
    };

    this.isVisible = false;
    this.hide();
  }

  addBlockingBackgroundDiv() {
    let blockingDiv = document.createElement('div');
    blockingDiv.classList.add('blocking');

    let modalParent = this.container.parentNode;
    modalParent.replaceChild(blockingDiv, this.container);
    blockingDiv.appendChild(this.container);

    return blockingDiv
  }

  setHeader(text) {
    this.header.innerHTML = '';

    if (this.closeable) {
      let closeBtn = document.createElement('button');
      closeBtn.innerText = '✕';
      closeBtn.classList.add('modal-close-btn');
      closeBtn.onclick = () => {this.hide()};
      this.header.appendChild(closeBtn);
    }

    let heading = document.createElement('h1');
    heading.innerText = text;
    this.header.appendChild(heading);

    this.center();
  }

  setBody(elements) {
    this.body.innerHTML = '';

    for (let element of elements) {
      this.body.appendChild(element);
    }

    this.center();
  }

  center() {
    const width = this.container.clientWidth;
    const height = this.container.clientHeight;

    this.container.style['margin-left'] = -1 * width/2 + 'px';
    this.container.style['margin-top'] = -1 * height/2 + 'px';
  }

  show() {
    if (this.blockingDiv != null) {
      this.blockingDiv.classList.remove('hidden');
    }
    this.container.classList.remove('hidden');

    this.center();

    window.addEventListener('resize', this.onResize, true);
    this.isVisible = true;
  }

  hide() {
    if (this.blockingDiv != null) {
      this.blockingDiv.classList.add('hidden');
    }
    this.container.classList.add('hidden');

    window.removeEventListener('resize', this.onResize), true;
    this.isVisible = false;
  }

  createAndAddDiv(className) {
    let div = document.createElement('div');
    div.classList.add(className);
    this.container.appendChild(div);

    return div;
  }

}
