class Modal {
  constructor(id, blocking) {
    this.container = document.getElementById(id);

    this.blockingDiv = null;
    if (blocking) {
      this.blockingDiv = this.addBlockingBackgroundDiv();
    }

    this.header = this.createAndAddDiv('modal-header');
    this.body = this.createAndAddDiv('modal-body');
    this.footer = this.createAndAddDiv('modal-footer');

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
    this.header.innerHTML = "";

    let closeBtn = document.createElement('button');
    closeBtn.innerText = "X";
    closeBtn.onclick = () => {this.hide()};

    let heading = document.createElement('h1');
    heading.innerText = text;
    heading.style.display = 'inline';

    this.header.appendChild(closeBtn);
    this.header.appendChild(heading);
  }

  setBody(elements) {
    this.body.innerHTML = "";

    for (let element of elements) {
      this.body.appendChild(element);
    }
  }

  show() {
    if (this.blockingDiv == null) {
      this.container.classList.remove('hidden');
    } else {
      this.blockingDiv.classList.remove('hidden');
    }

    this.isVisible = true;
  }

  hide() {
    if (this.blockingDiv == null) {
      this.container.classList.add('hidden');
    } else {
      this.blockingDiv.classList.add('hidden');
    }

    this.isVisible = false;
  }

  createAndAddDiv(className) {
    let div = document.createElement('div');
    div.classList.add(className);
    this.container.appendChild(div);

    return div;
  }

}
