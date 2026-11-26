package com.example.eventreserve.service;

import com.example.eventreserve.EventReserveApplication;
import com.example.eventreserve.entity.Event;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * カレンダー登録用のデータを作る。
 * ・.ics ファイル（iPhone・Android・Outlook などのカレンダーに追加できる共通形式）
 * ・Googleカレンダーの「予定を追加」画面を開くURL
 *
 * イベントには終了日時がないので、開始から1時間の予定として登録する。
 */
public final class CalendarLinks {

    /** 終了日時がないので、開始から何分の予定にするか */
    public static final int DEFAULT_MINUTES = 60;

    private static final ZoneId JAPAN = ZoneId.of(EventReserveApplication.TIME_ZONE);
    private static final DateTimeFormatter UTC_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private CalendarLinks() {
    }

    /** .ics ファイルの中身 */
    public static String ics(Event e, String pageUrl) {
        String start = utc(e.getStartAt());
        String end = utc(e.getStartAt().plusMinutes(DEFAULT_MINUTES));
        String now = utc(LocalDateTime.now());

        StringBuilder sb = new StringBuilder();
        line(sb, "BEGIN:VCALENDAR");
        line(sb, "VERSION:2.0");
        line(sb, "PRODID:-//EventReservation//JA");
        line(sb, "CALSCALE:GREGORIAN");
        line(sb, "METHOD:PUBLISH");
        line(sb, "BEGIN:VEVENT");
        line(sb, "UID:event-" + e.getId() + "@event-reservation");
        line(sb, "DTSTAMP:" + now);
        line(sb, "DTSTART:" + start);
        line(sb, "DTEND:" + end);
        line(sb, "SUMMARY:" + escape(e.getTitle()));
        line(sb, "LOCATION:" + escape(e.getLocation()));
        line(sb, "DESCRIPTION:" + escape(description(e, pageUrl)));
        line(sb, "URL:" + pageUrl);
        line(sb, "END:VEVENT");
        line(sb, "END:VCALENDAR");
        return sb.toString();
    }

    /** Googleカレンダーの「予定を追加」画面を開くURL */
    public static String googleUrl(Event e, String pageUrl) {
        String dates = utc(e.getStartAt()) + "/" + utc(e.getStartAt().plusMinutes(DEFAULT_MINUTES));
        return "https://calendar.google.com/calendar/render?action=TEMPLATE"
                + "&text=" + enc(e.getTitle())
                + "&dates=" + dates
                + "&location=" + enc(e.getLocation())
                + "&details=" + enc(description(e, pageUrl))
                + "&ctz=" + EventReserveApplication.TIME_ZONE;
    }

    private static String description(Event e, String pageUrl) {
        StringBuilder d = new StringBuilder();
        d.append("主催: ").append(e.getCompanyName()).append('\n');
        if (e.getDescription() != null) {
            d.append(e.getDescription()).append('\n');
        }
        d.append(pageUrl);
        return d.toString();
    }

    /** 日本時間の日時 → 世界標準時の「20261210T040000Z」形式 */
    private static String utc(LocalDateTime japanTime) {
        return japanTime.atZone(JAPAN).withZoneSameInstant(ZoneOffset.UTC).format(UTC_FORMAT);
    }

    private static String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }

    /** .ics の決まり: \ ; , と改行はエスケープする */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n");
    }

    /** .ics の決まり: 1行は75バイトまで。長い行は「改行＋空白」で折り返す。行末は CRLF */
    private static void line(StringBuilder sb, String text) {
        int bytes = 0;
        int i = 0;
        while (i < text.length()) {
            // 「\n」「\,」などのエスケープは2文字ひとまとめにして、途中で折り返さない
            int cp = text.codePointAt(i);
            int unitLength = Character.charCount(cp);
            if (cp == '\\' && i + 1 < text.length()) {
                unitLength = 1 + Character.charCount(text.codePointAt(i + 1));
            }
            String unit = text.substring(i, i + unitLength);
            int len = unit.getBytes(StandardCharsets.UTF_8).length;
            if (bytes + len > 74) {
                sb.append("\r\n ");
                bytes = 1;
            }
            sb.append(unit);
            bytes += len;
            i += unitLength;
        }
        sb.append("\r\n");
    }
}
