// Custom Attributes：CurseForge 创建项目页自动填表脚本（由 create_project.py 生成）
// 用法：作者后台界面切英文，打开「Create A Project」，F12 粘贴本脚本回车
// 逐步填写并点 Next、确认头像裁剪框；autoCreate 为 true 时连创建按钮一起点，否则停在最后一步
(async () => {
  const DATA = {"name": "Custom Attributes", "summary": "Tune your own attributes in game, with the editable range defined by the server.", "license": "MIT", "dropdowns": [["Class", "Mods"], ["Main category", "Utility & QoL"], ["Additional categories", "Server Utility"]], "distribution": "Allow distribution to 3rd party", "allowComments": true, "projectPages": true, "description": "Custom Attributes lets you view and tune **your own** attributes in game, while the editable range is decided by the server through a range expression.\n\nTypical use: you stacked a pile of buffs and now you are too fast to steer or jump too high for parkour — instead of tearing gear off, just lower the value inside the allowed range.\n\n## Features\n\n- Open the screen with the `K` key (rebindable) or the `/customattributes` command.\n- One row per attribute: name, slider, exact value field, reset.\n- Drag the slider for quick changes, release to submit; type a value and press Enter to submit; Esc discards the edit.\n- **Reset** restores the base value of that attribute — equipment and potion modifiers stay untouched.\n- Only attributes the server allows are listed; blacklisted ones and invalid ranges never show up.\n\n## Server configuration\n\n`config/customattributes-server.toml` (instance level, not per world)\n\n- `list_mode`: `WHITELIST` (default) or `BLACKLIST`, with 20 common attributes shipped in the whitelist.\n- `default_range`: the range applied to attributes without their own entry, default `[min, current]`.\n- `[ranges] entries`: one line per attribute, e.g. `minecraft:generic.movement_speed=[0, current]`.\n- `sync_interval_ticks`: how often out-of-range values are clamped back.\n\n## Range expressions\n\nFormat is `[lower, upper]`; both sides are expressions supporting `+ - * /`, parentheses and unary minus. Wildcards (case-insensitive): `current`, `base`, `default`, `min`, `max`, `inf` — Chinese aliases are accepted too.\n\nThe lower bound must evaluate to no more than the upper bound, otherwise the attribute is disabled instead of being swapped automatically. That rule is what stops players from erasing their own debuffs. A collapsed range (lower equals upper) keeps the row on screen with only **Reset** usable.\n\n## Behaviour\n\n- What you set is a locked final value; the server keeps it there no matter what gear you put on later.\n- `current` is the final value *without* this mod's own adjustment, so it never shrinks as you tune down.\n- Adjustments use a dedicated modifier and are stored in player data (they survive death); base values are never touched.\n- Values outside the range are clamped back on every sync tick, even with the screen closed.\n- Server side or single player only; nothing is sent to other clients.\n\n---\n\n在游戏内查看并调整**自己的**属性，可调区间由服务端用范围表达式规定。\n\n典型用途：身上叠了一堆增益，速度太快晕得难受、跳跃太高影响跑酷——不用拆装备，直接在区间内把数值调低。\n\n## 功能\n\n- 默认按键 `K` 打开界面（可在「控制 → 按键绑定」里改），也可以输入 `/customattributes`。\n- 每一行是一个属性：名称、滑块、数值输入框、重置。\n- 拖滑块快速调整，松手即提交；输入框里填精确值后回车提交；按 Esc 放弃这次编辑。\n- **重置**把该属性的基础值恢复为默认值，装备与药水的修饰不受影响。\n- 界面里只会出现服务端允许调整的属性，黑名单与无效区间不会显示。\n\n## 服务端配置\n\n路径 `config/customattributes-server.toml`（实例级，不随存档变化）\n\n- `list_mode`：`WHITELIST`（默认）或 `BLACKLIST`，白名单预置了 20 个常见属性。\n- `default_range`：未单独配置的属性用它，默认 `[min, current]`。\n- `[ranges] entries`：每个属性一行，例如 `minecraft:generic.movement_speed=[0, current]`。\n- `sync_interval_ticks`：越界数值被夹回区间的检查间隔。\n\n## 范围表达式\n\n格式固定为 `[下界, 上界]`，两端都是表达式，支持 `+ - * /`、括号与负号。通配符（大小写不敏感）：`current`、`base`、`default`、`min`、`max`、`inf`，也可以用中文写「当前值 / 基础值 / 默认值 / 最小值 / 最大值 / 无穷」。\n\n下界求值后必须不大于上界，否则该属性直接禁用而不是自动交换两端——这条规则用来防止玩家把自身的减益抹掉。区间塌缩成一个点时，该行仍保留「重置」。\n\n## 行为约定\n\n- 玩家设定的是「最终值等于多少」，服务端会持续维持这个数，之后穿装备、喝药水都不会把它带走。\n- `current` 取的是「没被本模组调整过」的最终值，所以往下调不会让它越缩越小。\n- 调整通过本模组专属 modifier 表达，目标值记在玩家存档里（死亡重生后保留），基础值始终原样。\n- 越界的数每个同步间隔都会被夹回区间，未打开界面也一样生效。\n- 只发给本人，属性数值不会同步给其他客户端。\n", "iconBase64": "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", "autoCreate": true, "singleStep": false};
  const report = [];

  // 判断元素是否可见
  const visible = (el) => !!el && el.getClientRects().length > 0 && getComputedStyle(el).visibility !== 'hidden';

  // 收集字段的描述文本（label、aria、placeholder、name、所在容器标题）
  const describe = (el) => {
    const parts = [el.name, el.id, el.placeholder, el.getAttribute('aria-label')];
    if (el.id) {
      const label = document.querySelector(`label[for="${CSS.escape(el.id)}"]`);
      if (label) parts.push(label.textContent);
    }
    const wrapLabel = el.closest('label');
    if (wrapLabel) parts.push(wrapLabel.textContent);
    let node = el.parentElement;
    for (let i = 0; i < 4 && node; i++, node = node.parentElement) {
      const title = node.querySelector('label, h1, h2, h3, h4, legend, .label, [class*="label"], [class*="title"]');
      if (title && !title.contains(el)) {
        parts.push(title.textContent);
        break;
      }
    }
    return parts.filter(Boolean).join(' ').toLowerCase();
  };

  // 按关键词查找字段
  const findField = (keys, excludes, selector) => {
    const list = [...document.querySelectorAll(selector)].filter(visible);
    return list.find((el) => {
      const text = describe(el);
      return keys.some((k) => text.includes(k)) && !excludes.some((k) => text.includes(k));
    });
  };

  // 以 React/Vue 可识别的方式写入输入框
  const setValue = (el, value) => {
    const proto = el.tagName === 'TEXTAREA' ? HTMLTextAreaElement.prototype : el.tagName === 'SELECT' ? HTMLSelectElement.prototype : HTMLInputElement.prototype;
    Object.getOwnPropertyDescriptor(proto, 'value').set.call(el, value);
    el.dispatchEvent(new Event('input', { bubbles: true }));
    el.dispatchEvent(new Event('change', { bubbles: true }));
    el.dispatchEvent(new Event('blur', { bubbles: true }));
  };

  const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
  const mark = (field, ok, detail) => report.push({ 字段: field, 结果: ok ? '已填写' : '未找到，需手动', 说明: detail || '' });

  // 简易 Markdown 转 HTML（用于富文本编辑器）
  const mdToHtml = (md) => {
    const esc = (s) => s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    const inline = (s) => esc(s)
      .replace(/`([^`]+)`/g, '<code>$1</code>')
      .replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
      .replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2">$1</a>');
    const lines = md.split(/\r?\n/);
    const out = [];
    let i = 0;
    while (i < lines.length) {
      const line = lines[i];
      if (/^\s*$/.test(line)) { i++; continue; }
      const h = line.match(/^(#{1,6})\s+(.*)$/);
      if (h) { out.push(`<h${h[1].length}>${inline(h[2])}</h${h[1].length}>`); i++; continue; }
      if (/^---+\s*$/.test(line)) { out.push('<hr>'); i++; continue; }
      if (/^>/.test(line)) {
        const quote = [];
        while (i < lines.length && /^>/.test(lines[i])) { quote.push(lines[i].replace(/^>\s?/, '')); i++; }
        out.push('<blockquote>' + quote.filter((q) => q.trim()).map((q) => `<p>${inline(q)}</p>`).join('') + '</blockquote>');
        continue;
      }
      if (/^\|/.test(line)) {
        const rows = [];
        while (i < lines.length && /^\|/.test(lines[i])) { rows.push(lines[i]); i++; }
        const cells = (r) => r.replace(/^\||\|$/g, '').split('|').map((c) => inline(c.trim()));
        const head = cells(rows[0]);
        const body = rows.slice(2).map(cells);
        out.push('<table><thead><tr>' + head.map((c) => `<th>${c}</th>`).join('') + '</tr></thead><tbody>' +
          body.map((r) => '<tr>' + r.map((c) => `<td>${c}</td>`).join('') + '</tr>').join('') + '</tbody></table>');
        continue;
      }
      if (/^\s*[*-]\s+/.test(line)) {
        const items = [];
        while (i < lines.length && /^\s*[*-]\s+/.test(lines[i])) { items.push(lines[i].replace(/^\s*[*-]\s+/, '')); i++; }
        out.push('<ul>' + items.map((t) => `<li>${inline(t)}</li>`).join('') + '</ul>');
        continue;
      }
      const para = [];
      while (i < lines.length && !/^\s*$/.test(lines[i]) && !/^(#{1,6}\s|\||\s*[*-]\s|---|>)/.test(lines[i])) { para.push(lines[i]); i++; }
      out.push(`<p>${inline(para.join(' '))}</p>`);
    }
    return out.join('\n');
  };

  // 6. 下拉框：按标签找到下方最近的下拉触发器，点开后选择选项
  const fireClick = (el) => {
    for (const type of ['pointerdown', 'mousedown', 'pointerup', 'mouseup', 'click']) {
      el.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window }));
    }
  };
  const findLabel = (text) => [...document.querySelectorAll('label, span, div, p, h3, h4, legend')].filter(visible)
    .find((el) => norm(el.textContent) === norm(text) || norm(el.textContent) === norm(text) + ' ?');
  const findTrigger = (label) => {
    const lr = label.getBoundingClientRect();
    const candidates = [...document.querySelectorAll('select, [role="combobox"], [aria-haspopup], button, div, input')].filter(visible)
      .filter((el) => !el.contains(label) && !label.contains(el))
      .map((el) => ({ el, r: el.getBoundingClientRect() }))
      .filter(({ r }) => r.top >= lr.bottom - 2 && r.top - lr.bottom < 90 && r.height >= 28 && r.height <= 70 && r.width >= 120 && r.left < lr.left + 60 && r.right > lr.left)
      .sort((a, b) => (a.r.top - b.r.top) || (a.r.width * a.r.height - b.r.width * b.r.height));
    return candidates.length ? candidates[0].el : null;
  };
  const findOption = (text, exclude) => [...document.querySelectorAll('[role="option"], li, label, span, div, button')].filter(visible)
    .filter((el) => !exclude.contains(el) && el.children.length <= 3 && norm(el.textContent) === norm(text))
    .sort((a, b) => a.getBoundingClientRect().height - b.getBoundingClientRect().height)[0];
  // 统一大小写、空白和弯引号，便于匹配 Tinker's 这类选项
  const norm = (t) => (t || '').replace(/[‘’ʼ]/g, "'").replace(/[*\s]+/g, ' ').trim().toLowerCase();
  // 优先找标签下方最近的 [role="combobox"]（2026-10-26 Waystone Horizons 实测可用）
  // 先沿祖先向上找只含一个下拉框的字段容器（License 步骤标签与下拉框之间隔着一段说明文字）
  const findCombo = (label) => {
    let scope = label.parentElement;
    for (let depth = 0; scope && depth < 6; depth++, scope = scope.parentElement) {
      const combos = [...scope.querySelectorAll('[role="combobox"]')].filter(visible);
      if (combos.length === 1) return combos[0];
      if (combos.length > 1) break;
    }
    const lr = label.getBoundingClientRect();
    return [...document.querySelectorAll('[role="combobox"]')].filter(visible)
      .map((el) => ({ el, r: el.getBoundingClientRect() }))
      .filter(({ r }) => r.top >= lr.top && r.top - lr.bottom < 260 && r.right > lr.left - 10 && r.left < lr.left + 60)
      .sort((a, b) => a.r.top - b.r.top)[0]?.el || null;
  };
  const comboText = (el) => norm(el.value || el.innerText || el.textContent);
  const waitOption = async (text, timeout = 3000, loose = false) => {
    const end = Date.now() + timeout;
    while (Date.now() < end) {
      const option = [...document.querySelectorAll('[role="option"]')].filter(visible).find((el) => norm(el.innerText || el.textContent) === norm(text) || (loose && norm(el.innerText || el.textContent).includes(norm(text))));
      if (option) return option;
      await sleep(150);
    }
    return null;
  };
  const pickCombo = async (labelText, optionText, combo, loose = false) => {
    const container = combo.closest('[class*="select"], [class*="Select"], [class*="dropdown"], [class*="Dropdown"]') || combo.parentElement;
    if (comboText(combo) === norm(optionText) || (container?.innerText || '').split('\n').some((t) => norm(t) === norm(optionText))) {
      mark(labelText, true, `${optionText}（已选）`);
      return true;
    }
    // MUI Select 只在 mousedown（button=0）时展开，click() 不会触发；Autocomplete 输入框也响应 mousedown
    const press = (el, type, extra = {}) => el.dispatchEvent(new MouseEvent(type, { bubbles: true, cancelable: true, view: window, button: 0, ...extra }));
    const key = (el, k) => el.dispatchEvent(new KeyboardEvent('keydown', { key: k, code: k, bubbles: true, cancelable: true }));
    combo.focus();
    press(combo, 'mousedown');
    await sleep(400);
    // Autocomplete 只输入撇号前的前缀过滤，避免直引号与弯引号不一致导致过滤为空
    const query = optionText.split(/['’]/)[0].trim();
    if (combo.tagName === 'INPUT' && !combo.readOnly) {
      setValue(combo, query);
      await sleep(500);
    }
    let option = await waitOption(optionText, 3000, loose);
    if (!option) {
      combo.focus();
      key(combo, 'ArrowDown');
      option = await waitOption(optionText, 1500, loose);
    }
    if (!option && combo.tagName === 'INPUT') {
      setValue(combo, '');
      press(combo, 'mousedown');
      key(combo, 'ArrowDown');
      option = await waitOption(optionText, 1500, loose);
    }
    if (!option) {
      const seen = [...document.querySelectorAll('[role="option"]')].filter(visible).map((el) => (el.innerText || el.textContent).trim()).slice(0, 12);
      mark(labelText, false, `没找到选项 ${optionText}；可见选项：${seen.join(' / ') || '无'}；控件：${combo.tagName}.${String(combo.className).split(' ').slice(0, 3).join('.')}`);
      key(document.activeElement || document.body, 'Escape');
      return true;
    }
    option.scrollIntoView({ block: 'nearest' });
    option.click();
    await sleep(600);
    // 多选菜单点完不会自动收起，用 Esc 关闭，避免挡住后面的下拉框
    const list = option.closest('[role="listbox"]');
    if (list && visible(list)) {
      key(combo, 'Escape');
      key(list, 'Escape');
      await sleep(300);
    }
    mark(labelText, true, optionText);
    return true;
  };
  const pickDropdown = async (labelText, optionText, loose = false) => {
    const label = findLabel(labelText);
    if (!label) { mark(labelText, false, '当前步骤没有该字段'); return; }
    const combo = findCombo(label);
    if (combo && await pickCombo(labelText, optionText, combo, loose)) return;
    const trigger = findTrigger(label);
    if (!trigger) { mark(labelText, false, '未找到下拉框'); return; }
    if (trigger.tagName === 'SELECT') {
      const option = [...trigger.options].find((o) => norm(o.text) === norm(optionText));
      if (option) { setValue(trigger, option.value); mark(labelText, true, optionText); } else mark(labelText, false, `没有选项 ${optionText}`);
      return;
    }
    if (norm(trigger.textContent) === norm(optionText)) { mark(labelText, true, `${optionText}（已选）`); return; }
    fireClick(trigger);
    trigger.focus && trigger.focus();
    await sleep(600);
    const option = findOption(optionText, label);
    if (option) {
      fireClick(option);
      mark(labelText, true, optionText);
      await sleep(800);
      document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
      await sleep(200);
    } else {
      mark(labelText, false, `下拉框已点开但没找到选项 ${optionText}`);
      fireClick(trigger);
    }
  };

  // 按向导步骤决定要填的字段；未知步骤全部尝试
  const STEP_FIELDS = {
    general: ['name', 'summary', 'dropdowns', 'comments', 'pages', 'icon'],
    description: ['description'],
    license: ['license'],
  };
  const stepOf = () => (location.hash.match(/create\/([a-z-]+)/i) || [])[1] || 'unknown';
  const buttonByText = (re) => [...document.querySelectorAll('button')].filter(visible).find((b) => re.test(norm(b.innerText || b.textContent)) && !b.disabled);
  // 按钮会随 React 重渲染短暂消失或处于 disabled，轮询等待而不是只查一次
  const waitButton = async (re, tries = 20) => {
    for (let i = 0; i < tries; i++) {
      const btn = buttonByText(re);
      if (btn) return btn;
      await sleep(300);
    }
    return null;
  };

  // 头像裁剪框：等待对话框出现后点确认按钮（图标已是正方形，默认裁剪即整张图）
  const confirmCrop = async () => {
    let dialog = null;
    for (let i = 0; i < 40 && !dialog; i++) {
      dialog = [...document.querySelectorAll('[role="dialog"], .MuiDialog-root, .MuiModal-root')].filter(visible)
        .find((el) => el.querySelector('img, canvas') && !el.closest('.MuiDrawer-root'));
      if (!dialog) await sleep(150);
    }
    if (!dialog) return '没有弹出裁剪框';
    await sleep(500);
    const buttons = [...dialog.querySelectorAll('button')].filter(visible).filter((b) => !b.disabled);
    const ok = buttons.find((b) => /^(save|apply|crop|confirm|done|ok|upload|set|use|submit)\b/.test(norm(b.innerText || b.textContent)));
    if (!ok) return `裁剪框里没找到确认按钮（按钮：${buttons.map((b) => (b.innerText || '').trim()).join(' / ') || '无'}）`;
    ok.click();
    for (let i = 0; i < 40 && dialog.isConnected && visible(dialog); i++) await sleep(150);
    return dialog.isConnected && visible(dialog) ? `已点击 ${ok.innerText.trim()}，但裁剪框没有关闭` : '';
  };

  for (let round = 0; round < 5; round++) {
    report.length = 0;
    const step = stepOf();

    // 0. 选游戏步骤：不先选 Minecraft，后面每一页都找不到字段
    if (step === 'choose-game') {
      const card = [...document.querySelectorAll('img[alt="Minecraft"], [alt="Minecraft"]')]
        .map((el) => el.closest('button, [role="button"], a, div'))
        .filter(visible)
        .sort((a, b) => a.getBoundingClientRect().height - b.getBoundingClientRect().height)[0];
      if (card) {
        card.click();
        await sleep(1500);
        console.log('已选择 Minecraft');
        continue;
      }
      console.log('%c当前在 Choose Game 步骤，没找到 Minecraft 卡片，请手动选择后重新运行', 'color:#c92a2a;font-weight:bold');
      break;
    }

    const want = (field) => !STEP_FIELDS[step] || STEP_FIELDS[step].includes(field);
    let nameField = null;
    let alreadyFilled = false;
    let summaryField = null;
    let blocked = '';

    // 1. 项目名称
    if (want('name')) {
      nameField = findField(['project name', 'name'], ['slug', 'summary', 'search', 'user'], 'input[type="text"], input:not([type])');
      alreadyFilled = !!nameField && nameField.value === DATA.name;
      if (nameField) { setValue(nameField, DATA.name); mark('项目名称', true, DATA.name); } else mark('项目名称', false);
    }

    // 2. 简介
    if (want('summary')) {
      summaryField = findField(['summary', 'short description'], ['search'], 'textarea, input[type="text"], input:not([type])');
      if (summaryField) { setValue(summaryField, DATA.summary); mark('简介 Summary', true); } else mark('简介 Summary', false);
    }

    // 3. 下拉框
    if (want('dropdowns')) {
      for (const [labelText, optionText] of DATA.dropdowns) {
        await pickDropdown(labelText, optionText);
      }
    }

    // 4. 允许评论复选框
    if (want('comments') && DATA.allowComments !== false) {
      const box = [...document.querySelectorAll('input[type="checkbox"], [role="checkbox"]')]
        .find((el) => /allow comments/i.test(el.closest('label')?.innerText || el.parentElement?.innerText || el.parentElement?.parentElement?.innerText || el.getAttribute('aria-label') || ''));
      const textNode = [...document.querySelectorAll('label, span, p, div')].filter(visible).find((el) => norm(el.textContent) === 'allow comments');
      const checked = (el) => el && (el.checked === true || el.getAttribute('aria-checked') === 'true');
      if (box || textNode) {
        if (!checked(box)) {
          (box && visible(box) ? box : (box?.closest('label') || textNode)).click();
          await sleep(300);
        }
        mark('Allow Comments', !box || checked(box), box ? (checked(box) ? '已勾选' : '点击后仍未勾选，请手动勾选') : '已点击文字，请确认是否勾上');
      } else mark('Allow Comments', false, '当前步骤没有该选项');
    }

    // 4.5 项目页面开关：向导里如果没这个选项，建好后到设置里补
    if (want('pages') && DATA.projectPages) {
      const pageBox = [...document.querySelectorAll('input[type="checkbox"], [role="checkbox"]')]
        .find((el) => /project pages/i.test(el.closest('label')?.innerText || el.parentElement?.innerText || el.parentElement?.parentElement?.innerText || el.getAttribute('aria-label') || ''));
      const pageText = [...document.querySelectorAll('label, span, p, div')].filter(visible).find((el) => norm(el.textContent) === 'project pages');
      const isOn = (el) => el && (el.checked === true || el.getAttribute('aria-checked') === 'true');
      if (pageBox || pageText) {
        if (!isOn(pageBox)) {
          (pageBox && visible(pageBox) ? pageBox : (pageBox?.closest('label') || pageText)).click();
          await sleep(300);
        }
        mark('Project Pages', !pageBox || isOn(pageBox),
          pageBox ? (isOn(pageBox) ? '已启用' : '点击后仍未勾选，请手动勾选') : '已点击文字，请确认是否勾上');
      } else mark('Project Pages', false, '当前步骤没有该选项，建好后到项目设置里启用');
    }

    // 5. 项目图标：写入文件输入框后自动确认裁剪框（名称已是本项目时视为已上传过）
    if (want('icon') && !DATA.iconBase64) mark('项目图标', false, '脚本未带图标，请手动上传 curseforge/icon.png');
    if (want('icon') && DATA.iconBase64) {
      const fileInput = [...document.querySelectorAll('input[type="file"]')].find((f) => !f.accept || /image|png/i.test(f.accept));
      if (fileInput && alreadyFilled) {
        mark('项目图标', true, '已上传过，跳过');
      } else if (fileInput) {
        const bytes = Uint8Array.from(atob(DATA.iconBase64), (c) => c.charCodeAt(0));
        const file = new File([bytes], 'cf-project-icon.png', { type: 'image/png' });
        const dt = new DataTransfer();
        dt.items.add(file);
        fileInput.files = dt.files;
        fileInput.dispatchEvent(new Event('input', { bubbles: true }));
        fileInput.dispatchEvent(new Event('change', { bubbles: true }));
        const cropError = await confirmCrop();
        if (cropError && cropError !== '没有弹出裁剪框') {
          mark('项目图标', false, cropError);
          blocked = '请手动处理头像裁剪框后再运行脚本';
        } else mark('项目图标', true, cropError || '已上传并确认裁剪');
      } else mark('项目图标', false, '请手动上传 curseforge/icon.png');
    }

    // 6. 描述：先切到 Markdown（若页面提供），优先 Markdown 文本框，否则写入富文本编辑器
    if (want('description')) {
      await sleep(800);
      let markdownMode = false;
      const formatSelect = [...document.querySelectorAll('select')].filter(visible).find((s) => [...s.options].some((o) => /markdown/i.test(o.text)));
      if (formatSelect) {
        const option = [...formatSelect.options].find((o) => /markdown/i.test(o.text));
        setValue(formatSelect, option.value);
        markdownMode = true;
        await sleep(500);
      } else {
        const toggle = [...document.querySelectorAll('button, label, [role="tab"], [role="radio"], input[type="radio"]')].filter(visible)
          .find((el) => /^\s*markdown\s*$/i.test(el.textContent || el.value || ''));
        if (toggle) { toggle.click(); markdownMode = true; await sleep(500); }
      }
      // 现网用 MUI 下拉做格式切换：combobox 显示 WYSIWYG，展开后有 Markdown 选项
      if (!markdownMode) {
        const combo = [...document.querySelectorAll('[role="combobox"]')].filter(visible)
          .find((el) => /wysiwyg|markdown|html/i.test(el.textContent || ''));
        if (combo && !/^\s*markdown\s*$/i.test(combo.textContent || '')) {
          fireClick(combo);
          await sleep(600);
          const option = [...document.querySelectorAll('[role="option"]')].filter(visible)
            .find((el) => /^\s*markdown\s*$/i.test(el.textContent || ''));
          if (option) {
            fireClick(option);
            markdownMode = true;
            await sleep(800);
          } else {
            document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
          }
        }
      }
      const descField = findField(['description'], ['summary', 'search'], 'textarea');
      const plainTextareas = [...document.querySelectorAll('textarea')].filter(visible).filter((t) => t !== summaryField);
      const targetTextarea = descField || (markdownMode || step === 'description' ? plainTextareas[0] : null);
      if (targetTextarea) {
        setValue(targetTextarea, DATA.description);
        mark('描述', true, 'Markdown 文本框');
      } else {
        let editor = [...document.querySelectorAll('[contenteditable="true"], .ProseMirror, .ql-editor')].filter(visible)[0];
        if (!editor) {
          const frame = [...document.querySelectorAll('iframe')].find((f) => { try { return f.contentDocument && f.contentDocument.body && f.contentDocument.body.isContentEditable; } catch (e) { return false; } });
          if (frame) editor = frame.contentDocument.body;
        }
        if (editor) {
          editor.focus();
          const doc = editor.ownerDocument;
          const sel = doc.getSelection();
          const range = doc.createRange();
          range.selectNodeContents(editor);
          sel.removeAllRanges();
          sel.addRange(range);
          const html = mdToHtml(DATA.description);
          if (!doc.execCommand('insertHTML', false, html)) editor.innerHTML = html;
          editor.dispatchEvent(new Event('input', { bubbles: true }));
          mark('描述', true, '富文本编辑器（请检查表格排版）');
        } else mark('描述', false, '没找到描述编辑器，请手动粘贴 description.md');
      }
    }

    // 7. 许可证：原生下拉框或 MUI 下拉框，选项文本包含目标即可（如 MIT License）
    if (want('license') && DATA.license) {
      const licenseText = DATA.license.toLowerCase();
      const licenseSelect = [...document.querySelectorAll('select')].filter(visible).find((s) => [...s.options].some((o) => o.text.toLowerCase().includes(licenseText)));
      if (licenseSelect) {
        const option = [...licenseSelect.options].find((o) => o.text.toLowerCase().includes(licenseText));
        setValue(licenseSelect, option.value);
        mark('许可证', true, option.text);
      } else {
        await pickDropdown(findLabel('Project License') ? 'Project License' : 'License', DATA.license, true);
      }
    }

    // 8. 第三方分发：默认允许
    if (want('license') && DATA.distribution) {
      await pickDropdown('Project distribution', DATA.distribution);
    }

    console.log(`%c步骤 ${step}`, 'font-weight:bold');
    console.table(report);
    const failed = report.filter((r) => r.结果 !== '已填写');
    if (blocked || failed.length) {
      console.log(`%c${blocked || '有字段未填上，已停在当前步骤；处理后可再次运行脚本'}`, 'color:#c92a2a;font-weight:bold');
      break;
    }
    // 9. 点 Next；按钮会随重渲染短暂消失或被禁用，先轮询等待
    const next = await waitButton(/^next$/);
    if (!next) {
      if (!DATA.autoCreate) {
        console.log('%c已到最后一步：请核对全部内容，确认无误后手动点击创建按钮。', 'color:#2b8a3e;font-weight:bold');
        break;
      }
      const create = await waitButton(/^(create|submit|create project|finish|done)$/, 10);
      if (!create) {
        const present = [...document.querySelectorAll('button')].filter(visible).map((b) => (b.innerText || '').trim()).filter(Boolean);
        const hasNext = present.some((t) => /^next$/i.test(t));
        console.log(`%c没找到可用的创建按钮（${hasNext ? '页面上仍有 Next，但处于禁用或未就绪状态' : '当前页面既无 Next 也无创建按钮'}）。可见按钮：${present.join(' / ')}`, 'color:#c92a2a;font-weight:bold');
        break;
      }
      console.log('%c正在自动创建项目…', 'color:#c92a2a;font-weight:bold');
      create.click();
      await sleep(2500);
      const projectId =
        (document.body.innerText.match(/Project\s*ID[^0-9]{0,40}(\d{4,})/i) || [])[1] || '';
      const slug = location.pathname.split('/').filter(Boolean).pop() || '';
      console.log('%c创建完成', 'color:#2b8a3e;font-weight:bold;font-size:14px');
      console.log({
        url: location.href,
        slug,
        projectId: projectId || '页面上没读到，请到项目设置里确认',
      });
      break;
    }
    const before = location.hash;
    await sleep(400);
    next.click();
    let moved = false;
    for (let i = 0; i < 60 && !moved; i++) { await sleep(150); moved = location.hash !== before; }
    if (!moved) {
      console.log('%c点击 Next 后页面没有前进，可能有必填项未通过校验，请查看页面提示。', 'color:#c92a2a;font-weight:bold');
      break;
    }
    await sleep(1200);
    // 单步模式：一次只推进一页，避免自动化工具把长任务误判成超时
    if (DATA.singleStep) {
      console.log('%c单步模式：当前页已完成，再次执行脚本继续', 'color:#1971c2;font-weight:bold');
      console.log({ step: stepOf(), hash: location.hash, report });
      return;
    }
  }
})();
