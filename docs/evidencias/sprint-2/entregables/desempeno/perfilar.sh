#!/usr/bin/env bash
# Perfilamiento de Vinilos en un dispositivo Android físico conectado por USB.
#
# Requisitos: adb en el PATH, la depuración USB activa y la app ya instalada (APK release).
# En Windows se ejecuta desde Git Bash.
#
# Uso:
#   ./perfilar.sh inicio [recorrido]   Datos del dispositivo, 3 arranques en frío y reinicio de contadores.
#   (Recorra la app siguiendo el guion de la página "Análisis de desempeño" de la wiki.)
#   ./perfilar.sh fin [recorrido]      Fluidez, memoria y captura de pantalla; imprime la fila de resultados.
#
# Con varios dispositivos conectados, indique cuál con ANDROID_SERIAL=<serial> ./perfilar.sh ...
# Los archivos quedan en ./perfil_<modelo>_<recorrido>/.

set -euo pipefail
export MSYS_NO_PATHCONV=1 # Evita que Git Bash convierta las rutas del teléfono en rutas de Windows.

PKG=com.example.misw4203_tsdc_vinilos
ACTIVITY="$PKG/.MainActivity"
FASE="${1:-}"
RECORRIDO="${2:-app}"

prop() { adb shell getprop "$1" | tr -d '\r'; }

adb get-state >/dev/null 2>&1 || { echo "No hay un dispositivo conectado (adb devices)." >&2; exit 1; }
adb shell pm path "$PKG" >/dev/null 2>&1 || { echo "La app $PKG no está instalada en el dispositivo." >&2; exit 1; }

MODELO="$(prop ro.product.model)"
DIR="perfil_${MODELO// /_}_${RECORRIDO}"
mkdir -p "$DIR"

case "$FASE" in
  inicio)
    ram_kb=$(adb shell cat /proc/meminfo | awk '/MemTotal/{print $2}')
    {
      echo "Fabricante: $(prop ro.product.manufacturer)"
      echo "Modelo: $MODELO"
      echo "Android: $(prop ro.build.version.release) (API $(prop ro.build.version.sdk))"
      echo "RAM: $(awk -v k="$ram_kb" 'BEGIN{printf "%.1f GB", k/1048576}')"
      echo "Pantalla: $(adb shell wm size | tr -d '\r' | awk '{print $NF}'), densidad $(adb shell wm density | tr -d '\r' | awk '{print $NF}')"
      echo "Versión de la app: $(adb shell dumpsys package "$PKG" | tr -d '\r' | awk -F= '/versionName/{print $2; exit}')"
      echo "Fecha: $(date '+%Y-%m-%d %H:%M')"
    } > "$DIR/dispositivo.txt"
    cat "$DIR/dispositivo.txt"

    : > "$DIR/arranque.txt"
    for i in 1 2 3; do
      adb shell am force-stop "$PKG"
      sleep 2
      t=$(adb shell am start -W -n "$ACTIVITY" | tr -d '\r' | awk -F': ' '/TotalTime/{print $2}')
      echo "$t" >> "$DIR/arranque.txt"
      echo "Arranque en frío $i: ${t} ms"
      sleep 3
    done

    adb shell dumpsys gfxinfo "$PKG" reset >/dev/null
    echo
    echo "Contadores reiniciados. Recorra la app según el guion y luego ejecute:"
    echo "  ./perfilar.sh fin $RECORRIDO"
    ;;

  fin)
    adb shell dumpsys gfxinfo "$PKG" | tr -d '\r' > "$DIR/gfxinfo.txt"
    adb shell dumpsys meminfo "$PKG" | tr -d '\r' > "$DIR/meminfo.txt"
    adb exec-out screencap -p > "$DIR/captura.png"

    campo() { awk -F': ' -v k="$1" '$1==k{print $2; exit}' "$DIR/gfxinfo.txt"; }
    kb()    { awk -v k="$1" -v c="$2" 'index($0,k){print $c; exit}' "$DIR/meminfo.txt"; }
    mb()    { awk -v x="${1:-0}" 'BEGIN{printf "%.1f MB", x/1024}'; }

    total_pss=$(kb "TOTAL PSS:" 3); [ -n "$total_pss" ] || total_pss=$(kb "TOTAL:" 2)
    arranque=$(sort -n "$DIR/arranque.txt" 2>/dev/null | sed -n '2p')
    android="$(prop ro.build.version.release) (API $(prop ro.build.version.sdk))"
    p50=$(campo "50th percentile"); p90=$(campo "90th percentile"); p99=$(campo "99th percentile")

    {
      echo "Dispositivo: $MODELO · Android $android"
      echo "Arranque en frío (mediana de 3): ${arranque:-?} ms"
      echo "Memoria total (PSS): $(mb "$total_pss")"
      echo "Java heap: $(mb "$(kb "Java Heap:" 3)")"
      echo "Native heap: $(mb "$(kb "Native Heap:" 3)")"
      echo "Gráficos: $(mb "$(kb "Graphics:" 2)")"
      echo "Cuadros dibujados: $(campo "Total frames rendered")"
      echo "Cuadros con jank: $(campo "Janky frames")"
      echo "Tiempo por cuadro p50 / p90 / p99: ${p50:--} / ${p90:--} / ${p99:--}"
      echo "Hilo de UI lento: $(campo "Number Slow UI thread")"
      echo "Cargas lentas de imágenes: $(campo "Number Slow bitmap uploads")"
      echo
      echo "Fila para la wiki:"
      echo "| $MODELO | $android | ${arranque:-?} ms | $(mb "$total_pss") | $(mb "$(kb "Java Heap:" 3)") | $(campo "Janky frames") | ${p50:--} / ${p90:--} / ${p99:--} |"
    } | tee "$DIR/resumen.txt"
    ;;

  *)
    sed -n '2,14p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
    ;;
esac
