package kopo.poly.mapper;

import kopo.poly.dto.MailDTO;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IMailMapper {

    //메일 리스트
    List<MailDTO> getMailList() throws Exception;

    //발송 메일 등록
    void insertMailInfo(MailDTO pDTO) throws Exception;


}
