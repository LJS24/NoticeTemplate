package kopo.poly.controller;


import kopo.poly.dto.OcrDTO;
import kopo.poly.service.IOcrService;
import kopo.poly.service.impl.OcrService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequestMapping(value = "/ocr")
@RequiredArgsConstructor
@Controller
public class OcrController {

    private final IOcrService ocrService;

    //업로드되는 파일이 저장되는 기본 폴더 설정(자바에서 경로는 /로 표현됨)
    final private String FILE_UPLOAD_SAVE_PATH = "c:/upload"; // 이 경로에 저장

    /**
     * 이미지 인식을 위한 파일 업로드 화면 호출
     */
    @GetMapping(value = "uploadImage")
    public String uploadImage() {
        log.info("{}.uploadImage!", this.getClass().getName());

        return "ocr/uploadImage";

    }

    @PostMapping(value = "readImage")
    public String readImage(ModelMap model, @RequestParam(value = "fileUpload") MultipartFile mf) throws Exception {

        log.info("{}.readImage start!", this.getClass().getName());

        //OCR 실행 결과
        String res;

        //업로드하는 실제 파일명
        //다운로드 기능 구현 시, 임의로 작성한 파일명을 원래대로 만들어주기 위한 목적
        String originalFileName = mf.getOriginalFilename();

        //파일 확장자 가져오기(파일 확장자를 포함한 전체 이름(myimage.jpg)에서 뒤쪽부터 .이 존재하는 위치 찾기)
        String ext = Objects.requireNonNull(originalFileName).substring(originalFileName.lastIndexOf(".") + 1,
                originalFileName.length()).toLowerCase();

        //이미지 파일만 실행되도록 함
        if (ext.equals("jpeg") || ext.equals("jpg") || ext.equals("gif") || ext.equals("png")) {

            //웹 서버에 저장되는 파일 이름
            //업로드하는 파일 이름에 한글, 특수 문자들이 저장될 수 있기 때문에 강제로 영어와 숫자로 구성된 파일명으로 변경해서 저장한다.
            //리눅스나 유닉스 등 운영체제는 다국어 지원에 취약하기 때문.
            String saveFileName = DateUtil.getDateTime("HHmmss") + "." + ext;

            //웹 서버에 업로드한 파일 저장하는 물리적 경로
            String saveFilePath = FileUtil.mkdirForDate(FILE_UPLOAD_SAVE_PATH);

            String fullFileInfo = saveFilePath + "/" + saveFileName;

            //정상적으로 값이 생성되었는지 로그 찍어서 확인
            log.info("ext: " + ext);
            log.info("saveFileName: " + saveFileName);
            log.info("saveFilePath: " + saveFilePath);
            log.info("fullFileInfo: " + fullFileInfo);

            //업로드되는 파일을 서버에 저장
            mf.transferTo(new File(fullFileInfo));

            OcrDTO pDTO = new OcrDTO();

            pDTO.setFileName(saveFileName); //저장되는 파일명
            pDTO.setFilePath(saveFilePath); //저장되는 경로
            pDTO.setExt(ext);  //확장자
            pDTO.setOrgFileName(originalFileName);  //원래 이름
            pDTO.setRegId("admin");

            //ocrService.getReadforImageText(pDTO) 결과를 Null 값 체크하여 rDTO 객체에 저장하기
            OcrDTO rDTO = Optional.ofNullable(ocrService.getReadforImageText(pDTO)).orElseGet(OcrDTO::new);

            res = CmmUtil.nvl(rDTO.getTextFromImage()); //인식 결과

            rDTO.setFileName(saveFileName);
            rDTO.setFilePath(saveFilePath);
            rDTO.setExt(ext);
            rDTO.setOrgFileName(originalFileName);
            rDTO.setRegId(pDTO.getRegId());


            // DB에 저장하는 프로세스 시작
            log.info("{}.insertOcr start!", this.getClass().getName());
            ocrService.insertOcr(rDTO);
            log.info("{}.insertOcr End!", this.getClass().getName());


            rDTO = null;
            pDTO = null;

        } else {
            res = "이미지 파일이 아니므로 인식이 불가능합니다.";
        }

        //이미지로부터 인식된 문자를 jsp에 전달하기
        model.addAttribute("res", res);

        log.info("{}.readImage End!", this.getClass().getName());


        return "ocr/readImage";
    }




}

