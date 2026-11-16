# Aday Memur marka görselleri (SVG) — optik form satırları, ilk balon fosforlu sarı
def rows(x0, y0, scale, white="#FFFFFF", mono=False):
    out = []
    for i in range(3):
        cy = y0 + i * 26 * scale
        r = 7.5 * scale
        if i == 0 and not mono:
            out.append(f'<circle cx="{x0+r}" cy="{cy}" r="{r}" fill="#FFE45A"/>')
        elif i == 0:
            out.append(f'<circle cx="{x0+r}" cy="{cy}" r="{r}" fill="{white}"/>')
        else:
            out.append(f'<circle cx="{x0+r}" cy="{cy}" r="{r-1.5*scale}" fill="none" stroke="{white}" stroke-width="{3*scale}"/>')
        w = (38 if i < 2 else 26) * scale
        op = 1 if i == 0 else 0.55
        out.append(f'<rect x="{x0+2*r+8*scale}" y="{cy-2.6*scale}" width="{w}" height="{5.2*scale}" rx="{2.6*scale}" fill="{white}" fill-opacity="{1 if mono else op}"/>')
    return "\n".join(out)

def mark_box(size, pad_ratio=0.0, bg="#1B1E23", radius_ratio=0.0):
    # içerik genişliği: 2r + 8 + 38 = 61 birim, yükseklik 52+15=67 birim
    s = size * (1 - 2 * pad_ratio) / 64 * 0.86
    cw, ch = 61 * s, (52 + 15) * s
    x0 = (size - cw) / 2
    y0 = (size - ch) / 2 + 7.5 * s
    rx = size * radius_ratio
    bgel = f'<rect width="{size}" height="{size}" rx="{rx}" fill="{bg}"/>' if bg else ''
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">{bgel}{rows(x0, y0, s)}</svg>'

def mono(size, pad_ratio):
    s = size * (1 - 2 * pad_ratio) / 64 * 0.86
    cw, ch = 61 * s, 67 * s
    x0 = (size - cw) / 2; y0 = (size - ch) / 2 + 7.5 * s
    return f'<svg xmlns="http://www.w3.org/2000/svg" width="{size}" height="{size}" viewBox="0 0 {size} {size}">{rows(x0, y0, s, mono=True)}</svg>'

open("icon.svg","w").write(mark_box(1024, 0.14))                     # tam kare (Play maskeyi uygular)
open("play-icon.svg","w").write(mark_box(512, 0.14))
open("adaptive-foreground.svg","w").write(mark_box(1024, 0.30, bg=None))  # güvenli alan
open("adaptive-monochrome.svg","w").write(mono(1024, 0.30))
open("notification-icon.svg","w").write(mono(96, 0.06))
open("splash-mark.svg","w").write(mark_box(512, 0.0, radius_ratio=0.22))
open("favicon.svg","w").write(mark_box(64, 0.08, radius_ratio=0.22))
