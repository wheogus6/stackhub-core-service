package core.controller;


import core.dto.*;
import core.service.FileService;
import core.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
@RequestMapping("/admin/member")
public class AdminMemberController {

    @Autowired
    MemberService memberService;
    @Autowired
    private FileService fileService;

    @GetMapping("/goMemberPage")
    public String goMemberPage() {
        return "admin/member";
    }


    @PostMapping("/getMemberList")
    @ResponseBody
    public Object getMemberList(MemberDto dto, Pageable pageable) throws Exception {
        Page<MemberDto> list = memberService.getMemberList(dto, pageable);

        PageDto pd = new PageDto();
        pd.rows = list.getContent();
        pd.pageSize = list.getTotalPages();
        pd.page = list.getNumber();
        pd.totalSize = list.getTotalElements();

        return pd;
    }

    @PostMapping("/getMemberListByPartner")
    @ResponseBody
    public Object getMemberListByPartner(MemberDto dto, Pageable pageable, HttpSession session) throws Exception {
        String ptLinkCode = session.getAttribute("ptLinkCode").toString();
        dto.setPtLinkCode(ptLinkCode);
        Page<MemberDto> list = memberService.getMemberListByPartner(dto, pageable);

        PageDto pd = new PageDto();
        pd.rows = list.getContent();
        pd.pageSize = list.getTotalPages();
        pd.page = list.getNumber();
        pd.totalSize = list.getTotalElements();

        return pd;
    }


    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> download(String fileUrl) {
        byte[] data = fileService.getDownloadFile(fileUrl);
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = buildHttpHeaders(fileUrl, data);

        return ResponseEntity.ok().headers(headers).body(resource);
    }

    @GetMapping("/downloadZip")
    public ResponseEntity<ByteArrayResource> downloadZip(String videoUrl, String memorial, String petName, String regDate, String uname) throws IOException {

        byte[] video = fileService.getDownloadFile(videoUrl);
        List<FileDto> imageFiles = fileService.getDownloadImg(memorial);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);

        String customFileName = regDate + "_" + uname + "_" + petName;

        addFileToZip(video, customFileName + ".mp4", zipOutputStream);

        for (FileDto dto : imageFiles) {
            byte[] img = fileService.getDownloadFile(dto.getFileDir());
            String fileName = dto.getFileId() + ".jpg";
            addFileToZip(img, fileName, zipOutputStream);
        }

        zipOutputStream.close();
        byte[] zipBytes = byteArrayOutputStream.toByteArray();
        ByteArrayResource resource = new ByteArrayResource(zipBytes);

        String zipFileName = customFileName + ".zip";
        String encodedFileName = URLEncoder.encode(zipFileName, StandardCharsets.UTF_8.toString()).replace("+", "%20");


        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"");
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(zipBytes.length));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/zip");
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");


        return ResponseEntity.ok().headers(headers).body(resource);
//        return ResponseEntity.ok()
//                .headers(headers)
//                .contentType(MediaType.parseMediaType("application/zip"))
//                .body(resource);
    }

    private void addFileToZip(byte[] fileData, String fileName, ZipOutputStream zipOutputStream) throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipEntry.setSize(fileData.length);
        zipOutputStream.putNextEntry(zipEntry);
        zipOutputStream.write(fileData);
        zipOutputStream.closeEntry();
    }



    private HttpHeaders buildHttpHeaders(String fileUrl, byte[] data) {
        HttpHeaders headers = new HttpHeaders();
        String fileName = fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        long fileSize = data.length;

        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        headers.add(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
        headers.add(HttpHeaders.CONTENT_TYPE, "application/octet-stream");

        return headers;
    }

    @GetMapping("/goPartnerAdminDtl")
    public String goPartnerAdminDtl(String memorialCode, Model model) {
        model.addAttribute("memorialCode", memorialCode);
        return "partner/partnerAdminDetail";
    }

    @GetMapping("/goMemberDtl")
    public String goMemberDtl(String ucode,String memorial, Model model) {
        model.addAttribute("ucode", ucode);
        model.addAttribute("memorialCode", memorial);
        return "admin/memberDtl";
    }


    @PostMapping("/getMemberDtl")
    @ResponseBody
    public MemberDto getMemberDtl(String ucode) throws Exception {
        MemberDto dto = memberService.getMemberDtl(ucode);
        return dto;
    }


    @PostMapping("/getMemberComments")
    @ResponseBody
    public List<CommentDto> getMemberComments(String memorialCode) throws Exception {
        List<CommentDto> list = memberService.getCommentList(memorialCode);
        return list;
    }


    @PostMapping("/editMemberDtl")
    @ResponseBody
    public String editMemberDtl(String ucode, String uname, String mobileNo, String funeralDate, String memo) throws Exception {
        String result = memberService.editMemberDtl(ucode, uname, mobileNo, funeralDate, memo);
        return result;
    }

    @PostMapping("/stopMember")
    @ResponseBody
    public String stopMember(String ucode) throws Exception {
        String result = memberService.stopMember(ucode);
        return result;
    }

    @PostMapping("/restoreMember")
    @ResponseBody
    public String restoreMember(String ucode) throws Exception {
        String result = memberService.restoreMember(ucode);
        return result;
    }

    @PostMapping("/deleteMember")
    @ResponseBody
    public String deleteMember(String ucode, String memorialCode, String ptLinkCode) throws Exception {
        String result = memberService.deleteMember(ucode, memorialCode, ptLinkCode);
        return result;
    }



    @PostMapping("/editComment")
    @ResponseBody
    public String editComment(Long id, String comment, String memorialCode) throws Exception {
        String result = memberService.editComment(id, comment, memorialCode);
        return result;
    }

    @PostMapping("/deleteComment")
    @ResponseBody
    public String deleteComment(Long id, String memorialCode) throws Exception {
        String result = memberService.deleteComment(id, memorialCode);
        return result;
    }



    @PostMapping("/createMember")
    @ResponseBody
    public MemberDto createMember(String name, String mobileNo, HttpSession session) throws Exception {
        String ptLinkCode = session.getAttribute("ptLinkCode").toString();
        String homepage = session.getAttribute("homepage").toString();
        MemberDto dto = memberService.createMember(name, mobileNo, ptLinkCode, homepage);
        return dto;
    }


    @GetMapping("/downloadMemberListByPartnerExcel")
    @ResponseBody
    public void downloadMemberListByPartnerExcel(MemberDto dto, HttpSession session, HttpServletResponse response) throws Exception {
        String ptLinkCode = session.getAttribute("ptLinkCode").toString();
        dto.setPtLinkCode(ptLinkCode);
        List<MemberDto> list = memberService.downloadMemberListByPartnerExcel(dto);


        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("member");

        int cellCnt = 0;
        int rowCnt = 0;
        Row row = null;
        Cell cell = null;

        final String fileName = "memberList.xlsx";

        final String[] colNames = {
                "신청일", "고객이름", "반려동물이름", "연락처", "진행상태"
        };

        final int[] colWidhs = {
                5000, 4000, 5000, 5000, 3000
        };
        row = sheet.createRow(rowCnt++);
        for (int i = 0; i < colNames.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(colNames[i]);
            sheet.setColumnWidth(i, colWidhs[i]);
        }


        for (MemberDto memberDto : list) {
            cellCnt = 0;
            row = sheet.createRow(rowCnt++);

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getRegDateStr());

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getUname());

            cell = row.createCell(cellCnt++);
            String petName = "";
            if (memberDto.getPetName() != null) {
                petName = memberDto.getPetName();
            } else {
                petName = "미업로드";
            }
            cell.setCellValue(petName);

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getMobileNo());

            cell = row.createCell(cellCnt++);
            String status = "";
            if (memberDto.getStatus() == null) {
                status = "미업로드";
            } else if (!memberDto.getStatus().equals("10")) {
                status = "미업로드";
            } else if (memberDto.getStatus().equals("10")) {
                status = "업로드";
            }
            cell.setCellValue(status);
        }

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try {
            wb.write(response.getOutputStream());
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @GetMapping("/downloadMemberListExcel")
    @ResponseBody
    public void downloadMemberListExcel(MemberDto dto, HttpSession session, HttpServletResponse response) throws Exception {

        List<MemberDto> list = memberService.downloadMemberListExcel(dto);


        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("member");

        int cellCnt = 0;
        int rowCnt = 0;
        Row row = null;
        Cell cell = null;

        final String fileName = "memberListByAdmin.xlsx";

        final String[] colNames = {
                "MEMORIALCODE", "이름", "업체명", "등록일자", "장례날짜", "연락처", "모바일 추모관 URL"
        };

        final int[] colWidhs = {
                7000, 3000, 4000, 6000, 4000, 5000, 15000
        };

        row = sheet.createRow(rowCnt++);
        for (int i = 0; i < colNames.length; i++) {
            cell = row.createCell(i);
            cell.setCellValue(colNames[i]);
            sheet.setColumnWidth(i, colWidhs[i]);
        }


        for (MemberDto memberDto : list) {
            cellCnt = 0;
            row = sheet.createRow(rowCnt++);

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getMemorialCode());

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getUname());

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getPtName());

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getRegDateStr());

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getFuneralDateStr());

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getMobileNo());

            cell = row.createCell(cellCnt++);
            cell.setCellValue(memberDto.getFuneralUrl());
        }

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        try {
            wb.write(response.getOutputStream());
            wb.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }




}
