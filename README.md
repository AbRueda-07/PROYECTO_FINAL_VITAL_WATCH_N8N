# VitalWatch – App Android + Automatización con n8n

Descripción del Proyecto

VitalWatch es una aplicación Android para el monitoreo y análisis de la presión arterial.
Su principal objetivo es facilitar el seguimiento de la salud del usuario mediante registros automáticos, análisis inteligentes y notificaciones en tiempo real.

La app está desarrollada en Java (Android Studio) y se integra con n8n, una herramienta de automatización que recibe los datos médicos y los procesa de forma automática, clasificando los valores de presión y generando alertas o reportes.

El flujo en n8n analiza la presión arterial, genera una recomendación con IA (Groq - Llama 3) y envía alertas médicas por Telegram, además de guardar todos los registros en Google Sheets.

⚙️ Cambios desde la Propuesta Inicial

Desde la primera versión, se realizaron mejoras estructurales y funcionales tanto en la app como en el flujo n8n.

Cambios Principales

Validación de datos antes del envío: no se permiten campos vacíos en el formulario.

Ajuste en el análisis de presión arterial para una clasificación más precisa.

Optimización del flujo n8n con un Merge limpio entre datos médicos e IA.

Separación del flujo de alertas y el flujo de registros para evitar duplicaciones.

Nuevas Implementaciones

Integración con Groq Chat Model (Llama 3) para generar recomendaciones automáticas.

Alertas médicas personalizadas enviadas por Telegram al profesional o usuario.

Conexión con Google Sheets para almacenar cada medición automáticamente.

Programación de reportes automáticos mediante Schedule Trigger (cada 3 minutos o semanal).

🧠 Flujo Principal en n8n
Estructura del Flujo “VitalWatch_Flujo_Completo”

Webhook → Recibe los datos enviados desde la app Android.

Code (Clasificar_Presión) → Clasifica los valores en: Baja, Normal, Alta o Prehipertensión.

AI Agent (Groq) → Genera una recomendación breve y empática para el paciente.

Merge → Combina los datos médicos y la respuesta del agente IA.

IF (Es_Alerta) → Detecta si la presión es Alta o Baja y activa una alerta.

Telegram Send Message → Envía el mensaje de alerta al chat configurado.

Google Sheets → Guarda todos los registros enviados desde la app.

Schedule Trigger + Code_Resumen → Genera un reporte periódico de todas las mediciones.

📲 App Android
Estructura Principal

FormularioActivity.java → Captura los datos del paciente.

ResultadoPresionActivity.java → Muestra el resultado y envía los datos a n8n.

VitalWatchRepository.java → Administra la lógica de almacenamiento local y envío al webhook.

MedicionPayload.java → Estructura los datos enviados al flujo.

WebhookClient.java → Realiza la conexión HTTP POST al webhook n8n.

Lógica del Envío

Los datos capturados en el formulario son enviados en formato JSON al Webhook configurado en n8n, donde se procesan automáticamente.

📦 Ejemplo de JSON Enviado
{
  "nombre": "Aranza Rueda",
  "presion": "160/100",
  "sistolica": 160,
  "diastolica": 100,
  "categoria_presion": "Alta",
  "contacto": "1224654604",
  "fecha": "2025-10-25T06:41:48Z"
}

🧩 Alertas Médicas

Cuando se detecta una presión fuera del rango normal, el sistema envía una notificación automática al médico o usuario por Telegram.

Ejemplo de Mensaje
⚠️ Alerta Vital Watch

👤 Paciente: Aranza Rueda
💓 Presión: 160/100
🏷️ Categoría: Alta
🩺 Recomendación: Hoy puedes evitar la sal y tomar suficiente agua. Procura descansar y mantenerte tranquila.
📅 Fecha: 2025-10-25
📱 Contacto: 1224654604

Consulta a tu médico de confianza.

🧰 Instrucciones de Instalación

Clona el repositorio en Android Studio.

Sincroniza Gradle automáticamente o desde la terminal.

Conecta un dispositivo o inicia un emulador Android.

Configura la URL del Webhook en tu clase WebhookClient.java.

Ejecuta la aplicación con Run > app.

Abre n8n, activa tu flujo y presiona Listen for Test Event para recibir la primera prueba.

🧾 Requisitos Técnicos

Android Studio: 2025.1.4 Narwhal o superior

JDK: 11+

Android SDK: API 31 o superior

Dependencias Principales:

implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.9.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'com.squareup.okhttp3:okhttp:4.11.0'
implementation 'org.json:json:20230227'

📊 Demostrativos
🔹 Flujo n8n:

Se visualizan los nodos conectados correctamente:
Webhook → Code → AI Agent → Merge → IF → Telegram → Google Sheets → Reporte.

🔹 App Android:

Formulario funcional, envío de datos en tiempo real y respuesta inmediata del sistema con clasificación médica.

🧩 Conclusión

VitalWatch combina desarrollo móvil e inteligencia artificial con automatización médica.
El proyecto demuestra cómo n8n puede servir como puente entre la app y servicios externos, automatizando la gestión de datos de salud y brindando soporte inteligente al usuario.
