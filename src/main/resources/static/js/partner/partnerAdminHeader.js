$(() => {

});

$("#logoutBtn").click(function () {
    if (confirm("로그아웃 하시겠습니까?")) {
        logout();
    }
});


$("#homeBtn").click(function () {
    location.href = "/gday/partner";
});

function logout() {
    $.ajax({
        type : "post",
        url : "/gday/partner/logout",
        async : true,
        dataType : 'text',
        success: function (data) {
            console.log(data);
            if (data === "00") {
                location.href = "/gday/partner";
            } else {
                alert("로그아웃에 실패했습니다.");
                return false;
            }
        },
    })
}