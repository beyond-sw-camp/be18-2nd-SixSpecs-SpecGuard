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
        }
    }

    @Getter @Setter
    public static class Github {
        private String token;
    }
}
