package core.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.jcraft.jsch.*;

import java.util.*;

import core.dto.FileDto;
import core.dto.ImageRequest;
import core.dto.ImageResponse;
import core.dto.MemberDto;
import core.entity.MemberPhoto;
import core.repository.MemberPhotoRepository;
import core.repository.MemberRepositoryCustom;
import core.utill.CommonUtill;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class FileService {

    @Autowired
    MemberPhotoRepository memberPhotoRepository;
    @Autowired
    MemberRepositoryCustom memberRepositoryCustom;

    @Value("${ftp.server.url}")
    private String ftpServer;

    @Value("${ftp.port}")
    private int ftpPort;

    @Value("${ftp.user}")
    private String ftpUser;

    @Value("${ftp.pwd}")
    private String ftpPwd;

    @Value("${ftp.folder}")
    private String ftpFolder;

    @Value("${ftp.return.url}")
    private String ftpReturnUrl;


    @Value("${back.ftp.server.url}")
    private String backupFtpServer;


    @Autowired
    private CommonUtill commonUtill;


    @Transactional
    public String uploadForm(FileDto dto) throws IOException {

        MemberDto memberDto = memberRepositoryCustom.getMemberSttus(dto.getPtLinkCode(), dto.getMemorialCode());

        String sttus = memberDto.getStatus();

        List<MemberPhoto> list = memberPhotoRepository.findByMemorialCode(dto.getMemorialCode());


        if (memberDto.getMemberSttus().equals("90")) {
            return "98";
        }

        if (!sttus.equals("90")) {
            return "90";
        }

        // 사진 갯수 적용 2중 체크
        if (list.size() < 6) {
            return "88";
        }


        LocalDateTime now = LocalDateTime.now();
        dto.setUpdDate(now);
        String funeral = dto.getFuneralDateStr() + "T00:00:00";
        LocalDateTime funeralDate = LocalDateTime.parse(funeral);
        dto.setFuneralDate(funeralDate);
        // 회원 상태값 변경
        memberRepositoryCustom.uploadForm(dto);

        return "00";
    }



    public void saveOriginalImg(MultipartFile img, String ptLinkCode, String memorialCode, String fileId, LocalDateTime now) throws IOException {
        Integer year = now.getYear();
        Integer month = now.getMonthValue();

        //저장 가능한 파일로 변환
        byte[] saveImg = img.getBytes();
        InputStream inputStream = new ByteArrayInputStream(saveImg);

        String imgName = "original-" + fileId + ".jpg";

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        //TODO: sftp용
        String directoryPath = ptLinkCode + "/" + memorialCode + "/" + year + "/" + month + "/";

        try {
            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            String[] folders = directoryPath.split("/");
            String fullPath = "";
            for (String folder : folders) {
                if (folder.length() > 0) {
                    fullPath += "/" + folder;
                    try {
                        channelSftp.ls(ftpFolder + fullPath);
                    } catch (SftpException e) {
                        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            channelSftp.mkdir(ftpFolder + fullPath);
                        } else {
                            throw e;
                        }
                    }
                }
            }

            channelSftp.put(inputStream, ftpFolder + directoryPath + imgName);
            log.info("save original image success" + memorialCode + "-" + imgName);

        } catch (Exception e) {
            log.info("save original image false" + memorialCode+ "-" + imgName);
            e.printStackTrace();
        }
    }



    @Transactional
    public String saveImg(FileDto dto) throws IOException {
        log.info("넘어오나요?");
        try {
            MultipartFile img = dto.getFile();
            if (img != null && !img.isEmpty()) {

                String originalFilename = img.getOriginalFilename();
                if (originalFilename != null) {

                    String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
                    // 허용된 확장자 목록
                    List<String> allowedExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
                    log.info("이미지 형식 " + extension);
                    if (!allowedExtensions.contains(extension)) {
                        return "89";
                    }
                } else {
                    log.info("이미지 이름 없음");
                    return "99";
                }
            } else {
                log.info("이미지 없음");
                return "99";
            }


            String ptLinkCode = dto.getPtLinkCode();
            String memorialCode = dto.getMemorialCode();

            MemberDto memberDto = memberRepositoryCustom.getMemberSttus(ptLinkCode, memorialCode);
            if (!memberDto.getStatus().equals("90")) {
                return "99";
            }

            String fileId = dto.getFileId();
            String type = dto.getType();
            LocalDateTime now = LocalDateTime.now();

            Integer width = 1920;
            Integer height = 1080;

            // 이미지 파일


            BufferedImage bufferedImage = ImageIO.read(img.getInputStream());

            // 메타 데이터 확인
            int metaData = getRotationAngle(img);
            // 회전이 있음 복구
            bufferedImage = rotateImage(bufferedImage, metaData);

            //기본 이미지
            BufferedImage defaultImg = defaultImg(bufferedImage, width, height);

            BufferedImage finalImg = addBlackBackground(defaultImg, width, height);

            String saveFileUrl = submitImg(finalImg, ptLinkCode, memorialCode, fileId, type, now);

            if (saveFileUrl.equals("")) {
                return "90";
            }

            // 저장된 이미지 정보 디비에 저장
            Optional<MemberPhoto> memberPhoto = memberPhotoRepository.findByFileIdAndTypeAndMemorialCode(fileId, type, memorialCode);

            if (memberPhoto.isPresent()) {
                MemberPhoto orgMemberPhoto = memberPhoto.get();
                String imgUrl = orgMemberPhoto.getFileDir();
                deleteSftpImage(imgUrl);

                orgMemberPhoto.setFileDir(saveFileUrl);
                orgMemberPhoto.setUpdDate(now);
                orgMemberPhoto.setBackUpSttus("90");
                memberPhotoRepository.save(orgMemberPhoto);
            } else {
                MemberPhoto newMemberPhoto = new MemberPhoto();
                newMemberPhoto.setMemorialCode(memorialCode);
                newMemberPhoto.setFileDir(saveFileUrl);
                newMemberPhoto.setRegDate(now);
                newMemberPhoto.setFileId(fileId);
                newMemberPhoto.setType(type);
                newMemberPhoto.setBackUpSttus("90");
                memberPhotoRepository.save(newMemberPhoto);
            }
            return saveFileUrl;
        } catch (Exception e) {
            log.info("saveImg error ======================> " + e.getMessage());
            return null;
        }
    }

    //TODO: 사진 회전
    public String rotateImg(FileDto dto) throws IOException {

        String ptLinkCode = dto.getPtLinkCode();
        String memorialCode = dto.getMemorialCode();
        String fileId = dto.getFileId();
        String type = dto.getType();
        LocalDateTime now = LocalDateTime.now();

        Integer width = 1920;
        Integer height = 1080;

        // 이미지 파일
        MultipartFile img = dto.getFile();
        BufferedImage bufferedImage = ImageIO.read(img.getInputStream());
        // 메타 데이터 확인
        int metaData = getRotationAngle(img);
        // 회전이 있음 복구
        bufferedImage = rotateImage(bufferedImage, metaData);

        // 이미지 회전 로직
        if (dto.getRotation() > 0) {
            bufferedImage = rotateImage(bufferedImage, dto.getRotation());
        }
        //기본 이미지
        BufferedImage defaultImg = defaultImg(bufferedImage, width, height);

        BufferedImage finalImg = addBlackBackground(defaultImg, width, height);

        String fileUrl = submitImg(finalImg, ptLinkCode, memorialCode, fileId, type, now);

        // 저장된 이미지 정보 디비에 저장
        Optional<MemberPhoto> memberPhoto = memberPhotoRepository.findByFileIdAndTypeAndMemorialCode(fileId, type, memorialCode);

        if (memberPhoto.isPresent()) {
            MemberPhoto orgMemberPhoto = memberPhoto.get();
            String imgUrl = orgMemberPhoto.getFileDir();
            deleteSftpImage(imgUrl);

            orgMemberPhoto.setFileDir(fileUrl);
            orgMemberPhoto.setUpdDate(now);
            orgMemberPhoto.setBackUpSttus("90");
            memberPhotoRepository.save(orgMemberPhoto);
        }
        return fileUrl;
    }


    @Transactional
    public String saveM01(String fileUrl, Integer left, Integer top, Integer widthPercentage, Integer heightPercentage,  String ptLinkCode, String memorialCode) throws IOException {
        String fileId = "m01";
        String type = "render";
        LocalDateTime now = LocalDateTime.now();

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        byte[] file = null;

        try {
            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            URI uri = new URI(fileUrl);
            String[] pathSegments = uri.getPath().split("/");
            String ftpName = pathSegments[1];
            String filePath = uri.getPath().substring(ftpName.length() + 2);
            file = channelSftp.get(ftpFolder + filePath).readAllBytes();

            ByteArrayInputStream bis = new ByteArrayInputStream(file);

            BufferedImage img = ImageIO.read(bis);

            BufferedImage croppedImage = img.getSubimage(left, top, widthPercentage, heightPercentage);

            croppedImage = resizeImage(croppedImage, 750, 890);

            String returnFileUrl = submitImg(croppedImage, ptLinkCode, memorialCode, fileId, type, now);

            Optional<MemberPhoto> memberPhoto = memberPhotoRepository.findByFileIdAndTypeAndMemorialCode(fileId, type, memorialCode);

            if (memberPhoto.isPresent()) {
                MemberPhoto orgMemberPhoto = memberPhoto.get();
                String imgUrl = orgMemberPhoto.getFileDir();
                deleteSftpImage(imgUrl);

                orgMemberPhoto.setFileDir(returnFileUrl);
                orgMemberPhoto.setUpdDate(now);
                orgMemberPhoto.setBackUpSttus("90");
                memberPhotoRepository.save(orgMemberPhoto);
            } else {
                MemberPhoto newMemberPhoto = new MemberPhoto();
                newMemberPhoto.setMemorialCode(memorialCode);
                newMemberPhoto.setFileDir(returnFileUrl);
                newMemberPhoto.setRegDate(now);
                newMemberPhoto.setFileId(fileId);
                newMemberPhoto.setType(type);
                newMemberPhoto.setBackUpSttus("90");
                memberPhotoRepository.save(newMemberPhoto);
            }
            return returnFileUrl;

        } catch (Exception e) {
            return "90";
        }
    }



    @Transactional
    public String saveM01Version1(MultipartFile file, Integer left, Integer top, Integer width, Integer height, String ptLinkCode, String memorialCode, Integer rotationM01) throws IOException {

        MemberDto memberDto = memberRepositoryCustom.getMemberSttus(ptLinkCode, memorialCode);
        if (!memberDto.getStatus().equals("90")) {
            return "99";
        }

        String fileId = "m01";
        String type = "render";
        LocalDateTime now = LocalDateTime.now();

        //메타 데이터 제거
        int metaData = getRotationAngle(file);
        BufferedImage img = ImageIO.read(file.getInputStream());
        img = rotateImage(img, metaData);

        if (rotationM01 > 0) {
            img = rotateImage(img, rotationM01);
        }

        img = resizeImage(img, img.getWidth(), img.getHeight());

        String fileUrl = submitImg(img, ptLinkCode, memorialCode, fileId, type, now);

        Optional<MemberPhoto> memberPhoto = memberPhotoRepository.findByFileIdAndTypeAndMemorialCode(fileId, type, memorialCode);

        if (memberPhoto.isPresent()) {
            MemberPhoto orgMemberPhoto = memberPhoto.get();
            String imgUrl = orgMemberPhoto.getFileDir();
            deleteSftpImage(imgUrl);

            orgMemberPhoto.setFileDir(fileUrl);
            orgMemberPhoto.setUpdDate(now);
            orgMemberPhoto.setBackUpSttus("90");
            memberPhotoRepository.save(orgMemberPhoto);
        } else {
            MemberPhoto newMemberPhoto = new MemberPhoto();
            newMemberPhoto.setMemorialCode(memorialCode);
            newMemberPhoto.setFileDir(fileUrl);
            newMemberPhoto.setRegDate(now);
            newMemberPhoto.setFileId(fileId);
            newMemberPhoto.setType(type);
            newMemberPhoto.setBackUpSttus("90");
            memberPhotoRepository.save(newMemberPhoto);
        }

        return fileUrl;
    }


    public String submitImg(BufferedImage bufferedImage, String ptLinkCode, String memorialCode, String fileId, String type, LocalDateTime now) throws IOException {
        //저장 가능한 파일로 변환
        byte[] saveImg = commonUtill.bufferedImageToByteArray(bufferedImage, "jpg");
        Integer year = now.getYear();
        Integer month = now.getMonthValue();

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        String fileUrl = "";

        try {
            String imgName = "";

            imgName = UUID.randomUUID() + "-" + fileId + ".jpg";

            //TODO: 디렉토리 만들기 위해 우선 분리
            String directoryPath = ptLinkCode + "/" + memorialCode + "/" + year + "/" + month + "/";

            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            //TODO: 디렉토리 경로가 존재하는지 확인하고 없으면 생성
            String[] folders = directoryPath.split("/");
            String fullPath = "";
            for (String folder : folders) {
                if (folder.length() > 0) {
                    fullPath += "/" + folder;
                    try {
                        channelSftp.ls(ftpFolder + fullPath);
                    } catch (SftpException e) {
                        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            channelSftp.mkdir(ftpFolder + fullPath);
                        } else {
                            throw e;
                        }
                    }
                }
            }
            InputStream inputStream = new ByteArrayInputStream(saveImg);
            channelSftp.put(inputStream, ftpFolder + directoryPath + imgName);
            fileUrl = ftpReturnUrl + directoryPath + imgName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;

    }


    @Transactional
    public void backUpImg() {
       List<FileDto> list = memberRepositoryCustom.findBackUpImg();

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;
        byte[] file = null;

        try {
            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            for (FileDto fileDto : list) {

                String fileDir = fileDto.getFileDir();
                String fileId = fileDto.getFileId();
                String imgName = fileId + ".jpg";

                URI uri = new URI(fileDir);
                String[] pathSegments = uri.getPath().split("/");
                String ftpName = pathSegments[1];
                String filePath = uri.getPath().substring(ftpName.length() + 2);
                file = channelSftp.get(ftpFolder + filePath).readAllBytes();

                String result = submitBackUpImg2(fileDto, file);
                if (result.equals("00")) {
                    memberRepositoryCustom.updateBackUpSttus(fileDto.getMemorialCode(), fileDto.getFileId());
                }
            }

        } catch (Exception e) {
            log.info("imageBackUp failed : orginal file server connection = " + e);
        }
    }


    public String submitBackUpImg2(FileDto fileDto, byte[] file) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {

            String fileDir = fileDto.getFileDir();
            String fileId = fileDto.getFileId();
            String imgName = fileId + ".jpg";

            URI uri = new URI(fileDir);
            String[] pathSegments = uri.getPath().split("/");
            String ftpName = pathSegments[1];
            String filePath = uri.getPath().substring(ftpName.length() + 2);


            session = jsch.getSession(ftpUser, backupFtpServer, ftpPort);
            session.setPassword(ftpPwd);
            Properties config2 = new Properties();
            config2.put("StrictHostKeyChecking", "no");
            session.setConfig(config2);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            String[] folders = filePath.split("/");
            String fullPath = "";
            for (String folder : folders) {
                if (folder.length() > 0) {
                    fullPath += "/" + folder;
                    try {
                        channelSftp.ls(ftpFolder + fullPath);
                    } catch (SftpException e) {
                        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            channelSftp.mkdir(ftpFolder + fullPath);
                        } else {
                            throw e;
                        }
                    }
                }
            }
            InputStream inputStream = new ByteArrayInputStream(file);
            channelSftp.put(inputStream, ftpFolder + filePath + imgName);
            return "00";
        } catch (Exception e) {
            log.info("imgBackUp failed =============> " + e + "/.../" + fileDto.getFileDir());
            return "99";
        }
    }


    public String submitPartnerLogoImg(MultipartFile img, String ptLinkCode) throws IOException {

        BufferedImage bufferedImage = ImageIO.read(img.getInputStream());
        int metaData = getRotationAngle(img);
        bufferedImage = rotateImage(bufferedImage, metaData);
        String originalFormat = img.getContentType().split("/")[1];
        //저장 가능한 파일로 변환
        byte[] saveImg = commonUtill.bufferedImageToByteArray(bufferedImage, originalFormat);

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        String fileUrl = "";

        try {
            String imgName = "";

            imgName = ptLinkCode  + "-logo.jpg";

            //TODO: 디렉토리 만들기 위해 우선 분리
            String directoryPath = ptLinkCode + "/" + "partnerFile/";

            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            //TODO: 디렉토리 경로가 존재하는지 확인하고 없으면 생성
            String[] folders = directoryPath.split("/");
            String fullPath = "";
            for (String folder : folders) {
                if (folder.length() > 0) {
                    fullPath += "/" + folder;
                    try {
                        channelSftp.ls(ftpFolder + fullPath);
                    } catch (SftpException e) {
                        if (e.id == ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                            channelSftp.mkdir(ftpFolder + fullPath);
                        } else {
                            throw e;
                        }
                    }
                }
            }
            InputStream inputStream = new ByteArrayInputStream(saveImg);
            channelSftp.put(inputStream, ftpFolder + directoryPath + imgName);
            fileUrl = ftpReturnUrl + directoryPath + imgName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileUrl;
    }


    @PostMapping("/downloadHtmlImg")
    public ResponseEntity<?> uploadImage(@RequestBody ImageRequest request) {
        String dataUrl = request.getImage();
        String base64Image = dataUrl.split(",")[1];

        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        String filePath = "captured-image.png"; // 저장할 파일 경로

        try (FileOutputStream fos = new FileOutputStream(new File(filePath))) {
            fos.write(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error saving image");
        }

        // 다운로드 URL을 반환합니다.
        String downloadUrl = "http://localhost:8080/" + filePath;
        return ResponseEntity.ok().body(new ImageResponse(downloadUrl));
    }


    //TODO: 기존 이미지 삭제
    public void deleteSftpImage(String imgUrl) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            URI uri = new URI(imgUrl);
            String[] pathSegments = uri.getPath().split("/");
            String ftpName = pathSegments[1];
            String filePath = uri.getPath().substring(ftpName.length() + 2);

            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            channelSftp.rm(ftpFolder + filePath);
        } catch (Exception e){
            log.info("deleteSftpImage failed: " + e.getMessage() + "///" + imgUrl);
            e.printStackTrace();
        }
    }


    public void deleteMemberDirectory(String ptLinkCode, String memorialCode) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;
        try {
            String directoryPath = ptLinkCode + "/" + memorialCode;

            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            deleteDirectory(channelSftp, ftpFolder + directoryPath);

        } catch (Exception e) {
            log.info("deleteSftpImage failed: " + e.getMessage() + "///" + ptLinkCode + "/" + memorialCode);
            e.printStackTrace();
        } finally {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private void deleteDirectory(ChannelSftp channelSftp, String path) throws SftpException {
        @SuppressWarnings("unchecked")
        Vector<ChannelSftp.LsEntry> files = channelSftp.ls(path);

        for (ChannelSftp.LsEntry entry : files) {
            String fileName = entry.getFilename();
            if (!fileName.equals(".") && !fileName.equals("..")) {
                String filePath = path + "/" + fileName;
                if (entry.getAttrs().isDir()) {
                    deleteDirectory(channelSftp, filePath);
                } else {
                    channelSftp.rm(filePath);
                }
            }
        }
        channelSftp.rmdir(path);
    }



    //TODO:------- 이미지 효과 로직 --------------------
    // 메타 데이터 확인
    public static int getRotationAngle(MultipartFile file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file.getInputStream());

            ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                switch (orientation) {
                    case 1:
                        return 0;
                    case 3:
                        return 180;
                    case 6:
                        return 90;
                    case 8:
                        return 270;
                    default:
                        return 0;
                }
            }
        } catch (IOException | ImageProcessingException e) {
            e.printStackTrace();
        } catch (MetadataException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }



    // 리사이징
    public BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    // 이미지 다시 회전
     public BufferedImage rotateImage(BufferedImage originalImage, int rotationAngle) {
        if (rotationAngle != 0) {
            // 이미지 회전
            double radians = Math.toRadians(rotationAngle);
            double sin = Math.abs(Math.sin(radians));
            double cos = Math.abs(Math.cos(radians));
            int newWidth = (int) Math.floor(originalImage.getWidth() * cos + originalImage.getHeight() * sin);
            int newHeight = (int) Math.floor(originalImage.getHeight() * cos + originalImage.getWidth() * sin);

            BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
            Graphics2D g = rotatedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.translate((newWidth - originalImage.getWidth()) / 2, (newHeight - originalImage.getHeight()) / 2);
            g.rotate(radians, originalImage.getWidth() / 2, originalImage.getHeight() / 2);
            g.drawRenderedImage(originalImage, null);
            g.dispose();
            return rotatedImage;
        }
        return originalImage;
    }


    public BufferedImage defaultImg(BufferedImage originalImage, Integer width, Integer height) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double ratio = Math.min((double) width / originalWidth, (double) height / originalHeight);
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage bufferedResizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        bufferedResizedImage.getGraphics().drawImage(resizedImage, 0, 0, null);
        return bufferedResizedImage;
    }


    // TODO : 이미지 배경 검정 처리
    public BufferedImage addBlackBackground(BufferedImage resizedImage, Integer width, Integer height) {
        BufferedImage blackBackgroundImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = blackBackgroundImage.createGraphics();
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);

        int x = (width - resizedImage.getWidth()) / 2;
        int y = (height - resizedImage.getHeight()) / 2;
        g2d.drawImage(resizedImage, x, y, null);
        g2d.dispose();

        return blackBackgroundImage;
    }


    public byte[] getDownloadFile(String fileUrl) {

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        byte[] file = null;
        try {
            session = jsch.getSession(ftpUser, ftpServer, ftpPort);
            session.setPassword(ftpPwd);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            URI uri = new URI(fileUrl);
            String[] pathSegments = uri.getPath().split("/");

            String ftpName = pathSegments[1];
            String filePath = uri.getPath().substring(ftpName.length() + 2);

            file = channelSftp.get(ftpFolder + filePath).readAllBytes();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }


    public List<FileDto> getDownloadImg(String memorialCode) {
       return memberRepositoryCustom.getDownloadImg(memorialCode);
    }



    //이미지 합성
//    public static BufferedImage composeImages(BufferedImage backgroundImage, BufferedImage foregroundImage) {
//
//        int backgroundWidth = backgroundImage.getWidth();
//        int backgroundHeight = backgroundImage.getHeight();
//        int foregroundWidth = foregroundImage.getWidth();
//        int foregroundHeight = foregroundImage.getHeight();
//
//        int x = (backgroundWidth - foregroundWidth) / 2;
//        int y = (backgroundHeight - foregroundHeight) / 2;
//
//        BufferedImage composedImage = new BufferedImage(backgroundWidth, backgroundHeight, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = composedImage.createGraphics();
//
//        g.drawImage(backgroundImage, 0, 0, null);
//
//        g.drawImage(foregroundImage, x, y, null);
//        g.dispose();
//
//        return composedImage;
//    }

//    public BufferedImage transFormImg(MultipartFile file, Integer rotaionList) throws IOException {
//        Integer width = 1920;
//        Integer height = 1080;
//        int metaData = getRotationAngle(file);
//
//        BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
//
//        bufferedImage = rotateImage(bufferedImage, metaData);
//        if (rotaionList > 0) {
//            bufferedImage = rotateImage(bufferedImage, rotaionList);
//        }
//        BufferedImage finalImage;
//        BufferedImage defaultImg = defaultImg(bufferedImage, width, height);
//        BufferedImage backGround = imgEffect(bufferedImage, width, height);
//        finalImage = composeImages(backGround, defaultImg);
//
//        return finalImage;
//    }

//    public BufferedImage imgEffect(BufferedImage originalImage, Integer width, Integer height) {
//
//        originalImage = resizeImage(originalImage, width, height);
//
//        originalImage = convertToBlackAndWhite(originalImage);
//
//        originalImage = applyBlur(originalImage);
//
//        return originalImage;
//    }
//
//
//    // 흑백 처리
//    public BufferedImage convertToBlackAndWhite(BufferedImage originalImage) {
//
//        ColorConvertOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
//        return op.filter(originalImage, null);
//    }
//    // 블러 처리
//    public BufferedImage applyBlur(BufferedImage image) {
//        float[] blurMatrix = {
//                0.01f, 0.01f, 0.01f,
//                0.01f, 2.2f, 0.01f,
//                0.01f, 0.01f, 0.01f
//        };
//        Kernel blurKernel = new Kernel(3, 3, blurMatrix);
//        ConvolveOp blur = new ConvolveOp(blurKernel, ConvolveOp.EDGE_NO_OP, null);
//        return blur.filter(image, null);
//    }

}
