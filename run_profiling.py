import subprocess
import time
import os
import sys

PKG = "com.example.misw4203_tsdc_vinilos"

def adb(cmd):
    full_cmd = f"adb {cmd}"
    res = subprocess.run(full_cmd, shell=True, capture_output=True, text=True)
    return res.stdout.strip()

def get_screen_size():
    out = adb("shell wm size")
    for part in out.split():
        if "x" in part and part.replace("x", "").isdigit():
            w, h = part.split("x")
            return int(w), int(h)
    return 1080, 2400

W, H = get_screen_size()

def rel_tap(rx, ry):
    x = int(rx * W)
    y = int(ry * H)
    adb(f"shell input tap {x} {y}")
    time.sleep(1)

def rel_swipe(rx1, ry1, rx2, ry2, duration=300):
    x1 = int(rx1 * W)
    y1 = int(ry1 * H)
    x2 = int(rx2 * W)
    y2 = int(ry2 * H)
    adb(f"shell input swipe {x1} {y1} {x2} {y2} {duration}")
    time.sleep(1)

def key(code):
    adb(f"shell input keyevent {code}")
    time.sleep(0.8)

def text(t):
    adb(f"shell input text {t}")
    time.sleep(0.8)

def bash(cmd, env_device=None):
    bash_path = r"C:\Program Files\Git\bin\bash.exe"
    repo_dir = r"D:/Proyectos/Mobil/MISW4203-TSDC-Vinilos"
    env_str = f"export DEVICE_NAME='{env_device}'; " if env_device else ""
    full_script = f"cd {repo_dir} && {env_str}{cmd}"
    res = subprocess.run([bash_path, "-c", full_script], capture_output=True, text=True)
    return res.stdout.strip()

def ensure_foreground():
    adb(f"shell am start -n {PKG}/.MainActivity")
    time.sleep(1)

def ensure_home():
    ensure_foreground()
    rel_tap(0.17, 0.94) # Tab Albumes
    time.sleep(1)

def hu01_actions():
    ensure_home()
    rel_tap(0.50, 0.16) # Search box
    text("Buscando")
    rel_tap(0.90, 0.16) # clear
    rel_swipe(0.50, 0.75, 0.50, 0.25, 300) # scroll down
    rel_swipe(0.50, 0.25, 0.50, 0.75, 300) # scroll up
    rel_tap(0.50, 0.94) # Tab Artistas
    time.sleep(1)
    rel_tap(0.17, 0.94) # Tab Albumes
    time.sleep(1)

def hu02_actions():
    ensure_home()
    rel_tap(0.30, 0.38) # Tap primer album (Buscando America)
    time.sleep(2)
    rel_swipe(0.50, 0.75, 0.50, 0.35, 300) # Scroll canciones
    rel_swipe(0.50, 0.35, 0.50, 0.75, 300)
    rel_tap(0.50, 0.25) # Tab Interpretes
    time.sleep(1)
    rel_tap(0.30, 0.38) # Tap interprete asociado
    time.sleep(2)
    key(4) # Back a detalle de album
    time.sleep(1)
    rel_tap(0.80, 0.25) # Tab Comentarios
    time.sleep(1)
    rel_swipe(0.50, 0.65, 0.50, 0.40, 300)
    key(4) # Back a catalogo
    time.sleep(1)

def hu03_actions():
    ensure_home()
    rel_tap(0.50, 0.94) # Tab Artistas
    time.sleep(1.5)
    rel_tap(0.40, 0.20) # Chip Bandas
    time.sleep(1)
    rel_tap(0.65, 0.20) # Chip Solistas
    time.sleep(1)
    rel_tap(0.18, 0.20) # Chip Todos
    time.sleep(1)
    rel_tap(0.50, 0.15) # Search
    text("Ruben")
    rel_tap(0.90, 0.15) # clear
    rel_swipe(0.50, 0.75, 0.50, 0.30, 300) # scroll down
    rel_swipe(0.50, 0.30, 0.50, 0.75, 300) # scroll up
    rel_tap(0.17, 0.94) # Tab Albumes
    time.sleep(1)

def hu04_actions():
    ensure_home()
    rel_tap(0.50, 0.94) # Tab Artistas
    time.sleep(1.5)
    rel_tap(0.50, 0.38) # Tap artista (Ruben Blades)
    time.sleep(2)
    rel_swipe(0.50, 0.75, 0.50, 0.35, 300) # Scroll detalle
    rel_swipe(0.50, 0.75, 0.50, 0.35, 300) # Scroll a coleccionistas
    rel_tap(0.35, 0.78) # Tap coleccionista seguidor
    time.sleep(2)
    key(4) # Back a artista
    time.sleep(1)
    key(4) # Back a lista artistas
    time.sleep(1)
    rel_tap(0.17, 0.94) # Volver a albumes
    time.sleep(1)

def hu05_actions():
    ensure_home()
    rel_tap(0.83, 0.94) # Tab Coleccionistas
    time.sleep(1.5)
    rel_tap(0.50, 0.16) # Search box
    text("Manolo")
    rel_tap(0.90, 0.16) # clear
    rel_swipe(0.50, 0.75, 0.50, 0.30, 300)
    rel_swipe(0.50, 0.30, 0.50, 0.75, 300)
    rel_tap(0.17, 0.94) # Tab Albumes
    time.sleep(1)
    rel_tap(0.83, 0.94) # Volver a Coleccionistas (comprobar restauracion)
    time.sleep(1)

def hu06_actions():
    ensure_home()
    rel_tap(0.83, 0.94) # Tab Coleccionistas
    time.sleep(1.5)
    rel_tap(0.50, 0.35) # Tap Manolo Bellon
    time.sleep(2)
    rel_swipe(0.50, 0.70, 0.50, 0.35, 300) # Scroll albumes
    rel_tap(0.50, 0.38) # Tab Artistas Favoritos
    time.sleep(1)
    rel_tap(0.80, 0.38) # Tab Comentarios
    time.sleep(1)
    rel_swipe(0.50, 0.70, 0.50, 0.45, 300)
    key(4) # Back a lista
    time.sleep(1)

def hu07_actions():
    ensure_home()
    rel_tap(0.88, 0.83) # Tap FAB (+) Crear Album
    time.sleep(2)
    rel_tap(0.50, 0.25) # Campo Nombre
    text("AlbumTest")
    rel_swipe(0.50, 0.75, 0.50, 0.30, 300) # Scroll formulario
    rel_swipe(0.50, 0.30, 0.50, 0.75, 300)
    key(4) # Cancelar y volver al catalogo
    time.sleep(1)

def hu08_actions():
    ensure_home()
    rel_tap(0.30, 0.38) # Tap primer album
    time.sleep(2)
    rel_tap(0.82, 0.30) # Tap boton Agregar Track en header
    time.sleep(1.5)
    rel_tap(0.50, 0.30) # Campo nombre cancion
    text("TrackTest")
    key(4) # cancelar y volver a detalle
    time.sleep(1)
    key(4) # volver a catalogo
    time.sleep(1)

def profile_hu(hu, actions_fn, device_name):
    print(f"\n=======================================================")
    print(f"=== PERFILANDO {hu.upper()} EN {device_name} (DATOS REALES) ===")
    print(f"=======================================================")
    
    # 1. Inicio
    print(f"-> Ejecutando inicio {hu}...")
    out_inicio = bash(f"./perfilar.sh inicio {hu}", env_device=device_name)
    print(out_inicio)
    time.sleep(2)
    
    # 2. Interaccion real
    print(f"-> Ejecutando interaccion de usuario (3 iteraciones)...")
    for i in range(1, 4):
        print(f"   [Iteracion {i}/3]")
        actions_fn()
        time.sleep(1)
        
    # 3. Asegurar primer plano antes de fin
    ensure_foreground()
    time.sleep(1)
    
    # 4. Fin
    print(f"-> Ejecutando fin {hu}...")
    out_fin = bash(f"./perfilar.sh fin {hu}", env_device=device_name)
    print(out_fin)
    return out_fin

def run_suite(device_name, target_hu="all"):
    global W, H
    W, H = get_screen_size()
    print(f"Iniciando suite en {device_name} con resolucion {W}x{H}...")
    
    hus = {
        "hu01": hu01_actions,
        "hu02": hu02_actions,
        "hu03": hu03_actions,
        "hu04": hu04_actions,
        "hu05": hu05_actions,
        "hu06": hu06_actions,
        "hu07": hu07_actions,
        "hu08": hu08_actions,
    }
    
    if target_hu == "all":
        for hu_name, fn in hus.items():
            profile_hu(hu_name, fn, device_name)
    elif target_hu in hus:
        profile_hu(target_hu, hus[target_hu], device_name)
    else:
        print(f"HU no reconocida: {target_hu}")

if __name__ == "__main__":
    dev = sys.argv[1] if len(sys.argv) > 1 else "Medium_Phone"
    target = sys.argv[2] if len(sys.argv) > 2 else "all"
    run_suite(dev, target)
