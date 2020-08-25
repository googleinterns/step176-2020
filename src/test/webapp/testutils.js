function createAndAddToDOM(elem, id) {
  let element = createElementWithId(elem, id);
  document.body.appendChild(element);
  return element;
}

function createElementWithId(elem, id) {
  let element = document.createElement(elem);
  element.setAttribute('id', id);
  return element;
}

export {createAndAddToDOM, createElementWithId};
