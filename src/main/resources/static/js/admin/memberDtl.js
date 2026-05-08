$(() => {
    $("#adminHeader").show();
    getMemberDtl();
});


function getMemberDtl() {
    let ucode = $("#ucode").val();

    $.ajax({
        type : "post",
        url : "/admin/member/getMemberDtl",
        async : true,
        dataType : 'json',
        data: {ucode: ucode},
        success: function (data) {
            getMemberComments();
            setMemberDtl(data);
        }
        , error() {
            alert("불러오기에 실패했습니다.");
        },
    })
}

function getMemberComments() {
    let memorialCode = $("#memorial").val();
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
    let memorialCode = $("#memorial").val();

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
        });
    }

});

$(document).on("click", ".saveCommentBtn[data-seq]", function () {
    let id = $(this).data("seq");
    let comment = $(`#commentInput${id}`).val();
    let memorialCode = $("#memorial").val();
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


function setMemberDtl(data) {
    $("#ptName").val(data.ptName);
    $("#funeralDate").val(data.funeralDateStr);
    $("#mobileNo").val(data.mobileNo);
    $("#uname").val(data.uname);

    if (isNullOrEmpty(data.funeralUrl)) {
        $("#emptyUrl").text("미업로드");
    } else {
        $("#funeralUrl").attr("href", data.funeralUrl).attr("target", "_blank");
        $("#funeralUrl").text(data.funeralUrl);
    }

    let memo = "";
    if (isNullOrEmpty(data.memo)) {
        memo = "";
    } else {
        memo = data.memo;
    }
    $("#memo").val(memo);
}

$("#list_bt").click(function () {
    location.href = "/admin/member/goMemberPage";
});

$("#edit_bt").click(function () {
    let ucode = $("#ucode").val();
    let uname = $("#uname").val();
    let mobileNo = $("#mobileNo").val();
    let funeralDate = $("#funeralDate").val();
    let memo = $("#memo").val();

    if (isNullOrEmpty(uname)) {
        alert("이름을 입력해주세요.");
        return false;
    }
    if (/[\s]/g.test(uname)) {
        alert("이름에 공백을 빼주세요");
        return false;
    }
    if (isNullOrEmpty(mobileNo)) {
        alert("연락처를 입력해주세요");
        return false;
    }

    $.ajax({
        type : "post",
        url : "/admin/member/editMemberDtl",
        async : true,
        dataType : 'text',
        data: {ucode: ucode,
            uname: uname,
            mobileNo: mobileNo,
            funeralDate: funeralDate,
            memo: memo},
        success: function (data) {
            if (data === "00") {
                alert("수정 되었습니다.");
                location.reload();
            }
        }
        , error() {
            alert("수정에 실패했습니다.");
        },
    })



});