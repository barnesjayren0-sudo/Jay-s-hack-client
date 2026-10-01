# Sourcehelper

Reference source archives for Jay Utility Client development.

## Downloads

### Respect Client Src.rar

**Download (MediaFire):**  
https://www.mediafire.com/file/v1a6s8bj7cc2zqa/Respect+Client+Src.rar/file

> Full Respect Client source (~126 MB). Hosted externally because it exceeds GitHub’s 100 MB file limit.

### CyemerSourceCode1.21.11.rar

Add this file to `Sourcehelper/` via local git when you have it (binary is under 100 MB and can live in the repo).

## Intended contents

| File | Notes |
|------|--------|
| `CyemerSourceCode1.21.11.rar` | Cyemer Fabric 1.21.11 source archive (~228 KB) |
| `Respect Client Src.rar` | [MediaFire download](https://www.mediafire.com/file/v1a6s8bj7cc2zqa/Respect+Client+Src.rar/file) (~126 MB) |

## Upload status

- **Cyemer** — optional in-repo binary; push with normal `git add` / `git commit` / `git push`.
- **Respect Client Src.rar** — **not stored in this repo**. Use the MediaFire link above, or Git LFS if you want a copy tracked in git.

## Local upload (Cyemer only)

```bash
cd Jay-s-hack-client
mkdir -p Sourcehelper
# copy CyemerSourceCode1.21.11.rar into Sourcehelper/

git add Sourcehelper/CyemerSourceCode1.21.11.rar Sourcehelper/README.md
git commit -m "Add Cyemer source archive to Sourcehelper"
git push origin main
```

These archives are **reference only**. Do not merge third-party code into the main client without reviewing licenses and compatibility.
