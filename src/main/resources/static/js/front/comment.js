$(() => {
    getCommentList();
});

$("#comment_save").click(function () {
    location.href = "/gday/front/goCommentWrite";
});


function getCommentList() {
    $.ajax({
        type : "post",
        url : "/gday/front/getCommentList",
        async : true,
        dataType : 'json',
        success: function (data) {
            setCommentList(data);
        }
        , error :function (error){
            alert("불러오기에 실패 했습니다.");
        },
    })

}

function setCommentList(data) {
    let comment = ``;

    data.forEach(function (val) {
        comment += ` <div class="comment_balloon">
                      <h5>from. <span>${val.name}</span></h5>
                      <p>${val.comment}</p>
                    </div>`
    });

    $("#commentList").append(comment);
}