let memorialCode = $("#memorialCode").val();
Kakao.init('89c237cdef25449c0f0d9da0a84482bb');
$(() => {
    getMemberInfo()
});


function getMemberInfo() {

    $.ajax({
        type : "post",
        url : "/gday/front/getMemberInfo",
        async : true,
        dataType : 'json',
        data : {memorialCode: memorialCode},
        success: function (data) {
            let memberSttus = data.memberSttus;
            let finishDate = data.finishDate;
            console.log(finishDate)

            if (memberSttus == "00") {
                if (finishDate > 0) {
                    setInfo(data);
                } else {
                    hideInfo();
                    alert("유효기간이 만료되어 이용이 불가합니다. \n" +
                        "연장을 원하시면 관리자에게 연락주세요.");
                    return false;
                }
            } else {
                hideInfo();
                alert("사용 중지된 추모관입니다. \n" +
                    "연장을 원하시면 관리자에게 연락주세요.");
                window.close();
            }

        }
        , error :function (error){
            alert("정보를 불러오는데 실패 했습니다.");
        },
    })
}

function hideInfo() {
    $("#letter").hide();
    $("#dDay").hide();
    $("#bottom").hide();
    $("#footer_btn").hide();
    $("#video").hide();
    $("#main_img").hide();
    $("#finishInfo").show();
}
let petName;
let imgUrl;

function setInfo(data) {
    imgUrl = data.m01;
    petName = data.petName;
    $("#petNameTitle").text(data.petName);
    $("#videoUrl").attr("src", data.videoUrl);
    $("#m01").attr("src", data.m01);

    const imageUrl = data.m01;
    const img = new Image();
    img.src = imageUrl;
    img.onload = function() {
        const imageHeight = img.height;
        console.log(imageHeight)
        if (imageHeight > 890) {
            $("#mainImgBg").css("height", "100%");
        }
    };

    $("#countDate").text(data.countDate);
    $("#homepage").text(data.ptName)

    $("#footer_btn").click(function () {
        location.href = data.homepage;
    });

    $("#m01Download").attr("src", data.m01);
    $("#ptLogoImg").attr("src", data.ptLogoImg);


    $("#downloadImgBtn").click(function () {
        const element = document.querySelector('.mobile_container2G');
        element.style.display = "block";

        new html2canvas(document.querySelector('.mobile_container2G'), {
            backgroundColor: "transparent",
            allowTaint: true,
            useCORS: true
        })
            .then(canvas => {

                let isInAppBrowser = /KAKAOTALK/i.test(navigator.userAgent);
                element.style.display = "none";
                if (isInAppBrowser) {
                    let dataURL = canvas.toDataURL('image/jpg');
                    let newWindow = window.open();
                    newWindow.document.write('<img src="' + dataURL + '" style="max-width: 100%; max-height: 100%; object-fit: contain"/>');
                    if (!newWindow) {
                        alert('팝업이 차단되었습니다. 팝업 차단을 해제하고 다시 시도하세요.');
                    }
                } else {
                    canvas.toBlob(blob => {
                        const link = document.createElement('a');
                        link.href = URL.createObjectURL(blob);
                        link.download = 'memorial.jpg';
                        link.click();
                        URL.revokeObjectURL(link.href);
                        element.style.display = "none";
                    }, 'image/jpeg');
                }

            });
    });
}


$("#goCommentBtn").click(function () {
    location.href = '/gday/front/goCommentPage'
});

function shareMessage() {

    Kakao.Link.sendDefault({
        objectType: 'feed',
        content: {
            title: petName + ' 추모장',
            description: '언제나 그리운 마음을 함께 합니다.',
            imageUrl: window.location.href,
            link: {
                mobileWebUrl: window.location.href,
                webUrl: window.location.href,
            },
        },
        buttons: [
            {
                title: '추모하기',
                link: {
                    mobileWebUrl: window.location.href,
                    webUrl: window.location.href,
                },
            }
        ],
        installTalk: true
    });
}


// $("#kakaoBtn").click(function () {
//
//     (async () => {
//         if (navigator.share) {
//             try {
//                 await navigator.share({
//                     title: '펫시네 추모관',
//                     text: '따뜻한 마음을 전해주세요.',
//                     url: window.location.href,
//                 });
//                 console.log('공유 성공');
//             } catch (error) {
//                 console.log('공유 실패');
//             }
//         } else {
//             alert('공유 기능을 지원하지 않는 브라우저입니다.');
//         }
//     })();
// });
