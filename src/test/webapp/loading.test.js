import {Loading} from '../../main/webapp/loading.js';

jest.useFakeTimers();

test('Minimum time enforced', async () => {
  let loader = new Loading(() => {new Promise(resolve => {setTimeout(resolve, 1)})}, true);
  let loaded = loader.load();

  jest.advanceTimersByTime(loader.minimumLoadTime - 1);
  expect(loader.done).toBeFalsy();

  jest.advanceTimersByTime(1);
  await loaded;
  expect(loader.done).toBeTruthy();
});

test('Loading wheel shows for short tasks', async () => {
  let taskTime = 600; // miliseconds

  let loader = new Loading(() => {new Promise(resolve => {setTimeout(resolve, taskTime)})}, true);
  let loaded = loader.load();

  expect(loader.loader.classList.contains('spinning-wheel')).toBeTruthy();
  expect(loader.container.isVisible).toBeTruthy();

  jest.advanceTimersByTime(taskTime);
  await loaded;
});

test('Loading bar shows for long tasks', async () => {
  let taskTime = 2000; // miliseconds

  let loader = new Loading(() => {new Promise(resolve => {setTimeout(resolve, taskTime)})}, false);
  let loaded = loader.load();

  expect(loader.loader.classList.contains('loading-bar')).toBeTruthy();
  expect(loader.container.isVisible).toBeTruthy();

  jest.advanceTimersByTime(taskTime);
  await loaded;
});

test('Progress function updates loading bar', async () => {
  const taskTime = 2500; // miliseconds
  const progressIncrement = 20;

  let progressFunc = function() {
    if (this.progress == null) {
      this.progress = progressIncrement;
    } else {
      this.progress += progressIncrement;
    }
    return this.progress;
  };

  function sleep(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
  }

  let loader = new Loading(sleep.bind(this, 2500), false, progressFunc);
  loader.updateProgressWrapper = async () => {
    if (loader.promiseQueue == null) {
      loader.promiseQueue = [];
    }
    loader.promiseQueue.push(loader.updateProgress());
  };
  loader.load();

  expect(document.getElementById('loading-modal').getElementsByTagName('progress')[0].value).toBe(0);


  for (let i = 0; i < 100 / progressIncrement; i++) {
    jest.advanceTimersByTime(500);
    await loader.promiseQueue[i];
    if (!loader.done && progressIncrement * (i + 1) >= 100) {
      expect(document.getElementById('loading-modal').getElementsByTagName('progress')[0].value).toBe(99);
    } else {
      expect(document.getElementById('loading-modal').getElementsByTagName('progress')[0].value).toBe(progressIncrement * (i + 1));
    }
  }

});
