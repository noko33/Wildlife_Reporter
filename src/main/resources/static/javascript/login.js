var script = document.createElement('script');
script.src = 'https://code.jquery.com/jquery-3.7.1.min.js'; // Check https://jquery.com/ for the current version
document.getElementsByTagName('head')[0].appendChild(script);

function getValue() {
  // Get the input element by its ID
  var emailbox = document.getElementById("femail");

    var passwordbox = document.getElementById("fpassword");

  // Get the value entered in the textbox
  var email = emailbox.value;
  var password = passwordbox.value;
  // Display the value (e.g., in a paragraph element)
  window.location.href = "http://localhost:8080/api/v1/login/email="+email+"password="+password;


  // Further actions can be performed with inputValue here
  // e.g., send it to a server, perform calculations, etc.
}

function createAccount(){
  var emailbox = document.getElementById("femail2");

    var passwordbox = document.getElementById("fpassword2");
    var userBox = document.getElementById("fuserId");

  // Get the value entered in the textbox
  var email = emailbox.value;
  var password = passwordbox.value;
  var userId = userBox.value;
  // Display the value (e.g., in a paragraph element)
  window.location.href = "http://localhost:8080/api/v1/createaccount/email="+email+"password="+password+"userId="+userId;


}

