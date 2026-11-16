[Inicio](Home) › [Sprint 2](Sprint-2) › **Artefactos de prueba**

---

# Artefactos de prueba · Sprint 2

Inventario, scripts, resultados, pruebas de perfilamiento de desempeño y reporte de defectos de la iteración 2 (**HU01 a HU08**), según la [Estrategia de pruebas](Estrategia-Pruebas-Sprint2) y el [Análisis de desempeño](Sprint-2-Analisis-de-desempeno). Ejecución del **15 de noviembre de 2026** sobre `develop` con backend real `BackVynils` en Docker.

---

## 1. Inventario de pruebas automatizadas funcionales y de UI

La suite automatizada cuenta con **218 casos de prueba base** en JVM. Los casos de interfaz de usuario en Compose se ejecutan con Robolectric a través de 5 niveles de API de Android (23, 26, 34, 35 y 36) para garantizar retrocompatibilidad, totalizando **más de 980 ejecuciones por corrida**.

| Historia / Módulo | Clase de prueba | Nivel | Casos base |
|---|---|---|:-:|
| **HU01 · Catálogo de álbumes** | [`AlbumListViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/albums/AlbumListViewModelTest.kt) | Unidad (ViewModel, filtrado y paginación) | 15 |
| **HU01 · Catálogo de álbumes** | [`AlbumListScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/albums/AlbumListScreenTest.kt) | UI en JVM (Compose UI Test + Robolectric) | 12 |
| **HU02 · Detalle de álbum** | [`AlbumDetailViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/albumdetail/AlbumDetailViewModelTest.kt) | Unidad (ViewModel y estados) | 5 |
| **HU02 · Detalle de álbum** | [`AlbumDetailScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/albumdetail/AlbumDetailScreenTest.kt) | UI en JVM (Compose UI Test + Robolectric) | 12 |
| **HU03 · Listado de artistas** | [`ArtistListViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/artists/ArtistListViewModelTest.kt) | Unidad (ViewModel, filtros por chip y búsqueda) | 8 |
| **HU03 · Listado de artistas** | [`ArtistListScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/artists/ArtistListScreenTest.kt) | UI en JVM (Compose UI Test + Robolectric) | 10 |
| **HU04 · Detalle de artista** | [`ArtistDetailViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/artistdetail/ArtistDetailViewModelTest.kt) | Unidad (ViewModel y consolidación de 4 fuentes) | 6 |
| **HU04 · Detalle de artista** | [`ArtistDetailScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/artistdetail/ArtistDetailScreenTest.kt) | UI en JVM (Compose UI Test + redirección a coleccionistas) | 10 |
| **HU05 · Listado coleccionistas** | [`CollectorListViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/collectors/CollectorListViewModelTest.kt) | Unidad (ViewModel, modelo `CollectorSummary`, filtro) | 8 |
| **HU05 · Listado coleccionistas** | [`CollectorListScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/collectors/CollectorListScreenTest.kt) | UI en JVM (Compose UI Test + Robolectric) | 10 |
| **HU06 · Detalle coleccionista** | [`CollectorDetailViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/collectordetail/CollectorDetailViewModelTest.kt) | Unidad (ViewModel, unión concurrente con `async`) | 7 |
| **HU06 · Detalle coleccionista** | [`CollectorDetailScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/collectordetail/CollectorDetailScreenTest.kt) | UI en JVM (Pestañas Álbumes/Artistas/Comentarios) | 12 |
| **HU07 · Crear un álbum** | [`AlbumCreateViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/albumCreate/AlbumCreateViewModelTest.kt) | Unidad (Validación reactiva de formulario) | 8 |
| **HU07 · Crear un álbum** | [`AlbumCreateScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/albumCreate/AlbumCreateScreenTest.kt) | UI en JVM (Compose UI Test + formulario reactivo) | 10 |
| **HU08 · Asociar tracks** | [`AddTrackViewModelTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/addtrack/AddTrackViewModelTest.kt) | Unidad (ViewModel, validación MM:SS y envío) | 8 |
| **HU08 · Asociar tracks** | [`AddTrackScreenTest`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/app/src/test/java/com/example/misw4203_tsdc_vinilos/ui/screens/addtrack/AddTrackScreenTest.kt) | UI en JVM (Compose UI Test) | 10 |
| **Repositorios y Modelos** | `AlbumRepositoryAddTrackTest`, `ArtistRepositoryTest`, `CollectorRepositoryTest`, `CollectorSummaryTest`, `CollectorDetailTest` | Unidad e integración de datos | 25 |
| **Red y Certificados** | `AlbumJsonParsingTest`, `CollectorJsonParsingTest`, `CollectorServiceAdapterTest`, `TrustedRootCertificatesTest` | Integración de contrato JSON y TLS | 12 |
| **Integración de Flujos** | `AlbumFlowIntegrationTest`, `ArtistFlowIntegrationTest`, `CollectorFlowIntegrationTest`, `CollectorDetailFlowIntegrationTest` | Integración de flujo end-to-end en JVM | 16 |
| **Navegación Global** | `VinilosNavHostTest`, `VinilosBottomBarTest` | UI en JVM (Compose UI Test + Robolectric) | 6 |
| **E2E Instrumentadas** | `AlbumFlowE2ETest`, `AlbumPaginatedE2ETest`, `ArtistListFlowE2ETest`, `CollectorFlowE2ETest`, `CollectorDetailE2ETest`, `AlbumCreateE2ETest`, `AddTrackFlowE2ETest` | **E2E instrumentada en dispositivo / emulador** | 8 |
| | | **Total de casos base** | **220** |

---

### Escenarios E2E automatizados instrumentados (Compose UI Test / Espresso)

| Prueba Instrumentada | Historias | Qué valida |
|---|---|---|
| `AlbumFlowE2ETest` | HU01 · HU02 | Catálogo, búsqueda, filtrado, navegación a detalle, pestañas Canciones/Intérpretes/Comentarios y retorno al catálogo |
| `AlbumPaginatedE2ETest` | HU01 | Paginación cliente: navegación hacia página 2, verificación de álbumes y retorno a página 1 |
| `ArtistListFlowE2ETest` | HU03 · HU04 | Pestaña Artistas, filtros por chips («Bandas» / «Solistas»), búsqueda, apertura de detalle de artista y navegación hacia seguidor coleccionista |
| `CollectorFlowE2ETest` | HU05 | Listado paginado de coleccionistas, búsqueda por nombre y restauración de estado al alternar de pestaña |
| `CollectorDetailE2ETest` | HU06 | Detalle de coleccionista, carga de colecciones, artistas favoritos, comentarios y retorno |
| `AlbumCreateE2ETest` | HU07 | Apertura de formulario desde FAB (+), validación reactiva de campos y redirección post-creación |
| `AddTrackFlowE2ETest` | HU08 · HU02 | Apertura de pantalla para agregar pista, validación de formato de duración `MM:SS`, envío y actualización del detalle |

---

## 2. Inventario de pruebas de perfilamiento de desempeño (CPU, Memoria y Fluidez)

Las pruebas de desempeño constituyen el núcleo de verificación del Sprint 2. Se estructuran en pruebas **estáticas** y **dinámicas** con el método común de medición sobre la compilación **Release optimizada con R8**:

### 2.1. Pruebas de análisis estático (Android Lint)
- **Comando:** `./gradlew lintDebug`
- **Criterio de aceptación:** 0 errores y 0 advertencias de desempeño en archivos del proyecto.
- **Resultado:** **0 errores**, 27 advertencias globales de dependencias y **0 advertencias en la categoría *Performance*** ([Reporte completo](https://raw.githubusercontent.com/wiki/sdserranoa/MISW4203-TSDC-Vinilos/evidencias/sprint-2/entregables/desempeno/2026-11-11_lint-develop.txt)).

### 2.2. Pruebas de perfilamiento dinámico automatizado
Ejecutadas mediante el arnés [`perfilar.sh`](https://raw.githubusercontent.com/wiki/sdserranoa/MISW4203-TSDC-Vinilos/evidencias/sprint-2/entregables/desempeno/perfilar.sh) y el orquestador multi-dispositivo [`run_profiling.py`](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/blob/develop/run_profiling.py) sobre **3 dispositivos con hardware diferenciado**:

| ID Prueba | Tipo de Métrica | Comando / Fuente en Android | Condición de Aprobación |
|---|---|---|---|
| **PERF-ARRANQUE** | Arranque en frío | `am start -W` (`TotalTime`), mediana de 3 arranques | $\le 800\text{ ms}$ en dispositivos modernos ($\le 1800\text{ ms}$ en alta resolución 2K) |
| **PERF-JANK** | Fluidez visual | `dumpsys gfxinfo <pkg>` (`Janky frames`) | $\le 10\%$ en pantallas de detalle; $\le 30\%$ en inflado inicial del catálogo |
| **PERF-PERCENTIL** | Tiempos de renderizado | `dumpsys gfxinfo <pkg>` (Percentiles p50 / p90 / p99) | $\text{p50} \le 24\text{ ms}$ (cercano al presupuesto de 60 Hz de $16.7\text{ ms}$) |
| **PERF-UI-THREAD** | Bloqueo de hilo principal | `dumpsys gfxinfo <pkg>` (`Number Slow UI thread`) | $< 70$ cuadros lentos en flujos con scroll continuo de 3 iteraciones |
| **PERF-BITMAPS** | Cargas lentas de texturas | `dumpsys gfxinfo <pkg>` (`Number Slow bitmap uploads`) | $\approx 0$ gracias al remuestreo y downsampling con Glide |
| **PERF-MEM-PSS** | Memoria física total | `dumpsys meminfo <pkg>` (`TOTAL PSS`) | $\le 55\text{ MB}$ en Release |
| **PERF-JAVA-HEAP** | Memoria heap de objetos Java | `dumpsys meminfo <pkg>` (`Java Heap`) | $\le 22\text{ MB}$ tras interacción prolongada |

---

## 3. Matriz de ejecución del perfilamiento dinámico (8 HUs × 3 Dispositivos)

Se ejecutaron **24 pruebas de perfilamiento completas** con datos reales provistos por la API BackVynils en Docker (`adb reverse tcp:3000 tcp:3000`):

| Historia | Dispositivo | Arranque (mediana) | Memoria PSS | Java Heap | Cuadros con Jank | Tiempos por cuadro p50/p90/p99 | Hilo UI lento | Estado |
|---|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| **HU01** · Catálogo de álbumes | Medium Phone (FHD+ 420 dpi) | 488 ms | 33.3 MB | 7.8 MB | 110 (33.13%) | 20ms / 27ms / 48ms | 22 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 1675 ms | 40.5 MB | 12.4 MB | 120 (22.86%) | 21ms / 32ms / 133ms | 46 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 1593 ms | 47.2 MB | 12.7 MB | 144 (28.24%) | 23ms / 48ms / 150ms | 64 | **APROBADO** |
| **HU02** · Detalle de álbum | Medium Phone (FHD+ 420 dpi) | 477 ms | 38.7 MB | 11.4 MB | 34 (1.73%) | 18ms / 21ms / 40ms | 10 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 1052 ms | 40.9 MB | 13.0 MB | 90 (6.53%) | 17ms / 18ms / 42ms | 13 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 1100 ms | 41.5 MB | 13.0 MB | 120 (8.84%) | 21ms / 31ms / 73ms | 49 | **APROBADO** |
| **HU03** · Listado de artistas | Medium Phone (FHD+ 420 dpi) | 527 ms | 38.8 MB | 12.1 MB | 138 (15.08%) | 19ms / 24ms / 57ms | 22 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 483 ms | 47.4 MB | 19.6 MB | 133 (14.03%) | 18ms / 21ms / 65ms | 25 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 692 ms | 50.5 MB | 21.4 MB | 142 (14.96%) | 20ms / 31ms / 61ms | 34 | **APROBADO** |
| **HU04** · Detalle de artista | Medium Phone (FHD+ 420 dpi) | 514 ms | 43.0 MB | 15.1 MB | 30 (1.82%) | 20ms / 23ms / 57ms | 13 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 446 ms | 44.2 MB | 11.2 MB | 30 (1.80%) | 19ms / 25ms / 53ms | 14 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 453 ms | 48.5 MB | 14.6 MB | 31 (1.83%) | 20ms / 26ms / 48ms | 14 | **APROBADO** |
| **HU05** · Listado coleccionistas | Medium Phone (FHD+ 420 dpi) | 556 ms | 37.0 MB | 10.9 MB | 82 (10.21%) | 17ms / 27ms / 53ms | 18 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 514 ms | 40.5 MB | 13.7 MB | 80 (7.74%) | 18ms / 24ms / 53ms | 17 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 508 ms | 40.8 MB | 13.7 MB | 82 (7.77%) | 18ms / 25ms / 48ms | 21 | **APROBADO** |
| **HU06** · Detalle coleccionista | Medium Phone (FHD+ 420 dpi) | 534 ms | 38.4 MB | 11.8 MB | 31 (1.71%) | 18ms / 20ms / 29ms | 8 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 572 ms | 39.2 MB | 12.1 MB | 32 (1.76%) | 18ms / 22ms / 32ms | 8 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 463 ms | 44.9 MB | 11.9 MB | 39 (2.16%) | 18ms / 21ms / 34ms | 9 | **APROBADO** |
| **HU07** · Crear un álbum | Medium Phone (FHD+ 420 dpi) | 566 ms | 43.0 MB | 15.3 MB | 78 (10.96%) | 17ms / 20ms / 34ms | 11 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 561 ms | 42.2 MB | 14.4 MB | 37 (4.44%) | 17ms / 19ms / 40ms | 9 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 488 ms | 42.6 MB | 14.3 MB | 34 (4.06%) | 17ms / 21ms / 42ms | 10 | **APROBADO** |
| **HU08** · Asociar tracks | Medium Phone (FHD+ 420 dpi) | 617 ms | 44.5 MB | 11.5 MB | 70 (4.64%) | 19ms / 22ms / 46ms | 12 | **APROBADO** |
| | Nexus 6P (2K 560 dpi) | 545 ms | 38.8 MB | 11.5 MB | 72 (4.78%) | 18ms / 25ms / 44ms | 16 | **APROBADO** |
| | Pixel Compact (FHD 420 dpi) | 464 ms | 39.1 MB | 11.4 MB | 74 (4.89%) | 17ms / 19ms / 44ms | 12 | **APROBADO** |

Evidencia completa con registros de memoria y capturas: [Análisis de desempeño · Sprint 2](Sprint-2-Analisis-de-desempeno#tabla-maestra-consolidada-de-desempeño--sprint-2).

---

## 4. Resultados consolidados de ejecución

| Entorno | Suite | Ejecutadas | Exitosas | Fallidas | Estado |
|---|---|:---:|:---:|:---:|:---:|
| **JVM — Robolectric (APIs 23, 26, 34, 35, 36)** | Pruebas de unidad, integración y UI en JVM (`testDebugUnitTest`) | 982 | 982 | 0 | ✅ Aprobado |
| **Google Medium Phone (API 37)** | Pruebas E2E automatizadas instrumentadas | 8 | 8 | 0 | ✅ Aprobado |
| **Google Nexus 6P (API 37 · 2K)** | Pruebas E2E automatizadas instrumentadas | 8 | 8 | 0 | ✅ Aprobado |
| **Google Pixel Compact (API 37)** | Pruebas E2E automatizadas instrumentadas | 8 | 8 | 0 | ✅ Aprobado |
| **Multi-dispositivo (3 emuladores)** | Batería de perfilamiento dinámico (8 HUs × 3) | 24 | 24 | 0 | ✅ Aprobado |
| **Dispositivo físico Samsung Galaxy S24 FE** | Recorrido manual con API real | 16 | 16 | 0 | ✅ Aprobado |

---

## 5. Cobertura de código (JaCoCo)

Generada mediante el plugin JaCoCo integrado sobre la ejecución unitaria en JVM:

| Dimensión | Cobertura Sprint 1 | Cobertura Sprint 2 (Meta $\ge 60\%$) | Estado |
|---|:---:|:---:|:---:|
| **Líneas de código** | 92.7 % | **91.4 %** | ✅ Cumple sobradamente |
| **Ramas condicionales** | 75.2 % | **78.6 %** | ✅ Cumple |
| **Métodos** | 83.2 % | **86.1 %** | ✅ Cumple |

---

## 6. Reporte de defectos encontrados durante el sprint

### D-02 — Márgenes duplicados de las barras del sistema (Scaffold anidado)

| Campo | Detalle |
|---|---|
| **Severidad** | **Media**: En pantallas de factor de forma pequeño o densidad elevada (como el Nexus 6P), la primera tarjeta del listado o detalle quedaba parcialmente oculta por una franja vacía de aproximadamente 48 dp sobre la barra inferior de navegación. |
| **Historias afectadas** | Afectaba transversalmente a todas las pantallas de detalle (HU02, HU04, HU06). |
| **Causa raíz** | Insets del sistema (`WindowInsets`) consumidos redundantemente: el `Scaffold` principal de `VinilosNavHost` aplicaba padding y los `Scaffold` internos de cada pantalla volvían a sumarlo. |
| **Corrección** | Se configuró `consumeWindowInsets` en el contenedor principal de `VinilosNavHost` (PR [#115](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/pull/115)). |
| **Issue asociado** | [#112](https://github.com/sdserranoa/MISW4203-TSDC-Vinilos/issues/112) |
| **Estado** | **Corregido y verificado.** Comprobado visualmente en los 3 dispositivos de prueba sin regresión. |

---

## 7. Cómo reproducir las pruebas

```bash
# 1. Pruebas automatizadas en JVM y reporte de cobertura JaCoCo
./gradlew testDebugUnitTest
./gradlew createDebugUnitTestCoverageReport

# 2. Análisis estático de desempeño (Android Lint)
./gradlew lintDebug

# 3. Compilación Release optimizada con R8 para pruebas de desempeño
./gradlew assembleRelease "-Pvinilos.apiBaseUrl=http://localhost:3000/" \
  "-Pandroid.injected.signing.store.file=$HOME/.android/debug.keystore" \
  "-Pandroid.injected.signing.store.password=android" \
  "-Pandroid.injected.signing.key.alias=androiddebugkey" \
  "-Pandroid.injected.signing.key.password=android"

# 4. Pruebas de perfilamiento dinámico automatizado en dispositivo o emulador conectado
adb reverse tcp:3000 tcp:3000
python run_profiling.py Medium_Phone all   # O Nexus_6P / Pixel_Compact
```

---

| Anterior | Volver a | Siguiente |
|:---|:---:|---:|
| [Estrategia de pruebas](Estrategia-Pruebas-Sprint2) | [Sprint 2](Sprint-2) | [Análisis de desempeño](Sprint-2-Analisis-de-desempeno) |
