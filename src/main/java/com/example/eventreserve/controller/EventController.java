package com.example.eventreserve.controller;

import com.example.eventreserve.config.LoginUser;
import com.example.eventreserve.entity.Reservation;
import com.example.eventreserve.form.ReservationForm;
import com.example.eventreserve.entity.Event;
import com.example.eventreserve.service.BusinessException;
import com.example.eventreserve.service.CalendarLinks;
import com.example.eventreserve.service.FavoriteService;
import com.example.eventreserve.service.EventSearch;
import com.example.eventreserve.service.EventService;
import com.example.eventreserve.service.EventView;
import com.example.eventreserve.service.ReservationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@Controller
public class EventController {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final FavoriteService favoriteService;

    public EventController(EventService eventService, ReservationService reservationService,
                           FavoriteService favoriteService) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.favoriteService = favoriteService;
    }

    /** イベント一覧（掲示板）。?q=...&from=...&to=...&available=true で絞り込み */
    @GetMapping("/events")
    public String list(@SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                       @ModelAttribute("search") EventSearch search, Model model) {
        model.addAttribute("events", eventService.listForMember(user.getId(), search));
        return "events/list";
    }

    /** イベント詳細ページ */
    @GetMapping("/events/{id}")
    public String detail(@PathVariable Long id,
                         @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                         Model model, RedirectAttributes redirect) {
        try {
            EventView view = eventService.getView(id, user.getId());
            model.addAttribute("view", view);
            model.addAttribute("googleCalendarUrl", CalendarLinks.googleUrl(view.getEvent(), pageUrl(id)));
            return "events/detail";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/events";
        }
    }

    /** カレンダー登録用の .ics ファイル（スマホやPCのカレンダーで開くと予定に追加される） */
    @GetMapping("/events/{id}/calendar.ics")
    public ResponseEntity<byte[]> calendar(@PathVariable Long id,
                                           @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user) {
        Event event = eventService.getView(id, user.getId()).getEvent();
        byte[] body = CalendarLinks.ics(event, pageUrl(id)).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("event-" + id + ".ics").build().toString())
                .contentType(new MediaType("text", "calendar", StandardCharsets.UTF_8))
                .body(body);
    }

    /** お気に入りの追加・解除。押したページ（back）に戻る */
    @PostMapping("/events/{id}/favorite")
    public String favorite(@PathVariable Long id,
                           @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                           @RequestParam(required = false) String back,
                           RedirectAttributes redirect) {
        if (user.isCompany()) {
            return "redirect:/events";
        }
        try {
            boolean added = favoriteService.toggle(id, user.getId());
            redirect.addFlashAttribute("message", added ? "お気に入りに追加しました" : "お気に入りから外しました");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        // 外部のサイトへ飛ばされないよう、このサイト内のパスだけ受け付ける
        boolean safe = back != null && back.startsWith("/") && !back.startsWith("//") && !back.contains("\\");
        return "redirect:" + (safe ? back : "/events");
    }

    /** このイベントの詳細ページのURL（カレンダーの説明欄に入れる） */
    private String pageUrl(Long id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/events/{id}").buildAndExpand(id).toUriString();
    }

    /** 予約ボタン（またはキャンセル待ちボタン）を押したときの、個人情報入力ページ */
    @GetMapping("/events/{id}/reserve")
    public String reserveForm(@PathVariable Long id,
                              @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                              @ModelAttribute ReservationForm reservationForm,
                              Model model, RedirectAttributes redirect) {
        if (user.isCompany()) {
            redirect.addFlashAttribute("error", "企業アカウントでは予約できません");
            return "redirect:/events";
        }
        try {
            EventView view = eventService.getView(id, user.getId());
            if (!view.isReservable() && !view.isWaitlistable()) {
                redirect.addFlashAttribute("error", reasonNotReservable(view));
                return "redirect:/events";
            }
            model.addAttribute("view", view);
            model.addAttribute("pageTitle", view.isFull() ? "キャンセル待ちの登録" : "予約情報の入力");
            return "events/reserve";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/events";
        }
    }

    /** 予約完了ボタン。空きがあれば予約確定、満席ならキャンセル待ちになる */
    @PostMapping("/events/{id}/reserve")
    public String reserve(@PathVariable Long id,
                          @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                          @Valid @ModelAttribute ReservationForm reservationForm, BindingResult result,
                          Model model, RedirectAttributes redirect) {
        if (user.isCompany()) {
            redirect.addFlashAttribute("error", "企業アカウントでは予約できません");
            return "redirect:/events";
        }
        try {
            if (result.hasErrors()) {
                EventView view = eventService.getView(id, user.getId());
                model.addAttribute("view", view);
                model.addAttribute("pageTitle", view.isFull() ? "キャンセル待ちの登録" : "予約情報の入力");
                return "events/reserve";
            }
            Reservation r = reservationService.reserve(id, user.getId(), reservationForm);
            // 完了ページへリダイレクト（再読み込みで二重予約にならないように）
            return "redirect:/reservations/" + r.getId() + "/complete";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/events";
        }
    }

    /** 予約完了ページ（キャンセル待ちのときは順番を表示） */
    @GetMapping("/reservations/{id}/complete")
    public String complete(@PathVariable Long id,
                           @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                           Model model, RedirectAttributes redirect) {
        try {
            Reservation r = reservationService.getOwn(id, user.getId());
            model.addAttribute("reservation", r);
            model.addAttribute("pageTitle", r.isWaiting() ? "キャンセル待ち登録完了" : "予約完了");
            model.addAttribute("position", reservationService.waitingPosition(r));
            model.addAttribute("googleCalendarUrl",
                    CalendarLinks.googleUrl(r.getEvent(), pageUrl(r.getEvent().getId())));
            return "events/complete";
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/events";
        }
    }

    private String reasonNotReservable(EventView view) {
        if (view.isReservedByMe()) return "このイベントはすでに予約済みです";
        if (view.isWaitingByMe()) return "このイベントはすでにキャンセル待ちに登録しています";
        return "予約期限を過ぎたため予約できません";
    }
}
