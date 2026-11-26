// 予約情報の入力ページ
// 必須項目がそろったら「予約完了」ボタンを押せるようにする。
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('reserve-form');
  const fullName = document.getElementById('fullName');
  const kana = document.getElementById('fullNameKana');
  const phone = document.getElementById('phone');
  const button = document.getElementById('reserve-btn');

  function check() {
    const nameOk = fullName.value.trim() !== '';
    const kanaOk = showState(kana, document.getElementById('kana-msg'),
      Rules.kana.test(kana.value.trim()), '全角カタカナで入力してください');
    const phoneOk = showState(phone, document.getElementById('phone-msg'),
      Rules.phone.test(phone.value.trim()), '電話番号の形が正しくありません（例: 090-1234-5678）');
    button.disabled = !(nameOk && kanaOk && phoneOk);
  }

  [fullName, kana, phone].forEach((el) => el.addEventListener('input', check));
  check();

  preventDoubleSubmit(form, button, '送信中…');
});
