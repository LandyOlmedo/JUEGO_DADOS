# Dice Roller - Juego de Dados en Kotlin

Proyecto Android desarrollado en Kotlin usando Jetpack Compose. Permite lanzar 6 dados, calcular combinaciones tipo póker y registrar el puntaje en un servidor. Incluye descarga dinámica de instrucciones desde GitHub.

## Características

- Interfaz moderna con Jetpack Compose
- Lógica de juego tipo póker con 6 dados
- Registro y envío de puntaje a servidor vía API REST
- Descarga de instrucciones desde un archivo JSON en GitHub
- Gestión de pantallas: menú, juego e instrucciones

## Tecnologías y habilidades demostradas

- **Kotlin** para desarrollo Android
- **Jetpack Compose** para UI declarativa
- **Retrofit** para consumo de APIs REST
- **Manejo de estado** y navegación entre pantallas
- **Consumo de JSON** remoto y manejo de errores

## Estructura principal

- `MainActivity.kt`: Lógica principal, UI y conexión a APIs
- Recursos gráficos: dados y temas personalizados
- `server.py`: Servidor de ejemplo para puntajes (opcional)

## Ejecución

1. Abrir el proyecto en Android Studio
2. Ejecutar en un emulador o dispositivo físico
3. Opcional: iniciar `server.py` para registrar puntajes

## Capturas de pantalla

![Pantalla principal](docs/screenshot_menu.png)
![Juego de dados](docs/screenshot_game.png)
![Instrucciones](docs/screenshot_instructions.png)

---
Desarrollado por Landy Olmedo, 2026
