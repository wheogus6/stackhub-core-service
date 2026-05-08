$(() => {
    $("#adminHeader").show();
    getPartnerAdminMemberDtl();
    getMemberComments();
});

function getMemberComments() {
    let memorialCode = $("#memorialCode").val();
    $.ajax({
        type : "post",
        url : "/admin/member/getMemberComments",
        async : true,
        dataType : 'json',
        data: {memorialCode: memorialCode},
        success: function (data) {
            setComment(data)
        }
        , error() {
            alert("댓글 불러오기에 실패했습니다.");
        },
    })
}

function setComment(data) {
    let list = ``;

    if (data.length > 0) {
        data.forEach(function (val) {
            list += `<tr>
                        <th>${val.name}</th>
                        <td class="comments" id="commentText${val.id}">${val.comment}</td>
                        <td class="comments inlineDisplayNone" id="commentInputArea${val.id}">
                         <textarea id="commentInput${val.id}" class="hiddenTextArea" maxlength="500">${val.comment}</textarea>
                        </td>
                        <td class="commentsBtn">
                          <div>
                            <button class="editCommentBtn" data-seq="${val.id}">수정</button>
                            <button class="saveCommentBtn inlineDisplayNone" id="save${val.id}" data-seq="${val.id}">저장</button>
                            <button class="deleteCommentBtn" data-seq="${val.id}">삭제</button>
                          </div>
                        </td>
                      </tr>`
        });
        $("#commentList").append(list);
    }
}



$(document).on("click", ".editCommentBtn[data-seq]", function () {
    let id = $(this).data("seq");
    $(`#commentText${id}`).hide();
    $(this).hide();
    $(`#commentInputArea${id}`).show();
    $(`#save${id}`).show();
});


$(document).on("click", ".deleteCommentBtn[data-seq]", function () {
    let id = $(this).data("seq");
    let memorialCode = $("#memorialCode").val();
    if (confirm("정말로 삭제하시겠습니까?")) {
        $.ajax({
            type : "post",
            url : "/admin/member/deleteComment",
            async : false,
            dataType : 'text',
            data: {id: id,
                memorialCode: memorialCode},
            success: function (data) {
                if (data === "00") {
                    location.reload();
                }
            }
            , error() {
                alert("저장에 실패했습니다.");
            },
        })
    }

});

$(document).on("click", ".saveCommentBtn[data-seq]", function () {
    let id = $(this).data("seq");
    let comment = $(`#commentInput${id}`).val();
    let memorialCode = $("#memorialCode").val();
    $.ajax({
        type : "post",
        url : "/admin/member/editComment",
        async : false,
        dataType : 'text',
        data: {id: id,
            comment: comment,
            memorialCode: memorialCode},
        success: function (data) {
            if (data === "00") {
                location.reload();
            }
        }
        , error() {
            alert("저장에 실패했습니다.");
        },
    })
});


function getPartnerAdminMemberDtl() {

    let memorialCode = $("#memorialCode").val();

    $.ajax({
        type : "post",
        url : "/gday/partner/getPartnerAdminMemberDtl",
        async : true,
        dataType : 'json',
        data : {memorialCode: memorialCode},
        success: function (data) {
            setInfo(data);
        }
        , error :function (error){
            alert("오류 정보를 가져오는데 실패하였습니다.");
        },
    })
}


function setInfo(data) {

    $("#uname").text(data.uname);

    $("#mobileNo").text(data.mobileNo);
    $("#regDate").text(data.regDateStr);
    $("#memo").val(data.memo)

    if (isNullOrEmpty(data.petName)) {
        $("#petName").text("미업로드");
    } else {
        $("#petName").text(data.petName);
    }

    if (isNullOrEmpty(data.funeralUrl)) {
        $("#videoUrlCopyBtn").hide();
        $("#funeralUrl").text("미업로드");
    } else {
        $("#funeralUrl").text(data.funeralUrl);
        $("#funeralUrl").attr("href", data.funeralUrl).attr("target", "_blank");
    }


    $("#createUserUrl").text(data.createUserUrl);
    $("#createUserUrl").attr("href", data.createUserUrl).attr("target", "_blank");

    console.log(data.videoUrl)

    if (isNullOrEmpty(data.videoUrl)) {
        $("#video_down_bt").text("미업로드");
    } else {
        $("#videoUrl").attr("href", "/admin/member/downloadZip?videoUrl=" + data.videoUrl + "&memorial=" + data.memorialCode + "&petName=" + data.petName + "&regDate=" + data.regDateStr + "&uname=" + data.uname);
        $("#videoUrl").attr("download");
    }
}



$("#list_bt").click(function () {
    history.back();
});

$("#createUrlCopyBtn").click(function () {
    let copyVal = $("#createUserUrl").text();
    navigator.clipboard.writeText(copyVal);
});

$("#funeralUrlCopyBtn").click(function () {
    let copyVal = $("#funeralUrl").text();
    navigator.clipboard.writeText(copyVal);
});