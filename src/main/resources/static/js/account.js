// アカウント画面：パスワード変更の入力チェック
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('password-form');
  const current = document.getElementById('currentPassword');
  const next = document.getElementById('newPassword');
  const confirm = document.getElementById('newPasswordConfirm');
  const button = document.getElementById('password-btn');

  function check() {
    const currentOk = current.value !== '';
    const nextOk = showState(next, document.getElementById('new-msg'),
      Rules.password.test(next.value), '英字と数字をふくむ8文字以上にしてください');
    const confirmOk = showState(confirm, document.getElementById('confirm-msg'),
      confirm.value !== '' && confirm.value === next.value, '新しいパスワードが一致しません');
    button.disabled = !(currentOk && nextOk && confirmOk);
  }

  [current, next, confirm].forEach((el) => el.addEventListener('input', check));
  check();
  preventDoubleSubmit(form, button, '変更中…');
});
