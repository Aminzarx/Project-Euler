/* ==========================================================================
   یگانه و علی — دعوت‌نامه عروسی
   Vanilla JS + GSAP. بدون مرحله بیلد، بدون بک‌اند.
   ========================================================================== */
(() => {
  'use strict';

  /* ---------------------------------------------------------------------
     تنظیمات
     --------------------------------------------------------------------- */
  const WEDDING_DATE = new Date('2026-08-30T19:00:00+03:30'); // یکشنبه ۸ شهریور ۱۴۰۵، ساعت ۱۹:۰۰ (IRST)
  const WEDDING_END = new Date('2026-08-30T23:30:00+03:30');
  const VENUE = 'پیوندسرا خاطره، جاده باغرود';
  const CONTACT_PHONE = '۰۹۱۵۲۰۳۴۲۶۱';
  const INVITE_URL = window.location.href.split('#')[0];

  const $ = (sel, ctx = document) => ctx.querySelector(sel);
  const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  const FA_DIGITS = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
  const toFa = n => String(n).replace(/[0-9]/g, d => FA_DIGITS[d]);

  /* ---------------------------------------------------------------------
     پیام کوتاه (Toast)
     --------------------------------------------------------------------- */
  let toastTimer;
  function showToast(message) {
    const toast = $('#toast');
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add('is-visible');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('is-visible'), 2600);
  }

  /* ---------------------------------------------------------------------
     صفحه بارگذاری
     --------------------------------------------------------------------- */
  function initLoader() {
    const loader = $('#loader');
    if (!loader) return;
    const hide = () => loader.classList.add('is-hidden');
    window.addEventListener('load', () => setTimeout(hide, 900));
    setTimeout(hide, 3200); // پشتیبان ایمنی
  }

  /* ---------------------------------------------------------------------
     تغییر پوسته (ذخیره‌شده)
     --------------------------------------------------------------------- */
  function initTheme() {
    const root = document.documentElement;
    const btn = $('#themeToggle');
    const stored = localStorage.getItem('wedding-theme');
    if (stored) root.setAttribute('data-theme', stored);

    btn.addEventListener('click', () => {
      const current = root.getAttribute('data-theme') === 'dark' ||
        (!root.hasAttribute('data-theme') && window.matchMedia('(prefers-color-scheme: dark)').matches)
        ? 'dark' : 'light';
      const next = current === 'dark' ? 'light' : 'dark';
      root.setAttribute('data-theme', next);
      localStorage.setItem('wedding-theme', next);
    });
  }

  /* ---------------------------------------------------------------------
     نوار پیشرفت اسکرول + بازگشت به بالا
     --------------------------------------------------------------------- */
  function initScrollChrome() {
    const bar = $('#scrollProgressBar');
    const backToTop = $('#backToTop');

    function onScroll() {
      const scrollTop = window.scrollY;
      const height = document.documentElement.scrollHeight - window.innerHeight;
      const pct = height > 0 ? (scrollTop / height) * 100 : 0;
      if (bar) bar.style.width = pct + '%';
      if (backToTop) backToTop.classList.toggle('is-visible', scrollTop > 700);
    }
    document.addEventListener('scroll', onScroll, { passive: true });
    onScroll();

    backToTop?.addEventListener('click', () => {
      window.scrollTo({ top: 0, behavior: prefersReducedMotion ? 'auto' : 'smooth' });
    });

    $('#scrollCue')?.addEventListener('click', () => {
      $('#details')?.scrollIntoView({ behavior: prefersReducedMotion ? 'auto' : 'smooth' });
    });
  }

  /* ---------------------------------------------------------------------
     هاله نرم دنبال‌کننده موس در هیرو (فقط دستگاه‌های دارای موس)
     --------------------------------------------------------------------- */
  function initHeroSpotlight() {
    const hero = $('#hero');
    const spotlight = $('#heroSpotlight');
    if (!hero || !spotlight) return;
    if (prefersReducedMotion || !window.matchMedia('(hover: hover)').matches) return;

    hero.addEventListener('mousemove', e => {
      const rect = hero.getBoundingClientRect();
      const x = ((e.clientX - rect.left) / rect.width) * 100;
      const y = ((e.clientY - rect.top) / rect.height) * 100;
      spotlight.style.setProperty('--mx', x + '%');
      spotlight.style.setProperty('--my', y + '%');
      spotlight.classList.add('is-active');
    });
    hero.addEventListener('mouseleave', () => spotlight.classList.remove('is-active'));
  }

  /* ---------------------------------------------------------------------
     ذرات طلایی شناور در پس‌زمینه هیرو
     --------------------------------------------------------------------- */
  function initParticles() {
    const canvas = $('#particles');
    if (!canvas || prefersReducedMotion) return;
    const ctx = canvas.getContext('2d');
    let w, h, particles, rafId;
    const COUNT = window.innerWidth < 700 ? 26 : 48;

    function resize() {
      w = canvas.width = canvas.offsetWidth * devicePixelRatio;
      h = canvas.height = canvas.offsetHeight * devicePixelRatio;
    }

    function createParticles() {
      particles = Array.from({ length: COUNT }, () => ({
        x: Math.random() * w,
        y: Math.random() * h,
        r: (Math.random() * 1.8 + 0.6) * devicePixelRatio,
        speedY: (Math.random() * 0.25 + 0.06) * devicePixelRatio,
        drift: (Math.random() - 0.5) * 0.18 * devicePixelRatio,
        alpha: Math.random() * 0.5 + 0.15,
        twinkle: Math.random() * Math.PI * 2,
      }));
    }

    function tick() {
      ctx.clearRect(0, 0, w, h);
      particles.forEach(p => {
        p.y -= p.speedY;
        p.x += p.drift;
        p.twinkle += 0.02;
        if (p.y < -10) { p.y = h + 10; p.x = Math.random() * w; }
        const a = p.alpha * (0.6 + 0.4 * Math.sin(p.twinkle));
        ctx.beginPath();
        ctx.fillStyle = `rgba(217, 179, 130, ${a})`;
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2);
        ctx.fill();
      });
      rafId = requestAnimationFrame(tick);
    }

    resize();
    createParticles();
    tick();

    let resizeTimer;
    window.addEventListener('resize', () => {
      clearTimeout(resizeTimer);
      resizeTimer = setTimeout(() => { resize(); createParticles(); }, 200);
    });

    document.addEventListener('visibilitychange', () => {
      if (document.hidden) cancelAnimationFrame(rafId);
      else tick();
    });
  }

  /* ---------------------------------------------------------------------
     انیمیشن‌های GSAP (با پشتیبان IntersectionObserver)
     --------------------------------------------------------------------- */
  function initFallbackReveal() {
    const heroEls = $$('.hero .reveal-line, .reveal-name');
    heroEls.forEach((el, i) => {
      setTimeout(() => el.classList.add('is-in'), prefersReducedMotion ? 0 : 250 + i * 130);
    });

    const rest = $$('.reveal-up, .reveal-side');
    if (prefersReducedMotion || typeof IntersectionObserver === 'undefined') {
      rest.forEach(el => el.classList.add('is-in'));
      return;
    }
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-in');
          observer.unobserve(entry.target);
        }
      });
    }, { threshold: 0.15, rootMargin: '0px 0px -60px 0px' });
    rest.forEach(el => observer.observe(el));
  }

  function initAnimations() {
    if (typeof gsap === 'undefined' || typeof ScrollTrigger === 'undefined') {
      initFallbackReveal();
      return;
    }
    gsap.registerPlugin(ScrollTrigger);
    const ease = 'power3.out';

    const tl = gsap.timeline({ delay: prefersReducedMotion ? 0 : 1.1 });
    tl.to('.reveal-name', {
      opacity: 1, y: 0, duration: 1.1, stagger: 0.15, ease,
      from: { opacity: 0, y: 40 },
    }, 0);
    tl.fromTo('.hero .reveal-line', { opacity: 0, y: 18 }, { opacity: 1, y: 0, duration: 0.9, stagger: 0.18, ease }, 0.5);

    gsap.set('.reveal-name', { y: 40 });
    gsap.set('.hero .reveal-line', { y: 18 });

    $$('.reveal-up').forEach(el => {
      gsap.fromTo(el, { opacity: 0, y: 34 }, {
        opacity: 1, y: 0, duration: 1, ease,
        scrollTrigger: { trigger: el, start: 'top 88%' },
      });
    });

    $$('.reveal-side').forEach((el, i) => {
      gsap.fromTo(el, { opacity: 0, x: i % 2 === 0 ? -30 : 30 }, {
        opacity: 1, x: 0, duration: 1, ease,
        scrollTrigger: { trigger: el, start: 'top 88%' },
      });
    });

    gsap.to('.hero-glow', {
      yPercent: 20, ease: 'none',
      scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom top', scrub: true },
    });
  }

  /* ---------------------------------------------------------------------
     شمارش معکوس
     --------------------------------------------------------------------- */
  function initCountdown() {
    const els = {
      d: $('#cd-days'), h: $('#cd-hours'), m: $('#cd-mins'), s: $('#cd-secs'),
    };
    if (!els.d) return;
    const pad = n => toFa(String(Math.max(0, n)).padStart(2, '0'));

    function tick() {
      const diff = WEDDING_DATE.getTime() - Date.now();
      if (diff <= 0) {
        els.d.textContent = els.h.textContent = els.m.textContent = els.s.textContent = toFa('00');
        clearInterval(timer);
        return;
      }
      const days = Math.floor(diff / 86400000);
      const hours = Math.floor((diff % 86400000) / 3600000);
      const mins = Math.floor((diff % 3600000) / 60000);
      const secs = Math.floor((diff % 60000) / 1000);
      els.d.textContent = pad(days);
      els.h.textContent = pad(hours);
      els.m.textContent = pad(mins);
      els.s.textContent = pad(secs);
    }
    tick();
    const timer = setInterval(tick, 1000);
  }

  /* ---------------------------------------------------------------------
     فرم اعلام حضور (RSVP)
     --------------------------------------------------------------------- */
  function initRsvp() {
    const form = $('#rsvpForm');
    if (!form) return;

    const validators = {
      guestName: v => v.trim().length >= 2 || 'لطفاً نام و نام خانوادگی خود را وارد کنید.',
      guestPhone: v => /^[0-9۰-۹+()\-.\s]{7,}$/.test(v.trim()) || 'لطفاً یک شماره تماس معتبر وارد کنید.',
      guestCount: v => !!v || 'لطفاً تعداد مهمانان را انتخاب کنید.',
      attendance: v => !!v || 'لطفاً وضعیت حضور خود را مشخص کنید.',
    };

    function fieldValue(name) {
      const field = form.elements[name];
      if (field instanceof RadioNodeList) {
        return Array.from(field).find(r => r.checked)?.value || '';
      }
      return field ? field.value : '';
    }

    function validateField(name) {
      const value = fieldValue(name);
      const result = validators[name](value);
      const row = form.querySelector(`[name="${name}"]`).closest('.form-row');
      const errorEl = form.querySelector(`[data-error-for="${name}"]`);
      if (result === true) {
        row.classList.remove('invalid');
        errorEl.textContent = '';
        return true;
      }
      row.classList.add('invalid');
      errorEl.textContent = result;
      return false;
    }

    Object.keys(validators).forEach(name => {
      form.querySelectorAll(`[name="${name}"]`).forEach(el => {
        el.addEventListener('blur', () => validateField(name));
        el.addEventListener('change', () => validateField(name));
      });
    });

    form.addEventListener('submit', e => {
      e.preventDefault();
      const allValid = Object.keys(validators).map(validateField).every(Boolean);
      if (!allValid) {
        form.querySelector('.invalid input, .invalid select')?.focus();
        return;
      }

      form.classList.add('is-submitting');
      setTimeout(() => {
        form.classList.remove('is-submitting');
        form.classList.add('is-success');
        launchConfetti();
        showToast('پاسخ شما با موفقیت ثبت شد');
      }, 1100);
    });
  }

  /* ---------------------------------------------------------------------
     کانفتی (کانواس، بدون وابستگی خارجی)
     --------------------------------------------------------------------- */
  function launchConfetti() {
    if (prefersReducedMotion) return;
    const canvas = $('#confettiCanvas');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    canvas.width = window.innerWidth;
    canvas.height = window.innerHeight;

    const colors = ['#d9b382', '#c98f7a', '#ece3d4', '#45594c', '#b8894f'];
    const pieces = Array.from({ length: 140 }, () => ({
      x: canvas.width / 2 + (Math.random() - 0.5) * 200,
      y: canvas.height * 0.35,
      vx: (Math.random() - 0.5) * 10,
      vy: Math.random() * -10 - 4,
      size: Math.random() * 7 + 4,
      color: colors[Math.floor(Math.random() * colors.length)],
      rot: Math.random() * Math.PI,
      vr: (Math.random() - 0.5) * 0.3,
      gravity: 0.28 + Math.random() * 0.08,
    }));

    let frame = 0;
    function tick() {
      frame++;
      ctx.clearRect(0, 0, canvas.width, canvas.height);
      let alive = false;
      pieces.forEach(p => {
        p.vy += p.gravity;
        p.x += p.vx;
        p.y += p.vy;
        p.rot += p.vr;
        if (p.y < canvas.height + 40) alive = true;
        ctx.save();
        ctx.translate(p.x, p.y);
        ctx.rotate(p.rot);
        ctx.fillStyle = p.color;
        ctx.fillRect(-p.size / 2, -p.size / 4, p.size, p.size / 2);
        ctx.restore();
      });
      if (alive && frame < 260) requestAnimationFrame(tick);
      else ctx.clearRect(0, 0, canvas.width, canvas.height);
    }
    tick();
  }

  /* ---------------------------------------------------------------------
     دکمه پخش موسیقی (پیش‌فرض خاموش، ترجیح کاربر ذخیره می‌شود)
     --------------------------------------------------------------------- */
  function initAudio() {
    const btn = $('#audioToggle');
    const audio = $('#bgAudio');
    if (!btn || !audio) return;
    audio.volume = 0.35;

    const wanted = localStorage.getItem('wedding-audio') === 'on';
    if (wanted) {
      btn.classList.add('is-playing');
      btn.setAttribute('aria-label', 'توقف موسیقی');
    }

    btn.addEventListener('click', () => {
      if (audio.paused) {
        audio.play().catch(() => {
          showToast('پخش موسیقی امکان‌پذیر نشد');
        });
        btn.classList.add('is-playing');
        btn.setAttribute('aria-label', 'توقف موسیقی');
        localStorage.setItem('wedding-audio', 'on');
      } else {
        audio.pause();
        btn.classList.remove('is-playing');
        btn.setAttribute('aria-label', 'پخش موسیقی');
        localStorage.setItem('wedding-audio', 'off');
      }
    });
  }

  /* ---------------------------------------------------------------------
     اشتراک‌گذاری + کپی لینک
     --------------------------------------------------------------------- */
  function initShare() {
    const shareData = {
      title: 'یگانه و علی — ۸ شهریور ۱۴۰۵',
      text: 'دعوت به جشن عروسی ما',
      url: INVITE_URL,
    };

    $('#shareToggle')?.addEventListener('click', async () => {
      if (navigator.share) {
        try { await navigator.share(shareData); } catch (_) { /* لغو شده توسط کاربر */ }
      } else {
        copyToClipboard(INVITE_URL);
        showToast('لینک دعوت‌نامه کپی شد');
      }
    });

    $('#copyLink')?.addEventListener('click', () => {
      copyToClipboard(INVITE_URL);
      showToast('لینک دعوت‌نامه کپی شد');
    });
  }

  function copyToClipboard(text) {
    if (navigator.clipboard) {
      navigator.clipboard.writeText(text).catch(() => fallbackCopy(text));
    } else {
      fallbackCopy(text);
    }
  }
  function fallbackCopy(text) {
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.select();
    try { document.execCommand('copy'); } catch (_) { /* noop */ }
    document.body.removeChild(ta);
  }

  /* ---------------------------------------------------------------------
     افزودن به تقویم (دانلود .ics) با یادآوری ۳ روز قبل
     --------------------------------------------------------------------- */
  function initCalendar() {
    $('#addToCalendar')?.addEventListener('click', () => {
      const fmt = d => d.toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z';
      const ics = [
        'BEGIN:VCALENDAR',
        'VERSION:2.0',
        'PRODID:-//Yeganeh & Ali Wedding//FA',
        'BEGIN:VEVENT',
        `UID:${Date.now()}@yeganeh-ali-wedding`,
        `DTSTAMP:${fmt(new Date())}`,
        `DTSTART:${fmt(WEDDING_DATE)}`,
        `DTEND:${fmt(WEDDING_END)}`,
        'SUMMARY:جشن عروسی یگانه و علی',
        `LOCATION:${VENUE}`,
        `DESCRIPTION:جشن عروسی یگانه و علی، ساعت ۱۹:۰۰. در صورت عدم امکان حضور لطفاً حداقل ۳ روز قبل با شماره ${CONTACT_PHONE} تماس بگیرید.`,
        'BEGIN:VALARM',
        'TRIGGER:-P3D',
        'ACTION:DISPLAY',
        'DESCRIPTION:۳ روز تا جشن عروسی یگانه و علی باقی مانده است.',
        'END:VALARM',
        'END:VEVENT',
        'END:VCALENDAR',
      ].join('\r\n');

      const blob = new Blob([ics], { type: 'text/calendar;charset=utf-8' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'yeganeh-ali-wedding.ics';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast('رویداد به همراه یادآوری در تقویم شما دانلود شد');
    });
  }

  /* ---------------------------------------------------------------------
     کد QR (دعوت‌نامه)
     --------------------------------------------------------------------- */
  function initQrCodes() {
    if (typeof QRCode === 'undefined') {
      $$('.qr-box').forEach(el => el.classList.add('is-empty'));
      return;
    }
    const opts = { width: 128, margin: 1, color: { dark: '#2b2620', light: '#ffffff' } };

    const inviteTarget = $('#inviteQr');
    if (inviteTarget) {
      QRCode.toCanvas(document.createElement('canvas'), INVITE_URL, opts, (err, canvas) => {
        if (!err) inviteTarget.appendChild(canvas);
      });
    }
  }

  /* ---------------------------------------------------------------------
     دانلود تصویر دعوت‌نامه (کارت یادگاری تولیدشده با کانواس)
     --------------------------------------------------------------------- */
  function initDownloadInvite() {
    $('#downloadInvite')?.addEventListener('click', async () => {
      if (document.fonts && document.fonts.ready) {
        try { await document.fonts.ready; } catch (_) { /* ادامه با فونت پیش‌فرض */ }
      }

      const w = 1080, h = 1350;
      const canvas = document.createElement('canvas');
      canvas.width = w; canvas.height = h;
      const ctx = canvas.getContext('2d');
      ctx.direction = 'rtl';
      ctx.textAlign = 'center';

      const grad = ctx.createLinearGradient(0, 0, 0, h);
      grad.addColorStop(0, '#f7f2ea');
      grad.addColorStop(1, '#ece3d4');
      ctx.fillStyle = grad;
      ctx.fillRect(0, 0, w, h);

      ctx.strokeStyle = '#b8894f';
      ctx.lineWidth = 3;
      ctx.strokeRect(48, 48, w - 96, h - 96);
      ctx.lineWidth = 1;
      ctx.strokeRect(64, 64, w - 128, h - 128);

      ctx.fillStyle = '#b8894f';
      ctx.font = '500 30px Vazirmatn, sans-serif';
      ctx.fillText('دعوت به جشن عروسی', w / 2, 330);

      ctx.fillStyle = '#2b2620';
      ctx.font = '700 120px "Noto Nastaliq Urdu", serif';
      ctx.fillText('یگانه و علی', w / 2, 500);

      ctx.strokeStyle = '#b8894f';
      ctx.beginPath();
      ctx.moveTo(w / 2 - 90, 545);
      ctx.lineTo(w / 2 + 90, 545);
      ctx.stroke();

      ctx.fillStyle = '#6f6759';
      ctx.font = '400 36px Vazirmatn, sans-serif';
      ctx.fillText('یکشنبه ۸ شهریور ۱۴۰۵، ساعت ۱۹:۰۰', w / 2, 630);
      ctx.fillText('پیوندسرا خاطره، جاده باغرود', w / 2, 685);

      ctx.fillStyle = '#2b2620';
      ctx.font = '400 34px "Noto Nastaliq Urdu", serif';
      wrapText(ctx, '«دو دل که به‌هم می‌رسند، زیباترین فصل زندگی از همان‌جا آغاز می‌شود.»', w / 2, 840, 760, 58);

      ctx.fillStyle = '#b8894f';
      ctx.font = '700 32px "Noto Nastaliq Urdu", serif';
      ctx.fillText('ی ع', w / 2, h - 120);

      const link = document.createElement('a');
      link.download = 'yeganeh-ali-invitation.png';
      link.href = canvas.toDataURL('image/png');
      link.click();
      showToast('تصویر دعوت‌نامه دانلود شد');
    });
  }

  function wrapText(ctx, text, x, y, maxWidth, lineHeight) {
    const words = text.split(' ');
    let line = '';
    let lines = [];
    words.forEach(word => {
      const test = line + word + ' ';
      if (ctx.measureText(test).width > maxWidth && line) {
        lines.push(line);
        line = word + ' ';
      } else {
        line = test;
      }
    });
    lines.push(line);
    const startY = y - ((lines.length - 1) * lineHeight) / 2;
    lines.forEach((l, i) => ctx.fillText(l.trim(), x, startY + i * lineHeight));
  }

  /* ---------------------------------------------------------------------
     اجرا
     --------------------------------------------------------------------- */
  document.addEventListener('DOMContentLoaded', () => {
    initLoader();
    initTheme();
    initScrollChrome();
    initParticles();
    initHeroSpotlight();
    initCountdown();
    initRsvp();
    initAudio();
    initShare();
    initCalendar();
    initQrCodes();
    initDownloadInvite();
    initAnimations();
  });
})();
