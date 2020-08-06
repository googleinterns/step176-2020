class Modal {
  constructor(id) {
    this.container = document.getElementById(id);

    this.header = this.createAndAddDiv('modal-header');
    this.body = this.createAndAddDiv('modal-body');
    this.footer = this.createAndAddDiv('modal-footer');

    this.isVisible = false;
    this.hide();
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

  show() {
    this.container.classList.remove('hidden');
    this.isVisible = true;
  }

  hide() {
    this.container.classList.add('hidden');
    this.isVisible = false;
  }

  createAndAddDiv(className) {
    let div = document.createElement('div');
    div.classList.add(className);
    this.container.appendChild(div);

    return div;
  }

}
