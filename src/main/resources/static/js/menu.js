// スマホ用：ヘッダーのメニューを開いたり閉じたりする
(() => {
  const header = document.getElementById('app-header');
  const toggle = document.getElementById('menu-toggle');
  if (!header || !toggle) return;

  toggle.addEventListener('click', () => {
    const open = header.classList.toggle('is-open');
    toggle.setAttribute('aria-expanded', String(open));
    toggle.setAttribute('aria-label', open ? 'メニューを閉じる' : 'メニューを開く');
  });
})();
