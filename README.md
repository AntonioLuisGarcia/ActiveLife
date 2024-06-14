# ActiveLife

## Video explicativo Final

- [Video de presentación](https://www.youtube.com/watch?v=9fsH-NhKEI4)

## APK
- [Drive con el apk](https://drive.google.com/drive/folders/1_MXtHELa0TV_IDPVVxhmIAMSC_exa2g8?usp=sharing)

## Documentacion
- [Drive con la documentacion](https://drive.google.com/drive/folders/1J-UPvfViZZX60nM_UqTiy8XR2lHvuKqz?usp=sharing)

## Prototipos realizados en Figma

- [Boceto Inicial](https://www.figma.com/design/bII2hj4Yicxg7YJi7n9Q2W/TFC%2FActiveLife-(sketch)?m=dev&t=hqDB1vHMK0EUDLuF-1)

- [Version final](https://www.figma.com/design/fN8ChGs6QZs3g5JfhOXOrB/TFC%2FActiveLife?m=dev&node-id=0-1&t=hqDB1vHMK0EUDLuF-1)

## Diario TFC

**Marzo 25, 2024**

    Primer commit y creación del repositorio. Creación del proyecto y dependecias basicas.

**Marzo 27, 2024**

    Implementación de funcionalidades básicas para la autenticación de usuarios, mediante firebase.

**Abril 2, 2024**

    Mejora en la autenticación con AutoLogin para mantener la sesión activa automáticamente. Desarrollo de la navegación entre las pantallas de login, register y la actividad principal.

**Abril 5, 2024**

    Implementación de la Sidebar y la funcionalidad de Logout para mejorar la navegación.

**Abril 6, 2024**

    Creación de nuevos fragments para expandir las funcionalidades de la aplicación, y comprobar la navegación.

**Abril 8, 2024**

    Desarrollo del fragmento de rutinas, para crearlas y subiralas a firebase. Añadir el nombre del usuario en la barra lateral.

**Abril 9, 2024**

    Implementación de una rutina con múltiples ejercicios, series y repeticiones, ampliando la personalización y complejidad de las rutinas.

**Abril 11, 2024**

    Desarrollo de un fragmento para visualizar las rutinas del usuario. 

**Abril 14, 2024**

    Implementación para mostrar los ejercicios en la lista de rutinas.

**Abril 16, 2024**

    Nueva navegación y nuevo fragment para la gestión de citas, mejorando la organización de eventos dentro de la aplicación.

**Abril 17, 2024**

    Creación de FragmentCrearCita y FragmentThree, además de añadir nuevos paquetes para organizar el proyecto mejor.

**Abril 18, 2024**

    Introducción de funcionalidades en la gestión de citas como la subida de fotos, y un botón para borrar citas.

**Abril 21, 2024**

    Cambios operativos en la ubicación de botones y la funcionalidad para mostrar imágenes en las citas. Implementación de opciones para eliminar rutinas y citas.

**Abril 23, 2024**

    Implementación de la edición de rutinas.

**Abril 24, 2024**

    Añadido un spinner para seleccionar un encargado(admin) en las citas.

**Abril 25, 2024**

    Mejoras en la navegación y suscripciones para mejorar la gestión de citas y perfiles de usuario. Opción de no escoger encargado en las citas.

**Mayo 2, 2024**

    Implementación de rutinas públicas con un diseño XML distinto, permitiendo una visualización mas atractiva.

**Mayo 4, 2024**

    Actualización del logo y mejora en los estilos de las pantallas de login y register.

**Mayo 5, 2024**

    Cambios en el datepicker y verificaciones para asegurar que las fechas seleccionadas sean coherentes.

**Mayo 6, 2024**

    Mejoras en el spinner, ahora autorellenado al edicitar las citas.

**Mayo 7, 2024**

    Ajustes en fragment_home.xml y métodos para formatear fechas, junto con la adición de un nuevo fragment para mostrar información en el home

**Mayo 9, 2024**

    Creación de fragmento about y aplicación de su navegación. Añadir Rutina para el home. Filtrar las rutinas por el userUuid del usario de la sesión.

**Mayo 14, 2024**

    Cargar imágenes en los ejercicios de las rutinas y aplicar estilos a los exercises y routines.

**Mayo 16, 2024**

    Añadir funcionalidad para hacer fotos con la cámara y seleccionar imágenes de la galería.

**Mayo 17, 2024**

    Implementar entidades (Entities) y objetos de acceso a datos (Daos) para la base de datos Room.

**Mayo 20, 2024**

    Nuevos campos en las citas, incluyendo nombre del encargado en la cita.

**Mayo 21, 2024**

    Llamada a la API para ejercicios predefinidos y correcciones en fallos al borrar rutina y al añadir varias fotos.

**Mayo 22, 2024**

    Creación de fragmentos para rutinas con ejercicios predefinidos y nuevas llamadas a la API.

**Mayo 23, 2024**

    Añadir la variable 'activo' a Firebase, mapeo igual para todas las rutinas, y navegación actualizada.

**Mayo 25, 2024**

    Edición de rutinas y mostrar la rutina del home más cercana al día de hoy.

**Mayo 26, 2024**

    Eliminación de ejercicios y actualización del estado a activo, y carga de imágenes desde el FragmentOne.

**Mayo 28, 2024**

    Uso de Utils para varias funciones, incluyendo la verificación de la conexión y la actualización del username.


**Mayo 29, 2024**

    Barra lateral con nuevo estilo, nuevos colores, y nuevo estilo de login y register.
    Cambio en el import, servicio de traducción, mapeo de la URI y cambios de ubicación.

**Mayo 30, 2024**

    Strings actualizados, quitar botones en rutinas públicas, y añadir loading.
    Nuevos colores y tema claro. Nuevos íconos para la barra lateral.

**Mayo 31, 2024**

    Eliminar rutina, añadir mediaUri, y botones para borrar imagen.
    Botones para borrar imagen repetidos en varios commits.

**Junio 1, 2024**

    Pedir permisos para la cámara, actualización de loadImageIntoView, y cambio de color en la letra del estado.
    Añadir showPublicRoutines al adapter y obtener getEncargadosUsername.
    Verificar si hay imagen y cambios relacionados.
    Comprobar que los campos se han rellenado correctamente en múltiples commits.
    Ajustar el tamaño de la imagen de la cita.
    Filtros para las rutinas, eliminar fragment_main.xml, y eliminar thema oscuro.
    Cambio en los estilos y en el var de gifUrl. Cambio en el import.
    Nuevos colores y visibilidad del ImageView en Gone si no hay imagen.
    Imágenes para el usuario sin foto y ocultar el ImageView si no hay imagen.
    Nuevos estilos y cambio de ubicación. Estilo en el spinner.
    Nuevo estilo para crear rutina.

**Junio 2, 2024**

    Botón para copiar rutinas públicas, botones para filtrar y ordenar las rutinas.
    Botones para filtrar y ordenar las citas. Color por defecto a las que no estén denegadas.
    Cargar imagen al editar cita. Usar uuid en lugar de uid.
    Encargado en el spinner.

**Junio 3, 2024**

    Fondo transparente, logo y fondo. Nuevo estilo en el register.
    Nuevo estilo en el login.

**Junio 4, 2024**

    Navegación, cita más cercana, y estado activo.
    Poner en gris las rutinas caducadas y estilos para el about.
    Textviews para editar la cita y rutina.
    Navegación actualizada, selección de filtros actualizada, y orden de lunes a domingo.
    Quitar valor active en Public Routines y poner email del user actual.
    Estilos para los botones y para el borde del LinearLayout.
    Cambiar color de fondo, nuevo estilo para editar perfil, y gradientes.

**Junio 5, 2024**

    Nuevos datos, respuesta y fileUrl. Cambio a userUUID.
    Nuevo color verde claro y mutableList.
    Filtros y cambio de nombre en atributos distintos en Firebase.
    Citas de color verde para las aceptadas. Eliminar exercise de rutina.
    Mapeo de exercise al copiar la rutina.

**Junio 6, 2024**

    Actualización del home cuando no hay ni cita ni rutina.
    Nuevos strings y método getCurrentDayOfWeek en Utils.
    Nuevos campos respuesta y pdf.
    Comprobación de que existe la respuesta y el pdf.
    Nuevos strings.xml.

**Junio 7, 2024**

    Strings, cambiar nombre del app_bar, y dialog para ver info personal.
    Colores de fondo.

**Junio 9, 2024**

    Fix al editar rutina y navegación a los listados de citas y rutinas al editarlas.
    Comentarios y traducción de la aplicación.
    Dokka.

**Junio 10, 2024**

    Navegación a los listados de citas y rutinas al editarlas.

**Junio 12, 2024**

    Poner le boton de eliminar imagen en el modal, imagen para cuando no hay exercise, titulo para cita y rutina en el home, textview para copiar rutina y crear cita en el home y nuevos strings


**Junio 13, 2024**

    Añadir seleccion de parte del cuerpo para cada ejercicio, y añadir algunos strings nuevos


**Junio 14, 2024**
    
    Ultimos arreglos de la app, como quitar algunos toast informativos, y cambiar algunos estilos
