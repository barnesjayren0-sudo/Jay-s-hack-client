# Jay's Hack Client v1.4.1

Fabric **1.21.11** · **PoJav / Mojo friendly** · Redmi A5-style small screens

---

## Mobile GUI

On small screens (width &lt; 500 or height &lt; 320) the ClickGUI goes **near full-screen** with:
- Bigger touch rows (28px)
- Wider sidebar hit targets
- **Search bar** — type to filter any module
- Close via **X**, **ESC**, or Right Shift

| Control | Action |
|---------|--------|
| Right Shift | Open / close |
| Search box | Filter modules by name |
| Sidebar | Category (clears search) |
| Row / pill | Toggle |

Works on **PoJav Launcher** and **Mojo Launcher** as a normal Fabric client jar (same as PC). Use a Fabric 1.21.11 profile on the launcher.

---

## Build

```bash
./gradlew build
```

Put `jays-hack-client-1.4.1.jar` + Fabric API into the launcher's `mods` folder.

https://github.com/barnesjayren0-sudo/Jay-s-hack-client
