# イベント予約アプリ（Java / Spring Boot）

ログイン・新規会員登録・イベント一覧（掲示板）・予約・企業の管理画面を持つWebアプリです。

| 役割 | 使うもの |
|---|---|
| サーバー | Java 17 / Spring Boot 4.1.2 |
| 画面 | HTML・CSS・JavaScript ＋ Thymeleaf（サーバー側テンプレート）|
| データベース | ローカル: H2（ファイル保存）／ 本番: PostgreSQL |
| 公開 | GitHub → Render（Docker）|

## 画面と機能

| URL | 画面 | 内容 |
|---|---|---|
| `/login` | ログイン | 「学生の方／企業の方」を切り替えてログイン。メール形式・パスワード形式が両方OKのときだけボタンが押せる。「新規会員登録はこちら」 |
| `/register` | 新規会員登録 | 学生か企業かを選んで登録（企業は企業名も入力）。メールアドレスとパスワード（ハッシュ化）をDBに保存し、そのままログイン |
| `/events` | イベント一覧（掲示板） | 企業が作成したイベントを表示。キーワード・開催日・空きありで検索できる。満席ならキャンセル待ちに登録できる |
| `/events/{id}/reserve` | 予約情報の入力 | 氏名・フリガナ・電話番号・学校名・備考を入力して「予約完了」 |
| `/events/{id}` | イベント詳細 | 説明の全文、残席、予約期限。お気に入り、Googleカレンダー／.ics でカレンダーに追加 |
| `/reservations/{id}/complete` | 予約完了 | 予約内容を表示（キャンセル待ちのときは順番も表示） |
| `/mypage` | マイページ（学生） | 自分の予約・キャンセル待ちの一覧（予約期限までキャンセル可）、お気に入り（締切間近を表示） |
| `/account` | アカウント | パスワードの変更、企業名の変更（企業のみ。掲示中のイベントにも反映） |
| `/admin/events` | イベント管理（企業） | イベント名・参加人数・説明・会場・日時・予約期限を入力して掲示板に追加、削除。企業アカウントだけ開ける |
| `/admin/events/{id}/edit` | イベント編集（企業） | 内容の変更。参加人数を増やすとキャンセル待ちが自動で繰り上がる |
| `/admin/events/{id}/reservations` | 予約者一覧 | 予約確定とキャンセル待ちの一覧、出欠チェックと出席率、CSVダウンロード（Excelで開ける） |

### 大事なルール
- **時刻は日本時間**: Render のサーバーは世界標準時で動くので、アプリ側で日本時間に固定しています（予約期限の判定がずれないように）
- **キャンセル待ち**: 満席のイベントはキャンセル待ちに登録できます。予約確定の人がキャンセルするか、企業が参加人数を増やすと、申し込み順に自動で予約確定になります（定員チェックと同じくロックしてから処理するので、同時に起きても順番が崩れません）
- **学生と企業を分ける**: 学生タブで企業アカウントのメールを入れてもログインできません（逆も同じ）。企業は予約できず、学生はイベント管理を開けません
- **予約期限**: 期限を過ぎたイベントは学生の掲示板から消え、予約できなくなります（企業のイベント管理画面には「締切」として残り、予約者一覧も見られます）。期限はイベント日時より前で、今より後である必要があります
- **定員を超えない**: 予約時にイベントの行をロックしてから数えるので、同時に押されても定員を超えません
- **同じイベントは1人1回**: サーバーでチェック＋DBの一意制約
- **パスワードはそのまま保存しない**: BCryptでハッシュ化して保存。DBを見られても元のパスワードはわかりません
- **入力チェックは2回**: JavaScript（画面）でチェックして、サーバー（Java）でも同じルールでチェック

## Eclipseで動かす

1. zip を**空のフォルダ**に解凍する（ほかのファイルが入っているフォルダには解凍しない）
2. Eclipse の「ファイル」→「インポート」→「Maven」→「既存 Maven プロジェクト」
3. 「ルート・ディレクトリー」に **`Event Reservation` フォルダ**（`pom.xml` がある場所）を選び、`pom.xml` にチェックが入っていることを確認して「完了」
   - 外側のフォルダを選ぶと、Maven のプロジェクトとして認識されず、Spring が見つからないエラーがたくさん出ます
   - 初回は依存ライブラリのダウンロードに数分かかります
3. `src/main/java/com/example/eventreserve/EventReserveApplication.java` を右クリック →「実行」→「Java アプリケーション」
4. ブラウザで http://localhost:8080 を開く

> Eclipse は「Eclipse IDE for Enterprise Java and Web Developers」を使い、JDK 17 以上を設定してください。
> 文字化けする場合は「ウィンドウ」→「設定」→「一般」→「ワークスペース」→ テキスト・ファイルのエンコードを UTF-8 にします。

### 試し方
最初はデータが空です。

1. `/register` で「企業として登録」→ イベント管理画面でイベントを作成
2. ログアウトして `/register` で「学生として登録」→ イベント一覧で予約
3. もう一度「企業の方」タブでログインすると、予約者の一覧が見られます

### データの保存場所
ホームフォルダの `event-reservation-data/eventdb.mv.db` に保存されます（Windows なら `C:\Users\<ユーザー名>\event-reservation-data`）。プロジェクトの外なので、新しい版を別の場所に解凍しても会員情報は残ります。フォルダごと消すと初期状態に戻ります。

起動するとコンソールに `データベース: jdbc:h2:file:... / 登録会員数: 3` のように、使っているDBと会員数が出ます。

> **テーブルの形を変えたとき（項目を消した・必須にしたなど）に保存でエラーが出たら、Eclipseでアプリを止めてから `event-reservation-data` フォルダを削除して起動し直してください。**

## GitHubに上げる

```bash
git init
git add .
git commit -m "イベント予約アプリの土台"
git branch -M main
git remote add origin https://github.com/<ユーザー名>/event-reserve.git
git push -u origin main
```

## Renderで公開する

### 1. データベースを作る
Render のダッシュボード →「New」→「Postgres」→ 無料プランで作成。
作成後の画面にある **Internal Database URL** を控えます。形はこうなっています:

```
postgresql://ユーザー名:パスワード@ホスト名/DB名
```

> 無料の Postgres は期限があります（作成から一定日数で削除）。提出・発表の日程に合わせて作ってください。

### 2. Webサービスを作る
「New」→「Web Service」→ GitHub のリポジトリを選ぶ → **Language: Docker** → 無料プラン。

**Environment（環境変数）** に次を追加します:

| キー | 値 |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://ホスト名:5432/DB名` |
| `SPRING_DATASOURCE_USERNAME` | ユーザー名 |
| `SPRING_DATASOURCE_PASSWORD` | パスワード |

Internal Database URL の `postgresql://` の部分を `jdbc:postgresql://` に変え、ユーザー名とパスワードは URL から外して別々に入れるのがポイントです。

「Create Web Service」を押すとビルドが始まり、数分で `https://〇〇.onrender.com` で公開されます。
GitHub に push するたびに自動で再デプロイされます。

> 無料プランはしばらくアクセスがないと止まり、次のアクセスで起動に30秒〜1分かかります。

## うまくいかないとき

- **pom.xml でエラーになる**: [Spring Initializr](https://start.spring.io/) で Maven / Java / Java 17 を選び、
  Dependencies に「Spring Web」「Thymeleaf」「Spring Data JPA」「Validation」「H2 Database」「PostgreSQL Driver」を追加して GENERATE。
  できたプロジェクトの `pom.xml` に次を足し、このプロジェクトの `src` を上書きコピーしてください。
  ```xml
  <dependency>
      <groupId>org.springframework.security</groupId>
      <artifactId>spring-security-crypto</artifactId>
  </dependency>
  ```
- **ログインボタンが押せない**: パスワードが「英字と数字をふくむ8文字以上」になっているか確認
- **ポート8080が使用中**: `application.properties` の `server.port` を `${PORT:8081}` などに変える

## フォルダ構成

```
src/main/java/com/example/eventreserve/
├── EventReserveApplication.java   起動クラス
├── config/       ログインチェック、パスワードのハッシュ化
├── controller/   画面ごとの処理（Auth / Event / Admin）
├── entity/       DBのテーブル（Member / Event / Reservation）
├── form/         画面から受け取る入力と入力チェック
├── repository/   DBの読み書き
└── service/      予約・登録などの処理（定員チェックはここ）
src/main/resources/
├── templates/    HTML（Thymeleaf）
├── static/css/   CSS
├── static/js/    JavaScript（入力チェック、ボタンの有効化）
└── application*.properties  設定
```

## これから足すとよいもの
- CSRF対策（フォームの不正送信を防ぐ）: Spring Security を入れると自動で付きます
- 予約確認・リマインドのメール送信
- 企業から予約者へのお知らせ（一斉連絡）
