async function handleLogin() {
  fetch('/status').then(response => response.json()).then((isLoggedIn) => {
    if (!isLoggedIn) {
      window.location.replace("/login");
    }
  });
}

function authorizeCallback(authResult) {
  if (authResult['code']) {
    $('#signinButton').attr('style', 'display: none');

    const codeMsg = 'code=' + authResult['code'];
    const request = new Request('/authorize', {
                                                method: 'POST',
                                                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8', },
                                                body: codeMsg});
    fetch(request).then(response => {
        console.log('auth code sent'); //TODO: handle failure case
        document.location.href = '/index.html';
    });
  } else {
    console.log('user is not authorized');
    window.location.href = '/authorize.html';
  }
}

export {handleLogin};