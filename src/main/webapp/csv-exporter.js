async function getCsv() {
  await (fetch('/csv')
    .then(response => {
      var encodedUri = encodeURI(response);
      var link = document.createElement("");
      link.setAttribute("href", encodedUri);
      link.setAttribute("download", "my_devices.csv");
      document.body.appendChild(link); 
      link.click();
      console.log("download sent");
    })
  );
}