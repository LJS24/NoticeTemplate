package kopo.poly.service;

import kopo.poly.dto.MailDTO;


import java.util.List;


public interface IMailService {

    //메일 발송
    int doSendMail(MailDTO pDTO) throws Exception;

    //메일 리스트
    List<MailDTO> getMailList() throws Exception;

    //발송 메일 등록
    void insertMailInfo(MailDTO pDTO) throws Exception;


}
