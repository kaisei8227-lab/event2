// 入力チェックの共通ルール。
// サーバー側（ValidationRules.java）と同じルールにしてある。変えるときは両方そろえる。
const Rules = {
  // 「名前@ドメイン.トップレベル」の形
  email: /^[\w.+-]+@[\w-]+(\.[\w-]+)+$/,
  // 英字と数字をふくむ8〜64文字
  password: /^(?=.*[A-Za-z])(?=.*\d).{8,64}$/,
  // 全角カタカナ
  kana: /^[ァ-ヶー　 ]+$/,
  // 0から始まる電話番号（ハイフン任意）
  phone: /^0\d{1,4}-?\d{1,4}-?\d{3,4}$/,
};

/**
 * 入力欄の下にメッセージを出し、枠の色を変える。
 * 何も入力していないあいだは何も出さない。
 */
function showState(input, msgEl, ok, errorText) {
  const empty = input.value === '';
  input.classList.toggle('is-valid', ok && !empty);
  input.classList.toggle('is-invalid', !ok && !empty);
  if (msgEl) {
    msgEl.textContent = (!ok && !empty) ? errorText : '';
  }
  return ok;
}

/** 学生／企業のどちらが選ばれているか（'USER' または 'COMPANY'） */
function selectedRole(form) {
  const checked = form.querySelector('input[name="role"]:checked');
  return checked ? checked.value : 'USER';
}

/** 送信ボタンの二度押しを防ぐ */
function preventDoubleSubmit(form, button, sendingText) {
  form.addEventListener('submit', () => {
    button.disabled = true;
    button.textContent = sendingText;
  });
}
