(() => {
  if (window !== window.top || location.origin !== 'https://app.fastmail.com' || window.__morpheScale) return;
  window.__morpheScale = true;
  const key = 'morphe.fastmail.fontScale';
  const choices = [70, 80, 85, 90, 95, 100, 105, 110, 115, 120, 130, 140, 150];
  let scale = 100;
  try { const stored = Number(localStorage.getItem(key)); if (choices.includes(stored)) scale = stored; } catch (_) {}
  const body = document.body;
  let rootView = null, originalConfig = null, appliedConfig = null;
  const configObserver = { changed() {
    const current = rootView.get('_systemAccessibilityConfig');
    if (current !== appliedConfig) { originalConfig = current; applyMetrics(); }
  }};
  const applyMetrics = () => {
    const view = window.FM && FM.root;
    if (!view || typeof view.drawZoom !== 'function' || typeof view.addObserverForKey !== 'function') return;
    if (view !== rootView) {
      rootView = view;
      originalConfig = view.get('_systemAccessibilityConfig');
      view.addObserverForKey('_systemAccessibilityConfig', configObserver, 'changed');
    }
    if (!originalConfig || !(originalConfig.fontScale > 0)) return;
    // Feed Fastmail its normal accessibility pipeline, keeping the native baseline
    // separate so repeated selection and system updates cannot compound scaling.
    appliedConfig = scale === 100 ? originalConfig : Object.assign({}, originalConfig,
      {fontScale: originalConfig.fontScale * scale / 100});
    if (view.get('_systemAccessibilityConfig') !== appliedConfig) view.set('_systemAccessibilityConfig', appliedConfig);
    view.drawZoom();
  };
  const style = document.createElement('style');
  style.id = 'morphe-font-scale';
  document.head.appendChild(style);
  const update = () => {
    const controls = `#morphe-font-scale-setting { margin-top: 1.25em; padding-top: 1em; border-top: 1px solid var(--ui-page-color-border); }
#morphe-font-scale-setting label { display: block; font-weight: 600; margin-bottom: .5em; }
#morphe-font-scale-setting select { font: inherit; color: var(--ui-page-color-fg); background: var(--ui-input-color-bg); border: 1px solid var(--ui-input-color-border); border-radius: .25em; padding: .5em .75em; width: 100%; }
#morphe-font-scale-setting p { color: var(--ui-page-color-fg-subtle); margin-top: .5em; }`;
    if (style.textContent !== controls) style.textContent = controls;
    applyMetrics();
  };
  const mount = () => {
    if (!rootView || !originalConfig) applyMetrics();
    const section = document.getElementById('s-accessibility');
    if (!section || document.getElementById('morphe-font-scale-setting')) return;
    const row = document.createElement('div'); row.id = 'morphe-font-scale-setting';
    const label = document.createElement('label'); label.htmlFor = 'morphe-font-scale-select'; label.textContent = 'Custom font scale';
    const select = document.createElement('select'); select.id = 'morphe-font-scale-select';
    for (const value of choices) {
      const option = document.createElement('option'); option.value = String(value);
      option.textContent = value === 100 ? 'Default (100%)' : `${value}%`;
      option.selected = value === scale; select.appendChild(option);
    }
    const help = document.createElement('p'); help.textContent = 'Adjust the custom font relative to your system text size. Saved only on this device.';
    select.addEventListener('change', () => {
      const next = Number(select.value); if (!choices.includes(next)) return;
      scale = next;
      try { localStorage.setItem(key, String(scale)); } catch (_) {}
      update(); window.dispatchEvent(new Event('resize'));
    });
    row.append(label, select, help); section.appendChild(row);
  };
  update(); mount();
  // A single coalesced lookup keeps the control present through SPA settings navigation.
  let pending = false;
  new MutationObserver(() => {
    if (pending) return; pending = true;
    requestAnimationFrame(() => { pending = false; mount(); });
  }).observe(body, { childList: true, subtree: true });
})();
