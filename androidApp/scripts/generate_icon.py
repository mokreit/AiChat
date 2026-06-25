"""Generate AiChat app icon - a modern chat bubble with AI neural pattern."""
import math
from PIL import Image, ImageDraw, ImageFont

def create_icon(size, output_path):
    """Create an app icon at the given size."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Scale factor
    s = size / 512.0
    
    # --- Background: rounded square with gradient ---
    margin = int(20 * s)
    # Draw rounded rectangle background
    for y in range(size):
        for x in range(size):
            # Check if inside rounded rect
            corner_r = int(80 * s)
            in_rect = True
            # Top-left corner
            if x < margin + corner_r and y < margin + corner_r:
                dx = x - (margin + corner_r)
                dy = y - (margin + corner_r)
                if dx*dx + dy*dy > corner_r*corner_r:
                    in_rect = False
            # Top-right corner
            elif x > size - margin - corner_r and y < margin + corner_r:
                dx = x - (size - margin - corner_r)
                dy = y - (margin + corner_r)
                if dx*dx + dy*dy > corner_r*corner_r:
                    in_rect = False
            # Bottom-left corner
            elif x < margin + corner_r and y > size - margin - corner_r:
                dx = x - (margin + corner_r)
                dy = y - (size - margin - corner_r)
                if dx*dx + dy*dy > corner_r*corner_r:
                    in_rect = False
            # Bottom-right corner
            elif x > size - margin - corner_r and y > size - margin - corner_r:
                dx = x - (size - margin - corner_r)
                dy = y - (size - margin - corner_r)
                if dx*dx + dy*dy > corner_r*corner_r:
                    in_rect = False
            # Outside rect bounds
            elif x < margin or x > size - margin or y < margin or y > size - margin:
                in_rect = False
            
            if in_rect:
                # Gradient: deep blue to teal
                t = (x + y) / (2.0 * size)
                r = int(20 + t * 10)
                g = int(60 + t * 80)
                b = int(140 + t * 60)
                img.putpixel((x, y), (r, g, b, 255))
    
    draw = ImageDraw.Draw(img)
    
    # --- Main chat bubble ---
    cx, cy = size // 2, int(size * 0.46)
    bubble_w = int(180 * s)
    bubble_h = int(120 * s)
    bubble_r = int(30 * s)
    
    # Draw main bubble (white with slight transparency)
    bubble_color = (255, 255, 255, 240)
    x0, y0 = cx - bubble_w, cy - bubble_h
    x1, y1 = cx + bubble_w, cy + bubble_h
    draw.rounded_rectangle([x0, y0, x1, y1], radius=bubble_r, fill=bubble_color)
    
    # Chat bubble tail
    tail_points = [
        (cx - int(40 * s), y1 - int(5 * s)),
        (cx - int(80 * s), y1 + int(50 * s)),
        (cx + int(10 * s), y1 - int(5 * s)),
    ]
    draw.polygon(tail_points, fill=bubble_color)
    
    # --- AI dots inside bubble ---
    dot_color = (30, 80, 180, 255)
    dot_r = int(16 * s)
    spacing = int(55 * s)
    dot_y = cy - int(10 * s)
    for i in range(3):
        dx = cx - spacing + i * spacing
        draw.ellipse([dx - dot_r, dot_y - dot_r, dx + dot_r, dot_y + dot_r], fill=dot_color)
    
    # --- Small decorative circuit lines ---
    line_color = (255, 255, 255, 120)
    lw = max(int(3 * s), 1)
    # Horizontal lines
    draw.line([(int(80*s), int(120*s)), (int(160*s), int(120*s))], fill=line_color, width=lw)
    draw.line([(int(352*s), int(120*s)), (int(432*s), int(120*s))], fill=line_color, width=lw)
    draw.line([(int(80*s), int(392*s)), (int(160*s), int(392*s))], fill=line_color, width=lw)
    draw.line([(int(352*s), int(392*s)), (int(432*s), int(392*s))], fill=line_color, width=lw)
    # Small dots at ends
    small_r = int(6 * s)
    for px, py in [(80,120),(160,120),(352,120),(432,120),(80,392),(160,392),(352,392),(432,392)]:
        draw.ellipse([int(px*s)-small_r, int(py*s)-small_r, int(px*s)+small_r, int(py*s)+small_r], fill=line_color)
    
    # --- Neural connection lines from bubble ---
    neural_color = (100, 200, 255, 100)
    # Left connections
    draw.line([(x0 + int(10*s), cy), (int(60*s), cy - int(40*s))], fill=neural_color, width=lw)
    draw.line([(int(60*s), cy - int(40*s)), (int(30*s), cy - int(80*s))], fill=neural_color, width=lw)
    draw.ellipse([int(30*s)-small_r, int(cy-80*s)-small_r, int(30*s)+small_r, int(cy-80*s)+small_r], fill=neural_color)
    # Right connections
    draw.line([(x1 - int(10*s), cy), (size - int(60*s), cy - int(40*s))], fill=neural_color, width=lw)
    draw.line([(size - int(60*s), cy - int(40*s)), (size - int(30*s), cy - int(80*s))], fill=neural_color, width=lw)
    draw.ellipse([size-int(30*s)-small_r, int(cy-80*s)-small_r, size-int(30*s)+small_r, int(cy-80*s)+small_r], fill=neural_color)
    
    # --- Text "AI" at bottom of bubble ---
    try:
        font_size = int(48 * s)
        font = ImageFont.truetype("arial.ttf", font_size)
    except:
        font = ImageFont.load_default()
    
    text = "AI"
    text_color = (30, 80, 180, 255)
    # Get text bounding box
    bbox = draw.textbbox((0, 0), text, font=font)
    tw = bbox[2] - bbox[0]
    th = bbox[3] - bbox[1]
    text_x = cx - tw // 2
    text_y = cy + int(30 * s)
    draw.text((text_x, text_y), text, fill=text_color, font=font)
    
    img.save(output_path, 'PNG')
    print(f"Generated: {output_path} ({size}x{size})")


def create_adaptive_icon(size, output_path):
    """Create adaptive icon foreground."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    s = size / 512.0
    
    cx, cy = size // 2, int(size * 0.46)
    
    # Main chat bubble
    bubble_w = int(140 * s)
    bubble_h = int(95 * s)
    bubble_r = int(24 * s)
    bubble_color = (255, 255, 255, 245)
    
    x0, y0 = cx - bubble_w, cy - bubble_h
    x1, y1 = cx + bubble_w, cy + bubble_h
    draw.rounded_rectangle([x0, y0, x1, y1], radius=bubble_r, fill=bubble_color)
    
    # Tail
    tail_points = [
        (cx - int(30 * s), y1 - int(3 * s)),
        (cx - int(60 * s), y1 + int(35 * s)),
        (cx + int(8 * s), y1 - int(3 * s)),
    ]
    draw.polygon(tail_points, fill=bubble_color)
    
    # AI dots
    dot_color = (25, 70, 160, 255)
    dot_r = int(13 * s)
    spacing = int(45 * s)
    dot_y = cy - int(8 * s)
    for i in range(3):
        dx = cx - spacing + i * spacing
        draw.ellipse([dx - dot_r, dot_y - dot_r, dx + dot_r, dot_y + dot_r], fill=dot_color)
    
    # AI text
    try:
        font_size = int(38 * s)
        font = ImageFont.truetype("arial.ttf", font_size)
    except:
        font = ImageFont.load_default()
    
    text = "AI"
    text_color = (25, 70, 160, 255)
    bbox = draw.textbbox((0, 0), text, font=font)
    tw = bbox[2] - bbox[0]
    draw.text((cx - tw // 2, cy + int(22 * s)), text, fill=text_color, font=font)
    
    img.save(output_path, 'PNG')
    print(f"Generated adaptive foreground: {output_path}")


def create_background(size, output_path):
    """Create adaptive icon background with gradient."""
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    
    for y in range(size):
        for x in range(size):
            t = (x + y) / (2.0 * size)
            r = int(15 + t * 15)
            g = int(50 + t * 80)
            b = int(130 + t * 70)
            img.putpixel((x, y), (r, g, b, 255))
    
    img.save(output_path, 'PNG')
    print(f"Generated adaptive background: {output_path}")


if __name__ == '__main__':
    import os
    base = r"d:\Code\AiChat-KMP\androidApp\src\androidMain\res"
    
    # Standard icon sizes for mipmap
    sizes = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192,
    }
    
    for folder, size in sizes.items():
        dir_path = os.path.join(base, folder)
        os.makedirs(dir_path, exist_ok=True)
        create_icon(size, os.path.join(dir_path, 'ic_launcher.png'))
        create_icon(size, os.path.join(dir_path, 'ic_launcher_round.png'))
    
    # Adaptive icon (always 108dp = 432px at xxxhdpi, but we use 512 for quality)
    anydpi_dir = os.path.join(base, 'mipmap-anydpi-v26')
    os.makedirs(anydpi_dir, exist_ok=True)
    
    # Create foreground and background at 512px
    fg_dir = os.path.join(base, 'drawable')
    os.makedirs(fg_dir, exist_ok=True)
    create_adaptive_icon(512, os.path.join(fg_dir, 'ic_launcher_foreground.png'))
    create_background(512, os.path.join(fg_dir, 'ic_launcher_background.png'))
    
    print("All icons generated!")
