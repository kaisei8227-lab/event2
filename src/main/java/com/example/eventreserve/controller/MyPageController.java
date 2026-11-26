package com.example.eventreserve.controller;

import com.example.eventreserve.config.LoginUser;
import com.example.eventreserve.service.BusinessException;
import com.example.eventreserve.service.FavoriteService;
import com.example.eventreserve.service.MyReservationView;
import com.example.eventreserve.service.ReservationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** 学生のマイページ（自分の予約一覧・キャンセル） */
@Controller
public class MyPageController {

    private final ReservationService reservationService;
    private final FavoriteService favoriteService;

    public MyPageController(ReservationService reservationService, FavoriteService favoriteService) {
        this.reservationService = reservationService;
        this.favoriteService = favoriteService;
    }

    @GetMapping("/mypage")
    public String mypage(@SessionAttribute(LoginUser.SESSION_KEY) LoginUser user, Model model) {
        if (user.isCompany()) {
            return "redirect:/admin/events";
        }
        List<MyReservationView> all = reservationService.listMine(user.getId());
        model.addAttribute("upcoming", all.stream().filter(v -> !v.isPast()).toList());
        model.addAttribute("past", all.stream().filter(MyReservationView::isPast).toList());
        model.addAttribute("favorites", favoriteService.listMine(user.getId()));
        return "mypage";
    }

    /** 予約（またはキャンセル待ち）をやめる */
    @PostMapping("/reservations/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                         RedirectAttributes redirect) {
        try {
            ReservationService.CancelResult result = reservationService.cancel(id, user.getId());
            redirect.addFlashAttribute("message", result.wasConfirmed()
                    ? "「" + result.eventTitle() + "」の予約をキャンセルしました"
                    : "「" + result.eventTitle() + "」のキャンセル待ちを取り消しました");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mypage";
    }
}
