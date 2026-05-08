$(() => {

    $("#id").on("keypress", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            loginCheck();
        }
    });


    $("#pwd").on("keypress", function (event) {
        if (event.key === "Enter") {
            event.preventDefault();
            loginCheck();
        }
    });

})

$("#loginBtn").click(function () {
    loginCheck();
});

function loginCheck() {

    let id = $("#id").val();
    let pwd = $("#pwd").val();

    if (isNullOrEmpty(id)) {
        alert("ID를 입력해주세요.");
        return false;
    }
    if (isNullOrEmpty(pwd)) {
        alert("패스워드를 입려해주세요.");
        return false;
    }

    $.ajax({
        type : "post",
        url : "/admin/login/loginCheck",
        async : true,
        dataType : 'text',
        data:{id: id, pwd: pwd},
        success: function (data) {
            console.log(data);
            if (data === "00") {
                location.href = "/";
            } else if (data === "99") {
                alert("비밀번호가 일치하지 않습니다.");
                return false;
            } else if (data === "98") {
                alert("존재하지 않는 아이디입니다.");
                return false;
            }
        },
    })
}
