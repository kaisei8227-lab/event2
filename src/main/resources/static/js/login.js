// ログイン画面
// ・学生／企業の切り替えで見出しを変える
// ・メールアドレスとパスワードの形が両方正しいときだけ、ログインボタンを押せるようにする
//   （パスワードが登録したものと一致するか、学生／企業が合っているかはサーバー側で判定する）
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('login-form');
  const email = document.getElementById('email');
  const password = document.getElementById('password');
  const button = document.getElementById('login-btn');
  const emailMsg = document.getElementById('email-msg');
  const passwordMsg = document.getElementById('password-msg');
  const title = document.getElementById('login-title');
  const registerLink = document.getElementById('register-link');
  const baseRegisterUrl = registerLink.getAttribute('href');

  // 前回ログインに使ったタブ（学生／企業）を覚えておき、次に開いたとき最初から選んでおく。
  // エラーで戻ってきたときはサーバーが選んだタブを優先する。
  const ROLE_KEY = 'eventReservation.lastRole';
  const hasError = document.querySelector('.flash-error') !== null;
  if (!hasError) {
    try {
      const saved = localStorage.getItem(ROLE_KEY);
      const radio = form.querySelector('input[name="role"][value="' + saved + '"]');
      if (radio) radio.checked = true;
    } catch (e) { /* 保存できない環境では何もしない */ }
  }
  form.addEventListener('submit', () => {
    try { localStorage.setItem(ROLE_KEY, selectedRole(form)); } catch (e) { /* 何もしない */ }
  });

  function updateRole() {
    const role = selectedRole(form);
    title.textContent = role === 'COMPANY' ? '企業ログイン' : '学生ログイン';
    // 新規会員登録にも選んだ種類を引き継ぐ
    registerLink.setAttribute('href', baseRegisterUrl + '?role=' + role);
  }

  function check() {
    const emailOk = showState(email, emailMsg, Rules.email.test(email.value.trim()),
      'メールアドレスの形が正しくありません（例: name@example.com）');
    const passwordOk = showState(password, passwordMsg, Rules.password.test(password.value),
      'パスワードは英字と数字をふくむ8文字以上です');
    button.disabled = !(emailOk && passwordOk);
  }

  form.querySelectorAll('input[name="role"]').forEach((r) => r.addEventListener('change', updateRole));
  email.addEventListener('input', check);
  password.addEventListener('input', check);
  updateRole();
  check(); // エラーで戻ってきたときにメールアドレスが入っている場合に備える

  preventDoubleSubmit(form, button, 'ログイン中…');
});
