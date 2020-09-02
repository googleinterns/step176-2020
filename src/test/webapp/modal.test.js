import {Modal} from '../../main/webapp/modal.js';
import {createElementWithId} from './testutils.js';

beforeEach(() => {
  document.body.innerHTML = '';
});

test('Modal creates own container', () => {
  let id = 'modal-container';
  expect(document.getElementById(id)).toBeNull();

  let modal = new Modal(id);

  expect(document.getElementById(id)).toEqual(modal.container);
});

test('Modal is closeable and nonblocking by default', () => {
  let modal = new Modal('modal-container');

  // There is no blocking div
  expect(document.querySelectorAll('.blocking').length).toBe(0);
  // There is a close button
  expect(document.querySelectorAll('.modal-close-btn').length).toBe(1);
});

test('Modal can be set to be uncloseable', () => {
  let blocking = false;
  let closeable = false;
  let modal = new Modal('modal-container', blocking, closeable);

  // No close button means the user has no way to close the modal by default
  expect(document.querySelectorAll('.modal-close-btn').length).toBe(0);
});

test('Modal can be set to be blocking', () => {
  let blocking = true;
  let modal = new Modal('modal-container', blocking);

  expect(document.querySelectorAll('.blocking').length).toBe(1);
});

test('Sucessfully set body', () => {
  let pId = 'theParagraph';
  let divId = 'theDiv';

  let modal = new Modal('modal-container');
  modal.setBody([createElementWithId('p', pId), createElementWithId('div', divId)]);

  expect(document.getElementById(pId)).not.toBeNull();
  expect(document.getElementById(divId)).not.toBeNull();
});

test('Focus is set correctly', () => {
  let modal1 = new Modal('modal-container');
  modal1.show();
  expect(document.activeElement).toBe(modal1.container);

  let modal2 = new Modal('another-container');
  modal2.show();
  expect(document.activeElement).toBe(modal2.container);

  modal2.hide();
  expect(document.activeElement).toBe(modal1.container);
});

test('Remove callback is invoked correctly', () => {
  let modal = new Modal('modal-container');
  let func = jest.fn(() => {return 32;});
  modal.setRemoveCallback(func);

  modal.remove();
  expect(func.mock.calls.length).toBe(1);
});
