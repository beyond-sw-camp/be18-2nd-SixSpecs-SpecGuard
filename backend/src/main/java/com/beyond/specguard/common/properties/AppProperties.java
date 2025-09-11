package com.beyond.specguard.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Redis redis = new Redis();
    private Github github = new Github();
    private Verify verify = new Verify();

    @Getter @Setter
    public static class Jwt {
        private long accessTtl;
        private long refreshTtl;
        private long inviteTtl;
    }

    @Getter
    @Setter
    public static class Redis {
        private Prefix prefix = new Prefix();

        @Getter @Setter
        public static class Prefix {
            private String refresh;
            private String blacklist;
            private String session;
            private String verifyAttempt;
            private String verifyPhone;
        }
    }

    @Getter @Setter
    public static class Verify {
        private Long ttlSeconds;
        private int maxAttempts;
        private String receiverEmail;
        private String receiverSms;
    }

    @Getter @Setter
    public static class Github {
        private String token;
    }
}
