$(() => {

});

$("#logoutBtn").click(function () {
    if (confirm("로그아웃 하시겠습니까?")) {
        logout();
    }
});

startSessionCheck(5000);

function startSessionCheck(interval) {
    let codeLogout = $("#codeLogout").val();
    if (!isNullOrEmpty(codeLogout)) {
        setInterval(checkSession, interval);
    }
}

function checkSession() {
    $.ajax({
        type : "post",
        url : "/checkSession",
        async : true,
        dataType : 'text',
        success: function (data) {
            console.log(data)
            if (data == "90") {
                logout();
            }
        },
    })
}


function logout() {
    $.ajax({
        type : "post",
        url : "/admin/login/logout",
        async : true,
        dataType : 'text',
        success: function (data) {
            console.log(data);
            if (data === "00") {
                location.href = "/";
            } else {
                alert("로그아웃에 실패했습니다.");
                return false;
            }
        },
    })
}