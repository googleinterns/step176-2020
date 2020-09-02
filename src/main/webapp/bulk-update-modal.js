import {Modal} from './modal.js';
import {AnnotatedFields, getAnnotatedFieldFromDisplay} from './fields.js';
import {Loading} from './loading.js';

class BulkUpdateModal {
  constructor(modalId) {
    this.modal = new Modal(modalId, /*Blocking*/ true);

    // Used to give feedback/alerts to the user
    this.alertDiv = document.createElement('div');
    this.alertDiv.classList.add('alert');
    this.alertDiv.setAttribute('aria-live', 'aggressive');
  }

  populateAndShowModal(deviceIds, serialNumbers, selectedValues, selectedFields, devicesCount) {
    let bodyElements = this.createModalBody(deviceIds, serialNumbers, selectedValues, selectedFields, devicesCount);
    this.modal.setHeader('Perform Bulk Update');
    this.modal.setBody(bodyElements);
    this.modal.show();
  }

  createModalBody(deviceIds, serialNumbers, selectedValues, selectedFields, devicesCount) {
    let warning = this.createModalWarning(devicesCount, selectedValues, selectedFields);
    let form = this.createModalForm(deviceIds, serialNumbers, selectedValues, selectedFields);
    return [this.alertDiv, warning, form];
  }

  createModalForm(deviceIds, serialNumbers, selectedValues, selectedFields) {
    let form = document.createElement('form');
    form.setAttribute('method', 'POST');
    form.setAttribute('action', '/update');

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      let loader = new Loading(this.submitForm.bind(this, form), false);
      let response = await loader.load();

      // Refresh the view to reflect the updates.
      document.dispatchEvent(new CustomEvent('refreshData'));

      if (response.status == 200) {
        this.alertUserSuccess();
      } else {
        let json = await response.json();
        this.alertUserFailure(json.failedDevices);
      }
    });

    for (let i = 0; i < selectedValues.length; i++) {
      const aggregationField = selectedFields[i];
      const aggregationFieldValue = selectedValues[i];
      const apiAggregationFieldName = getAnnotatedFieldFromDisplay(aggregationField).API;

      let label = document.createElement('label');
      label.innerHTML = aggregationField;
      label.setAttribute('for', apiAggregationFieldName);
      label.classList.add('update-label');

      let input = document.createElement('input');
      input.setAttribute('type', 'text');
      input.setAttribute('value', aggregationFieldValue);
      input.setAttribute('name', apiAggregationFieldName);
      input.setAttribute('id', apiAggregationFieldName);
      input.classList.add('update-input');

      let container = document.createElement('div');
      container.appendChild(label);
      container.appendChild(input);

      form.appendChild(container);
    }

    let devicesInput = document.createElement('input');
    devicesInput.setAttribute('type', 'hidden');
    devicesInput.setAttribute('value', deviceIds);
    devicesInput.setAttribute('name', 'deviceIds');
    form.appendChild(devicesInput);

    let serialNumbersInput = document.createElement('input');
    serialNumbersInput.setAttribute('type', 'hidden');
    serialNumbersInput.setAttribute('value', serialNumbers);
    serialNumbersInput.setAttribute('name', 'serialNumbers');
    form.appendChild(serialNumbersInput);

    let submit = document.createElement('input');
    submit.setAttribute('type', 'submit');
    form.appendChild(submit);

    return form;
  }

  async submitForm(form) {
    // Form Data encoding is not accepted by server so we need to convert it
    const data = new FormData(form);
    let body = new URLSearchParams();
    for (let pair of data) {
      body.append(pair[0], pair[1]);
    }

    return await fetch(form.action, {
      method: form.method,
      body: body
    });
  }

  createModalWarning(devicesCount, selectedValues, selectedFields) {
    let div = document.createElement('div');

    let p1 = document.createElement('p');
    p1.innerHTML  = `Warning: You are about to update <b>${devicesCount}</b> devices with the following properties:`;

    let ul = document.createElement('ul');
    for (let i = 0; i < selectedValues.length; i++) {
      let li = document.createElement('li');

      const aggregationField = selectedFields[i];
      const aggregationFieldValue = selectedValues[i];

      li.innerHTML = `<b>${aggregationField}</b>: ${aggregationFieldValue}`;

      ul.appendChild(li);
    }

    let p2 = document.createElement('p');
    p2.innerText = 'This is a non-reversible action.  To continue, enter the desired values and press submit.';

    div.appendChild(p1);
    div.appendChild(ul);
    div.appendChild(p2);

    return div;
  }

  alertUserSuccess() {
    this.alertDiv.innerHTML = '';

    this.alertDiv.style['background-color'] = '#2ffb2f'; // light greenish
    this.alertDiv.innerText = 'Updated devices successfully!';
  }

  alertUserFailure(failedDevices) {
    this.alertDiv.innerHTML = '';

    this.alertDiv.style['background-color'] = '#ff4d4d'; // reddish

    let errorMessage = document.createElement('p');
    errorMessage.innerText = 'ERROR: Failed to update the following devices:';

    let failedDevicesList = document.createElement('ul');
    for (let device of failedDevices) {
      let li = document.createElement('li');
      li.innerText = device.serialNumber;
      failedDevicesList.appendChild(li);
    }

    this.alertDiv.append(errorMessage, failedDevicesList);
  }
}

export {BulkUpdateModal}
