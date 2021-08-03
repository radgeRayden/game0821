import .bottle
import .renderer

@@ 'on bottle.load
fn ()
    renderer.init;
    ;

fn draw (rp)
    ;

@@ 'on bottle.update
fn (dt)
    renderer.present draw

bottle.run;
