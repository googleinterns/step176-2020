function authorizeCallback(authResult) {
  if (authResult['code']) {
    $('#signinButton').attr('style', 'display: none');
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/authorize', true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    xhr.onreadystatechange = function() { 
        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
            console.log("logged refresh token successfully");
            window.location.href = "/index.html";
        }
    }
    xhr.send("code=" + authResult['code']);
  } else {
    console.log("user is not authorized");
    window.location.href = "/authorize.html";
  }
}

function getDevices() {
    const historyEl = document.getElementById('devices');
    historyEl.innerHTML = 'things r loading!!!';
    fetch("/devices").then(response => response.json())
    .then(function(stuff) {
        historyEl.innerHTML = JSON.stringify(stuff);
    }).catch(function() {
        console.log("error");
        window.location.href = "/authorize.html";
    });
}


async function handleLogin() {
  fetch('/status').then(response => response.json()).then((isLoggedIn) => {
    if (!isLoggedIn) {
      window.location.replace("/login");
    }
  });
}
