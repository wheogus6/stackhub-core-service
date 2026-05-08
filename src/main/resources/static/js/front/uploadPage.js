let rotationM01 = 0;
let rotation01 = 0;
let rotation02 = 0;
let rotation03 = 0;
let rotation04 = 0;
let rotation05 = 0;

let photoValidation = {
    m01: false,
    p01: false,
    p02: false,
    p03: false,
    p04: false,
    p05: false
}

const allowedTypes = [
    'image/jpeg',
    'image/png',
    'image/gif',
    'image/bmp',
    'image/webp'
];

$(() => {
    let sttus = $("#sttus").val();
    getUploadImg();

    if (sttus != "90") {
        getThumbImg();
        $("#uploadBtn").hide();
        $("#terms1").prop("checked", true);
        $("#terms1").prop("disabled", true);
        $("#terms2").prop("checked", true);
        $("#terms2").prop("disabled", true);
    } else {
        getToday();
    }

    $("#petName").focusout(function () {
        $.ajax({
            type: "post",
            url: "/gday/front/savePetName",
            async: true,
            dataType: 'json',
            data: {petName: $(this).val(),
                memorialCode: $("#memorialCode").val()},
            success: function () {
            }
        })
    });


    $("#mobileNo").focusout(function () {
        $.ajax({
            type: "post",
            url: "/gday/front/saveMobileNo",
            async: true,
            dataType: 'json',
            data: {mobileNo: $(this).val(),
                memorialCode: $("#memorialCode").val()},
            success: function () {
            }
        })
    });

    $("#funeralDateStr").on("change", function () {
        $.ajax({
            type: "post",
            url: "/gday/front/saveFuneralDate",
            async: true,
            dataType: 'json',
            data: {funeralDateText: $(this).val(),
                memorialCode: $("#memorialCode").val()},
            success: function () {
            }
        })
    });

    $("#terms1").on("change", function () {
        $.ajax({
            type: "post",
            url: "/gday/front/saveTerms1",
            async: true,
            dataType: 'json',
            data: {terms1: $(this).prop("checked"),
                memorialCode: $("#memorialCode").val()},
            success: function () {
            }
        })
    });

    $("#terms2").on("change", function () {
        $.ajax({
            type: "post",
            url: "/gday/front/saveTerms2",
            async: true,
            dataType: 'json',
            data: {terms2: $(this).prop("checked"),
                memorialCode: $("#memorialCode").val()},
            success: function () {
            }
        })
    });

    $("input[name=skinId]").click(function () {

        $.ajax({
            type: "post",
            url: "/gday/front/saveSkinId",
            async: true,
            dataType: 'json',
            data: {skinId: $(this).val(),
                memorialCode: $("#memorialCode").val()},
            success: function () {
            }
        })
    });

});

function getUploadImg() {

    $.ajax({
        type : "post",
        url : "/gday/front/getUploadImg",
        async : true,
        dataType : 'json',
        data : {memorialCode: $("#memorialCode").val()},
        success: function (data) {
            if (!isNullOrEmpty(data.memberDto)) {
                let memberDto = data.memberDto;
                if (!isNullOrEmpty(memberDto.petName)) {
                    $("#petName").val(memberDto.petName);
                }
                if (!isNullOrEmpty(memberDto.funeralDateStr)) {
                    $("#funeralDateStr").val(memberDto.funeralDateStr);
                }
                if (!isNullOrEmpty(memberDto.mobileNo)) {
                    $("#mobileNo").val(memberDto.mobileNo);
                }

                if (!isNullOrEmpty(memberDto.terms1)) {
                    if (memberDto.terms1 == "1") {
                        $("#terms1").prop("checked", true);
                    } else {
                        $("#terms1").prop("checked", false);
                    }
                }

                if (!isNullOrEmpty(memberDto.terms2)) {
                    if (memberDto.terms2 == "1") {
                        $("#terms2").prop("checked", true);
                    } else {
                        $("#terms2").prop("checked", false);
                    }
                }

                if (!isNullOrEmpty(memberDto.skinId)) {
                    $(`input[name=skinId][value=${memberDto.skinId}]`).prop("checked", true);
                }
            }
            if (data.imgList.length > 0) {
                data.imgList.forEach(function (val) {
                    let fileId = val.fileId;
                    if (fileId == "m01") {
                        $("#m01Img").attr("src", val.fileDir);
                        $("#m01Img").show();
                        $("#m01Btn").hide();
                        $("#m01EditDisplay").show();
                        photoValidation.m01 = true;
                    }

                    if (fileId == "01") {
                        $("#01Img").attr("src", val.fileDir);
                        $("#01Img").show();
                        $("#01Btn").hide();
                        $("#01change").show();
                        photoValidation.p01 = true;
                    }
                    if (fileId == "02") {
                        $("#02Img").attr("src", val.fileDir);
                        $("#02Img").show();
                        $("#02Btn").hide();
                        $("#02change").show();
                        photoValidation.p02 = true;
                    }
                    if (fileId == "03") {
                        $("#03Img").attr("src", val.fileDir);
                        $("#03Img").show();
                        $("#03Btn").hide();
                        $("#03change").show();
                        photoValidation.p03 = true;
                    }
                    if (fileId == "04") {
                        $("#04Img").attr("src", val.fileDir);
                        $("#04Img").show();
                        $("#04Btn").hide();
                        $("#04change").show();
                        photoValidation.p04 = true;
                    }
                    if (fileId == "05") {
                        $("#05Img").attr("src", val.fileDir);
                        $("#05Img").show();
                        $("#05Btn").hide();
                        $("#05change").show();
                        photoValidation.p05 = true;
                    }
                });
            }
        }
        , error :function (error){
            alert("불러오기에 실패 했습니다.");
        },
    })
}


document.addEventListener("DOMContentLoaded", function() {
    const btns = document.querySelectorAll(".agreeBtn");

    btns.forEach((btn) => {
        btn.addEventListener("click", () => {
            const agreeItem = btn.parentNode;
            const agreeAnswer = agreeItem.querySelector('.agreeAnswer');
            const isActive = agreeItem.classList.contains("active");

            removeActiveClasses();

            if (!isActive) {
                agreeItem.classList.add("active");
                agreeAnswer.style.height = agreeAnswer.scrollHeight + "px";
            } else {
                agreeItem.classList.remove("active");
                agreeAnswer.style.height = 0;
            }
        });
    });

    function removeActiveClasses() {
        btns.forEach((btn) => {
            const agreeItem = btn.parentNode;
            const agreeAnswer = agreeItem.querySelector('.agreeAnswer');
            agreeItem.classList.remove("active");
            agreeAnswer.style.height = 0;
        });
    }
});


function getThumbImg() {

    $.ajax({
        type : "post",
        url : "/gday/front/getThumbImg",
        async : true,
        dataType : 'json',
        data : {memorialCode: $("#memorialCode").val()},
        success: function (data) {
            if (data.length > 0) {
                data.forEach(function (val) {
                    let fileId = val.fileId;
                    if (fileId == "m01") {
                        $("#m01Img").attr("src", val.fileDir);
                        $("#m01Img").show();
                        $("#m01Btn").hide();
                        photoValidation.m01 = true;
                    }

                    if (fileId == "01") {
                        $("#01Img").attr("src", val.fileDir);
                        $("#01Img").show();
                        $("#01Btn").hide();
                        photoValidation.p01 = true;
                    }
                    if (fileId == "02") {
                        $("#02Img").attr("src", val.fileDir);
                        $("#02Img").show();
                        $("#02Btn").hide();
                        photoValidation.p02 = true;
                    }
                    if (fileId == "03") {
                        $("#03Img").attr("src", val.fileDir);
                        $("#03Img").show();
                        $("#03Btn").hide();
                        photoValidation.p03 = true;
                    }
                    if (fileId == "04") {
                        $("#04Img").attr("src", val.fileDir);
                        $("#04Img").show();
                        $("#04Btn").hide();
                        photoValidation.p04 = true;
                    }
                    if (fileId == "05") {
                        $("#05Img").attr("src", val.fileDir);
                        $("#05Img").show();
                        $("#05Btn").hide();
                        photoValidation.p05 = true;
                    }
                });

                $("#petName").val($("#savePetName").val())
                $("#mobileNo").val($("#saveMobileNo").val())
                $("#funeralDateStr").val($("#saveFuneralDateStr").val())
            }
        }
        , error :function (error){
            alert("불러오기에 실패 했습니다.");
        },
    })
}

let upload = 0;

$("#uploadBtn").click(function () {

    let terms1 = $("#terms1").prop("checked");
    let terms2 = $("#terms2").prop("checked");
    let mobileNo = $("#mobileNo").val();
    let funeralDateStr = $("#funeralDateStr").val();
    let petName = $("#petName").val();
    let m01File = $("#m01").val();
    let file01 = $("#01").val();
    let file02 = $("#02").val();
    let file03 = $("#03").val();
    let file04 = $("#04").val();
    let file05 = $("#05").val();



    if (isNullOrEmpty(petName)) {
        alert("반려동물 이름을 입력해주세요.");
        return false;
    }

    if (isNullOrEmpty(funeralDateStr)) {
        alert("무지개 다리 건넌날을 입력해주세요.");
        return false;
    }

    if (isNullOrEmpty(mobileNo)) {
        alert("연락처를 입력해주세요.");
        return false;
    }
    // else if (mobileNo.length < 9 || mobileNo.length > 11) {
    //     alert("연락처 형식을 맞춰주세요.");
    //     return false;
    // } else if (!telNoTest(mobileNo)) {
    //     alert("연락처 형식을 맞춰주세요.");
    //     return false;
    // }

    if (!terms1) {
        alert("개인정보처리방침에 동의해주세요.");
        return false;
    }
    if (!terms2) {
        alert("이용약관에 동의해주세요.");
        return false;
    }

    let srcM01 = $("#m01Img").prop("src");
    let src01 = $("#01Img").prop("src");
    let src02 = $("#02Img").prop("src");
    let src03 = $("#03Img").prop("src");
    let src04 = $("#04Img").prop("src");
    let src05 = $("#05Img").prop("src");

    if (!photoValidation.m01) {
        if (isNullOrEmpty(m01File) || isNullOrEmpty(srcM01)) {
            alert("추모관 사진을 등록해주세요.");
            return false;
        }
    }
    if (!photoValidation.p01) {
        if (isNullOrEmpty(file01) || isNullOrEmpty(src01)) {
            alert("첫번째 추모영상 사진을 등록해주세요.");
            return false;
        }
    }

    if (!photoValidation.p02) {
        if (isNullOrEmpty(file02) || isNullOrEmpty(src02)) {
            alert("두번째 추모영상 사진을 등록해주세요.");
            return false;
        }
    }
    if (!photoValidation.p03) {
        if (isNullOrEmpty(file03) || isNullOrEmpty(src03)) {
            alert("세번째 추모영상 사진을 등록해주세요.");
            return false;
        }
    }
    if (!photoValidation.p04) {
        if (isNullOrEmpty(file04) || isNullOrEmpty(src04)) {
            alert("네번째 추모영상 사진을 등록해주세요.");
            return false;
        }
    }
    if (!photoValidation.p05) {
        if (isNullOrEmpty(file05) || isNullOrEmpty(src05)) {
            alert("다섯번째 추모영상 사진을 등록해주세요.");
            return false;
        }
    }

    upload++
    if (upload == 1) {
        if (confirm("최종 업로드 후에는 수정이 불가능하오니, \n꼭 확인 후 업로드 해주세요. \n최종 업로드 하시겠습니까?")) {
            $("#uploadBtn").prop("disabled", true);
            $("#uploadBtn").text("업로드 중");
            uploadForm();
        } else {
            upload = 0;
            return false;
        }
    } else {
        upload = 0;
        return false;
    }


});



function uploadForm() {

    // let form = document.getElementById("uploadForm");

    let formData = new FormData();
    formData.append("petName", $("#petName").val());
    formData.append("funeralDateStr", $("#funeralDateStr").val());
    formData.append("mobileNo", $("#mobileNo").val());

    let skinId = "";
    $("input[name=skinId]").each(function () {
        let checked = $(this).prop("checked");
        if (checked) {
            skinId = $(this).val();
        }
    });

    formData.append("skinId", skinId);


    $.ajax({
        type : "post",
        url : "/gday/front/uploadForm",
        async : true,
        dataType : 'text',
        data: formData,
        contentType: false,
        processData: false,
        traditional: true,
        success: function (data) {
            if (data === "00") {
                // alert("업로드 완료. \n 영상 제작 완료 시 문자로 안내해 드리겠습니다.");
                location.href = "/gday/front/uploadComplete";
            } else if (data == "98") {
                alert("사용 중지 된 사용자입니다.");
                window.close();
            } else if (data == "88") {
                alert("업로드 중 오류가 발생하였습니다. 다시 처음부터 업로드 부탁드립니다.");
                location.reload();
            } else {
                alert("이미 업로드를 완료하셨습니다.");
                return false;
            }
        }
        , error() {
            alert("");
        },
    })
}


//사진 선택
$(document).on("click", ".photoSelectBtn[data-photo-id]", function () {
    let id = $(this).data('photo-id');
    let type = "render";
    $(`#${id}`).click();

    let width = $(this).width();
    let height = $(this).height();

    selectPhoto(id, type, width, height);
});

//사진 변경
$(document).on("click", ".photoChangeBtn[data-photo-id]", function () {
    let id = $(this).data('photo-id');
    let type = "render";
    $(`#${id}`).val("");
    $(`#${id}`).click();

    selectPhoto(id, type);
});



function selectPhoto(id, type, width, height) {

    $(`#${id}`).on("change", async function (event) {
        switch (id) {
            case "01":
                rotation01 = 0;
                break;
            case "02":
                rotation02 = 0;
                break;
            case "03":
                rotation03 = 0;
                break;
            case "04":
                rotation04 = 0;
                break;
            case "05":
                rotation05 = 0;
                break;
        }

        let btn = id + "Btn";
        let img = id + "Img";
        let editDisplay = id + "EditDisplay";
        let file = this.files[0];
        let change = id + "change";

        const fileType = file.type;

        if (!allowedTypes.includes(fileType)) {
            $("#loadingModal").addClass("on");
            $("#loadingText").text("가져오는 중");
            showDots();
            try {
                const convertedBlob = await heic2any({
                    blob: file,
                    toType: "image/jpeg",
                    quality: 0.7
                });

                let convertedFile = new File([convertedBlob], file.name.replace(/\.\w+$/, ".jpg"), {
                    type: "image/jpeg"
                });
                if (convertedFile && fileType.startsWith('image')) {
                    try {
                        let url = await saveImg(convertedFile, id, type);
                        if (url == "90") {
                            this.value = ''
                            alert("사진을 불러오는데 실패하였습니다.");
                            return false;
                        } else if (url == "89") {
                            this.value = ''
                            alert(`지원하지 않는 이미지 포맷입니다. (${fileType})\njpg, png, gif, bmp, webp만 허용됩니다.`);
                            return false;
                        } else if (url == "99") {
                            this.value = ''
                            alert("잘못된 파일입니다.");
                            return false;
                        }
                        $(`#${img}`).attr("src", url);
                        $(`#${img}`).show();
                        $(`#${btn}`).hide();
                        $(`#${change}`).hide();
                        $(`#${editDisplay}`).show();
                        setValidation(id);
                    } catch (error) {
                        this.value = ''
                        alert("사진을 불러오는데 실패하였습니다.");
                        return false;
                    }
                } else {
                    this.value = ''
                    alert("이미지 파일만 선택 가능합니다.");
                    return false;
                }
            } catch (err) {
                alert(`지원하지 않는 이미지 포맷입니다. (${fileType})\njpg, png, gif, bmp, webp만 허용됩니다.`);
                this.value = ''
                return false;
            }
        } else {
            if (file && fileType.startsWith('image')) {
                try {
                    $("#loadingModal").addClass("on");
                    $("#loadingText").text("가져오는 중");
                    showDots();
                    let url = await saveImg(file, id, type);
                    if (url == "90") {
                        this.value = ''
                        alert("사진을 불러오는데 실패하였습니다.");
                        return false;
                    } else if (url == "89") {
                        this.value = ''
                        alert(`지원하지 않는 이미지 포맷입니다. (${fileType})\njpg, png, gif, bmp, webp만 허용됩니다.`);
                        return false;
                    } else if (url == "99") {
                        this.value = ''
                        alert("잘못된 파일입니다.");
                        return false;
                    }
                    $(`#${img}`).attr("src", url);
                    $(`#${img}`).show();
                    $(`#${btn}`).hide();
                    $(`#${change}`).hide();
                    $(`#${editDisplay}`).show();
                    setValidation(id);
                } catch (error) {
                    this.value = ''
                    alert("사진을 불러오는데 실패하였습니다.");
                    return false;
                }
            } else {
                this.value = ''
                alert("이미지 파일만 선택 가능합니다.");
                return false;
            }
        }

        $("#loadingModal").removeClass("on");
        hideDots();
    });
}




function setValidation(id) {
    switch (id) {
        case "m01":
            photoValidation.m01 = true;
            break;
        case "01":
            photoValidation.p01 = true;
            break;
        case "02":
            photoValidation.p02 = true;
            break;
        case "03":
            photoValidation.p03 = true;
            break;
        case "04":
            photoValidation.p04 = true;
            break;
        case "05":
            photoValidation.p05 = true;
            break;
    }
}


function saveImg(file, id, type) {
    let formData = new FormData();
    formData.append("file", file);
    formData.append("fileId", id);
    formData.append("type", type);

    return new Promise((resolve, reject) => {
        $.ajax({
            type : "post",
            url : "/gday/front/saveImg",
            async: true,
            dataType : 'text',
            data: formData,
            contentType: false,
            processData: false,
            traditional: true,
            success: function (data) {
                if (data == "99") {
                    alert("이미 업로드를 완료하셨습니다.");
                    reject("99");
                } else {
                    resolve(data);
                }
            },
            error: function () {
                $("#loadingModal").removeClass("on");
                hideDots();
                alert("사진을 불러오는데 실패하였습니다.\n 다시 시도해주세요.");
                reject("error");
            }
        });
    });
}



function saveM01Version1(file, id, type, rotationM01, text) {

    let formData = new FormData();
    formData.append("file", file);
    formData.append("fileId", id);
    formData.append("type", type);
    formData.append("rotationM01", rotationM01);


    return new Promise((resolve, reject) => {
        $.ajax({
            type : "post",
            url : "/gday/front/saveM01Version1",
            async: true,
            dataType : 'text',
            data: formData,
            contentType: false,
            processData: false,
            traditional: true,
            success: function (data) {
                $("#loadingModal").removeClass("on");
                hideDots();
                if (data == "99") {
                    alert("이미 업로드를 완료하셨습니다.");
                    reject("99");
                } else {
                    resolve(data);
                }
            },
            error: function () {
                $("#loadingModal").removeClass("on");
                hideDots();
                alert("사진을 불러오는데 실패하였습니다.\n 다시 시도해주세요.");
                reject("error");
            }
        })
    });

}




// 회전 버튼
$(document).on("click", ".rotationBtn[data-rotate-id]", async function () {
    let id = $(this).data("rotate-id");

    //이미지 돌림
    let rotation = 0;

    switch (id) {

        case "01":
            rotation01 += 90;
            if (rotation01 === 360) {
                rotation01 = 0;
            }
            rotation = rotation01;
            break;
        case "02":
            rotation02 += 90;
            if (rotation02 === 360) {
                rotation02 = 0;
            }
            rotation = rotation02;
            break;
        case "03":
            rotation03 += 90;
            if (rotation03 === 360) {
                rotation03 = 0;
            }
            rotation = rotation03;
            break;
        case "04":
            rotation04 += 90;
            if (rotation04 === 360) {
                rotation04 = 0;
            }
            rotation = rotation04;
            break;
        case "05":
            rotation05 += 90;
            if (rotation05 === 360) {
                rotation05 = 0;
            }
            rotation = rotation05;
            break;
    }
    let type = "render";
    let fileInput = $(`#${id}`)[0];
    let img = id + "Img";
    let file = fileInput.files[0];
    const fileType = file.type;
    if (!allowedTypes.includes(fileType)) {
        $("#loadingModal").addClass("on");
        $("#loadingText").text("변환 작업 중");
        showDots();

        const convertedBlob = await heic2any({
            blob: file,
            toType: "image/jpeg",
            quality: 0.7
        });

        file = new File([convertedBlob], file.name.replace(/\.\w+$/, ".jpg"), {
            type: "image/jpeg"
        });
    } else {
        $("#loadingModal").addClass("on");
        $("#loadingText").text("변환 작업 중");
        showDots();
    }

    let url = await rotateImg(file, id, type, rotation);
    $(`#${img}`).attr("src", url);
});


function rotateImg(file, id, type, rotation) {
    let formData = new FormData();
    formData.append("file", file);
    formData.append("fileId", id);
    formData.append("type", type);
    formData.append("rotation", rotation);

    return new Promise((resolve, reject) => {
        $.ajax({
            type: "post",
            url: "/gday/front/rotateImg",
            async: true,
            dataType: 'text',
            data: formData,
            contentType: false,
            processData: false,
            traditional: true,
            success: function (data) {
                $("#loadingModal").removeClass("on");
                hideDots();
                if (data == "99") {
                    alert("이미 업로드를 완료하셨습니다.");
                    reject("99");
                } else {
                    resolve(data);
                }
            }
            , error() {
                $("#loadingModal").removeClass("on");
                hideDots()
                alert("사진을 불러오는데 실패하였습니다.\n 다시 시도해주세요.");
                reject("error");
            },
        })
    });
}


function getToday() {
    let date = new Date();
    let year = date.getFullYear();
    let month = ("0" + (1 + date.getMonth())).slice(-2);
    let day = ("0" + date.getDate()).slice(-2);
    let today = year + "-" + month + "-" + day;
    $("#funeralDateStr").val(today);
}

function telNoTest(no) {
    let regex010 = /^010(?:\d{3}|\d{4})\d{4}$/; // 010 번호 형식
    let regex02 = /^02(?:\d{3}|\d{4})\d{4}$/; // 02 번호 형식
    let regex031032 = /^(031|032)(?:\d{3}|\d{4})\d{4}$/; // 031 또는 032 번호 형식

    return regex010.test(no) || regex02.test(no) || regex031032.test(no);

}


// $("#terms1View").click(function () {
//     document.body.style.overflow = 'hidden';
//     $("#firstModal").addClass("on");
// });
//
// $("#terms2View").click(function () {
//     document.body.style.overflow = 'hidden';
//     $("#secondModal").addClass("on");
// });
//
//
// $("#firstModalClose").click(function () {
//     document.body.style.overflow = '';
//     $("#firstModal").removeClass("on");
// });
//
// $("#secondModalClose").click(function () {
//     document.body.style.overflow = '';
//     $("#secondModal").removeClass("on");
// });


// 추모관 사진 처리
$("#m01Btn").click(function () {

        $("#m01").click();

});


let maxWidth = 0;
let maxHeight = 0;
let firstWidth = 0;
let firstHeight = 0;
let imgWidth = 0;
let imgHeight = 0;

$("#m01").on("change", async function () {

    rotationM01 = 0;

    let file = this.files[0];
    let fileType = file.type;

    if (file && fileType.startsWith('image')) {

        let text = "가져오는 중";
        let url = "";

        if (!allowedTypes.includes(fileType)) {
            $("#loadingModal").addClass("on");
            $("#loadingText").text(text);
            showDots();

            const convertedBlob = await heic2any({
                blob: file,
                toType: "image/jpeg",
                quality: 0.7
            });

            let convertedFile = new File([convertedBlob], file.name.replace(/\.\w+$/, ".jpg"), {
                type: "image/jpeg"
            });
            url = await saveM01Version1(convertedFile, "m01" ,fileType, rotationM01, text);
        } else {
            $("#loadingModal").addClass("on");
            $("#loadingText").text(text);
            url = await saveM01Version1(file, "m01" ,fileType, rotationM01, text);
        }

        if (url == "90") {
            this.value = ''
            alert("사진을 불러오는데 실패하였습니다.");
            return false;
        } else if (url == "89") {
            this.value = ''
            alert(`지원하지 않는 이미지 포맷입니다. (${fileType})\njpg, png, gif, bmp, webp만 허용됩니다.`);
            return false;
        } else if (url == "99") {
            this.value = ''
            alert("잘못된 파일입니다.");
            return false;
        }
        $("#modalImg").attr("src", url);
        let img = document.getElementById('modalImg');
        img.onload = function () {
            imgWidth = img.naturalWidth;
            imgHeight = img.naturalHeight;


            document.body.style.overflow = 'hidden';
            $("#m01Modal").addClass("on");

            let imgWidth2 = $("#modalImg").width();
            let imgHeight2 = $("#modalImg").height();

            maxWidth = imgWidth2;
            maxHeight = imgHeight2;

            firstWidth = imgWidth2;
            firstHeight = imgHeight2;

            $("#overlay").css({
                width: "200px",
                height: "250px",
            });

        }
    } else {
        this.value = ''
        alert("이미지 파일만 선택 가능합니다.");
        return false;
    }

});



async function rotateImage(degrees) {
    rotationM01 += degrees;

    let fileInput = $("#m01")[0];
    let file = fileInput.files[0];
    let type = fileInput.type;
    let fileName = "m01";
    let fileType = file.type;

    let text = "변환 작업 중";

    $("#loadingModal").addClass("on");
    $("#loadingText").text(text);
    showDots();
    if (!allowedTypes.includes(fileType)) {
        const convertedBlob = await heic2any({
            blob: file,
            toType: "image/jpeg",
            quality: 0.7
        });

        file = new File([convertedBlob], file.name.replace(/\.\w+$/, ".jpg"), {
            type: "image/jpeg"
        });
    }

    let url = await saveM01Version1(file, fileName, type, rotationM01, text);

    if (url == "90") {
        alert("사진을 불러오는데 실패하였습니다.");
        return false;
    }

    $("#modalImg").attr("src", url);

    let img = document.getElementById('modalImg');
    img.onload = function () {
        imgWidth = img.naturalWidth;
        imgHeight = img.naturalHeight;

        document.body.style.overflow = 'hidden';

        // 회전 후 새 이미지 크기 계산
        let imgWidth2 = $("#modalImg").width();
        let imgHeight2 = $("#modalImg").height();

        maxWidth = imgWidth2;
        maxHeight = imgHeight2;

        firstWidth = imgWidth2;
        firstHeight = imgHeight2;

        // 기존 자르기 영역 크기와 위치를 재조정
        $("#overlay").css({
            width: "200px",  // 적절한 값으로 조정
            height: "250px",  // 적절한 값으로 조정
            left: (imgWidth2 - 200) / 2 + "px",  // 중앙으로 위치 조정
            top: (imgHeight2 - 250) / 2 + "px"  // 중앙으로 위치 조정
        });
    };
}





$("#m01ModalClose").click(function () {
    document.body.style.overflow = '';
    $("#m01Modal").removeClass("on");
});

$("#m01Rotate").click(function () {
    rotateImage(90);
});


let isResizing = false;
$("#resizeHandle").on("mousedown touchstart", function (e) {
    isResizing = true;

    $("#overlay").css({
        transform: 'none',
    });

    let initialWidth, initialHeight, initialX;
    let aspectRatio;

    initialWidth = $("#overlay").width();
    initialHeight = $("#overlay").height();

    aspectRatio = initialWidth / initialHeight;

    if (e.type === "touchstart") {
        initialX = e.touches[0].clientX;
    } else {
        initialX = e.clientX;
    }

    $(document).on("mousemove touchmove", function (e) {
        if (!isResizing) return;
        let currentX;
        if (e.type === "touchmove") {
            currentX = e.touches[0].clientX;
        } else {
            currentX = e.clientX;
        }

        let deltaX = currentX - initialX;

        let newWidth = initialWidth + deltaX;
        let newHeight = newWidth / aspectRatio;

        let imgWidth = $("#imgContainer").width() - 6;
        let imgHeight = $("#imgContainer").height() - 10;
        if (newWidth > imgWidth) {
            newWidth = imgWidth;
            newHeight = newWidth / aspectRatio;
        }

        if (newHeight > imgHeight) {
            newHeight = imgHeight;
            newWidth = newHeight * aspectRatio;
        }

        if (newWidth === 200) {
            newWidth = 200;
        }
        if (newHeight === 250) {
            newHeight = 250;
        }

        if (newWidth >= 200 && newHeight >= 250) {
            $("#overlay").css({
                width: newWidth + "px",
                height: newHeight + "px",
            });
        }
    });

    $(document).on("mouseup touchend", function () {
        isResizing = false;
        $(document).off("mousemove touchmove");
        $(document).off("mouseup touchend");
    });
});

let isDrag = false;

$("#overlay").on("mousedown touchstart", function (e) {
    if (e.target.id === "resizeHandle") {
        return;
    }

    $("#overlay").css({
        transform: 'none',
    });

    isDrag = true;
    let x, y;

    if (e.type === "mousedown") {
        x = e.clientX;
        y = e.clientY;
    } else {
        x = e.touches[0].clientX;
        y = e.touches[0].clientY;
    }

    let box = $(this);
    let left = parseInt(box.css("left"));
    let top = parseInt(box.css("top"));

    let imgWidth = $("#imgContainer").width() - 6;
    let imgHeight = $("#imgContainer").height() - 10;

    $(document).on("mousemove touchmove", function (e) {
        if (!isDrag) return;

        let newX, newY;
        if (e.type === "mousemove") {
            newX = left + e.clientX - x;
            newY = top + e.clientY - y;
        } else {
            newX = left + e.touches[0].clientX - x;
            newY = top + e.touches[0].clientY - y;
        }

        newX = Math.min(Math.max(0, newX), imgWidth - box.width());
        newY = Math.min(Math.max(0, newY), imgHeight - box.height());

        $("#overlay").css({
            left: newX + "px",
            top: newY + "px",
        });
    });

    $(document).on("mouseup touchend", function () {
        isDrag = false;
        $(document).off("mousemove touchmove");
        $(document).off("mouseup touchend");
    });
});



$("#complete").click(function () {
    saveM01Modal();
});

async function saveM01Modal() {
    const img = $("#modalImg");
    const overlay = $("#overlay");


    const left = parseInt(overlay.css("left"));
    const top = parseInt(overlay.css("top"));
    const width = overlay.width();
    const height = overlay.height();

    const src = $('#modalImg').attr('src');


    const imgWidth2 = img.width();
    const imgHeight2 = img.height();


    const leftPercentage = (left / imgWidth2) * imgWidth;
    const topPercentage = (top / imgHeight2) * imgHeight;
    const widthPercentage = (width / imgWidth2) * imgWidth;
    const heightPercentage = (height / imgHeight2) * imgHeight;


    let fileInput = $("#m01")[0];
    let file = fileInput.files[0];
    let fileType = file.type;
    $("#loadingModal").addClass("on");
    $("#loadingText").text("변환 작업 중");
    showDots();

    if (!allowedTypes.includes(fileType)) {
        const convertedBlob = await heic2any({
            blob: file,
            toType: "image/jpeg",
            quality: 0.7
        });

        file = new File([convertedBlob], file.name.replace(/\.\w+$/, ".jpg"), {
            type: "image/jpeg"
        });
    }

    let formData = new FormData();
    formData.append("file", file);
    formData.append("leftPercentage", parseInt(leftPercentage));
    formData.append("topPercentage", parseInt(topPercentage));
    formData.append("widthPercentage", parseInt(widthPercentage));
    formData.append("heightPercentage", parseInt(heightPercentage));
    formData.append("fileUrl", src);

    let url = await saveM01(formData)

    if (url == "90") {
        return false;
    }

    document.body.style.overflow = '';
    $("#m01Modal").removeClass("on");

    $("#m01Img").attr("src", url);
    $("#m01Img").show();
    $("#m01Btn").hide();
    $("#m01EditDisplay").show();

}


function saveM01(formData) {

    return new Promise((resolve, reject) => {
        $.ajax({
            type : "post",
            url : "/gday/front/saveM01",
            async: true,
            dataType : 'text',
            data: formData,
            contentType: false,
            processData: false,
            traditional: true,
            success: function (data) {
                $("#loadingModal").removeClass("on");
                hideDots();
                resolve(data);
            }
            , error() {
                alert("사진을 불러오는데 실패하였습니다.\n 다시 시도해주세요.");
                reject("90");
            },
        })
    });
}

$("#m01ChangeBtn").click(function () {
    $("#m01").click();
});

let dotsInterval;

function showDots() {
    let dots = '';
    let count = 0;
    dotsInterval = setInterval(function() {
        if (count < 5) {
            dots += '.';
            count++;
        } else {
            dots = '';
            count = 0;
        }
        $("#dots").text(dots);
    }, 300);
    return dotsInterval;
}

function hideDots() {
    clearInterval(dotsInterval);
    $("#dots").text('');
}