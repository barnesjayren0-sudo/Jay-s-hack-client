# Sourcehelper

Reference source archives for Jay Utility Client development.

## Intended contents

| File | Notes |
|------|--------|
| `CyemerSourceCode1.21.11.rar` | Cyemer Fabric 1.21.11 source archive (~228 KB) |
| `Respect Client Src.rar` | Respect Client full source (~126 MB) |

## Upload status

- **Cyemer**: add via local git (binary) if not present — GitHub API from this environment cannot safely push large binaries.
- **Respect Client Src.rar**: **cannot** be committed through normal GitHub uploads — file is **over 100 MB**. Use [Git LFS](https://git-lfs.github.com/) from your machine, or host the archive elsewhere and link it here.

## Local upload (recommended)

```bash
cd Jay-s-hack-client
mkdir -p Sourcehelper
# copy the .rar files into Sourcehelper/

# Cyemer only (under 100MB):
git add Sourcehelper/CyemerSourceCode1.21.11.rar Sourcehelper/README.md
git commit -m "Add Cyemer source archive to Sourcehelper"
git push origin main

# Respect Client (over 100MB) — Git LFS:
git lfs install
git lfs track "Sourcehelper/*.rar"
git add .gitattributes Sourcehelper/"Respect Client Src.rar"
git commit -m "Add Respect Client source via Git LFS"
git push origin main
```

These archives are **reference only**. Do not merge third-party code into the main client without reviewing licenses and compatibility.
