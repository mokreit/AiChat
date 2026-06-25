"""Crop and resize the user's icon into all Android mipmap sizes."""
from PIL import Image, ImageDraw
import os

src_path = r"F:\Comfy\Project\ComfyUI-Shared\output\MiaoMiao_Output_00100_.png"
base = r"d:\Code\AiChat-KMP\androidApp\src\androidMain\res"

img = Image.open(src_path).convert("RGBA")
print(f"Source: {img.size}")

# Center crop to square
w, h = img.size
s = min(w, h)
left = (w - s) // 2
top = (h - s) // 2
img_sq = img.crop((left, top, left + s, top + s))
print(f"Cropped to square: {img_sq.size}")

# Standard mipmap sizes
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
    resized = img_sq.resize((size, size), Image.LANCZOS)
    # Standard icon
    resized.save(os.path.join(dir_path, 'ic_launcher.png'), 'PNG')
    # Round icon - make circular
    mask = Image.new('L', (size, size), 0)
    draw = ImageDraw.Draw(mask)
    draw.ellipse((0, 0, size-1, size-1), fill=255)
    round_img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    round_img.paste(resized, (0, 0), mask)
    round_img.save(os.path.join(dir_path, 'ic_launcher_round.png'), 'PNG')
    print(f"Generated {folder}: {size}x{size}")

# Adaptive icon foreground (with padding for safe zone)
# Adaptive icons have 72dp canvas with 66dp safe zone (108/100 ratio)
fg_size = 432  # xxxhdpi foreground size
fg_dir = os.path.join(base, 'drawable')
os.makedirs(fg_dir, exist_ok=True)

fg_img = img_sq.resize((300, 300), Image.LANCZOS)  # smaller to fit in safe zone
fg_canvas = Image.new('RGBA', (fg_size, fg_size), (0, 0, 0, 0))
offset = (fg_size - 300) // 2
fg_canvas.paste(fg_img, (offset, offset), fg_img)
fg_canvas.save(os.path.join(fg_dir, 'ic_launcher_foreground.png'), 'PNG')
print(f"Generated adaptive foreground: {fg_size}x{fg_size}")

# Adaptive icon background - use solid color from image corners
bg_dir = os.path.join(base, 'drawable')
# Sample a dominant color from top-left
bg_color = img_sq.getpixel((10, 10))
bg_img = Image.new('RGBA', (fg_size, fg_size), bg_color)
bg_img.save(os.path.join(bg_dir, 'ic_launcher_background.png'), 'PNG')
print(f"Generated adaptive background with color: {bg_color}")

print("Done!")
