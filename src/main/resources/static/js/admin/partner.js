$(() => {
    $("#adminHeader").show();
    getPartnerList();
    getAllPartnerList();
});

$("#ptName").on("keypress", function (enent) {
    if (enent.key === "Enter") {
        enent.preventDefault();
        $("#ptLinkCode").val("");
        $("#searchCode").val("");
        getPartnerList();
    }
});

$("#searchBtn").click(function () {
    $("#ptLinkCode").val("");
    $("#searchCode").val("");
    getPartnerList();
});

$("#ptLinkCode").on("change", function () {
    let code = $(this).val();
    $("#searchCode").val(code);
    $("#ptName").val("");
    getPartnerList();
});

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

function getPartnerList() {
    let form = $("#searchForm");

    $.ajax({
        type : "post",
        url : "/admin/partner/getPartnerList",
        async : true,
        dataType : 'json',
        data : form.serialize(),
        success: function (data) {

            $("#page").val(data.page)
            $("#pageSize").val(data.pageSize);
            $("#totalSize").val(data.totalSize);

            if (data.rows.length > 0) {
                setPartnerList(data.rows);
                initPage();
            } else {
                $("#partnerList").empty();
                let html = `<tr>
                                <td colspan="5">
                                   내역이 없습니다.  
                                </td>    
                            </tr>`
                $("#partnerList").append(html);
            }
        }
        , error :function (error){
            alert("오류 목록을 가져오는데 실패하였습니다.");
        },
    })
}

function setPartnerList(contents) {
    let list = ``;
    $("#partnerList").empty();
    contents.forEach(function (val) {
        list += `<tr>
                  <td>${val.ptLinkCode}</td>
                  <td>${val.id}</td>
                  <td>${val.ptName}</td>
                  <td>${val.telNo}</td>
                  <td class="btnCollect">
                      <div>
                      <button class="bottomTabBtn dtlBtn" data-dtl="${val.ptLinkCode}">상세</button>
                      <button class="bottomTabBtn deleteBtn" data-pt="${val.ptCode}">
                  삭제</button> 
                      </div>
                  </td>
                </tr>`
    });
    $("#partnerList").append(list);
}


$(document).on("click", ".deleteBtn[data-pt]", function () {
    let id = $(this).data("pt");

    if (confirm("정말로 삭제하시겠습니까?")) {
        $.ajax({
            type : "post",
            url : "/admin/partner/deletePartner",
            async : false,
            dataType : 'text',
            data: {ptCode: id},
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


$(document).on("click", ".dtlBtn[data-dtl]", function () {
    let id = $(this).data("dtl");
    $("#ptlCode").val(id);
    $("#partnerForm").attr("action", "/admin/partner/goPartnerDtl");
    $("#partnerForm").submit();
});


$("#newPartnerBtn").click(function () {

    $("#ptlCode").val("new");
    $("#partnerForm").attr("action", "/admin/partner/goPartnerDtl");
    $("#partnerForm").submit();
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
        endPage = totalPage;
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
    getPartnerList();
}