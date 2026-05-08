package core.controller;

import core.dto.CommentDto;
import core.dto.FileDto;
import core.dto.MemberDto;
import core.service.FileService;
import core.service.MemberService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gday/front")
public class FrontController {

    @Autowired
    MemberService memberService;
    @Autowired
    FileService fileService;

    @GetMapping("/memorialHall")
    public String memorialHall(String funeral, Model model, HttpSession session) {
        model.addAttribute("memorialCode", funeral);
        session.setAttribute("memorialCode", funeral);
        MemberDto dto = memberService.getMemberInfo(funeral);

        String skinId = dto.getSkinId();
        String returnUrl = "/front/memorialHall";
        if (skinId.equals("01")) {
            returnUrl = "front/memorialHall";
        } else if (skinId.equals("02")) {
            returnUrl = "front/memorialHall02";
        } else {
            returnUrl = "front/memorialHall03";
        }
        return returnUrl;
    }

    @GetMapping("/uploadComplete")
    public String uploadComplete() {
        return "front/uploadComplete";
    }


    @PostMapping("/getMemberInfo")
    @ResponseBody
    public MemberDto getMemberInfo(String memorialCode, HttpSession session) {
//        String test = session.getAttribute("memorialCode").toString();
        MemberDto dto = memberService.getMemberInfo(memorialCode);
        return dto;
    }

    @GetMapping("/goCommentPage")
    public String goCommentPage() {
        return "front/comment";
    }

    @GetMapping("/goCommentWrite")
    public String goCommentWrite() {
        return "front/commentWrite";
    }

    @PostMapping("/commentWrite")
    @ResponseBody
    public String commentWrite(String name, String comment, HttpSession session) {
        String memorialCode = session.getAttribute("memorialCode").toString();

        String result = memberService.commentWrite(memorialCode, name, comment);
        return result;
    }

    @PostMapping("/getCommentList")
    @ResponseBody
    public List<CommentDto> getCommentList(HttpSession session) {
        String memorialCode = session.getAttribute("memorialCode").toString();
        List<CommentDto> list = memberService.getCommentList(memorialCode);
        return list;
    }

    @GetMapping("/create")
    public String goCreatePage(String memberCreateCode, Model model, HttpSession session) throws Exception {
        model.addAttribute("memberCreateCode", memberCreateCode);

        String[] code = memberCreateCode.split("-");
        String ptLinkCode = code[0];
        String memorialCode = code[1];

        MemberDto dto = memberService.getMemberSttus(ptLinkCode, memorialCode);
        session.setAttribute("ptCode", ptLinkCode);
        session.setAttribute("memorialCode", memorialCode);
        model.addAttribute("sttus", dto.getStatus());
        model.addAttribute("memorialCode", memorialCode);

        model.addAttribute("petName", dto.getPetName());
        model.addAttribute("mobileNo", dto.getMobileNo());
        model.addAttribute("funeralDateStr", dto.getFuneralDateStr());
        model.addAttribute("ptLogoImg", dto.getPtLogoImg());

        String url = "/front/uploadPage";

        if (dto.getMemberSttus().equals("00")) {
            if (dto.getStatus().equals("90")) {
                url = "front/uploadPage";
            } else {
                url = "front/uploadComplete";
            }
        } else {
            url = "front/uploadCancel";
        }


        return url;
    }


    @PostMapping("/saveImg")
    @ResponseBody
    public String saveImg(FileDto dto, HttpSession session) throws IOException {
        String ptLinkCode = session.getAttribute("ptCode").toString();
        String memorialCode = session.getAttribute("memorialCode").toString();
        dto.setPtLinkCode(ptLinkCode);
        dto.setMemorialCode(memorialCode);
        String result = fileService.saveImg(dto);
        return result;
    }

    @PostMapping("/rotateImg")
    @ResponseBody
    public String rotateImg(FileDto dto, HttpSession session) throws IOException {

        String ptLinkCode = session.getAttribute("ptCode").toString();
        String memorialCode = session.getAttribute("memorialCode").toString();
        dto.setPtLinkCode(ptLinkCode);
        dto.setMemorialCode(memorialCode);
        String result = fileService.rotateImg(dto);
        return result;
    }


    @PostMapping("/uploadForm")
    @ResponseBody
    public String uploadForm(FileDto dto, HttpSession session) throws IOException {

        String memorialCode = session.getAttribute("memorialCode").toString();
        String ptLinkCode = session.getAttribute("ptCode").toString();
        dto.setMemorialCode(memorialCode);
        dto.setPtLinkCode(ptLinkCode);

        String result = fileService.uploadForm(dto);
        return result;
    }



    @PostMapping("/getThumbImg")
    @ResponseBody
    public List<FileDto> getThumbImg(String memorialCode) {
        return memberService.getThumbImg(memorialCode);
    }

    @PostMapping("/getUploadImg")
    @ResponseBody
    public Map<String, Object> getUploadImg(String memorialCode) throws Exception {
        return memberService.getUploadImg(memorialCode);
    }


    @PostMapping("/saveM01")
    @ResponseBody
    public String saveM01(String fileUrl, Integer leftPercentage, Integer topPercentage, Integer widthPercentage, Integer heightPercentage, Integer rotationM01, HttpSession session) throws IOException {

        String ptLinkCode = session.getAttribute("ptCode").toString();
        String memorialCode = session.getAttribute("memorialCode").toString();

        String returnFileUrl = fileService.saveM01(fileUrl, leftPercentage, topPercentage, widthPercentage, heightPercentage, ptLinkCode, memorialCode);

        return returnFileUrl;
    }

    @PostMapping("/saveM01Version1")
    @ResponseBody
    public String saveM01Version1(MultipartFile file, Integer leftPercentage, Integer topPercentage, Integer widthPercentage, Integer heightPercentage, Integer rotationM01, HttpSession session) throws IOException {

        String ptLinkCode = session.getAttribute("ptCode").toString();
        String memorialCode = session.getAttribute("memorialCode").toString();

        String fileUrl = fileService.saveM01Version1(file, leftPercentage, topPercentage, widthPercentage, heightPercentage, ptLinkCode, memorialCode, rotationM01);

        return fileUrl;
    }

    @PostMapping( "/download2G")
    public ResponseEntity<InputStreamResource> download2G(@RequestParam("img") MultipartFile file) throws IOException {
        byte[] imageBytes = file.getBytes();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(imageBytes);


        byte[] processedBytes = bos.toByteArray(); // 실제 이미지 처리 후 바이트 배열

        // InputStreamResource로 변환
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(processedBytes));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=memorial.jpg")
                .contentType(MediaType.IMAGE_JPEG)
                .contentLength(processedBytes.length)
                .body(resource);
    }

    @PostMapping
    public  List<FileDto> getUploadPhoto(String memorialCode) {
        return memberService.getUploadPhoto(memorialCode);
    }

    @PostMapping("/savePetName")
    @ResponseBody
    public void savePetName(String petName, String memorialCode) {
        memberService.savePetName(petName, memorialCode);
    }


    @PostMapping("/saveTerms1")
    @ResponseBody
    public void saveTerms1(boolean terms1, String memorialCode) {
        memberService.saveTerms1(terms1, memorialCode);
    }

    @PostMapping("/saveTerms2")
    @ResponseBody
    public void saveTerms2(boolean terms2, String memorialCode) {
        memberService.saveTerms2(terms2, memorialCode);
    }

    @PostMapping("/saveSkinId")
    @ResponseBody
    public void saveSkinId(String skinId, String memorialCode) {
        memberService.saveSkinId(skinId, memorialCode);
    }


    @PostMapping("/saveMobileNo")
    @ResponseBody
    public void saveMobileNo(String mobileNo, String memorialCode) throws Exception {
        memberService.saveMobileNo(mobileNo, memorialCode);
    }


    @PostMapping("/saveFuneralDate")
    public void saveFuneralDate(String funeralDateText, String memorialCode) {
        memberService.saveFuneralDate(funeralDateText, memorialCode);
    }

}
