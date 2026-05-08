$(() => {
    $("#adminHeader").show();
    getMemberListByPartner();
});

$("#search_bt").click(function () {
    getMemberListByPartner();
});


function getMemberListByPartner() {
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
        url : "/admin/member/getMemberListByPartner",
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

function setMemberList(data) {
    let list = ``;
    let petName = "";
    let uploadButton = ``;
    let downloadBtn = ``;

    data.forEach(function (val) {

        if (isNullOrEmpty(val.petName)) {
            petName = "-";
        } else {
            petName = val.petName;
        }
        // 상태 00 업로드 완료 , 10 렌더 완료, 90 미업로드
        if (val.status == "90") {
            uploadButton = `<button class="not_yet_upload_bt">미업로드</button>`;
        } else {
            uploadButton = `<button class="down_bt">업로드</button>`;
        }



        if (isNullOrEmpty(val.videoUrl)) {
            downloadBtn = `<button class="not_yet_upload_bt">다운불가</button>`
        } else {
            downloadBtn = `<button class="down_bt"><a download href="/admin/member/downloadZip?videoUrl=${val.videoUrl}&memorial=${val.memorialCode}&petName=${petName}&regDate=${val.regDateStr}&uname=${val.uname}" download>다운받기</a></button>`
        }

        list += `<tr>
              <td>${val.regDateStr}</td>
              <td>${val.uname}</td>
              <td>${petName}</td> 
              <td>${val.mobileNo}</td>
              <td>
                ${uploadButton}
              </td>
              <td>
                 ${downloadBtn}
              </td>
              <td>
                <button class="member_detail_bt" data-memorial="${val.memorialCode}">상세</button>
              </td>
            </tr>`;

    });
    $("#memberList").empty().append(list);
}




$(document).on("click", ".member_detail_bt[data-memorial]", function () {
    let id = $(this).data("memorial");

    $("#memorialCode").val(id);
    $("#dtlForm").attr("method", "get");
    $("#dtlForm").attr("action", "/admin/member/goPartnerAdminDtl")
    $("#dtlForm").submit();
});


$("#createMemberBtn").click(function () {
    let uName = $("#uName").val();
    let mobileNo = $("#mobileNo").val();

    if (isNullOrEmpty(uName)) {
        alert("고객이름을 입력해주세요.");
        return false;
    }
    if (isNullOrEmpty(mobileNo)) {
        alert("연락처를 입력해주세요.");
        return false;
    }

    createMember(uName, mobileNo);

});

function createMember(uName, mobileNo) {
    $.ajax({
        type : "post",
        url : "/admin/member/createMember",
        async : true,
        dataType : 'json',
        data:{name: uName, mobileNo: mobileNo},
        success: function (data) {
            console.log(data);
            showSaveMemberInfo(data);
            // 리스트 또 불러오기
            getMemberListByPartner();
        }, error : function (error){
            console.log(error)
            alert("저장에 실패하였습니다.");
        }
    })
}

function showSaveMemberInfo(data) {
    $("#uName").val(data.uname);
    $("#mobileNo").val(data.mobileNo);
    $("#memberCreateUrl").val(data.createUserUrl);
    $("#memberCreateUrl").show();
    $("#copyUrl").show();
}

$("#copyUrl").click(function () {
    let contents = $("#memberCreateUrl").val();
    navigator.clipboard.writeText(contents);
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
    let url = '/admin/member/downloadMemberListByPartnerExcel?';

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
    // let endPage = startPage + pageCount - 2;
    let endPage = startPage + pageCount - 1;

    if (startPage < 1) {
        startPage = 0;
    }
    if (endPage > totalPage) {
        endPage = totalPage - 1;
    }
    if (endPage < 1) {
        endPage = 0;
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
    getMemberListByPartner();
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

$("#reset_bt").click(function () {
    let form = document.getElementById('searchForm');
    form.reset();
});