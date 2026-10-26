#!/usr/bin/env python3
"""
prepare_assets.py
Prepares liturgical visual assets for the Orthodox Communion Counter app:
1. Church watermark background (1280x800) with deep burgundy tint and ~15% opacity.
2. High-quality liturgical app icon (Orthodox cross ☦ with church silhouette and gold circular rim)
   for all standard Android mipmap buckets.
"""

import os
import math
from PIL import Image, ImageDraw, ImageFilter, ImageOps, ImageEnhance

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
APP_DIR = os.path.abspath(os.path.join(SCRIPT_DIR, ".."))
RES_DIR = os.path.join(APP_DIR, "res")

INPUT_CHURCH_IMAGE = "/Users/apple/.gemini/antigravity/brain/6f0c231a-6026-4265-9ef2-4a2bcb8e9a27/.user_uploaded/media_1792852628556_196e6788.webp"
FALLBACK_INPUT = os.path.join(APP_DIR, "church_source.webp")

BG_BURGUNDY = (28, 6, 9)          # #1C0609
BURGUNDY_DARK = (42, 8, 12)       # #2A080C
BURGUNDY_WINE = (56, 11, 18)      # #380B12
GOLD_PRIMARY = (212, 175, 55)     # #D4AF37
GOLD_BRIGHT = (243, 229, 171)     # #F3E5AB
GOLD_DARK = (140, 109, 31)        # #8C6D1F
GOLD_ACCENT = (255, 238, 160)     # #FFEEA0

def get_input_image_path():
    if os.path.exists(INPUT_CHURCH_IMAGE):
        return INPUT_CHURCH_IMAGE
    if os.path.exists(FALLBACK_INPUT):
        return FALLBACK_INPUT
    raise FileNotFoundError(f"Input church image not found at {INPUT_CHURCH_IMAGE}")

def generate_church_watermark():
    """
    Generates apps/communion-counter/res/drawable/bg_church_watermark.png
    Dimensions: 1280x800 (landscape tablet standard)
    Blends darkened monochrome church with soft edge fade at ~15% opacity over #1C0609.
    """
    canvas_w, canvas_h = 1280, 800
    base = Image.new("RGBA", (canvas_w, canvas_h), (*BG_BURGUNDY, 255))

    input_path = get_input_image_path()
    church_src = Image.open(input_path).convert("RGB")
    src_w, src_h = church_src.size

    # Scale church image to fit height nicely (approx 760px high)
    target_h = 760
    scale = target_h / float(src_h)
    target_w = int(src_w * scale)
    church_resized = church_src.resize((target_w, target_h), Image.Resampling.LANCZOS)

    # Convert to grayscale and enhance contrast so architectural details stand out
    gray = ImageOps.grayscale(church_resized)
    gray = ImageEnhance.Contrast(gray).enhance(1.3)
    gray = ImageEnhance.Sharpness(gray).enhance(1.2)

    # Colorize grayscale into liturgical warm golden/burgundy monochrome
    # Black maps to #1C0609, White maps to warm gold #D4AF37
    tinted = ImageOps.colorize(gray, black=BG_BURGUNDY, white=GOLD_BRIGHT)

    # Create mask with smooth vignette (fade out towards edges)
    mask = Image.new("L", (target_w, target_h), 0)
    draw_mask = ImageDraw.Draw(mask)
    margin_x = int(target_w * 0.12)
    margin_y = int(target_h * 0.10)
    draw_mask.rectangle(
        [margin_x, margin_y, target_w - margin_x, target_h - margin_y],
        fill=255
    )
    mask = mask.filter(ImageFilter.GaussianBlur(radius=35))

    # Scale overall opacity to ~15% (255 * 0.15 ~= 38)
    opacity_factor = 0.15
    mask = mask.point(lambda p: int(p * opacity_factor))

    # Paste church centered on the 1280x800 background
    pos_x = (canvas_w - target_w) // 2
    pos_y = (canvas_h - target_h) // 2
    base.paste(tinted, (pos_x, pos_y), mask)

    # Flatten to RGB and save
    final_img = base.convert("RGB")
    drawable_dir = os.path.join(RES_DIR, "drawable")
    os.makedirs(drawable_dir, exist_ok=True)
    out_path = os.path.join(drawable_dir, "bg_church_watermark.png")
    final_img.save(out_path, format="PNG", optimize=True)
    print(f"Generated watermark background: {out_path} ({final_img.size})")

def generate_launcher_icons():
    """
    Generates apps/communion-counter/res/mipmap-*/ic_launcher.png
    Creates master 512x512 icon featuring:
    - Deep burgundy circular backdrop
    - Rich embossed circular gold rim
    - Church silhouette in the background
    - Prominent Eastern Orthodox 3-bar cross (☦) in gold with highlights
    """
    size = 512
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    center = size / 2.0
    outer_radius = 236.0

    # Draw subtle drop shadow
    shadow_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    shadow_draw = ImageDraw.Draw(shadow_img)
    shadow_draw.ellipse(
        [center - outer_radius, center - outer_radius + 6,
         center + outer_radius, center + outer_radius + 6],
        fill=(0, 0, 0, 110)
    )
    shadow_img = shadow_img.filter(ImageFilter.GaussianBlur(radius=8))
    img.paste(shadow_img, (0, 0), shadow_img)

    # Circular deep burgundy background with subtle radial gradient
    bg_circle = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    bg_draw = ImageDraw.Draw(bg_circle)
    for r in range(int(outer_radius), 0, -2):
        t = r / outer_radius
        # Interpolate between BURGUNDY_DARK (#2A080C) at edge and BURGUNDY_WINE (#380B12) at center
        red = int(BURGUNDY_WINE[0] * (1.0 - t) + BURGUNDY_DARK[0] * t)
        green = int(BURGUNDY_WINE[1] * (1.0 - t) + BURGUNDY_DARK[1] * t)
        blue = int(BURGUNDY_WINE[2] * (1.0 - t) + BURGUNDY_DARK[2] * t)
        bg_draw.ellipse([center - r, center - r, center + r, center + r], fill=(red, green, blue, 255))

    img.paste(bg_circle, (0, 0), bg_circle)

    # Add church silhouette overlay in lower half of the circle
    input_path = get_input_image_path()
    church_src = Image.open(input_path).convert("L")
    church_w, church_h = church_src.size
    scaled_w = 340
    scaled_h = int(scaled_w * (church_h / float(church_w)))
    church_scaled = church_src.resize((scaled_w, scaled_h), Image.Resampling.LANCZOS)
    church_tinted = ImageOps.colorize(church_scaled, black=BURGUNDY_DARK, white=GOLD_PRIMARY)

    church_mask = Image.new("L", (scaled_w, scaled_h), 0)
    ch_draw = ImageDraw.Draw(church_mask)
    ch_draw.rectangle([0, int(scaled_h * 0.15), scaled_w, scaled_h], fill=70)
    church_mask = church_mask.filter(ImageFilter.GaussianBlur(radius=15))

    # Circular mask to keep within inner circle
    inner_clip = Image.new("L", (size, size), 0)
    clip_draw = ImageDraw.Draw(inner_clip)
    clip_draw.ellipse([center - 200, center - 200, center + 200, center + 200], fill=255)

    church_canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    church_pos_x = int(center - scaled_w // 2)
    church_pos_y = int(center - scaled_h // 2 + 50)
    church_canvas.paste(church_tinted, (church_pos_x, church_pos_y), church_mask)
    img.paste(church_canvas, (0, 0), inner_clip)

    # Draw Outer Rich Circular Gold Rim
    # Outer stroke (Gold Dark)
    draw.ellipse([center - outer_radius, center - outer_radius,
                  center + outer_radius, center + outer_radius],
                 outline=GOLD_DARK, width=4)
    # Main Gold Band
    draw.ellipse([center - (outer_radius - 4), center - (outer_radius - 4),
                  center + (outer_radius - 4), center + (outer_radius - 4)],
                 outline=GOLD_PRIMARY, width=12)
    # Bright Gold Inner Highlight
    draw.ellipse([center - (outer_radius - 16), center - (outer_radius - 16),
                  center + (outer_radius - 16), center + (outer_radius - 16)],
                 outline=GOLD_BRIGHT, width=3)
    # Delicate inner bead rim
    draw.ellipse([center - (outer_radius - 22), center - (outer_radius - 22),
                  center + (outer_radius - 22), center + (outer_radius - 22)],
                 outline=GOLD_DARK, width=2)

    # Draw Orthodox Three-Bar Cross (☦)
    # Traditional Eastern Orthodox Cross:
    # 1. Vertical post
    # 2. Upper horizontal bar (Titulus)
    # 3. Middle horizontal bar (Main hands beam)
    # 4. Lower slanted bar (Footrest / Suppedaneum): left raised, right lowered

    cross_img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    cross_draw = ImageDraw.Draw(cross_img)

    # Dimensions
    vert_x0 = center - 13
    vert_x1 = center + 13
    vert_y0 = 85
    vert_y1 = 410

    # Main bar
    main_y0 = 205
    main_y1 = 233
    main_x0 = center - 110
    main_x1 = center + 110

    # Upper bar (Titulus)
    top_y0 = 135
    top_y1 = 157
    top_x0 = center - 55
    top_x1 = center + 55

    # Footrest slanted bar (angle approx 18 deg: left higher, right lower)
    # Footrest center at y=330, width=105, height=20
    foot_cx = center
    foot_cy = 330
    foot_half_w = 52
    foot_half_h = 10
    foot_dy = 18  # slant offset

    # Points of polygon for slanted footrest
    p1 = (foot_cx - foot_half_w, foot_cy - foot_dy - foot_half_h)
    p2 = (foot_cx + foot_half_w, foot_cy + foot_dy - foot_half_h)
    p3 = (foot_cx + foot_half_w, foot_cy + foot_dy + foot_half_h)
    p4 = (foot_cx - foot_half_w, foot_cy - foot_dy + foot_half_h)

    # 1. Subtle gold drop shadow for the cross
    cross_shadow = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    cs_draw = ImageDraw.Draw(cross_shadow)
    sy = 4
    cs_draw.rectangle([vert_x0, vert_y0 + sy, vert_x1, vert_y1 + sy], fill=(10, 2, 4, 180))
    cs_draw.rectangle([main_x0, main_y0 + sy, main_x1, main_y1 + sy], fill=(10, 2, 4, 180))
    cs_draw.rectangle([top_x0, top_y0 + sy, top_x1, top_y1 + sy], fill=(10, 2, 4, 180))
    cs_draw.polygon([(x, y + sy) for x, y in [p1, p2, p3, p4]], fill=(10, 2, 4, 180))
    cross_shadow = cross_shadow.filter(ImageFilter.GaussianBlur(radius=4))
    img.paste(cross_shadow, (0, 0), cross_shadow)

    # 2. Dark gold outer contour / bevel
    border_px = 3
    cross_draw.rectangle([vert_x0 - border_px, vert_y0 - border_px,
                          vert_x1 + border_px, vert_y1 + border_px], fill=GOLD_DARK)
    cross_draw.rectangle([main_x0 - border_px, main_y0 - border_px,
                          main_x1 + border_px, main_y1 + border_px], fill=GOLD_DARK)
    cross_draw.rectangle([top_x0 - border_px, top_y0 - border_px,
                          top_x1 + border_px, top_y1 + border_px], fill=GOLD_DARK)
    cross_draw.polygon([(p1[0] - border_px, p1[1] - border_px),
                        (p2[0] + border_px, p2[1] - border_px),
                        (p3[0] + border_px, p3[1] + border_px),
                        (p4[0] - border_px, p4[1] + border_px)], fill=GOLD_DARK)

    # 3. Main gold fill
    cross_draw.rectangle([vert_x0, vert_y0, vert_x1, vert_y1], fill=GOLD_PRIMARY)
    cross_draw.rectangle([main_x0, main_y0, main_x1, main_y1], fill=GOLD_PRIMARY)
    cross_draw.rectangle([top_x0, top_y0, top_x1, top_y1], fill=GOLD_PRIMARY)
    cross_draw.polygon([p1, p2, p3, p4], fill=GOLD_PRIMARY)

    # 4. Inner bright gold highlights
    hl_w = 4
    cross_draw.line([(center, vert_y0 + 4), (center, vert_y1 - 4)], fill=GOLD_BRIGHT, width=hl_w)
    cross_draw.line([(main_x0 + 4, (main_y0 + main_y1) // 2),
                     (main_x1 - 4, (main_y0 + main_y1) // 2)], fill=GOLD_BRIGHT, width=hl_w)
    cross_draw.line([(top_x0 + 4, (top_y0 + top_y1) // 2),
                     (top_x1 - 4, (top_y0 + top_y1) // 2)], fill=GOLD_BRIGHT, width=hl_w)
    cross_draw.line([(foot_cx - foot_half_w + 4, foot_cy - foot_dy),
                     (foot_cx + foot_half_w - 4, foot_cy + foot_dy)], fill=GOLD_BRIGHT, width=hl_w)

    img.paste(cross_img, (0, 0), cross_img)

    # Export to mipmap buckets
    buckets = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }

    for bucket, icon_size in buckets.items():
        bucket_dir = os.path.join(RES_DIR, bucket)
        os.makedirs(bucket_dir, exist_ok=True)
        scaled_icon = img.resize((icon_size, icon_size), Image.Resampling.LANCZOS)
        out_file = os.path.join(bucket_dir, "ic_launcher.png")
        scaled_icon.save(out_file, format="PNG", optimize=True)
        print(f"Generated launcher icon: {out_file} ({icon_size}x{icon_size})")

def main():
    print("Preparing liturgical assets for Communion Counter...")
    generate_church_watermark()
    generate_launcher_icons()
    print("Asset generation complete!")

if __name__ == "__main__":
    main()
