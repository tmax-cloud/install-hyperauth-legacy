const checkEl = document.querySelector("input[type='checkbox']");
// const offEl = document.querySelector("#off");
// const onEl = document.querySelector("#on");
const formElEmailOtp = document.querySelector("#emailOtpAuthUrl");
const formElSimpleLogin = document.querySelector("#simpleLoginUrl");
// if (checkEl.checked === true) {
//   offEl.style.display = "none";
//   onEl.style.display = "";
// }

checkEl.addEventListener("click", (e) => {
  const saveButton = document.getElementById("additionalAuth-save-button");
  if(saveButton.getAttribute("disabled") != null){
    saveButton.removeAttribute("disabled");
  } else {
    saveButton.setAttribute("disabled", true);
  }
  // if (offEl.style.display === "none") {
  //   offEl.style.display = "";
  // } else {
  //   offEl.style.display = "none";
  // }

  // if (onEl.style.display === "none") {
  //   onEl.style.display = "";
  // } else {
  //   onEl.style.display = "none";
  // }
});

formElSimpleLogin.addEventListener("submit", (e) => {
  if (checkEl.checked !== true) {
    const input = document.createElement("input");
    input.setAttribute("type", "hidden");
    input.setAttribute("name", "simpleLogin");
    input.setAttribute("value", "false");
    formEl.append(input);

    // checkEl.value="false"
    // checkEl.checked="true";
  }
});

function openCancelModal() {
  document.getElementById("cancelModal").classList.remove("hidden");
}

function cancelChangeAdditionalAuth() {
  location.href = document.location;
}
