# FitJourney

FitJourney es una aplicación móvil nativa Android diseñada para transformar la gestión del fitness personal. Permite a los usuarios organizar sus entrenamientos, diseñar rutinas personalizadas y seguir su progreso físico con una interfaz intuitiva.

## Funcionalidades Principales

Autenticación segura: Registro e inicio de sesión integrados.

Gestión de Rutinas: Creación, edición y eliminación de rutinas de entrenamiento a medida.

Catálogo de Ejercicios: Biblioteca dinámica para gestionar los ejercicios de cada usuario.

Seguimiento de Progreso: Visualización del historial de entrenamientos y métricas de usuario.

## Stack Tecnológico
Lenguaje de programación: Java y JavaScript

Arquitectura: MVVM (Model-View-ViewModel) con Android Architecture Components.

Red: Retrofit para consumo de API REST.

Framework de la api: Node.js

Asincronía: LiveData para la reactividad en la UI.

Base de Datos: SQL (a través de SQLite/Room).

Control de versiones: Git.

## Arquitectura del Proyecto
La aplicación implementa el patrón MVVM para asegurar la escalabilidad y el testeo del código:

View: Responsable de mostrar los datos y capturar eventos del usuario.

ViewModel: Gestiona los datos de la UI y se comunica con el Repositorio, sobreviviendo a los cambios de configuración.

Repository: Capa única de acceso a datos (gestiona la lógica entre la API remota y la base de datos local).

Data Sources: Implementación técnica de las llamadas a la API y acceso a base de datos.

## Requisitos para el desarrollo
Para compilar y ejecutar este proyecto, necesitarás:

Android Studio (versión Bumblebee o superior recomendada).

JDK 11+ configurado en el IDE.

Tener el backend corriendo.

## Capturas de pantalla
<div style="display: flex; gap: 10px;">
  <img src="https://github.com/user-attachments/assets/f449d811-0c0a-4e6a-8c60-d9935bfcfe44" width="250" />
  <img src="https://github.com/user-attachments/assets/a5829c80-8d72-4ac8-85cf-d672641d577d" width="250" />
  <img src="https://github.com/user-attachments/assets/f03442e1-d32f-4cbd-9174-9b6deaa4c1a2" width="250" />
  <img src="https://github.com/user-attachments/assets/e2f08222-d9fa-42eb-99bd-29d19ed64d8c" width="250" />
</div>
