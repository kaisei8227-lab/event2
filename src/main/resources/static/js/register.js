// 新規会員登録画面
// ・学生／企業の切り替えで、企業名の欄を出したり隠したりする
// ・すべて正しく入力されたときだけ「登録する」を押せる
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('register-form');
  const companyField = document.getElementById('company-field');
  const companyName = document.getElementById('companyName');
  const email = document.getElementById('email');
  const password = document.getElementById('password');
  const confirm = document.getElementById('passwordConfirm');
  const button = document.getElementById('register-btn');
  const title = document.getElementById('register-title');

  // ログイン画面から ?role=COMPANY で来たら企業を選んだ状態にする
  const fromLogin = new URLSearchParams(location.search).get('role');
  if (fromLogin === 'COMPANY' || fromLogin === 'USER') {
    const radio = form.querySelector('input[name="role"][value="' + fromLogin + '"]');
    if (radio) radio.checked = true;
  }

  function isCompany() {
    return selectedRole(form) === 'COMPANY';
  }

  function updateRole() {
    companyField.hidden = !isCompany();
    title.textContent = isCompany() ? '新規会員登録（企業）' : '新規会員登録（学生）';
    check();
  }

  function check() {
    const companyOk = !isCompany() || companyName.value.trim() !== '';
    const emailOk = showState(email, document.getElementById('email-msg'),
      Rules.email.test(email.value.trim()),
      'メールアドレスの形が正しくありません（例: name@example.com）');
    const passwordOk = showState(password, document.getElementById('password-msg'),
      Rules.password.test(password.value),
      '英字と数字をふくむ8文字以上にしてください');
    const confirmOk = showState(confirm, document.getElementById('confirm-msg'),
      confirm.value !== '' && confirm.value === password.value,
      'パスワードが一致しません');
    button.disabled = !(companyOk && emailOk && passwordOk && confirmOk);
  }

  form.querySelectorAll('input[name="role"]').forEach((r) => r.addEventListener('change', updateRole));
  [companyName, email, password, confirm].forEach((el) => el.addEventListener('input', check));
  updateRole();

  preventDoubleSubmit(form, button, '登録中…');
});
