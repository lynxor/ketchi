function setHidden(){
    $("#challenge").val(Recaptcha.get_challenge());
    $("#challenge_response").val(Recaptcha.get_response());
}

function initReCaptcha(){
    Recaptcha.create("6Lf6PMQSAAAAANYmb-BYDxXHkq1y4IiYBfORxk9Y", "recaptcha_div", {
        theme: "red"
    });
}