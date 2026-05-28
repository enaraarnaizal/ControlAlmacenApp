#  ControlAlmacenApp (App Tablet)

Aplicación Android (Tablet) para gestionar inventario de un almacén (sin servidor), con base de datos local **Room/SQLite**.  
Diseñada para un uso rápido, sencillo e intuitivo por parte de empleados y administradores.

---

##  Tecnologías
- **Android Studio**
- **Java**
- **Room (SQLite)**
- **Arquitectura MVC**
- **GitHub** (control de versiones)

---

# Control de Almacén

Aplicación Android para la gestión de inventario de un almacén pequeño.  
Está pensada para ser utilizada en una tablet situada en el acceso al almacén, permitiendo registrar entradas y salidas de productos de forma rápida.

La aplicación está desarrollada en **Java**, con interfaces diseñadas en **XML** y base de datos local mediante **Room / SQLite**.

---

## Manual de usuario

### 1. Objetivo de la aplicación

Control de Almacén permite gestionar de forma sencilla los productos de un almacén.  
Su objetivo principal es que los empleados puedan registrar rápidamente cuándo se añade o se retira un producto, manteniendo actualizado el stock disponible.

Además, la aplicación permite a los administradores gestionar usuarios, productos, albaranes e inventario.

---

## 2. Tipos de usuario

La aplicación dispone de dos tipos de perfil:

### Usuario normal

Puede acceder a la pantalla principal de Diario y realizar operaciones básicas sobre los productos:

- Ver productos disponibles.
- Buscar productos.
- Añadir unidades.
- Retirar unidades.
- Añadir nuevos productos.
- Editar, deshabilitar o borrar productos desde el menú de gestión.

### Administrador

Tiene acceso a todas las funcionalidades de la aplicación:

- Diario.
- Gestión de productos.
- Gestión de perfiles.
- Gestión de albaranes.
- Consulta de inventario.
- Generación de informes.

Para acceder a algunas opciones privadas, el administrador debe introducir su contraseña.

---

## 3. Inicio de sesión

Al abrir la aplicación aparece una pantalla con los perfiles activos.

Para iniciar sesión:

1. Seleccionar el perfil correspondiente.
2. Pulsar sobre la tarjeta del usuario.
3. La aplicación abrirá la pantalla principal de Diario.

Los perfiles deshabilitados no aparecen en esta pantalla, por lo que no pueden acceder a la aplicación.

---

## 4. Pantalla Diario

La pantalla Diario es la pantalla principal de trabajo.

Desde esta pantalla se pueden consultar los productos del almacén y modificar sus cantidades.

Cada producto muestra:

- Imagen del producto.
- Nombre.
- Cantidad actual.
- Estado de cantidad mínima.
- Botón para sumar unidades.
- Botón para restar unidades.

### Añadir una unidad

Para añadir una unidad a un producto:

1. Buscar el producto en la lista.
2. Pulsar el botón `+`.
3. La cantidad del producto aumenta automáticamente.

### Retirar una unidad

Para retirar una unidad:

1. Buscar el producto.
2. Pulsar el botón `-`.
3. La cantidad del producto disminuye automáticamente.

Si la cantidad llega al límite mínimo configurado, la aplicación muestra una alerta visual.

---

## 5. Buscar y filtrar productos

En la parte superior de la pantalla Diario hay un buscador que permite localizar productos por nombre.

También existen filtros rápidos:

- **Todos:** muestra todos los productos.
- **Límite mínimo:** muestra productos cuya cantidad está en el límite mínimo o por debajo.
- **Stock normal:** muestra productos que no están en situación crítica.

---

## 6. Añadir un producto

Para añadir un nuevo producto:

1. Pulsar el botón de añadir producto.
2. Introducir el nombre.
3. Introducir la cantidad inicial.
4. Introducir la cantidad mínima, si se desea.
5. Añadir una imagen del producto:
   - Sacando una foto con la cámara.
   - Seleccionando una imagen desde la galería.
6. Pulsar **Guardar**.

La aplicación comprueba que los campos obligatorios estén rellenados.  
También avisa si ya existe un producto con el mismo nombre.

---

## 7. Editar un producto

Para editar un producto:

1. Abrir el menú lateral.
2. Pulsar **Editar Producto**.
3. Seleccionar el producto que se desea modificar.
4. Cambiar los datos necesarios:
   - Nombre.
   - Cantidad.
   - Cantidad mínima.
   - Imagen.
   - Estado habilitado/deshabilitado.
5. Pulsar **Guardar cambios**.

---

## 8. Deshabilitar un producto

Un producto puede deshabilitarse cuando ya no se quiere controlar temporalmente.

Para deshabilitarlo:

1. Abrir el menú lateral.
2. Pulsar **Deshabilitar Producto**.
3. Seleccionar el producto.
4. Confirmar la acción.

Un producto deshabilitado no se tiene en cuenta para los avisos de cantidad mínima.

---

## 9. Borrar un producto

Para borrar un producto:

1. Abrir el menú lateral.
2. Pulsar **Borrar Producto**.
3. Seleccionar el producto.
4. Confirmar el borrado.

Esta acción elimina el producto de la base de datos.

---

## 10. Menú lateral

El menú lateral contiene las opciones avanzadas de la aplicación.

Está dividido en dos zonas:

### Gestión de productos

Disponible para los usuarios de la aplicación:

- Editar producto.
- Deshabilitar producto.
- Borrar producto.

### Administración

Disponible solo para administradores:

- Perfiles.
- Albaranes.
- Inventario.

Si el usuario no es administrador, estas opciones aparecen restringidas.

---

## 11. Gestión de perfiles

La pantalla de perfiles permite administrar los usuarios de la aplicación.

Desde esta pantalla se puede:

- Crear un nuevo perfil.
- Editar un perfil existente.
- Habilitar o deshabilitar un perfil.
- Borrar un perfil.
- Marcar un perfil como administrador.

### Crear un perfil

1. Entrar en **Perfiles**.
2. Pulsar el botón de nuevo perfil.
3. Introducir el nombre.
4. Indicar si será administrador.
5. Si es administrador, introducir email y contraseña.
6. Pulsar **Guardar**.

### Editar un perfil

1. Entrar en **Perfiles**.
2. Pulsar el botón de editar del usuario.
3. Modificar los datos necesarios.
4. Pulsar **Guardar**.

### Deshabilitar un perfil

1. Entrar en **Perfiles**.
2. Pulsar el botón de deshabilitar.
3. Confirmar la acción.

Un perfil deshabilitado no puede iniciar sesión.

---

## 12. Gestión de albaranes

La pantalla de albaranes permite guardar y consultar albaranes de entrega.

Desde esta pantalla se puede:

- Añadir nuevos albaranes.
- Consultar albaranes guardados.
- Editar albaranes.
- Filtrar albaranes por fecha.
- Seleccionar albaranes.
- Generar informes.

Los filtros disponibles son:

- Todos.
- Mes actual.
- Semana actual.
- Rango de fechas.

---

## 13. Añadir un albarán

Para añadir un albarán:

1. Entrar en **Albaranes**.
2. Pulsar el botón de nuevo albarán.
3. Introducir el CIF.
4. Introducir el nombre.
5. Introducir el importe.
6. Marcar si está pagado o no.
7. Añadir una imagen del albarán.
8. Pulsar **Guardar**.

La aplicación valida que los datos obligatorios estén completos y que el importe sea correcto.

---

## 14. Ver y editar un albarán

Para ver un albarán:

1. Entrar en **Albaranes**.
2. Localizar el albarán en la lista.
3. Pulsar **Ver detalles**.

Se mostrará la información del albarán:

- Nombre.
- CIF.
- Importe.
- Fecha.
- Estado de pago.
- Imagen.

Para editarlo:

1. Abrir el detalle del albarán.
2. Pulsar **Editar albarán**.
3. Modificar los datos.
4. Pulsar **Guardar cambios**.

---

## 15. Generar informe de albaranes

Para generar un informe:

1. Entrar en **Albaranes**.
2. Seleccionar uno o varios albaranes.
3. Pulsar **Generar Informe**.

El informe muestra:

- Número total de albaranes seleccionados.
- Importe total.
- Número de albaranes pagados.
- Número de albaranes sin pagar.
- CIFs incluidos.
- Nombres incluidos.

---

## 16. Inventario

La pantalla de inventario permite consultar el estado general del almacén.

Muestra un resumen con:

- Total de productos.
- Total de unidades.
- Productos en límite mínimo.
- Productos deshabilitados.

También permite filtrar por:

- Todos los productos.
- Productos en límite mínimo.
- Productos deshabilitados.

Esta pantalla está reservada a administradores.

---

## 17. Cierre de sesión

Para cerrar sesión:

1. Pulsar el icono de salida situado en la parte superior.
2. La aplicación vuelve a la pantalla de selección de usuario.

---

## 18. Control de inactividad

La aplicación controla el tiempo de inactividad del usuario.

Si pasa demasiado tiempo sin interacción, la sesión se cierra automáticamente y se vuelve a la pantalla inicial.

Cada interacción del usuario reinicia el tiempo de actividad.

---


## 🔗 LINKS

###  Mockups en Figma  
[![Mockup en Figma](https://img.shields.io/badge/Abrir_en-Figma-blue?logo=figma)](https://www.figma.com/make/N8KyadhP248zclhwFIBMf3/Tablet-format-mockup?p=f&t=hAddy6BxYTXOF53I-0&fullscreen=1)

###  Planificación en Trello  
[![Planificación en Trello](https://img.shields.io/badge/Abrir_en-Trello-026AA7?logo=trello&logoColor=white)](https://trello.com/b/FIajnHcb/controlalmacenapp)

###  Diagrama de Base de Datos (draw.io)  
[![Diagrama en Draw.io](https://img.shields.io/badge/Ver_Diagrama-Draw.io-orange?logo=diagramsdotnet)](https://drive.google.com/file/d/1zFJ9oP2Sqk98xDYLeB4oXFHxZpzJpROD/view?usp=sharing)

###  Testeo manual (Google Sheets)  
[![Testeo manual en Google Sheets](https://img.shields.io/badge/Ver_Testeo-Google_Sheets-34A853?logo=googlesheets&logoColor=white)](https://docs.google.com/spreadsheets/d/11qPl-wiuNrBxovcAEhGzVfkFiQk-ToRxB4DbKtx_xmI/edit?gid=0#gid=0)

---
