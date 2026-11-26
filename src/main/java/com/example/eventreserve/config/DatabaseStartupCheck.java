package com.example.eventreserve.config;

import com.example.eventreserve.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 起動時に「どのデータベースを使っているか」と「登録会員数」をログに出す。
 *
 * Render 上で H2（コンテナ内のファイル）を使っていたら起動を止める。
 * H2 のままだと、Render が再起動するたび（無料プランは15分アクセスがないと止まる）に
 * 会員情報が消えて「登録したのにログインできない」状態になるため。
 */
@Component
public class DatabaseStartupCheck implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStartupCheck.class);

    private final DataSource dataSource;
    private final MemberRepository memberRepository;

    public DatabaseStartupCheck(DataSource dataSource, MemberRepository memberRepository) {
        this.dataSource = dataSource;
        this.memberRepository = memberRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String url;
        try (Connection c = dataSource.getConnection()) {
            url = c.getMetaData().getURL();
        }
        // Render は全サービスに環境変数 RENDER を自動で設定する
        boolean onRender = System.getenv("RENDER") != null;

        if (onRender && url.startsWith("jdbc:h2")) {
            throw new IllegalStateException(
                    "Render 上で H2 を使っています。このままだと再起動のたびに会員情報が消えます。"
                    + "Environment に SPRING_PROFILES_ACTIVE=prod と SPRING_DATASOURCE_URL / USERNAME / PASSWORD を設定してください");
        }

        // ユーザー名やパスワードは出さず、URL の ? より前だけ出す
        String shownUrl = url.contains("?") ? url.substring(0, url.indexOf('?')) : url;
        log.info("データベース: {} / 登録会員数: {}", shownUrl, memberRepository.count());
    }
}
