package kopo.poly.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import kopo.poly.dto.MailDTO;
import kopo.poly.dto.MsgDTO;
import kopo.poly.dto.NoticeDTO;
import kopo.poly.service.IMailService;
import kopo.poly.util.CmmUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@RequestMapping(value = "/mail")
@RequiredArgsConstructor
@Controller
public class MailController {

    private final IMailService mailService; // 메일 발송을 위한 서비스 객체를 사용하기

    /**
     * 메일 발송하기폼
     */
    @GetMapping(value = "mailForm")
    public String mailForm() throws Exception {

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info(this.getClass().getName() + "mailForm Start!");

        return "mail/mailForm";
    }

    /**
     * 메일 발송하기
     */
    @ResponseBody
    @PostMapping(value = "sendMail")
    public MsgDTO sendAndSaveMail(HttpServletRequest request, ModelMap model, HttpSession session) throws Exception {

        log.info(this.getClass().getName() + ".sendAndSaveMail Start!");

        String msg = ""; // 발송 결과 메시지

        // 웹 URL로부터 전달받는 값들
        String title = CmmUtil.nvl(request.getParameter("title")); // 제목
        String contents = CmmUtil.nvl(request.getParameter("contents")); // 내용
        String toMail = CmmUtil.nvl(request.getParameter("toMail")); // 수신인
        String fromMail = CmmUtil.nvl("ewepyon@naver.com"); // 발신인

        // 현재 시각을 sendDt에 저장
        String sendDt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()); // 발송 날짜

        // 로그 출력
        log.info("toMail : " + toMail);
        log.info("title : " + title);
        log.info("fromMail : " + fromMail);
        log.info("sendDt : " + sendDt);

        // 메일 발송할 정보 넣기 위한 DTO 객체 생성
        MailDTO pDTO = new MailDTO();

        // 웹에서 받은 값을 DTO에 넣기
        pDTO.setToMail(toMail); // 수신인 저장
        pDTO.setTitle(title); // 제목 저장
        pDTO.setContents(contents); // 내용 저장
        pDTO.setFromMail(fromMail); // 발신인 저장
        pDTO.setSendDt(sendDt); // 현재 시각을 발송 시각으로 저장

        // 메일 발송하기
        int res = mailService.doSendMail(pDTO);

        if (res == 1) { // 메일 발송 성공
            msg = "메일 발송하였습니다.";

            try {
                // 발송 후 메일 정보를 저장
                mailService.insertMailInfo(pDTO);

                msg += " 또한 메일 정보를 성공적으로 저장했습니다.";

            } catch (Exception e) {
                // 메일 저장 실패 시 예외 처리
                msg += " 그러나 메일 정보를 저장하는데 실패했습니다. : " + e.getMessage();
                log.info(e.toString());
            }

        } else { // 메일 발송 실패
            msg = "메일 발송에 실패하였습니다.";
        }

        log.info(msg);

        // 결과 메시지 전달하기
        MsgDTO dto = new MsgDTO();
        dto.setMsg(msg);

        log.info(this.getClass().getName() + ".sendAndSaveMail End!");



        return dto;
    }


    @GetMapping(value = "maillist")
    public String mailList(HttpSession session, ModelMap model)
            throws Exception {

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info("{}.mailList Start!", this.getClass().getName());

        // 로그인된 사용자 아이디는 Session에 저장함
        // 교육용으로 아직 로그인을 구현하지 않았기 때문에 Session에 데이터를 저장하지 않았음
        // 추후 로그인을 구현할 것으로 가정하고, 공지사항 리스트 출력하는 함수에서 로그인 한 것처럼 Session 값을 생성함
        session.setAttribute("SESSION_USER_ID", "USER01");

        // 공지사항 리스트 조회하기
        // Java 8부터 제공되는 Optional 활용하여 NPE(Null Pointer Exception) 처리
        List<MailDTO> rList = Optional.ofNullable(mailService.getMailList())
                .orElseGet(ArrayList::new);

//        List<NoticeDTO> rList = noticeService.getNoticeList();
//
//        if (rList == null) {
//            rList = new ArrayList<>();
//        }

        // 조회된 리스트 결과값 넣어주기
        model.addAttribute("rList", rList);

        log.info("Retrieved mail list size: {}", rList.size());
        for (MailDTO dto : rList) {
            log.info("MailDTO: seq = {}, title = {}, contents = {}, toMail = {} , fromMail = {} ,sendDt = {}",
                    dto.getSeq(), dto.getTitle(), dto.getContents(), dto.getToMail(), dto.getFromMail(), dto.getSendDt());
        }

        // 로그 찍기(추후 찍은 로그를 통해 이 함수 호출이 끝났는지 파악하기 용이하다.)
        log.info("{}.mailList End!", this.getClass().getName());

        // 함수 처리가 끝나고 보여줄 JSP 파일명
        // webapp/WEB-INF/views/notice/noticeList.jsp
        return "mail/mailList";

    }

    @ResponseBody
    @PostMapping(value = "mailInsert")
    public MsgDTO mailInsert(HttpServletRequest request, HttpSession session) {

        log.info("{}.mailInsert Start!", this.getClass().getName());

        String msg = ""; // 메시지 내용
        MsgDTO dto; // 결과 메시지 구조

        try {
            // 로그인된 사용자 아이디를 가져오기
            // 로그인을 아직 구현하지 않았기에 공지사항 리스트에서 로그인 한 것처럼 Session 값을 저장함
            String seq = CmmUtil.nvl(request.getParameter("seq")); //번호
            String title = CmmUtil.nvl(request.getParameter("title")); // 제목
            String contents = CmmUtil.nvl(request.getParameter("contents")); // 내용
            String toMail = CmmUtil.nvl(request.getParameter("toMail")); // 수신인
            String fromMail = CmmUtil.nvl(request.getParameter("fromMail")); // 발신인
            String sendDt = CmmUtil.nvl(request.getParameter("toMail")); // 수신인

            /*
             * ####################################################################################
             * 반드시, 값을 받았으면, 꼭 로그를 찍어서 값이 제대로 들어오는지 파악해야함 반드시 작성할 것
             * ####################################################################################
             */
            log.info("seq : {} / title : {} / contents : {} / toMail : {} / fromMail : {} / sendDt : {} ",
                    seq, title, contents, toMail, fromMail, sendDt);

            // 데이터 저장하기 위해 DTO에 저장하기
            MailDTO pDTO = new MailDTO();

            pDTO.setSeq(seq);
            pDTO.setTitle(title);
            pDTO.setContents(contents);
            pDTO.setToMail(toMail);
            pDTO.setFromMail(fromMail);
            pDTO.setSendDt(sendDt);

            /*
             * 게시글 등록하기위한 비즈니스 로직을 호출
             */
            mailService.insertMailInfo(pDTO);

            // 저장이 완료되면 사용자에게 보여줄 메시지
            msg = "등록되었습니다.";

        } catch (Exception e) {

            // 저장이 실패되면 사용자에게 보여줄 메시지
            msg = "실패하였습니다. : " + e.getMessage();
            log.info(e.toString());

        } finally {
            // 결과 메시지 전달하기
            dto = new MsgDTO();
            dto.setMsg(msg);

            log.info("{}.noticeInsert End!", this.getClass().getName());
        }

        return dto;
    }
}

