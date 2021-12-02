![](http://cf.way2muchnoise.eu/full_508083_downloads.svg) ![](http://cf.way2muchnoise.eu/versions/508083.svg)

# Dynamic Render Distance

## Links

- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/dynamic-render-distance)
- [ModRepo](https://modrepo.de/minecraft/renderdistance/overview)
- [GitHub](https://github.com/henkelmax/dynamic-render-distance)
- [FAQ](https://modrepo.de/minecraft/renderdistance/faq)

---

This is a server side only fabric mod, that dynamically adjusts the render and simulation distance based on the servers performance.

## Config Options

Name | Default Value | Description
---|---|---
min_mspt | `30` | The lower threshold in mspt when the mod starts increasing the render/simulation distance
max_mspt | `40` | The upper threshold in mspt when the mod starts decreasing the render/simulation distance
tick_interval | `200` | The interval of the mspt check
min_render_distance | `10` | The minimum render distance the server can have
max_render_distance | `32` | The maximum render distance the server can have
min_simulation_distance | `10` | The minimum simulation distance the server can have
max_simulation_distance | `32` | The maximum simulation distance the server can have
render_to_simulation_ratio | `2` | The ratio between simulation and render distance
fixed_render_distance | `0` | The fixed render distance; 0 means dynamic render distance
fixed_simulation_distance | `0` | The fixed simulation distance; 0 means dynamic simulation distance

## Commands

`/renderdistance current` shows you the current simulation and render distance (Can be executed by non OP players as well)

`/renderdistance fixed ratio|render|simulation <value>|auto` sets the render/simulation distance or ratio to a fixed value

`/renderdistance limit render|simulation <min> <max>` sets minimum and maximum render/simulation distance

`/renderdistance mspt` shows the average MSPT over the last interval

`/renderdistance tps` shows the average TPS over the last interval
