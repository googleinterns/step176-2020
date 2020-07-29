function authorizeCallback(authResult) {
  if (authResult['code']) {
    $('#signinButton').attr('style', 'display: none');
    var xhr = new XMLHttpRequest();
    xhr.open("POST", '/devices', true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

    xhr.onreadystatechange = function() { 
        if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
            console.log("logged successfully");
        }
    }
    xhr.send("code=" + authResult['code']);
  } else {
    console.log("user is not signed in");
    window.location.href = "/login";
  }
}

// function getDevices() {
//     var xhr = new XMLHttpRequest();
//     xhr.open("GET", '/devices', true);
//     xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

//     xhr.onreadystatechange = function() { // Call a function when the state changes.
//         if (this.readyState === XMLHttpRequest.DONE && this.status === 200) {
// console.log("donne");
//         }
//     }
//     xhr.send("code=" + authResult['code']);
//   } else {
//     console.log("user is not signed in");
//   }
// }

async function handleLogin() {
  fetch('/status').then(response => response.json()).then((isLoggedIn) => {
    if (!isLoggedIn) {
      window.location.replace("/login");
    }
  });
}
