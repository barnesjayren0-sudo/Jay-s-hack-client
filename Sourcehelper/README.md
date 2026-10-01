# Sourcehelper

Reference source archives for Jay Utility Client development.

## Intended contents

| File | Notes |
|------|--------|
| `CyemerSourceCode1.21.11.rar` | Cyemer Fabric 1.21.11 source archive (~228 KB) |
| `Respect Client Src.rar` | Respect Client full source (~126 MB) |

## Downloads

### Respect Client Src

Hosted externally (over GitHub’s 100 MB limit):

**MediaFire:** https://www.mediafire.com/file/v1a6s8bj7cc2zqa/Respect+Client+Src.rar/file

Direct-style link (same file):
https://www.mediafire.com/file/v1a6s8bj7cc2zqa/Respect+Client+Src.rar/file

### Cyemer

Add `CyemerSourceCode1.21.11.rar` to this folder via local git when available (binary under 100 MB).

## Upload status

- **Cyemer**: add via local git (binary) if not present — GitHub API from some environments cannot safely push large binaries.
- **Respect Client Src.rar**: **not stored in this repo** (over 100 MB). Download from the MediaFire link above, or use [Git LFS](https://git-lfs.github.com/) if you prefer to keep a copy in git.

## Local upload (optional)

```bash
cd Jay-s-hack-client
mkdir -p Sourcehelper
# copy CyemerSourceCode1.21.11.rar into Sourcehelper/

git add Sourcehelper/CyemerSourceCode1.21.11.rar Sourcehelper/README.md
git commit -m "Add Cyemer source archive to Sourcehelper"
git push origin main
```

These archives are **reference only**. Do not merge third-party code into the main client without reviewing licenses and compatibility.
