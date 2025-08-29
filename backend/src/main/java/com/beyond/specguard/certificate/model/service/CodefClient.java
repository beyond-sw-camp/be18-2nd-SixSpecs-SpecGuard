package com.beyond.specguard.certificate.model.service;

import com.beyond.specguard.certificate.model.config.EasyCodefClientInfo;
import com.beyond.specguard.certificate.model.dto.CodefVerificationRequest;
import com.beyond.specguard.certificate.model.dto.CodefVerificationResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CodefClient {

    private final EasyCodefClientInfo easyCodefClientInfo;

    public CodefVerificationResponse verifyCertificate(CodefVerificationRequest request) throws IOException, InterruptedException {
        // (Connected ID 미사용)
        /* #1.쉬운 코드에프 객체 생성 및 클라이언트 정보 설정 */
        EasyCodef codef = new EasyCodef();
        codef.setClientInfoForDemo(easyCodefClientInfo.DEMO_CLIENT_ID, easyCodefClientInfo.DEMO_CLIENT_SECRET);
        codef.setPublicKey(easyCodefClientInfo.PUBLIC_KEY);

        log.debug(easyCodefClientInfo.DEMO_CLIENT_ID);
        log.debug(easyCodefClientInfo.DEMO_CLIENT_SECRET);

        /* #5.요청 파라미터 설정 - 각 상품별 파라미터를 설정(https://developer.codef.io/products) */
        HashMap<String, Object> parameterMap = new HashMap<>();
        parameterMap.put("organization", request.getOrganization()); // 기관코드 설정
        parameterMap.put("userName", request.getUserName());
        parameterMap.put("docNo", request.getDocNo());


        log.debug(request.toString());
        log.debug(parameterMap.toString());

        /* #6.코드에프 정보 조회 요청 - 서비스타입(API:정식, DEMO:데모, SANDBOX:샌드박스) */
        String productUrl = "/v1/kr/etc/hr/qnet-certificate/status";
        String result = codef.requestProduct(productUrl, EasyCodefServiceType.DEMO, parameterMap);

        /*	#7.코드에프 정보 결과 확인	*/
        log.debug(result);

        return new ObjectMapper().readValue(result, CodefVerificationResponse.class);
    }
}
