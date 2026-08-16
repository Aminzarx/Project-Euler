# FTP Deployment Guide (static build + PHP)

For hosting where you only have file upload access (FTP/SFTP/a hosting control panel's file
manager) — typical shared hosting (cPanel, DirectAdmin, Plesk, etc.). No Node.js, no SSH, no
build step on the server: everything is pre-built locally and uploaded as plain files.

## Requirements on the host

- **PHP 7.4+** — used only for the RSVP form and guestbook (two small scripts). Virtually
  every FTP-based shared hosting plan has this by default.
- **Apache with `mod_rewrite`** and `AllowOverride All` (or equivalent) for the `.htaccess`
  file under `api/` to take effect. This is the default on nearly all cPanel-style hosting.
  If your host uses Nginx instead, see "Nginx instead of Apache" below.
- Write permission for the PHP process on a `data/` folder (created automatically on first
  RSVP/guestbook submission — just make sure the uploaded folder isn't read-only).

## Steps

1. **Build the static bundle** (on your own machine, wherever this repo is checked out):

   ```bash
   cd wedding
   npm install   # first time only
   npm run build:static
   ```

   This produces `wedding/out/` — a self-contained folder of plain HTML/CSS/JS plus the PHP
   backend. Nothing in it needs a build step to run.

2. **Upload it.** Using FTP/SFTP or your host's file manager, upload the **entire contents**
   of `wedding/out/` into a folder named `Wedding` in your site's web root, so the result is:

   ```
   public_html/
     Wedding/
       index.html
       _next/...
       config.php
       api/
         rsvp.php
         guestbook.php
         .htaccess
       lib/
         Store.php
         Sms.php
         Validate.php
       data/
         .htaccess       (created empty; guestbook.json/rsvp.json appear after first use)
   ```

   The site should now load at `https://zarandix.ir/Wedding`.

3. **Add the background music** (optional): upload your track as
   `Wedding/audio/wedding-song.mp3`. If skipped, the music player stays visible but silently
   does nothing when pressed.

4. **Configure SMS** (optional, can be done anytime later): open `Wedding/config.php` in your
   host's file editor (or edit locally and re-upload just that one file) and set
   `sms_provider` plus the matching provider's credentials. See
   `wedding/docs/README.md` → SMS provider table for what each provider needs — the same
   field names apply here, just as PHP array values instead of environment variables.

   **Do this only after the initial upload**, and keep a backup of your filled-in
   `config.php` — if you ever re-run step 2 with a fresh build, upload every file *except*
   `config.php` (or restore your backup afterward), so you don't overwrite real credentials
   with the placeholder defaults.

## How the two backends stay in sync

The RSVP modal and guestbook form call `/Wedding/api/rsvp` and `/Wedding/api/guestbook` (no
`.php` extension) — identical to how they call the Node deployment's routes. The
`.htaccess` file inside `api/` rewrites those exact paths to `rsvp.php`/`guestbook.php`
under the hood, so the React code never needs to know or care which backend is serving it.

`php/lib/Sms.php` and `php/lib/Store.php` are hand-written PHP equivalents of
`lib/sms/` and `lib/store/` on the Node side — same provider list, same JSON file shape,
same validation rules. If you change the RSVP/guestbook logic on one side, mirror the change
on the other (`wedding/php/`) so both deployment paths keep behaving identically.

## Nginx instead of Apache

If your host runs Nginx rather than Apache, `.htaccess` is ignored. Ask whoever manages the
server config to add the equivalent rewrite inside the `location /Wedding { ... }` block:

```nginx
location = /Wedding/api/rsvp { rewrite ^ /Wedding/api/rsvp.php last; }
location = /Wedding/api/guestbook { rewrite ^ /Wedding/api/guestbook.php last; }
location ~ \.php$ { fastcgi_pass ...; include fastcgi_params; fastcgi_param SCRIPT_FILENAME $document_root$fastcgi_script_name; }
```

(The exact `fastcgi_pass` target depends on how PHP-FPM is set up on that host.)

## Updating the site later

Repeat step 1 to produce a fresh `wedding/out/`, then re-upload — except `config.php` (see
step 4) and anything in `data/` (your real guestbook messages and RSVP responses; these are
never touched by a rebuild since they only exist on the server, not in the build output).
