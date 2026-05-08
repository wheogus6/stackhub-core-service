$(() => {

});
$("#comment").keyup(function (e) {
    let content = $(this).val();

    // 글자수 세기
    if (content.length == 0 || content == "") {
        $(".textCount").text("0자");
    } else {
        $(".textCount").text(content.length + "자");
    }

    // 글자수 제한
    if (content.length > 500) {
        $(this).val($(this).val().substring(0, 500));
        alert("글자수는 500자까지 입력 가능합니다.");
    }
});

$("#saveBtn").click(function () {
    let name = $("#name").val();
    let comment = $("#comment").val();

    if (isNullOrEmpty(name)) {
        alert("이름을 입력해주세요.");
        return false;
    }
    if (isNullOrEmpty(comment)) {
        alert("내용을 입력해주세요.");
        return false;
    }
    if (comment.length > 500) {
        alert("내용은 500자 이내로 입력해주세요.");
        return false;
    }
    commentWrite(name, comment);
});

function commentWrite(name, comment) {
    console.log("asdas")
    $.ajax({
        type : "post",
        url : "/gday/front/commentWrite",
        async : true,
        dataType : 'text',
        data : {name: name,
                comment: comment},
        success: function (data) {
            if (data === "00") {
                alert("등록되었습니다.");
                location.href = '/gday/front/goCommentPage';
            }
        }
        , error :function (error){
            alert("댓글 저장에 실패 했습니다.");
        },
    })

}
