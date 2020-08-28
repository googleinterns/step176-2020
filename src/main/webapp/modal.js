const TAB_KEYCODE = 9;

// Used to decide, if multiple modals are open simultanesouly, which one should receive
// tab commands for accessibility.  Because relatively few modals should be open at any
// given time (< 10), we can use a simple list
const visibleModals = [];
const focusableElements = 'button, input, select, textarea';
let firstElement = null;
let lastElement = null;


function updateFocusableElements() {
  if (visibleModals.length <= 0) {
    firstElement = null;
    lastElement = null;
    return;
  }

  let modalToFocus = visibleModals[visibleModals.length - 1];
  let focusableElements = modalToFocus.container.querySelectorAll('button, input, select, textarea');

  firstElement = focusableElements[0];
  lastElement = focusableElements[focusableElements.length - 1];
}

document.addEventListener('keydown', function(e) {
  if (visibleModals.length <= 0 || firstElement == null || lastElement == null) {
    return;
  }

  let isTabPressed = e.keyCode == TAB_KEYCODE;
  if (!isTabPressed) {
    return;
  }

  if (e.shiftKey) {
    if (document.activeElement == firstElement) {
      lastElement.focus();
      e.preventDefault();
    }
  } else {
    if (document.activeElement == lastElement) {
      firstElement.focus();
      e.preventDefault();
    }
  }
});


class Modal {
  constructor(id, blocking, closeable) {
    this.container = document.createElement('div');
    this.configureContainer(id);
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

    this.setHeader('');

    this.onResize = () => {
      this.center();
    };

    this.previouslyFocused = null;
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

  configureContainer(id) {
    this.container.setAttribute('id', id);
    this.container.setAttribute('role', 'dialog');
    this.container.setAttribute('aria-modal', 'true');
    this.container.setAttribute('tabindex', '-1');
    this.container.classList.add('modal', 'hidden');
  }

  setHeader(text) {
    this.header.innerHTML = '';

    if (this.closeable) {
      let closeBtn = document.createElement('button');
      closeBtn.innerText = '✕';
      closeBtn.setAttribute('aria-label', 'Close modal');
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

    this.setVisibility(true);
    window.addEventListener('resize', this.onResize, true);
  }

  hide() {
    if (this.blockingDiv != null) {
      this.blockingDiv.classList.add('hidden');
    }
    this.container.classList.add('hidden');

    this.setVisibility(false);
    window.removeEventListener('resize', this.onResize), true;
  }

  createAndAddDiv(className) {
    let div = document.createElement('div');
    div.classList.add(className);
    this.container.appendChild(div);

    return div;
  }

  setVisibility(visibility) {
    if (visibility == this.isVisible) {
      return;
    }

    if (visibility) {
      visibleModals.push(this);
      this.previouslyFocused = document.activeElement;
      this.container.focus();
    } else {
      visibleModals.splice(visibleModals.indexOf(this));
      if (this.previouslyFocused != null) {
        this.previouslyFocused.focus();
      }
    }
    this.isVisible = visibility;
    updateFocusableElements();
  }

  remove() {
    this.setVisibility(false);
    if (this.blockingDiv != null) {
      this.blockingDiv.remove();
    } else {
      this.container.remove();
    }
  }

}

export {Modal};
