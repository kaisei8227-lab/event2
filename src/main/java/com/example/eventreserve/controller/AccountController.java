package com.example.eventreserve.controller;

import com.example.eventreserve.config.LoginUser;
import com.example.eventreserve.entity.Member;
import com.example.eventreserve.form.CompanyNameForm;
import com.example.eventreserve.form.PasswordChangeForm;
import com.example.eventreserve.service.BusinessException;
import com.example.eventreserve.service.MemberService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** 会員情報の変更（パスワード・企業名） */
@Controller
public class AccountController {

    private final MemberService memberService;

    public AccountController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/account")
    public String account(@SessionAttribute(LoginUser.SESSION_KEY) LoginUser user, Model model) {
        prepare(user, model, new PasswordChangeForm(), null);
        return "account";
    }

    /** パスワードを変える */
    @PostMapping("/account/password")
    public String changePassword(@SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                                 @Valid @ModelAttribute PasswordChangeForm passwordChangeForm, BindingResult result,
                                 Model model, RedirectAttributes redirect) {
        if (passwordChangeForm.getNewPassword() != null
                && !passwordChangeForm.getNewPassword().equals(passwordChangeForm.getNewPasswordConfirm())) {
            result.rejectValue("newPasswordConfirm", "mismatch", "新しいパスワードが一致しません");
        }
        if (!result.hasErrors()) {
            try {
                memberService.changePassword(user.getId(),
                        passwordChangeForm.getCurrentPassword(), passwordChangeForm.getNewPassword());
                redirect.addFlashAttribute("message", "パスワードを変更しました");
                return "redirect:/account";
            } catch (BusinessException e) {
                result.rejectValue("currentPassword", "wrong", e.getMessage());
            }
        }
        prepare(user, model, passwordChangeForm, null);
        return "account";
    }

    /** 企業名を変える（企業アカウントだけ） */
    @PostMapping("/account/company")
    public String changeCompanyName(@SessionAttribute(LoginUser.SESSION_KEY) LoginUser user,
                                    @Valid @ModelAttribute CompanyNameForm companyNameForm, BindingResult result,
                                    HttpSession session, Model model, RedirectAttributes redirect) {
        if (!user.isCompany()) {
            return "redirect:/account";
        }
        if (!result.hasErrors()) {
            try {
                Member m = memberService.changeCompanyName(user.getId(), companyNameForm.getCompanyName());
                // ヘッダーの表示名も新しい企業名にする
                session.setAttribute(LoginUser.SESSION_KEY, new LoginUser(m));
                redirect.addFlashAttribute("message", "企業名を変更しました（掲示中のイベントにも反映しました）");
                return "redirect:/account";
            } catch (BusinessException e) {
                result.rejectValue("companyName", "error", e.getMessage());
            }
        }
        prepare(user, model, new PasswordChangeForm(), companyNameForm);
        return "account";
    }

    private void prepare(LoginUser user, Model model, PasswordChangeForm pw, CompanyNameForm company) {
        Member m = memberService.get(user.getId());
        model.addAttribute("member", m);
        model.addAttribute("passwordChangeForm", pw);
        if (company == null) {
            company = new CompanyNameForm();
            company.setCompanyName(m.getCompanyName());
        }
        model.addAttribute("companyNameForm", company);
    }
}
