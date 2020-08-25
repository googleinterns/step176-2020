import {Modal} from './modal.js';
import {AnnotatedFields, getAnnotatedFieldFromDisplay} from './fields.js';

class BulkUpdateModal {
  constructor(modalId) {
    this.modal = new Modal(modalId, /*Blocking*/ true);

    document.addEventListener('displayBulkUpdateMenu', (e) => {
      let detail = e.detail;
      this.populateAndShowModal(
          detail.deviceIds,
          detail.selectedValues,
          detail.selectedFields,
          detail.devicesCount);
    }, false);
  }

  populateAndShowModal(deviceIds, selectedValues, selectedFields, devicesCount) {
    let bodyElements = this.createModalBody(deviceIds, selectedValues, selectedFields, devicesCount);
    this.modal.setHeader('Perform Bulk Update');
    this.modal.setBody(bodyElements);
    this.modal.show();
  }

  createModalBody(deviceIds, selectedValues, selectedFields, devicesCount) {
    let warning = this.createModalWarning(devicesCount, selectedValues, selectedFields);
    let form = this.createModalForm(deviceIds, selectedValues, selectedFields);
    return [warning, form];
  }

  createModalForm(deviceIds, selectedValues, selectedFields) {
    let form = document.createElement('form');
    form.setAttribute('method', 'POST');
    form.setAttribute('action', '/update');

    for (let i = 0; i < selectedValues.length; i++) {
      const aggregationField = selectedFields[i];
      const aggregationFieldValue = selectedValues[i];

      let label = document.createElement('label');
      label.innerHTML = aggregationField;
      label.setAttribute('for', getAnnotatedFieldFromDisplay(aggregationField).API);
      label.classList.add('update-label');

      let input = document.createElement('input');
      input.setAttribute('type', 'text');
      input.setAttribute('value', aggregationFieldValue);
      input.setAttribute('name', getAnnotatedFieldFromDisplay(aggregationField).API);
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

    let submit = document.createElement('input');
    submit.setAttribute('type', 'submit');
    form.appendChild(submit);

    return form;
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
}

export {BulkUpdateModal}
