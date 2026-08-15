# Audio asset

Place the wedding background track here as:

```
wedding-song.mp3
```

`components/audio/MusicPlayer.tsx` references this exact filename. Any royalty-cleared,
soft instrumental track works well — the player fades volume in/out automatically, so an
abrupt intro/outro in the source file is fine. If this file is absent, the player fails
silently (buttons stay interactive, no console error shown to guests).
