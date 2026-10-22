# Vinilos — Aplicación Móvil Android

## Descripción del Proyecto

**Vinilos** es una aplicación móvil para dispositivos Android desarrollada para **The Software Design Company (TSDC)** (Curso MISW4203). El objetivo principal del proyecto es llevar la funcionalidad de la aplicación web Vinilos al entorno móvil, aprovechando una API REST orientada a la gestión de álbumes, artistas, músicos y coleccionistas.

El principal reto consiste en replicar y adaptar las funcionalidades existentes teniendo en cuenta las restricciones de diseño de los dispositivos móviles, las políticas del sistema operativo Android y su modelo de programación reactiva.

Este proyecto se desarrolla como una aplicación nativa para Android, utilizando **Kotlin** y **Jetpack Compose**.

---

## Tecnologías y Stack Utilizado

- **Kotlin**: Lenguaje principal de programación.
- **Android SDK**: Herramientas y bibliotecas de desarrollo para Android (`minSdk 26`, `compileSdk 37`, `targetSdk 37`).
- **Jetpack Compose**: Toolkit declarativo moderno para la construcción de interfaces de usuario nativas.
- **Material 3**: Sistema de diseño y componentes visuales de Material Design.
- **Navigation Compose**: Gestión de navegación entre pantallas.
- **Gradle**: Sistema de automatización de compilación y gestión de dependencias.
- **JDK 25**: Entorno Java requerido para ejecutar Gradle y el daemon de compilación (configurado en `gradle/gradle-daemon-jvm.properties`).
- **API REST**: Servicio remoto para la consulta y gestión de datos de la aplicación.

---

## Estructura General del Proyecto

La estructura del repositorio sigue la organización estándar de proyectos Android modernos con Gradle:

```text
MISW4203-TSDC-Vinilos/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/example/misw4203_tsdc_vinilos/
│   │   │   │       ├── MainActivity.kt
│   │   │   │       ├── navigation/
│   │   │   │       ├── ui/
│   │   │   │       │   ├── components/
│   │   │   │       │   ├── screens/
│   │   │   │       │   └── theme/
│   │   │   │       ├── data/
│   │   │   │       └── model/
│   │   │   ├── res/
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle.kts
├── gradle/
│   ├── wrapper/
│   └── gradle-daemon-jvm.properties
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

### Descripción de Directorios y Paquetes

- **`MainActivity.kt`**: Punto de entrada principal de la aplicación Android que inicializa la interfaz de usuario con Jetpack Compose.
- **`navigation/`**: Configuración de rutas y navegación entre las diferentes pantallas de la aplicación.
- **`ui/components/`**: Componentes visuales reutilizables (tarjetas, botones, barras superiores, etc.).
- **`ui/screens/`**: Pantallas principales de la aplicación (álbumes, artistas, músicos, detalles, etc.).
- **`ui/theme/`**: Configuración de estilos visuales, colores, tipografía y temas de Material 3 (`Theme.kt`, `Color.kt`, `Type.kt`).
- **`data/`**: Capa de datos, clientes de red y repositorios encargados de consumir la API REST.
- **`model/`**: Clases de datos que representan las entidades del dominio (Álbum, Artista, Músico, etc.).
- **`res/`**: Recursos estáticos de Android (iconos, imágenes, cadenas de texto y temas XML).

---

## Requisitos Previos

Para desarrollar, compilar y ejecutar el proyecto localmente, asegúrese de contar con lo siguiente:

1. **Android Studio** (versión reciente recomendada).
2. **JDK 25** instalado y configurado en el sistema y en Android Studio para Gradle.
3. **Android SDK** con las plataformas y herramientas de compilación compatibles (API 37).
4. **Git** para el control de versiones.
5. Un **Emulador Android** configurado (AVD) o un dispositivo físico con depuración USB habilitada.

> **Nota:** El proyecto incluye Gradle Wrapper (`gradlew`) y el archivo `gradle/gradle-daemon-jvm.properties` configurado específicamente para utilizar **JDK 25**.

---

## Configuración y Puesta en Marcha

### 1. Clonar el Repositorio

Abra su terminal y ejecute los siguientes comandos para clonar el repositorio y acceder al directorio del proyecto:

```bash
git clone https://github.com/sdserranoa/MISW4203-TSDC-Vinilos.git
cd MISW4203-TSDC-Vinilos
```

### 2. Verificar la Versión de Java (JDK 25)

Asegúrese de tener configurado JDK 25 en su equipo. Puede verificarlo desde la terminal:

```bash
java -version
```

Para comprobar la versión de la JVM utilizada por Gradle al compilar, ejecute:

- **En Windows (PowerShell / CMD):**
  ```bash
  .\gradlew.bat --version
  ```
- **En macOS / Linux:**
  ```bash
  ./gradlew --version
  ```
La salida debe indicar que la JVM de Gradle corresponde a **Java 25**.

### 3. Abrir el Proyecto en Android Studio

1. Inicie **Android Studio**.
2. Seleccione **Open** (Abrir proyecto existente).
3. Elija la carpeta raíz del repositorio (`MISW4203-TSDC-Vinilos`).
4. Espere a que Android Studio importe el proyecto y sincronice las dependencias de Gradle utilizando JDK 25.
5. Si se le solicita instalar componentes adicionales del SDK, acepte la instalación.

### 4. Compilación desde la Terminal

Si prefiere utilizar la línea de comandos, puede verificar la compilación ejecutando:

- **En Windows (PowerShell / CMD):**
  ```bash
  .\gradlew.bat assembleDebug
  ```
- **En macOS / Linux:**
  ```bash
  ./gradlew assembleDebug
  ```

Si la compilación finaliza correctamente, verá el mensaje:
```text
BUILD SUCCESSFUL
```

---

## Ejecución de la Aplicación

### Opción A: Desde Android Studio
1. Conecte un dispositivo físico con depuración USB o inicie un emulador virtual desde el **Device Manager**.
2. Asegúrese de que la configuración de ejecución seleccionada en la barra superior sea **`app`**.
3. Haga clic en el botón **Run ▶** (o presione `Shift + F10`).

### Opción B: Instalación directa mediante ADB
Una vez compilado el APK de depuración, puede instalarlo directamente en un dispositivo conectado ejecutando:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Integración con la API REST

La aplicación se comunica con el servicio backend de Vinilos para la obtención y gestión de la información. La integración se implementa de manera progresiva mediante la capa de red y los repositorios configurados en el paquete `data`.

---

## Integración Continua (CI/CD) — Pipeline de Features (`#DONE`)

El repositorio implementa un flujo de integración continua automatizado mediante **GitHub Actions** para asegurar la calidad del código antes de integrarlo a la rama `develop`.

### Flujo de Automatización de Features (`feature_develop.yml`)

Cada vez que un desarrollador sube cambios a una rama de tipo `feature/**`, se dispara automáticamente el workflow **Feature PR Auto Create**, el cual ejecuta dos etapas secuenciales:

1. **Etapa de Validación de Calidad (Prerequisito Obligatorio):**
   - **Pruebas Unitarias:** Ejecuta `./gradlew testDebugUnitTest --continue` para asegurar que todas las pruebas locales (JUnit) pasen exitosamente.
   - **Análisis Estático (Lint):** Ejecuta `./gradlew lintDebug` para detectar malas prácticas, problemas de compatibilidad y errores de código.
   - **Publicación de Reportes:** Los reportes de pruebas (`HTML`/`XML`) y de Lint (`HTML`/`SARIF`) se suben como artefactos descargables del workflow en GitHub Actions.
   - *Importante:* Si alguna prueba unitaria o regla de Lint falla, el pipeline se detiene de inmediato y **bloquea la creación del Pull Request**, protegiendo la estabilidad de `develop`.

2. **Etapa de Creación del Pull Request (`#DONE`):**
   - Si la validación de calidad pasa exitosamente, el pipeline analiza el mensaje del último commit.
   - **Sin `#DONE`:** El pipeline valida los cambios y finaliza exitosamente, proporcionando retroalimentación continua al desarrollador durante el avance de su trabajo.
   - **Con `#DONE`:** Indica que la historia de usuario o funcionalidad está terminada. El pipeline crea automáticamente un Pull Request desde la rama `feature/**` hacia `develop`, documentando en el cuerpo del PR que los tests y el análisis estático fueron aprobados.

### Guía para Desarrolladores: Cómo usar `#DONE`

1. **Crear la rama de funcionalidad desde `develop`:**
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/HU01-listar-albumes
   ```

2. **Realizar commits incrementales de desarrollo (sin `#DONE`):**
   ```bash
   git commit -m "feat: Implementar modelo y servicio de álbumes"
   git push origin feature/HU01-listar-albumes
   ```
   *(El pipeline validará que las pruebas pasen en cada push, sin abrir aún el Pull Request).*

3. **Finalizar la funcionalidad con la etiqueta `#DONE`:**
   Cuando la funcionalidad esté completa y cuente con sus respectivas pruebas unitarias:
   ```bash
   git commit -m "feat: Completar interfaz y pruebas unitarias de álbumes #DONE"
   git push origin feature/HU01-listar-albumes
   ```
   *(El pipeline ejecutará las pruebas y, al aprobarlas, creará automáticamente el Pull Request hacia `develop`).*

---

## Contribución y Buenas Prácticas

- Mantenga una estructura limpia y modular siguiendo los principios de arquitectura recomendados por Android (MVVM / Capas).
- Valide que el proyecto compile exitosamente antes de realizar un `push` o crear un Pull Request.
- Asegúrese de no versionar archivos locales (`local.properties`, credenciales de firma, cachés de IDE, etc.).
