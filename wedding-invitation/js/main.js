/* ==========================================================================
   Elena & Daniel — Wedding Invitation
   Vanilla JS + GSAP. No build step, no backend.
   ========================================================================== */
(() => {
  'use strict';

  /* ---------------------------------------------------------------------
     Config
     --------------------------------------------------------------------- */
  const WEDDING_DATE = new Date('2026-11-14T17:00:00+01:00');
  const WEDDING_END = new Date('2026-11-15T00:30:00+01:00');
  const VENUE = 'Villa Serrano, Via del Lago 14, 22021, Lake Como, Italy';
  const INVITE_URL = window.location.href.split('#')[0];

  const $ = (sel, ctx = document) => ctx.querySelector(sel);
  const $$ = (sel, ctx = document) => Array.from(ctx.querySelectorAll(sel));
  const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* ---------------------------------------------------------------------
     Toast helper
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
     Loading screen
     --------------------------------------------------------------------- */
  function initLoader() {
    const loader = $('#loader');
    if (!loader) return;
    const hide = () => loader.classList.add('is-hidden');
    window.addEventListener('load', () => setTimeout(hide, 900));
    setTimeout(hide, 3200); // safety fallback
  }

  /* ---------------------------------------------------------------------
     Theme toggle (persisted)
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
     Scroll progress + back to top
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
      $('#story')?.scrollIntoView({ behavior: prefersReducedMotion ? 'auto' : 'smooth' });
    });
  }

  /* ---------------------------------------------------------------------
     Hero particles — soft floating gold dust on canvas
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
     GSAP reveal animations
     --------------------------------------------------------------------- */
  function initAnimations() {
    if (typeof gsap === 'undefined') {
      $$('.reveal-line, .reveal-name, .reveal-up, .reveal-side').forEach(el => el.style.opacity = 1);
      return;
    }
    gsap.registerPlugin(ScrollTrigger);
    const ease = 'power3.out';

    // Hero intro timeline
    const tl = gsap.timeline({ delay: prefersReducedMotion ? 0 : 1.1 });
    tl.to('.reveal-name', {
      opacity: 1, y: 0, duration: 1.1, stagger: 0.15, ease,
      from: { opacity: 0, y: 40 },
    }, 0);
    tl.fromTo('.hero .reveal-line', { opacity: 0, y: 18 }, { opacity: 1, y: 0, duration: 0.9, stagger: 0.18, ease }, 0.5);

    gsap.set('.reveal-name', { y: 40 });
    gsap.set('.hero .reveal-line', { y: 18 });

    // Scroll-triggered reveals
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

    // Parallax hero glow
    gsap.to('.hero-glow', {
      yPercent: 20, ease: 'none',
      scrollTrigger: { trigger: '.hero', start: 'top top', end: 'bottom top', scrub: true },
    });

    // Gallery item stagger
    gsap.utils.toArray('.gallery-item').forEach((el, i) => {
      gsap.fromTo(el, { opacity: 0, y: 24 }, {
        opacity: 1, y: 0, duration: 0.7, delay: (i % 4) * 0.06, ease,
        scrollTrigger: { trigger: el, start: 'top 92%' },
      });
    });
  }

  /* ---------------------------------------------------------------------
     Countdown
     --------------------------------------------------------------------- */
  function initCountdown() {
    const els = {
      d: $('#cd-days'), h: $('#cd-hours'), m: $('#cd-mins'), s: $('#cd-secs'),
    };
    if (!els.d) return;
    const pad = n => String(Math.max(0, n)).padStart(2, '0');

    function tick() {
      const diff = WEDDING_DATE.getTime() - Date.now();
      if (diff <= 0) {
        els.d.textContent = els.h.textContent = els.m.textContent = els.s.textContent = '00';
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
     Gallery — generated elegant placeholder frames + lightbox
     --------------------------------------------------------------------- */
  const GALLERY = [
    { caption: 'Florence, where it began', cls: 'tall', tone: 'linear-gradient(155deg,#d9b382,#8a6a45)' },
    { caption: 'The proposal, at sunset', cls: 'wide', tone: 'linear-gradient(155deg,#c98f7a,#8a5346)' },
    { caption: 'Villa Serrano gardens', cls: '', tone: 'linear-gradient(155deg,#45594c,#2f3f35)' },
    { caption: 'Lake Como mornings', cls: '', tone: 'linear-gradient(155deg,#8a8378,#5a5548)' },
    { caption: 'Engagement portraits', cls: 'tall', tone: 'linear-gradient(155deg,#ece3d4,#c9b48f)' },
    { caption: 'A quiet evening together', cls: '', tone: 'linear-gradient(155deg,#b8894f,#6f532c)' },
    { caption: 'Family gathering, 2025', cls: 'wide', tone: 'linear-gradient(155deg,#d9b382,#c98f7a)' },
    { caption: 'Details of the day to come', cls: '', tone: 'linear-gradient(155deg,#2f3f35,#45594c)' },
  ];

  function initGallery() {
    const grid = $('#galleryGrid');
    if (!grid) return;
    grid.innerHTML = GALLERY.map((g, i) => `
      <div class="gallery-item ${g.cls}" data-index="${i}" style="background-image:${g.tone}">
        <span class="gi-label">${g.caption}</span>
      </div>
    `).join('');

    const items = $$('.gallery-item', grid);
    const lightbox = $('#lightbox');
    const imgEl = $('#lightboxImg');
    const capEl = $('#lightboxCaption');
    let current = 0;

    function open(i) {
      current = i;
      render();
      lightbox.classList.add('is-open');
      lightbox.setAttribute('aria-hidden', 'false');
      document.body.style.overflow = 'hidden';
    }
    function close() {
      lightbox.classList.remove('is-open');
      lightbox.setAttribute('aria-hidden', 'true');
      document.body.style.overflow = '';
    }
    function render() {
      const g = GALLERY[current];
      imgEl.style.backgroundImage = g.tone;
      capEl.textContent = g.caption;
    }
    function step(dir) {
      current = (current + dir + GALLERY.length) % GALLERY.length;
      render();
    }

    items.forEach(el => el.addEventListener('click', () => open(Number(el.dataset.index))));
    $('#lightboxClose')?.addEventListener('click', close);
    $('#lightboxPrev')?.addEventListener('click', () => step(-1));
    $('#lightboxNext')?.addEventListener('click', () => step(1));
    lightbox?.addEventListener('click', e => { if (e.target === lightbox) close(); });
    document.addEventListener('keydown', e => {
      if (!lightbox.classList.contains('is-open')) return;
      if (e.key === 'Escape') close();
      if (e.key === 'ArrowLeft') step(-1);
      if (e.key === 'ArrowRight') step(1);
    });
  }

  /* ---------------------------------------------------------------------
     RSVP form
     --------------------------------------------------------------------- */
  function initRsvp() {
    const form = $('#rsvpForm');
    if (!form) return;

    const validators = {
      guestName: v => v.trim().length >= 2 || 'Please enter your full name.',
      guestPhone: v => /^[0-9+()\-.\s]{7,}$/.test(v.trim()) || 'Please enter a valid phone number.',
      guestCount: v => !!v || 'Please select the number of guests.',
      attendance: v => !!v || 'Please let us know if you can attend.',
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
        showToast('RSVP received — thank you!');
      }, 1100);
    });
  }

  /* ---------------------------------------------------------------------
     Confetti (canvas, lightweight, no dependency)
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
     Audio toggle (muted by default, preference remembered)
     --------------------------------------------------------------------- */
  function initAudio() {
    const btn = $('#audioToggle');
    const audio = $('#bgAudio');
    if (!btn || !audio) return;
    audio.volume = 0.35;

    const wanted = localStorage.getItem('wedding-audio') === 'on';
    if (wanted) {
      btn.classList.add('is-playing');
      btn.setAttribute('aria-label', 'Pause background music');
    }

    btn.addEventListener('click', () => {
      if (audio.paused) {
        audio.play().catch(() => {
          showToast('Add your own track at assets/audio/wedding-theme.mp3');
        });
        btn.classList.add('is-playing');
        btn.setAttribute('aria-label', 'Pause background music');
        localStorage.setItem('wedding-audio', 'on');
      } else {
        audio.pause();
        btn.classList.remove('is-playing');
        btn.setAttribute('aria-label', 'Play background music');
        localStorage.setItem('wedding-audio', 'off');
      }
    });
  }

  /* ---------------------------------------------------------------------
     Share + copy link
     --------------------------------------------------------------------- */
  function initShare() {
    const shareData = {
      title: 'Elena & Daniel — November 14, 2026',
      text: 'You are invited to our wedding celebration.',
      url: INVITE_URL,
    };

    $('#shareToggle')?.addEventListener('click', async () => {
      if (navigator.share) {
        try { await navigator.share(shareData); } catch (_) { /* user cancelled */ }
      } else {
        copyToClipboard(INVITE_URL);
        showToast('Invitation link copied to clipboard');
      }
    });

    $('#copyLink')?.addEventListener('click', () => {
      copyToClipboard(INVITE_URL);
      showToast('Invitation link copied to clipboard');
    });

    $('#copyIban')?.addEventListener('click', () => {
      const iban = $('#ibanValue')?.textContent.replace(/\s/g, '') || '';
      copyToClipboard(iban);
      showToast('IBAN copied to clipboard');
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
     Add to calendar (.ics download)
     --------------------------------------------------------------------- */
  function initCalendar() {
    $('#addToCalendar')?.addEventListener('click', () => {
      const fmt = d => d.toISOString().replace(/[-:]/g, '').split('.')[0] + 'Z';
      const ics = [
        'BEGIN:VCALENDAR',
        'VERSION:2.0',
        'PRODID:-//Elena & Daniel Wedding//EN',
        'BEGIN:VEVENT',
        `UID:${Date.now()}@elena-daniel-wedding`,
        `DTSTAMP:${fmt(new Date())}`,
        `DTSTART:${fmt(WEDDING_DATE)}`,
        `DTEND:${fmt(WEDDING_END)}`,
        'SUMMARY:Elena & Daniel\'s Wedding',
        `LOCATION:${VENUE}`,
        'DESCRIPTION:Ceremony at 5:00 PM followed by reception. We can\'t wait to celebrate with you.',
        'END:VEVENT',
        'END:VCALENDAR',
      ].join('\r\n');

      const blob = new Blob([ics], { type: 'text/calendar' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'elena-daniel-wedding.ics';
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast('Calendar event downloaded');
    });
  }

  /* ---------------------------------------------------------------------
     QR codes (gift + invitation)
     --------------------------------------------------------------------- */
  function initQrCodes() {
    if (typeof QRCode === 'undefined') return;
    const opts = { width: 128, margin: 1, color: { dark: '#2b2620', light: '#ffffff' } };

    const giftTarget = $('#giftQr');
    if (giftTarget) {
      QRCode.toCanvas(document.createElement('canvas'), 'IT60X05428111010000000123456', opts, (err, canvas) => {
        if (!err) giftTarget.appendChild(canvas);
      });
    }
    const inviteTarget = $('#inviteQr');
    if (inviteTarget) {
      QRCode.toCanvas(document.createElement('canvas'), INVITE_URL, opts, (err, canvas) => {
        if (!err) inviteTarget.appendChild(canvas);
      });
    }
  }

  /* ---------------------------------------------------------------------
     Download invitation as image (canvas-generated keepsake card)
     --------------------------------------------------------------------- */
  function initDownloadInvite() {
    $('#downloadInvite')?.addEventListener('click', () => {
      const w = 1080, h = 1350;
      const canvas = document.createElement('canvas');
      canvas.width = w; canvas.height = h;
      const ctx = canvas.getContext('2d');

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

      ctx.textAlign = 'center';
      ctx.fillStyle = '#b8894f';
      ctx.font = '400 26px Jost, sans-serif';
      ctx.fillText('THE WEDDING CELEBRATION OF', w / 2, 340);

      ctx.fillStyle = '#2b2620';
      ctx.font = '500 120px "Cinzel", serif';
      ctx.fillText('Elena & Daniel', w / 2, 480);

      ctx.strokeStyle = '#b8894f';
      ctx.beginPath();
      ctx.moveTo(w / 2 - 90, 540);
      ctx.lineTo(w / 2 + 90, 540);
      ctx.stroke();

      ctx.fillStyle = '#6f6759';
      ctx.font = '300 34px Jost, sans-serif';
      ctx.fillText('Saturday, November 14, 2026', w / 2, 620);
      ctx.fillText('Lake Como, Italy', w / 2, 670);

      ctx.fillStyle = '#2b2620';
      ctx.font = 'italic 300 32px "Cormorant Garamond", serif';
      wrapText(ctx, '"Two souls, one story — and the most beautiful chapter is only just beginning."', w / 2, 820, 760, 44);

      ctx.fillStyle = '#b8894f';
      ctx.font = '500 22px "Cinzel", serif';
      ctx.fillText('E & D', w / 2, h - 120);

      const link = document.createElement('a');
      link.download = 'elena-daniel-invitation.png';
      link.href = canvas.toDataURL('image/png');
      link.click();
      showToast('Invitation image downloaded');
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
     Init
     --------------------------------------------------------------------- */
  document.addEventListener('DOMContentLoaded', () => {
    initLoader();
    initTheme();
    initScrollChrome();
    initParticles();
    initGallery();
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
