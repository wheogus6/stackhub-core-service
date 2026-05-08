$(() => {
    $("#adminHeader").show();
    newOrDtl();
});

function newOrDtl() {
    let ptlCode = $("#ptlCode").val();
    if (ptlCode === "new") {
        newCompany();
        $("#newOrEditBtn").text("등록");
    } else {
        getPartnerDtl(ptlCode);
        $("#newOrEditBtn").text("수정");
    }
}

function newCompany() {
    $("#downloadLinkText").hide();
    $("#downloadLinkDisplay").hide();
    $("#createLinkText").hide();
    $("#createLinkDisplay").hide();
}

function getPartnerDtl(ptlCode) {
    $.ajax({
        type : "post",
        url : "/admin/partner/getPartnerDtl",
        async : true,
        dataType : 'json',
        data: {ptlCode: ptlCode},
        success: function (data) {
            setPartnerDtl(data);
        }
        , error() {
            alert("불러오기에 실패했습니다.");
        },
    })
}

function setPartnerDtl(data) {
    // $("#idArea").hide();
    $("#ptId").val(data.id);
    $("#pwd").val("");
    $("#ptName").val(data.ptName);
    $("#telNo").val(data.telNo);
    $("#homepage").val(data.homepage);
    $("#addr").val(data.addr);
    $("#memo").val(data.memo);
    $("#fileName").val(data.ptLogoImg)
}

$("#newOrEditBtn").click(function () {
    let ptlCode = $("#ptlCode").val();

    if (ptlCode === "new") {
        createPartner(ptlCode);
    } else {
        editPartner(ptlCode);
    }
});

function createPartner(ptlCode) {
    if (validation(ptlCode)) {

        let form = document.getElementById('companyForm');
        let formData = new FormData(form);

        $.ajax({
            type : "post",
            url : "/admin/partner/newPartner",
            async : true,
            dataType : 'text',
            processData: false,
            contentType: false,
            traditional: true,
            data: formData,
            success: function (data) {
                if (data === "00") {
                    alert("업체를 등록하였습니다.");
                    location.href = "/admin/partner/goPartnerPage";
                } else if (data == "99") {
                    alert("중복 된 아이디 입니다.");
                    return false;
                } else {
                    alert("등록에 실패했습니다.");
                    location.reload();
                }
            }
            , error() {
                alert("등록에 실패했습니다.");
                location.reload();
            },
        })
    }
}

function editPartner(ptlCode) {
    if (validation(ptlCode)) {
        let ptLinkCode = $("#ptlCode").val();
        let form = $("#companyForm");
        let formData = new FormData(form[0]);
        formData.append("ptLinkCode", ptLinkCode);

        $.ajax({
            type : "post",
            url : "/admin/partner/editPartner",
            async : true,
            dataType : 'text',
            processData: false,
            contentType: false,
            data: formData,
            success: function (data) {
                console.log(data);
                if (data === "00") {
                    alert("업체를 수정하였습니다.");
                    location.reload();
                } else if (data == "99") {
                    alert("중복 된 아이디입니다.");
                } else if (data == "98"){
                    alert("이미 등록 된 업체명입니다.");
                }else {
                    alert("수정에 실패했습니다.");
                    location.reload();
                }
            }
            , error() {
                alert("수정에 실패했습니다.");
                location.reload();
            },
        })
    }
}


function validation(ptlCode) {

    let id = $("#ptId").val();
    let pwd = $("#pwd").val();
    let ptName = $("#ptName").val();
    let cpTelNo = $("#telNo").val();
    let homepage = $("#homepage").val();
    let addr = $("#addr").val();

    if (isNullOrEmpty(id)) {
        alert("아이디를 입력해주세요.");
        return false;
    }
    if (/[\s]/g.test(id)) {
        alert("아이디에 공백을 빼주세요");
        return false;
    }

    if (ptlCode === "new") {
        if (isNullOrEmpty(pwd)) {
            alert("비밀번호를 입력해주세요.");
            return false;
        }
        if (/[\s]/g.test(pwd)) {
            alert("비밀번호에 공백을 빼주세요");
            return false;
        }
    }

    if (isNullOrEmpty(ptName)) {
        alert("업체이름을 입력해주세요.");
        return false;
    }
    if (isNullOrEmpty(cpTelNo)) {
        alert("연락처를 입력해주세요");
        return false;
    }
    if (isNullOrEmpty(homepage)) {
        alert("홈페이지를 입력해주세요");
        return false;
    }
    // if (isNullOrEmpty(addr)) {
    //     alert("주소를 입력해주세요");
    //     return false;
    // }

    return true;
}

$("#list_bt").click(function () {
    location.href = "/admin/partner/goPartnerPage";
});

const allowedTypes = [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/bmp',
    'image/webp'
];

$("#file").on('change', function() {
    let file = this.files[0];

    const fileType = file.type.toLowerCase();

    if (!allowedTypes.includes(fileType)) {
        alert(`지원하지 않는 이미지 포맷입니다. (${fileType})\njpg, png, gif, bmp, webp만 허용됩니다.`);
        this.value = ''
        return false;
    }

    let fileName = $("#file").prop('files')[0].name;

    $(".upload-name").val(fileName);
});