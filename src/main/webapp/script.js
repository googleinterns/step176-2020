function signInCallback(authResult) {
  if (authResult['code']) {
    $('#signinButton').attr('style', 'display: none');
    $.ajax({
      type: 'POST',
      url: '/token',
      headers: {
        'X-Requested-With': 'XMLHttpRequest'
      },
      contentType: 'application/octet-stream; charset=utf-8',
      success: function(result) {
        console.log("was sent properly");
      },
      processData: false,
      data: '{code: ' + authResult['code'] + '}'
    });
  } else {
    console.log("sending wenttttt wrong");
  }
}
