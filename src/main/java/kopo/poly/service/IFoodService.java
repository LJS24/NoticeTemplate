package kopo.poly.service;

import kopo.poly.dto.FoodDTO;

import java.util.List;

public interface IFoodService {

    //한국폴리텍대학 서울강서캠퍼스 식단 정보 가져오기
    List <FoodDTO> toDayFood() throws Exception;

    // 스프링 스케줄러 실행을 함수 추가함
    void toDayFoodBatch() throws Exception;
}
