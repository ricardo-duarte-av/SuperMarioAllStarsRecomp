// make_icon.swift - draws the app icon: original pixel art (four gold stars, one per game, over a
// sky, a hill and brick ground), no artwork from the game.
//
//   swift tools/make_icon.swift <out.png> <size> [--full]
//
// Default: macOS icon layout (artwork inset in a rounded square, transparent
// margin). --full: artwork fills the square (Windows .ico).
import AppKit

let args = CommandLine.arguments
let outPath = args[1]
let size = Int(args[2])!
let full = args.contains("--full")

func rgb(_ hex: UInt32) -> NSColor {
    NSColor(srgbRed: CGFloat((hex >> 16) & 255) / 255, green: CGFloat((hex >> 8) & 255) / 255,
            blue: CGFloat(hex & 255) / 255, alpha: 1)
}

// 32 x 32 grid. Back to front: sky, a green hill, brick ground, then four gold stars (one per game).
var grid = [[UInt32?]](repeating: [UInt32?](repeating: nil, count: 32), count: 32)
func fillRows(_ a: Int, _ b: Int, _ color: UInt32) { for r in a...b { for c in 0..<32 { grid[r][c] = color } } }

// Sky (rows 0-21): deeper blue at the top, lighter toward the horizon, in bands.
let sky: [UInt32] = [0x2C5CD8, 0x3468E0, 0x3C76E8, 0x4884EE, 0x5692F2, 0x64A0F6, 0x74AEF8, 0x86BCFA]
for r in 0...21 { fillRows(r, r, sky[min(r / 3, sky.count - 1)]) }

func inEllipse(_ x: Double, _ y: Double, _ cx: Double, _ cy: Double, _ rx: Double, _ ry: Double) -> Bool {
    let dx = (x - cx) / rx, dy = (y - cy) / ry
    return dx * dx + dy * dy <= 1
}
// Hill behind the ground, right side: outlined, with a lighter top.
for r in 12...21 {
    for c in 0..<32 {
        let x = Double(c) + 0.5, y = Double(r) + 0.5
        guard inEllipse(x, y, 25, 25, 9, 8) else { continue }
        let edge = !inEllipse(x, y, 25, 25, 8, 7)
        grid[r][c] = edge ? 0x0C4A1C : (inEllipse(x, y, 23, 20.5, 3.5, 2.5) ? 0x5CD86C : 0x2CA844)
    }
}

// Ground (rows 22-31): bricks with dark mortar, a light top edge, rows offset by half a brick.
fillRows(22, 31, 0xC85418)
for c in 0..<32 { grid[22][c] = 0xF8A070 }
for r in [26, 30] { for c in 0..<32 { grid[r][c] = 0x5A1E04 } }
for r in 23...31 where r != 26 && r != 30 {
    let shift = r < 26 ? 0 : (r < 30 ? 4 : 0)
    for c in stride(from: shift, to: 32, by: 8) { grid[r][c] = 0x5A1E04 }
}

// Stars: five points, upright, outlined, lit from the top left.
func inStar(_ x: Double, _ y: Double, _ cx: Double, _ cy: Double, _ R: Double) -> Bool {
    let inner = R * 0.56
    var pts: [(Double, Double)] = []
    for i in 0..<10 {
        let ang = -Double.pi / 2 + Double(i) * Double.pi / 5
        let rad = i % 2 == 0 ? R : inner
        pts.append((cx + rad * cos(ang), cy + rad * sin(ang)))
    }
    var inside = false
    var j = pts.count - 1
    for i in 0..<pts.count {
        let (xi, yi) = pts[i], (xj, yj) = pts[j]
        if (yi > y) != (yj > y) && x < (xj - xi) * (y - yi) / (yj - yi) + xi { inside.toggle() }
        j = i
    }
    return inside
}
let stars: [(Double, Double, Double)] = [(5.6, 13.0, 5.8), (12.4, 7.0, 5.8), (19.6, 13.0, 5.8), (26.4, 7.0, 5.8)]
let ink: UInt32 = 0x5A3200, gold: UInt32 = 0xFFC820, goldLight: UInt32 = 0xFFF0A0, goldShade: UInt32 = 0xD88A08
func starAt(_ c: Int, _ r: Int) -> Int? {
    guard (0..<32).contains(c), (0..<32).contains(r) else { return nil }
    let x = Double(c) + 0.5, y = Double(r) + 0.5
    return stars.firstIndex { inStar(x, y, $0.0, $0.1, $0.2) }
}
for r in 0..<32 {
    for c in 0..<32 {
        guard let k = starAt(c, r) else { continue }
        let nbrs: [Int?] = [starAt(c - 1, r), starAt(c + 1, r), starAt(c, r - 1), starAt(c, r + 1)]
        if nbrs.contains(where: { $0 != k }) {
            grid[r][c] = ink; continue
        }
        let (cx, cy, R) = stars[k]
        let light = -((Double(c) + 0.5 - cx) * 0.7 + (Double(r) + 0.5 - cy) * 0.7) / R
        grid[r][c] = light > 0.28 ? goldLight : (light < -0.22 ? goldShade : gold)
    }
}

// Render: whole pixels only, so the art stays crisp.
let rep = NSBitmapImageRep(bitmapDataPlanes: nil, pixelsWide: size, pixelsHigh: size, bitsPerSample: 8,
                           samplesPerPixel: 4, hasAlpha: true, isPlanar: false, colorSpaceName: .deviceRGB,
                           bytesPerRow: 0, bitsPerPixel: 0)!
NSGraphicsContext.saveGraphicsState()
let ctx = NSGraphicsContext(bitmapImageRep: rep)!
NSGraphicsContext.current = ctx
ctx.imageInterpolation = .none
ctx.shouldAntialias = false

let art = full ? CGFloat(size) : CGFloat(size) * 832 / 1024   // Apple's grid: 824-ish content in 1024
let cell = art / 32
let origin = (CGFloat(size) - art) / 2
let radius = art * (full ? 0.18 : 0.225)
let frame = NSRect(x: origin, y: origin, width: art, height: art)
ctx.shouldAntialias = true
NSBezierPath(roundedRect: frame, xRadius: radius, yRadius: radius).addClip()
ctx.shouldAntialias = false
for r in 0..<32 {
    for c in 0..<32 {
        guard let color = grid[r][c] else { continue }
        rgb(color).setFill()
        // AppKit's y axis points up; row 0 is the top.
        let x = (origin + CGFloat(c) * cell).rounded(.down)
        let y = (origin + art - CGFloat(r + 1) * cell).rounded(.down)
        let x2 = (origin + CGFloat(c + 1) * cell).rounded(.down)
        let y2 = (origin + art - CGFloat(r) * cell).rounded(.down)
        NSRect(x: x, y: y, width: x2 - x, height: y2 - y).fill()
    }
}
NSGraphicsContext.restoreGraphicsState()
try! rep.representation(using: .png, properties: [:])!.write(to: URL(fileURLWithPath: outPath))
