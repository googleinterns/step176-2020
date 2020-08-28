import {BulkUpdateModal} from '../../main/webapp/bulk-update-modal.js';
import {Loading} from '../../main/webapp/loading.js';
jest.mock('../../main/webapp/loading.js');

const SUCCESS_RESPONSE = {'status': 200};
const FAILURE_RESPONSE = {'status': 500, json: () => Promise.resolve( {'failedDevices': [{'deviceId': 'id1'}, {'deviceId': 'id2'}]} )};

beforeEach(() => {
  document.body.innerHTML = '';
});

test('Form submit success', async () => {
  let updateModal = createAndShowBulkUpdateModal();

  jest.spyOn(Loading.prototype, 'load').mockImplementationOnce(() => {return SUCCESS_RESPONSE});
  let success = jest.spyOn(updateModal, 'alertUserSuccess');

  let form = document.getElementsByTagName('form')[0];
  await form.submit();

  await Promise.resolve();
  expect(success.mock.calls.length).toBe(1);
});

test('Form submit failure', async () => {
  let updateModal = createAndShowBulkUpdateModal();

  jest.spyOn(Loading.prototype, 'load').mockImplementationOnce(() => {return FAILURE_RESPONSE});
  let failure = jest.spyOn(updateModal, 'alertUserFailure');

  let form = document.getElementsByTagName('form')[0];
  await form.submit();

  // Force all active promises to finish
  await Promise.resolve();
  expect(failure.mock.calls.length).toBe(1);
});

function createAndShowBulkUpdateModal() {
  let updateModal = new BulkUpdateModal('bulk-update');

  let deviceIds = '[\'id1\', \'id2\']';
  let selectedFields = ['User'];
  let selectedValues = ['Andrew'];
  let devicesCount = 2;
  updateModal.populateAndShowModal(deviceIds, selectedValues, selectedFields, devicesCount);

  return updateModal
}
