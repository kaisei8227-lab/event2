package com.example.eventreserve.service;

import com.example.eventreserve.entity.Member;
import com.example.eventreserve.entity.Role;
import com.example.eventreserve.repository.EventRepository;
import com.example.eventreserve.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final EventRepository eventRepository;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder,
                         EventRepository eventRepository) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventRepository = eventRepository;
    }

    /**
     * 新規会員登録。メールアドレスとハッシュ化したパスワードをDBに保管する。
     * companyName は企業のときだけ使う（学生のときは保存しない）。
     */
    @Transactional
    public Member register(String email, String rawPassword, Role role, String companyName) {
        String normalized = normalize(email);
        if (memberRepository.existsByEmail(normalized)) {
            throw new BusinessException("このメールアドレスはすでに登録されています");
        }
        String hash = passwordEncoder.encode(rawPassword);
        String company = (role == Role.COMPANY && companyName != null) ? companyName.trim() : null;
        try {
            // saveAndFlush でこの場ですぐDBに書き込み、一意制約の違反をここで受け止める
            return memberRepository.saveAndFlush(new Member(normalized, hash, role, company));
        } catch (DataIntegrityViolationException e) {
            // ほぼ同時に同じメールアドレスで登録された場合（上の existsByEmail をすり抜けたとき）
            throw new BusinessException("このメールアドレスはすでに登録されています");
        }
    }

    /** ログインの結果 */
    public enum LoginStatus { SUCCESS, NOT_FOUND, BAD_PASSWORD, WRONG_ROLE }

    /** ログインの結果と、成功したとき（または種類だけ違ったとき）の会員 */
    public record LoginResult(LoginStatus status, Member member) {
        public boolean isSuccess() {
            return status == LoginStatus.SUCCESS;
        }
    }

    /**
     * ログイン判定。
     * メールアドレスが登録されていて、パスワードが一致し、
     * さらに選んだ種類（学生／企業）と登録した種類が同じときだけ成功。
     */
    @Transactional(readOnly = true)
    public LoginResult login(String email, String rawPassword, Role role) {
        String normalized = normalize(email);
        Optional<Member> found = memberRepository.findByEmail(normalized);
        // 失敗した理由はサーバーのログ（Eclipseのコンソール / Renderの Logs）にも出す
        if (found.isEmpty()) {
            log.info("ログイン失敗: メールアドレスが登録されていません email={} (登録会員数={})",
                    normalized, memberRepository.count());
            return new LoginResult(LoginStatus.NOT_FOUND, null);
        }
        Member m = found.get();
        if (!passwordEncoder.matches(rawPassword, m.getPasswordHash())) {
            log.info("ログイン失敗: パスワードが違います email={}", normalized);
            return new LoginResult(LoginStatus.BAD_PASSWORD, null);
        }
        if (m.getRole() != role) {
            // パスワードまで合っているので、種類が違うことは本人に伝えてよい
            log.info("ログイン失敗: 種類が違います email={} 登録={} 選択={}", normalized, m.getRole(), role);
            return new LoginResult(LoginStatus.WRONG_ROLE, m);
        }
        return new LoginResult(LoginStatus.SUCCESS, m);
    }

    /** パスワードを変える。今のパスワードが合っていなければエラー */
    @Transactional
    public void changePassword(Long memberId, String currentPassword, String newPassword) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("会員が見つかりません"));
        if (!passwordEncoder.matches(currentPassword, m.getPasswordHash())) {
            throw new BusinessException("今のパスワードが違います");
        }
        m.changePasswordHash(passwordEncoder.encode(newPassword));
    }

    /** 企業名を変える。掲示中の自社イベントの企業名もいっしょに変える */
    @Transactional
    public Member changeCompanyName(Long memberId, String companyName) {
        Member m = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("会員が見つかりません"));
        if (!m.isCompany()) {
            throw new BusinessException("企業アカウントではありません");
        }
        String name = companyName.trim();
        m.changeCompanyName(name);
        eventRepository.findByCreatedByIdOrderByStartAtAsc(memberId)
                .forEach(e -> e.changeCompanyName(name));
        return m;
    }

    @Transactional(readOnly = true)
    public Member get(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException("会員が見つかりません"));
    }

    @Transactional(readOnly = true)
    public boolean exists(String email) {
        return memberRepository.existsByEmail(normalize(email));
    }

    /** 大文字・小文字や前後の空白の違いで別人扱いにならないようにそろえる */
    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
