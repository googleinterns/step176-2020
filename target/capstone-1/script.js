function signInCallback(authResult) {
  if (authResult['code']) {
    $('#signinButton').attr('style', 'display: none');
    const request = new Request('/token', {method: 'POST', body: '{"foo": "bar"}'});
    fetch(request).then(response => {
      console.log("fetchhed");
    });
  } else {
    console.log("no auth code!");
  }
}
