// class="js-confirm" のフォームは、送信の前に data-message の内容で確認する
// （キャンセル・削除など、元に戻せない操作に使う）
document.querySelectorAll('form.js-confirm').forEach((form) => {
  form.addEventListener('submit', (e) => {
    if (!window.confirm(form.dataset.message)) {
      e.preventDefault();
    }
  });
});
