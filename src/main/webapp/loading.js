class Loading {
  /*
   * func: the asynchronous function whose progress is being measured
   * isShort: if true, a spinning wheel will be displayed; otherwise, an actual loading bar will be used
   * status: a function that returns a number from 0-100 indicating how close to finishing func is.
   *    Used only if isShort is false.
   */
  constructor(func, isShort, status) {
    this.func = func;
    this.isShort = isShort;
    this.status = status;

    this.progress = 0;
    this.minimumLoadTime = 500; // miliseconds

    this.container = new Modal('loading-bar', true);
    this.container.setHeader("Loading...");
    if (this.isShort) {
      let loader = document.createElement('div');
      loader.classList.add('spinning-wheel');
      this.container.setBody([loader]);
    } else {
      let loader = document.createElement('progress');
      this.container.setBody([loader]);
    }

    this.done = false;
  }

  async doTask() {
    let passedMinimumLoadTime = new Promise(resolve => setTimeout(resolve, this.minimumLoadTime));

    await this.func();
    await passedMinimumLoadTime;

    this.done = true;
  }

  async updateProgress() {
    let progress = await this.status();
    if (progress >= 100 && !this.done) {
      progress = 99;
    }
    this.progress = progress;

    if (!this.done) {
      // frequency with which to update, perhaps make this user controllable at some point?
      setTimeout(this.updateProgress(), 500);
    }
  }

  async load() {
    this.container.show();

    if (!this.isShort) {
      setTimeout(this.updateProgress(), 250);
    }

    await this.doTask();

    if (!this.isShort) {
      // add a done button
    } else {
      this.container.hide();
    }
  }
}
