# Proyecto_Presion_Arterial

       VitalWatch — App Android + Automatizaciones con n8n 
 
Monitoreo simple de presión arterial con respaldo en Google Sheets, alertas 
automáticas por Telegram. 
La app envía lecturas y registros de pacientes a un webhook n8n, donde se 
clasifican, se generan recomendaciones con IA, se almacenan y se notifican 
alertas. 
    Funcionalidades 
 
• Registro de pacientes (formulario Android). 
• Análisis de presión arterial. 
• Envío de datos a n8n vía Webhook. 
• Clasificación automática (Baja, Normal, Alta). 
• Recomendaciones con IA (Groq Llama 3). 
• Alertas por Telegram y respaldo en Sheets. 
         Integración con n8n 
 
   Webhook: recibe datos del formulario y la medición. 
   Code (JS o Python): clasifica la presión según valores sistólica/diastólica. 
   Agente de IA (Groq): genera recomendación breve. 
   Merge: combina datos del paciente y la recomendación. 
   IF: si la categoría es “Alta” o “Baja”, envía alerta a Telegram. 
   Google Sheets: guarda todos los registros. 
    Flujo principal de nodos 
Webhook → Código_Clasificar_Presión → Agente_IA → Merge → IF_Alerta → 
Telegram + Sheets  
       App Android 
 
• Estructura basada en Java (Android Studio). 
• Uso de OkHttp para conectar con n8n. 
• Clases principales: 
   - formulario.java (registra paciente) 
   - resultado_presion.java (envía datos de presión) 
   - WebhookClient.java (POST JSON a n8n) 
 
Ejemplo de JSON enviado 
{ 
} 
"nombre": "Aranza", 
"presion": "195/80", 
"categoria_presion": "Prehipertensión", 
"contacto": "1664564", 
"fecha": "2025-10-25T06:41:48.371Z" 
Créditos 
Proyecto académico — Ingeniería en Sistemas. 
Desarrollado por: Aranza Rueda – Melany Orantes – Arli Castilo 
Universidad Mariano Gálvez, 2025 
