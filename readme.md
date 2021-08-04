![](http://cf.way2muchnoise.eu/full_508083_downloads.svg) ![](http://cf.way2muchnoise.eu/versions/508083.svg)

# Dynamic Render Distance

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dynamic-render-distance)
- [ModRepo](https://modrepo.de/minecraft/renderdistance/overview)
- [GitHub](https://github.com/henkelmax/dynamic-render-distance)
- [FAQ](https://modrepo.de/minecraft/renderdistance/faq)

---

This is a server side only fabric mod, that dynamically adjusts the render distance based on the servers performance.

## Config Options

Name | Default Value | Description
---|---|---
min_mspt | `30` | The lower threshold in mspt when the mod starts increasing the render
distance max_mspt | `40` | The upper threshold in mspt when the mod starts decreasing the render distance
tick_interval | `200` | The interval of the mspt check
min_render_distance | `10` | The minimum render distance the server can
have max_render_distance | `32` | The maximum render distance the server can have
fixed_render_distance | `0` | The fixed render distance; 0 means dynamic render distance

## Commands

`/renderdistance current` shows you the current render distance (Can be executed by non OP players as well)

`/renderdistance set <number>` sets the render distance to a fixed value

`/renderdistance set auto` lets the mod dynamically change the render distance again

`/renderdistance mspt` shows the average MSPT over the last interval

`/renderdistance tps` shows the average TPS over the last interval
