// 企業のイベント作成・編集フォーム（削除の確認は confirm.js）

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('event-form');
  if (!form) return;

  const title = document.getElementById('title');
  const capacity = document.getElementById('capacity');
  const description = document.getElementById('description');
  const location_ = document.getElementById('location');
  const descCount = document.getElementById('desc-count');
  const startAt = document.getElementById('startAt');
  const deadline = document.getElementById('deadline');
  const deadlineMsg = document.getElementById('deadline-msg');
  const button = document.getElementById('event-btn');

  // 過去の日時を選べないようにする（datetime-local の min は「YYYY-MM-DDTHH:MM」）
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  const nowText = now.toISOString().slice(0, 16);
  startAt.min = nowText;
  deadline.min = nowText;

  function check() {
    descCount.textContent = description.value.length;

    // 予約期限の上限をイベント日時にする
    deadline.max = startAt.value || '';

    const titleOk = title.value.trim() !== '';
    const locationOk = location_.value.trim() !== '';
    const cap = Number(capacity.value);
    const capacityOk = Number.isInteger(cap) && cap >= 1 && cap <= 1000;
    const datesFilled = startAt.value !== '' && deadline.value !== '';
    // 文字列のまま比べられる（同じ形なので辞書順＝時刻順）
    const orderOk = !datesFilled || deadline.value <= startAt.value;
    deadlineMsg.textContent = orderOk ? '' : '予約期限はイベント日時より前にしてください';

    button.disabled = !(titleOk && capacityOk && locationOk && datesFilled && orderOk);
  }

  [title, capacity, description, location_, startAt, deadline].forEach((el) => {
    el.addEventListener('input', check);
    el.addEventListener('change', check);
  });
  check();

  preventDoubleSubmit(form, button, '追加中…');
});
