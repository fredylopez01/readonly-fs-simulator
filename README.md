# Read-Only File System Simulator

Simulador educativo de sistemas de archivos de solo lectura (SquashFS, ISO 9660, CRAMFS) implementado en Java con interfaz gráfica Swing.

## Objetivo y Funcionalidad

Este proyecto simula el comportamiento de sistemas de archivos de solo lectura, permitiendo al usuario:

- **Crear y gestionar archivos y carpetas** en una estructura jerárquica
- **Alternar entre modo lectura-escritura y solo lectura** para observar restricciones.
- **Visualizar el árbol de archivos** en tiempo real
- **Consultar registro de operaciones** (log de auditoría)
- **Observar excepciones del sistema** cuando se intenta modificar en modo solo lectura

### Funcionalidades principales:

- Creación de archivos y carpetas
- Modificación de contenido de archivos
- Eliminación y renombrado de elementos
- Activación/desactivación de modo solo lectura
- Sistema de logging de todas las operaciones
- Cálculo de estadísticas (número de archivos, tamaño total)
- Manejo de excepciones tipo POSIX (EROFS, EEXIST, ENOENT, EINVAL)

## Arquitectura de la Solución

El proyecto utiliza el patrón arquitectónico **MVP (Model-View-Presenter)** en una arquitectura monolítica.

```
┌─────────────────────────────────────────────────┐
│                    Main.java                    │
│          (Punto de entrada - Bootstrap)         │
└────────────┬────────────────────────────────────┘
             │
             ├──────────────┬──────────────┬
             │              │              │
        ┌────▼─────┐  ┌─────▼────┐  ┌──────▼───┐
        │   VIEW   │  │PRESENTER │  │  MODEL   │
        │ MainView │◄─┤  Main    │─►│FileSystem│
        │          │  │ Presenter│  │          │
        └──────────┘  └──────────┘  └─────┬────┘
                                          │
                    ┌─────────────────────┴─────────────────┐
                    │                                       │
              ┌─────▼────────┐                    ┌─────────▼────────┐
              │FileSystemItem│                    │OperationLogger   │
              │  (abstract)  │                    │                  │
              └─────┬────────┘                    └──────────────────┘
                    │
            ┌───────┴────────┐
            │                │
       ┌────▼─────┐    ┌─────▼────┐
       │   File   │    │  Folder  │
       └──────────┘    └──────────┘
```

### Responsabilidades por capa:

**Model (Modelo de dominio):**

- `FileSystem`: Gestiona la estructura completa del sistema de archivos
- `FileSystemItem`: Clase abstracta base para elementos del sistema
- `File`: Representa archivos con contenido
- `Folder`: Representa carpetas que contienen otros elementos
- `OperationLogger`: Registra todas las operaciones del sistema
- `exceptions/*`: Manejo de excepciones personalizadas

**View (Interfaz gráfica):**

- `MainView`: Ventana principal con componentes Swing (JTree, JTextArea, controles)
- Maneja únicamente la presentación y captura de eventos de usuario

**Presenter (Controlador):**

- `MainPresenter`: Conecta Model y View, procesa lógica de negocio
- Traduce acciones del usuario en llamadas al modelo
- Actualiza la vista con los cambios del modelo

## Descripción de Módulos

### 1. **model/FileSystem.java**

Núcleo del sistema que gestiona:

- Estado del modo solo lectura (`readOnlyMode`)
- Operaciones CRUD sobre archivos/carpetas
- Validación de permisos mediante `checkWritePermission()`
- Estructura de árbol desde el nodo raíz

**Métodos principales:**

- `createFile(name, content, parent)`: Crea archivos
- `createFolder(name, parent)`: Crea carpetas
- `deleteItem(item)`: Elimina elementos
- `modifyFile(file, newContent)`: Modifica contenido
- `setReadOnlyMode(enabled)`: Alterna modo de operación

### 2. **model/FileSystemItem.java**

Clase abstracta que define:

- Atributos comunes: nombre, tipo, padre, timestamps
- Método `getPath()`: Construye ruta completa (análogo a `d_path()` del kernel Linux)
- Método abstracto `getSize()`: Implementado por subclases

### 3. **model/File.java y model/Folder.java**

Implementaciones concretas:

- **File**: Almacena contenido como String, calcula tamaño en bytes
- **Folder**: Mantiene lista de hijos (`children`), calcula tamaño recursivo

### 4. **model/OperationLogger.java**

Sistema de auditoría:

- Registra todas las operaciones con timestamp
- Escribe a archivo `acciones.log` y mantiene lista en memoria
- Formato: `[timestamp] [TIPO_OPERACION] descripción`

### 5. **model/exceptions/\***

Jerarquía de excepciones personalizada:

- `FileSystemException`: Excepción base
- `ReadOnlyException`: Simula EROFS (errno 30) - operación bloqueada en modo solo lectura
- `ItemAlreadyExistsException`: Simula EEXIST (errno 17)
- `ItemNotFoundException`: Simula ENOENT (errno 2)
- `InvalidOperationException`: Simula EINVAL (errno 22)

### 6. **view/MainView.java**

Interfaz gráfica con:

- `JTree`: Visualización jerárquica del sistema de archivos
- `JTextArea`: Consola de log de operaciones
- `JCheckBox`: Toggle de modo solo lectura
- Botones de operaciones: Crear, Modificar, Eliminar, Renombrar
- Diálogos modales para entrada de datos

### 7. **presenter/MainPresenter.java**

Orquestador que:

- Conecta callbacks de la vista con métodos del modelo
- Maneja excepciones y muestra mensajes apropiados al usuario
- Refresca la vista después de cada operación
- Mantiene mapa de items (`itemMap`) para selección eficiente

## Manejo de Excepciones

El sistema implementa un robusto manejo de errores tipo POSIX:

| Excepción                    | Errno POSIX | Causa                                     | Manejo                                      |
| ---------------------------- | ----------- | ----------------------------------------- | ------------------------------------------- |
| `ReadOnlyException`          | EROFS (30)  | Intento de escritura en modo solo lectura | Bloquea operación, muestra diálogo de error |
| `ItemAlreadyExistsException` | EEXIST (17) | Nombre duplicado en el mismo directorio   | Solicita nuevo nombre al usuario            |
| `ItemNotFoundException`      | ENOENT (2)  | Item seleccionado no encontrado           | Informa al usuario, mantiene estado actual  |
| `InvalidOperationException`  | EINVAL (22) | Operación inválida (ej: eliminar root)    | Rechaza operación con mensaje explicativo   |

**Flujo de manejo:**

1. El presenter invoca operación del modelo dentro de bloque try-catch
2. El modelo valida permisos mediante `checkWritePermission()`
3. Si está en modo solo lectura, lanza `ReadOnlyException`
4. El presenter captura la excepción y:
   - Muestra diálogo de error al usuario
   - Registra el intento bloqueado en el log
   - Actualiza barra de estado con mensaje descriptivo

## Guía de Ejecución

### Requisitos previos:

- **Java JDK 11 o superior** instalado
- Variable de entorno `JAVA_HOME` configurada

### Compilación y generación del JAR:

**Opción 1: Usando línea de comandos**

```bash
# 1. Navegar al directorio raíz del proyecto
cd /ruta/al/proyecto

# 2. Compilar todos los archivos Java
javac -d bin $(Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })

# 3. Crear el archivo JAR ejecutable
jar cfm app.jar src/manifest.txt -C bin .

# 4. Ejecutar el JAR
java -jar FileSystemSimulator.jar
```

### Verificación de ejecución:

Al ejecutar correctamente, debería:

1. Abrirse ventana gráfica de 800x500 píxeles
2. Mostrar árbol de archivos con estructura demo (root, documents, images, config)
3. Mostrar log con mensaje "File system initialized in read-write mode"
4. Estar en modo READ-WRITE (indicador verde)

## Uso de la Aplicación

1. **Activar modo solo lectura**: Marcar checkbox "Read-Only Mode"
2. **Crear archivo**: Botón "➕ Create File", ingresar nombre y contenido
3. **Crear carpeta**: Botón "📁 Create Folder", ingresar nombre
4. **Modificar archivo**: Seleccionar archivo, botón "✏️ Modify", editar contenido
5. **Renombrar**: Seleccionar item, botón "🖊️ Rename", ingresar nuevo nombre
6. **Eliminar**: Seleccionar item, botón "🗑️ Delete", confirmar eliminación
7. **Ver log**: Panel derecho muestra todas las operaciones realizadas

## 📄 Logs

El sistema genera automáticamente el archivo `acciones.log` con registro de todas las operaciones:

```
================================================================================
READ-ONLY FILE SYSTEM SIMULATOR - OPERATION LOG
================================================================================
Session started: 2025-11-13 15:30:45.123
================================================================================

[2025-11-13 15:30:45.125] [SYSTEM] File system initialized in read-write mode
[2025-11-13 15:30:45.130] [SYSTEM] Demo structure created
[2025-11-13 15:31:02.456] [CREATE_FILE] Created file: /root/test.txt (12 bytes)
[2025-11-13 15:31:15.789] [MODE_CHANGE] File system changed from READ-WRITE to READ-ONLY
[2025-11-13 15:31:20.012] [ERROR] Attempted 'modify_file' in read-only mode - BLOCKED
```

## Pruebas Sugeridas

1. **Prueba de modo solo lectura:**

   - Crear archivo en modo escritura
   - Activar modo solo lectura
   - Intentar modificar → debe bloquearse con error EROFS

2. **Prueba de nombres duplicados:**

   - Crear archivo "test.txt"
   - Intentar crear otro "test.txt" en misma carpeta → error EEXIST

3. **Prueba de operaciones inválidas:**

   - Intentar eliminar carpeta root → error EINVAL

4. **Prueba de persistencia del log:**
   - Realizar varias operaciones
   - Cerrar aplicación
   - Revisar archivo `acciones.log` → debe contener todo el historial

## Tecnologías Utilizadas

- **Java 11+**: Lenguaje de programación
- **Java Swing**: Framework de interfaz gráfica
- **Patrón MVP**: Arquitectura de presentación
- **File I/O**: Sistema de logging persistente

## Referencias

Este simulador está basado en los conceptos de sistemas de archivos de solo lectura:

- SquashFS (Linux compressed read-only file system)
- ISO 9660 (CD-ROM file system)
- CRAMFS (Compressed ROM file system)

---

**Nota:** Este simulador es con fines educativos y no implementa características avanzadas como compresión real, checksums o manejo de bloques físicos.
