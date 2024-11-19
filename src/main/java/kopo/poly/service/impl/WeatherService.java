package kopo.poly.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.poly.dto.WeatherDTO;
import kopo.poly.dto.WeatherDailyDTO;
import kopo.poly.service.IWeatherService;
import kopo.poly.util.CmmUtil;
import kopo.poly.util.DateUtil;
import kopo.poly.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WeatherService implements IWeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    @Override
    public WeatherDTO getWeather(WeatherDTO pDTO) throws Exception {

        log.info(this.getClass().getName() + ".getWeather Start!");

        String lat = CmmUtil.nvl(pDTO.getLat());
        String lon = CmmUtil.nvl(pDTO.getLon());

        String apiParam = "?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric";
        log.info("apiParam " + apiParam);

        String json = NetworkUtil.get(IWeatherService.apiURL + apiParam);
        log.info("json " + json);

        // JSON 데이터를 Map 형태로 변환
        Map<String, Object> rMap = new ObjectMapper().readValue(json, LinkedHashMap.class);

        // 현재 날씨 정보 처리
        Map<String, Object> current = (Map<String, Object>) rMap.get("current");
        double currentTemp = (double) current.get("temp"); // 현재 기온
        log.info("현재 기온 : " + currentTemp);

        // 현재 날씨 아이콘 처리
        List<Map<String, String>> currentWeather = (List<Map<String, String>>) current.get("weather");
        String currentIcon = currentWeather.get(0).get("icon");
        String currentIconUrl = "http://openweathermap.org/img/wn/" + currentIcon + "@2x.png";
        log.info("현재 날씨 아이콘 URL : " + currentIconUrl);

        // 일별 날씨 정보 처리
        List<Map<String, Object>> dailyList = (List<Map<String, Object>>) rMap.get("daily");
        List<WeatherDailyDTO> pList = new LinkedList<>();

        for (Map<String, Object> dailyMap : dailyList) {

            String day = DateUtil.getLongDateTime(dailyMap.get("dt"), "yyyy-MM-dd"); // 기준 날짜
            String sunrise = DateUtil.getLongDateTime(dailyMap.get("sunrise")); // 해 뜨는 시간
            String sunset = DateUtil.getLongDateTime(dailyMap.get("sunset")); // 해 지는 시간
            String moonrise = DateUtil.getLongDateTime(dailyMap.get("moonrise")); // 달 뜨는 시간
            String moonset = DateUtil.getLongDateTime(dailyMap.get("moonset")); // 달 지는 시간

            log.info("날짜 : " + day);
            log.info("해 뜨는 시간 : " + sunrise);
            log.info("해 지는 시간 : " + sunset);
            log.info("달 뜨는 시간 : " + moonrise);
            log.info("달 지는 시간 : " + moonset);

            // 일별 기온 정보 처리
            Map<String, Double> dailyTemp = (Map<String, Double>) dailyMap.get("temp");
            String dayTemp = String.valueOf(dailyTemp.get("day")); // 평균 기온
            String dayTempMax = String.valueOf(dailyTemp.get("max")); // 최고 기온
            String dayTempMin = String.valueOf(dailyTemp.get("min")); // 최저 기온

            log.info("평균 기온 : " + dayTemp);
            log.info("최고 기온 : " + dayTempMax);
            log.info("최저 기온 : " + dayTempMin);

            // 일별 날씨 아이콘 처리
            List<Map<String, String>> dailyWeather = (List<Map<String, String>>) dailyMap.get("weather");
            String dailyIcon = dailyWeather.get(0).get("icon");
            String dailyIconUrl = "http://openweathermap.org/img/wn/" + dailyIcon + "@2x.png";
            log.info("날씨 아이콘 URL : " + dailyIconUrl);

            WeatherDailyDTO wdDTO = new WeatherDailyDTO();

            wdDTO.setDay(day);
            wdDTO.setSunrise(sunrise);
            wdDTO.setSunset(sunset);
            wdDTO.setMoonrise(moonrise);
            wdDTO.setMoonset(moonset);
            wdDTO.setDayTemp(dayTemp);
            wdDTO.setDayTempMax(dayTempMax);
            wdDTO.setDayTempMin(dayTempMin);
            wdDTO.setWeatherIconUrl(dailyIconUrl); // 아이콘 URL 추가

            pList.add(wdDTO); // 일별 데이터를 리스트에 추가
        }

        WeatherDTO rDTO = new WeatherDTO();
        rDTO.setLat(lat);
        rDTO.setLon(lon);
        rDTO.setCurrentTemp(currentTemp);
        rDTO.setCurrentIconUrl(currentIconUrl); // 현재 날씨 아이콘 URL 추가
        rDTO.setDailyList(pList);

        log.info(this.getClass().getName() + ".getWeather End!");

        return rDTO;
    }
}
