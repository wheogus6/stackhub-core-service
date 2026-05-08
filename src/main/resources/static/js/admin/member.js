$(() => {
    $("#adminHeader").show();
    getAllPartnerList();
    getMemberList();
});

$("#search_bt").click(function () {
    getMemberList();
});

$("#ptLinkCode").on("change", function () {
    getMemberList();
});



function getMemberList() {
    let start = $("#startDate").val();
    let end = $("#endDate").val();

    if (start !== "" && end === "") {
        alert("종료일을 입력해주세요.");
        return false;
    }
    if (start === "" && end !== "") {
        alert("시작일을 입력해주세요.");
        return false;
    }

    let form = $("#searchForm");

    $.ajax({
        type : "post",
        url : "/admin/member/getMemberList",
        async : true,
        dataType : 'json',
        data : form.serialize(),
        success: function (data) {

            $("#page").val(data.page)
            $("#pageSize").val(data.pageSize);
            $("#totalSize").val(data.totalSize);

            if (data.rows.length > 0) {
                setMemberList(data.rows);
                initPage();
            } else {
                $("#memberList").empty();
                let html = `<tr>
                                <td colspan="6">
                                   내역이 없습니다.  
                                </td>    
                            </tr>`
                $("#memberList").append(html);
            }
        }
        , error :function (error){
            alert("오류 목록을 가져오는데 실패하였습니다.");
        },
    })
}



function setMemberList(contents) {
    let list = ``;
    $("#memberList").empty();


    contents.forEach(function (val) {
        let funeralUrl = ``;
        let sttusText = ``;

        let changeBtn = ``;
        if (val.memberSttus == "00") {
            changeBtn = `<button class="cancelBtn" data-member-val="${val.ucode}" data-sttus-val="${val.memberSttus}">취소</button>`;
        } else {
            changeBtn = `<button class="restoreBtn" data-member-val="${val.ucode}" data-sttus-val="${val.memberSttus}">복구</button>`;
        }

        if (val.funeralUrl == "미업로드") {
            funeralUrl = `<td>미업로드</td>`;
        } else {
            funeralUrl = `<td>
                        <a href="${val.funeralUrl}" target="_blank">이동</a>
                      </td>`;
        }

        list += ` <tr class="memberListTr">
                      <td>${val.memorialCode}</td>
                      <td>${val.uname}</td>
                      <td>${val.ptName}</td>
                      <td>${val.regDateStr}</td>
                      <td>${val.funeralDateStr}</td>
                      <td>${val.mobileNo}</td>
                      ${funeralUrl}
                      <td class="btnCollect">
                        <div>
                          <button class="detailBtn detail_btn" data-member-val="${val.ucode}" data-memorial-val="${val.memorialCode}">상세</button>
                          ${changeBtn}
                          <button class="deleteBtn" data-member-val="${val.ucode}" data-memorial-val="${val.memorialCode}" data-plink-val="${val.ptLinkCode}">삭제</button>
                         </div>
                      </td>
                    </tr>`;
    });
    $("#memberList").append(list);

}


$(document).on("click", ".deleteBtn[data-member-val]", function () {
    let id = $(this).data("member-val");
    let memorialCode = $(this).data("memorial-val");
    let ptLinkCode = $(this).data("plink-val");

    if (confirm("정말로 삭제하시겠습니까?")) {
        $.ajax({
            type: "post",
            url: "/admin/member/deleteMember",
            async: false,
            dataType: 'text',
            data: {ucode: id, memorialCode: memorialCode, ptLinkCode: ptLinkCode},
            success: function (data) {
                if (data === "00") {
                    location.reload();
                }
            }
            , error() {
                alert("삭제에 실패했습니다.");
            },
        });
    } else {
        return false;
    }

});


$(document).on("click", ".cancelBtn[data-member-val]", function () {
    let id = $(this).data("member-val");
    let sttus = $(this).data("sttus-val");

    if (confirm("정말로 취소하시겠습니까?")) {
        if (sttus == "90") {
            alert("이미 취소한 계정입니다.");
            return false;
        } else {
            $.ajax({
                type: "post",
                url: "/admin/member/stopMember",
                async: false,
                dataType: 'text',
                data: {ucode: id},
                success: function (data) {
                    if (data === "00") {
                        location.reload();
                    }
                }
                , error() {
                    alert("취소에 실패했습니다.");
                },
            });
        }
    } else {
        return false;
    }
});

$(document).on("click", ".restoreBtn[data-member-val]", function () {
    let id = $(this).data("member-val");
    let sttus = $(this).data("sttus-val");

    if (confirm("정말로 복구하시겠습니까?")) {
            $.ajax({
                type: "post",
                url: "/admin/member/restoreMember",
                async: false,
                dataType: 'text',
                data: {ucode: id},
                success: function (data) {
                    if (data === "00") {
                        location.reload();
                    }
                }
                , error() {
                    alert("취소에 실패했습니다.");
                },
            });
    } else {
        return false;
    }

});



$(document).on("click", ".detail_btn[data-member-val]", function () {
    let id = $(this).data("member-val");
    let memorial = $(this).data("memorial-val");
    $("#ucode").val(id);
    $("#memorial").val(memorial);
    $("#searchForm").attr("method", "get");
    $("#searchForm").attr("action", "/admin/member/goMemberDtl");
    $("#searchForm").submit();
});



function initPage() {
    let pageCount = 10;

    let currentPage = $("#page").val();

    let totalSize = $("#totalSize").val();

    let totalPage = $("#pageSize").val();

    let totalPageList = Math.ceil(totalPage / pageCount);

    let pageList = Math.ceil(currentPage / pageCount);

    if (pageList < 1) {
        pageList = 1;
    }
    if (pageList > totalPageList) {
        pageList = totalPageList;
    }

    let startPage = ((pageList - 1) * pageCount);
    let endPage = startPage + pageCount - 1;

    if (startPage < 1) {
        startPage = 1;
    }
    if (endPage > totalPage) {
        endPage = totalPage ;
    }
    if (endPage < 1) {
        endPage = 1;
    }

    let page = `<ul class="pagination">`;

    page += `<li class="page-item"><a class="page pageFirst" data-page="${startPage}"> < </a></li>`;

    for (let i = 1; i <= totalPage; i++) {
        let p = i;
        page += `<li class="page-item"><a class="page goPage" data-page="${p}" >${i}</a></li>`
    }

    page += `<li class="page-item"><a class="page pageLast" data-page="${endPage}"> > </a></li>`;

    page += `</ul>`;
    $("#pageZone").empty().append(page);
}

$(document).on("click", ".pageFirst[data-page]", function () {
    let page = $(this).data("page");
    goToPage(page)
});


$(document).on("click", ".goPage[data-page]", function () {
    let page = $(this).data("page");
    goToPage(page)
});

$(document).on("click", ".pageLast[data-page]", function () {
    let page = $(this).data("page");
    goToPage(page)
});


function goToPage(page) {
    $("#page").val(page);
    getMemberList();
}



function getAllPartnerList() {
    $.ajax({
        type : "post",
        url : "/admin/partner/getAllPartnerList",
        async : true,
        dataType : 'json',
        success: function (data) {
            if (data.length > 0) {
                setSelectOption(data);
            }
        }
        , error :function (error){
            alert("오류 목록을 가져오는데 실패하였습니다.");
        },
    })
}


function setSelectOption(data) {
    let option = ``;
    data.forEach(function (val) {
        option += `<option value="${val.ptLinkCode}">${val.ptName}</option>`
    });

    $("#ptLinkCode").append(option);
}


$("#startDate").on("change", function () {
    let start = $('#startDate').val();
    let end = $('#endDate').val();
    if (start !== "" && end !== "") {
        if (start > end) {
            alert("시작일이 종료일보다 클 수 없습니다.");
            $('#startDate').val(end);
            $('#endDate').val(start);
            return false;
        }
    }
});

$("#endDate").on("change", function () {
    let start = $('#startDate').val();
    let end = $('#endDate').val();
    if (start !== "" && end !== "") {
        if (start > end) {
            alert("시작일이 종료일보다 클 수 없습니다.");
            $('#startDate').val(end);
            $('#endDate').val(start);
            return false;
        }
    }
});


$("#excel_bt").click(function () {
    downloadExcelFile();
});

function downloadExcelFile() {
    let start = $("#startDate").val();
    let end = $("#endDate").val();

    if (start !== "" && end === "") {
        alert("종료일을 입력해주세요.");
        return false;
    }
    if (start === "" && end !== "") {
        alert("시작일을 입력해주세요.");
        return false;
    }

    let form = $("#searchForm");
    let url = '/admin/member/downloadMemberListExcel?';

    memberListExcel(form, url);

}

function memberListExcel(form, url) {
    const param = form.serialize();
    let link = $('<a>', {
        title: 'some title',
        href: url + param
    }).appendTo('body');
    link[0].click();
}