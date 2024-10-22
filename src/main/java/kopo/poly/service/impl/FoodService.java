package kopo.poly.service.impl;

import kopo.poly.dto.FoodDTO;
import kopo.poly.service.IFoodService;
import kopo.poly.util.CmmUtil;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
public class FoodService implements IFoodService {

    @Override
    public List<FoodDTO> toDayFood() throws Exception{
        //로그 찍기
        log.info("{}.toDayFood Start!", this.getClass().getName());

        //크롤링 결과(0보다 크면 성공)

        //서울강서캠퍼스 식단 정보 가져올 사이트 주소
        String url = "http://www.kopo.ac.kr/kangseo/content.do?menu=262";

        //JSOUP 라이브러리를 통해 사이트 접속되면, 그 사이트의 전체 html소스를 저장할 변수
        Document doc; //

        //사이트 접속(http 프로토콜만 가능, https 프로토콜은 보안상 불가능)
        doc = Jsoup.connect(url).get();

        Elements element = doc.select("table.tbl_table tbody");

        //Iterator를 사용하여 식단 정보를 가져오기
        Iterator<Element> foodIt = element.select("tr").iterator(); //요일별 학식 메뉴

        FoodDTO pDTO;

        List<FoodDTO> pList = new ArrayList<>();
        int idx = 0; //반복 횟수를 월요일~금요일 범위로 설정(5일)

        //수집된 데이터 DB에 저장하기
        while (foodIt.hasNext()) {

            //반복 횟수 카운트하기. 5번째가 금요일이므로 6번째인 토요일은 실행 안 되도록,
            //반복문 5회만 실행하기(월~금)
            if (idx++ > 4) {
                break;
            }

            pDTO = new FoodDTO();

            String food = CmmUtil.nvl(foodIt.next().text()).trim();

            log.info("food: {}", food);
            //앞 3글자가 요일이므로 요일 저장
            pDTO.setDay(food.substring(0, 3));

            //식단 정보
            pDTO.setFood_nm(food.substring(4));

            pList.add(pDTO);
        }
        //로그 찍기
        log.info("{}.toDayFood End!", this.getClass().getName());

        return pList;

        }
    @Scheduled(cron = "* * 3 * * *")
    @Override
    public void toDayFoodBatch() throws Exception {

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info("{}.toDayFoodBatch Start!", this.getClass().getName());

        List<FoodDTO> rList = this.toDayFood();

        rList.forEach(dto -> {
            log.info("{} FOOD : ", dto);
        });

        // 로그 찍기(추후 찍은 로그를 통해 이 함수에 접근했는지 파악하기 용이하다.)
        log.info("{}.toDayFoodBatch End!", this.getClass().getName());

    }
}


