package com.beyond.specguard.common.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OAuth2StateUtil {

    private OAuth2StateUtil() {
    }

    /**
     * state 값에서 inviteToken을 추출한다.
     * state는 [랜덤UUID]__[inviteToken] 형식으로 들어온다.
     *
     * @param state OAuth2 state 값
     * @return 추출된 inviteToken (없으면 null)
     */
    public static String extractInviteToken(String state) {
        if (state == null) {
            log.warn(" state 값이 null입니다.");
            return null;
        }
        if (state.contains("__")) {
            return state.split("__")[1];
        }
        return state;
    }
}
