package com.example.eventreserve.controller;

import com.example.eventreserve.config.LoginUser;
import com.example.eventreserve.entity.Event;
import com.example.eventreserve.entity.Reservation;
import com.example.eventreserve.form.EventForm;
import com.example.eventreserve.service.BusinessException;
import com.example.eventreserve.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** 企業のイベント管理画面（企業アカウントだけが開ける。LoginInterceptor でチェック） */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;

    public AdminController(EventService eventService) {
        this.eventService = eventService;
    }

    /** 作成フォーム＋自社イベント一覧 */
    @GetMapping("/events")
    public String events(@SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                         @ModelAttribute EventForm eventForm, Model model) {
        model.addAttribute("events", eventService.listForCompany(user.getId()));
        return "admin/events";
    }

    /** イベントを作成して掲示板に追加する */
    @PostMapping("/events")
    public String publish(@SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                          @Valid @ModelAttribute EventForm eventForm, BindingResult result,
                          Model model, RedirectAttributes redirect) {
        checkDates(eventForm, result);
        if (result.hasErrors()) {
            model.addAttribute("events", eventService.listForCompany(user.getId()));
            return "admin/events";
        }
        eventService.publish(eventForm, user.getId());
        redirect.addFlashAttribute("message", "イベントを掲示板に追加しました");
        return "redirect:/admin/events";
    }

    // ---------- 編集 ----------

    /** 編集画面（今の内容が入った状態で開く） */
    @GetMapping("/events/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                           Model model, RedirectAttributes redirect) {
        try {
            Event event = eventService.findOwnEvent(id, user.getId());
            model.addAttribute("event", event);
            model.addAttribute("eventForm", EventForm.from(event));
            return "admin/edit";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/events";
        }
    }

    /** 編集を保存する */
    @PostMapping("/events/{id}/edit")
    public String edit(@PathVariable Long id,
                       @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                       @Valid @ModelAttribute EventForm eventForm, BindingResult result,
                       Model model, RedirectAttributes redirect) {
        Event event;
        try {
            event = eventService.findOwnEvent(id, user.getId());
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/events";
        }
        checkDates(eventForm, result);
        if (result.hasErrors()) {
            model.addAttribute("event", event);
            return "admin/edit";
        }
        try {
            int promoted = eventService.update(id, user.getId(), eventForm);
            redirect.addFlashAttribute("message", promoted > 0
                    ? "イベントを更新しました（キャンセル待ちの " + promoted + " 人を予約確定にしました）"
                    : "イベントを更新しました");
            return "redirect:/admin/events";
        } catch (BusinessException e) {
            // 参加人数を予約確定の人数より少なくしようとしたとき など
            result.rejectValue("capacity", "tooSmall", e.getMessage());
            model.addAttribute("event", event);
            return "admin/edit";
        }
    }

    /** 日時の前後関係のチェック（作成・編集で共通） */
    private void checkDates(EventForm form, BindingResult result) {
        LocalDateTime now = LocalDateTime.now();
        if (form.getStartAt() != null && form.getStartAt().isBefore(now)) {
            result.rejectValue("startAt", "past", "イベント日時は今より後にしてください");
        }
        if (form.getDeadline() != null && form.getDeadline().isBefore(now)) {
            result.rejectValue("deadline", "past", "予約期限は今より後にしてください");
        }
        if (form.getStartAt() != null && form.getDeadline() != null
                && form.getDeadline().isAfter(form.getStartAt())) {
            result.rejectValue("deadline", "afterStart", "予約期限はイベント日時より前にしてください");
        }
    }

    // ---------- 削除 ----------

    @PostMapping("/events/{id}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                         RedirectAttributes redirect) {
        try {
            eventService.delete(id, user.getId());
            redirect.addFlashAttribute("message", "イベントを削除しました");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/events";
    }

    // ---------- 予約者一覧・CSV ----------

    @GetMapping("/events/{id}/reservations")
    public String reservations(@PathVariable Long id,
                               @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                               Model model, RedirectAttributes redirect) {
        try {
            model.addAttribute("event", eventService.findOwnEvent(id, user.getId()));
            model.addAttribute("confirmed", eventService.confirmedOf(id, user.getId()));
            model.addAttribute("waiting", eventService.waitingOf(id, user.getId()));
            return "admin/reservations";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/events";
        }
    }

    /** 出欠を記録する（value: present=出席 / absent=欠席 / clear=未確認に戻す） */
    @PostMapping("/reservations/{reservationId}/attendance")
    public String attendance(@PathVariable Long reservationId,
                             @RequestParam String value,
                             @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                             RedirectAttributes redirect) {
        Boolean attended = switch (value) {
            case "present" -> Boolean.TRUE;
            case "absent" -> Boolean.FALSE;
            default -> null;
        };
        try {
            Reservation r = eventService.markAttendance(reservationId, user.getId(), attended);
            // 押した人の行に戻る（#r123 でその行までスクロール）
            return "redirect:/admin/events/" + r.getEvent().getId() + "/reservations#r" + r.getId();
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/events";
        }
    }

    /**
     * 予約者一覧をCSVでダウンロード（Excelで開ける）。
     * 予約確定 → キャンセル待ち の順に並べる。
     */
    @GetMapping("/events/{id}/reservations.csv")
    public ResponseEntity<byte[]> reservationsCsv(@PathVariable Long id,
                                                  @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user) {
        Event event = eventService.findOwnEvent(id, user.getId());
        List<Reservation> rows = new ArrayList<>(eventService.confirmedOf(id, user.getId()));
        List<Reservation> waiting = eventService.waitingOf(id, user.getId());
        rows.addAll(waiting);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        StringBuilder csv = new StringBuilder();
        csv.append('﻿'); // BOM: これがないとExcelで日本語が文字化けする
        csv.append(line("No.", "状態", "キャンセル待ち順", "出欠", "氏名", "フリガナ", "メールアドレス",
                "電話番号", "学校名", "備考", "申込日時", "繰り上げ日時"));
        int no = 1;
        int waitNo = 1;
        for (Reservation r : rows) {
            csv.append(line(
                    String.valueOf(no++),
                    r.getStatus().getLabel(),
                    r.isWaiting() ? String.valueOf(waitNo++) : "",
                    r.isConfirmed() ? r.getAttendanceLabel() : "",
                    r.getFullName(),
                    r.getFullNameKana(),
                    r.getMember().getEmail(),
                    r.getPhone(),
                    r.getSchool(),
                    r.getNote(),
                    r.getCreatedAt().format(fmt),
                    r.getPromotedAt() == null ? "" : r.getPromotedAt().format(fmt)));
        }

        String filename = "予約者一覧_" + event.getTitle().replaceAll("[\\\\/:*?\"<>|\\s]", "_") + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString())
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    /** CSVの1行。値は "" で囲み、中の " は "" にする */
    private String line(String... values) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            String v = values[i] == null ? "" : values[i];
            // = + - @ で始まる値はExcelが数式として実行してしまうので、先頭に ' を付けて文字として扱わせる
            if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
                v = "'" + v;
            }
            sb.append('"').append(v.replace("\"", "\"\"")).append('"');
        }
        return sb.append("\r\n").toString();
    }
}
