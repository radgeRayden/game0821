using import Rc
using import glm

import .bottle
import .renderer
using import .atlas


@@ 'on bottle.load
fn ()
    renderer.init;
    let tile-atlas = (Rc.wrap (Atlas "assets/tiles.png"))
    let ship-atlas = (Rc.wrap (Atlas "assets/ships.png"))

    let bg = (renderer.new-layer "background" tileset)
    let ships = (renderer.new-layer "ships" tileset)
    let ui = (renderer.new-layer "ui" tile-atlas)

    let ship0 = (vec4 0 0 0.25 0.5)

    'add ships (vec2 100 100) ship0
    'add ships (vec2 200 100) ship0
    'add ships (vec2 300 150) ship0
    ;

fn draw (rp)
    let bg = (renderer.get-layer "background")
    'draw bg rp
    ;

@@ 'on bottle.update
fn (dt)
    renderer.present draw

bottle.run;
