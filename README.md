# Smart Buy para Android

Proyecto académico de Emma Alexandra Arévalo Rodríguez, SENA ADSO, ficha 3186670.

Evidencia GA8-220501096-AA2-EV02.

## Funciones

- Registro con correo, contraseña y confirmación.
- Inicio de sesión mediante Firebase Authentication.
- Validación de campos y mensajes de error.
- Pantalla de bienvenida y cierre de sesión.

## Herramientas

Android Studio, Kotlin, Jetpack Compose, Material 3 y Firebase Authentication.

## Ejecución

Abrir este repositorio como proyecto en Android Studio, sincronizar Gradle y ejecutar en un dispositivo Android compatible. Se requiere conexión a internet para autenticarse. La versión mínima configurada es Android 7.0 (API 24); las pruebas funcionales se realizaron en un teléfono con Android 15.

La configuración de Firebase está en app/google-services.json. No se incluyen contraseñas de usuarios ni credenciales de cuentas de servicio.

## Pruebas documentadas

Registro, inicio de sesión válido, rechazo de contraseña incorrecta, aviso por datos incompletos y cierre de sesión. Las pruebas fueron manuales en un dispositivo físico. Los archivos ExampleUnitTest y ExampleInstrumentedTest son ejemplos de la plantilla de Android Studio.
