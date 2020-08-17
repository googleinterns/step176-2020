import {Modal} from './modal.js';

class Loading {
  /*
   * func: the asynchronous function whose progress is being measured
   * isShort: if true, a spinning wheel will be displayed; otherwise, an actual loading bar will be used
   * status: a function that returns a number from 0-100 indicating how close to finishing func is.
   *    Used only if isShort is false. If isShort is true and status is undefined, an indeterminate progress
   *    bar will be created.
   */
  constructor(func, isShort, status) {
    this.func = func;
    this.isShort = isShort;
    this.status = status;

    this.progress = 0;
    this.minimumLoadTime = 500; // miliseconds

    this.container = new Modal('loading-modal', /*blocking*/ true, /*closeable*/ false);
    this.container.setHeader("Loading...");
    if (this.isShort) {
      this.loader = document.createElement('div');
      this.loader.classList.add('spinning-wheel');
      this.container.setBody([this.loader]);
    } else {
      let loaderContainer = document.createElement('div');
      loaderContainer.classList.add('loading-bar-container');

      this.loader = document.createElement('progress');
      this.loader.setAttribute('max', 100);
      this.loader.classList.add('loading-bar');

      if (this.status != null) {
        this.loader.setAttribute('value', 0);
      }

      loaderContainer.appendChild(this.loader);
      this.container.setBody([loaderContainer]);
    }

    this.done = false;
  }

  async doTask() {
    let passedMinimumLoadTime = new Promise(resolve => setTimeout(resolve, this.minimumLoadTime));

    await this.func();
    await passedMinimumLoadTime;

    this.done = true;
  }

  // Used for testing
  async updateProgressWrapper() {
    this.updateProgress();
  }

  async updateProgress() {
    let progress = await this.status();
    if (progress >= 100 && !this.done) {
      progress = 99;
    }

    this.progress = progress;
    this.setProgress();

    if (!this.done) {
      // frequency with which to update, perhaps make this user controllable at some point?
      setTimeout(this.updateProgressWrapper.bind(this), 500);
    }
  }

  setProgress() {
    if (!this.isShort) {
      this.loader.setAttribute('value', this.progress);
    }
  }

  async load() {
    this.container.show();

    if (!this.isShort && this.status != null) {
      setTimeout(this.updateProgressWrapper.bind(this), 250);
    }

    await this.doTask();

    if (!this.isShort) {
      // add a done button
      this.container.remove();
    } else {
      this.container.remove();
    }
  }
}

export {Loading};
