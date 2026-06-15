# 🚗 UAM-LIFT

**UAM-LIFT** es una plataforma de *carpooling* (viajes compartidos) diseñada exclusivamente para la comunidad estudiantil de la Universidad Americana (UAM). Su objetivo es facilitar el transporte seguro, económico y ecológico entre los estudiantes, permitiendo organizar viajes hacia y desde el campus universitario.

## ✨ Características Principales

* **Seguridad Estudiantil:** Registro e inicio de sesión restringido estrictamente a correos institucionales (`@uamv.edu.ni`).
* **Verificación por Correo:** Sistema de validación de cuentas mediante envío de correos electrónicos con tokens de tiempo limitado (TTL).
* **Gestión de Viajes:** Los usuarios pueden registrar sus vehículos (`Carro`), crear rutas (`Viaje`) y unirse a viajes existentes (`ViajeUsuario`).
* **Destinos Inteligentes:** Manejo de destinos frecuentes y predeterminados para facilitar la creación rápida de rutas (`DestinoDefecto`).
* **Chat en Tiempo Real:** Comunicación instantánea entre conductor y pasajeros coordinada a través de un módulo independiente de WebSockets (`WebSockerChat`).

## 🛠️ Tecnologías Utilizadas

### Backend (API Rest & WebSockets)
* **Lenguaje:** Java 21
* **Framework:** Spring Boot
* **Base de Datos:** PostgreSQL 
* **ORM:** Spring Data JPA / Hibernate
* **Tiempo Real:** Spring WebSockets
* **Notificaciones:** Spring Boot Mail (SMTP)

### Frontend (Móvil)
* **Plataforma:** Android (Kotlin/Java)

## 📁 Estructura del Proyecto

El backend está dividido lógicamente en los siguientes módulos y paquetes principales:
* `models/`: Entidades de la base de datos (`Usuario`, `Carro`, `Viaje`, `Destino`, `EmailVerification`, etc.).
* `repositories/`: Interfaces de acceso a datos (Spring Data JPA).
* `services/`: Lógica de negocio (`UsuarioServicio`, `ViajeServicio`, `EmailVerificationService`, etc.).
* `validators/`: Validaciones personalizadas de reglas de negocio (`ValidacionViajeImp`).
* `WebSockerChat/`: Módulo encargado de la gestión de la mensajería en tiempo real.

## 🚀 Requisitos Previos

Para ejecutar este proyecto en tu entorno local, necesitas tener instalado:
* **Java Development Kit (JDK) 21**
* **PostgreSQL** (Corriendo en el puerto `5432`)
* **Maven** (Incluido en la mayoría de IDEs como IntelliJ IDEA)
* **IntelliJ IDEA** o cualquier otro IDE compatible con Spring Boot.

## ⚙️ Configuración y Ejecución local

1. **Clonar el repositorio:**
```bash
   git clone [https://github.com/tu-usuario/UAM-LIFT.git](https://github.com/tu-usuario/UAM-LIFT.git)
   cd UAM-LIFT# 🚗 UAM-LIFT

**UAM-LIFT** es una plataforma de *carpooling* (viajes compartidos) diseñada exclusivamente para la comunidad estudiantil de la Universidad Americana (UAM). Su objetivo es facilitar el transporte seguro, económico y ecológico entre los estudiantes, permitiendo organizar viajes hacia y desde el campus universitario.

## ✨ Características Principales

* **Seguridad Estudiantil:** Registro e inicio de sesión restringido estrictamente a correos institucionales (`@uamv.edu.ni`).
* **Verificación por Correo:** Sistema de validación de cuentas mediante envío de correos electrónicos con tokens de tiempo limitado (TTL).
* **Gestión de Viajes:** Los usuarios pueden registrar sus vehículos (`Carro`), crear rutas (`Viaje`) y unirse a viajes existentes (`ViajeUsuario`).
* **Destinos Inteligentes:** Manejo de destinos frecuentes y predeterminados para facilitar la creación rápida de rutas (`DestinoDefecto`).
* **Chat en Tiempo Real:** Comunicación instantánea entre conductor y pasajeros coordinada a través de un módulo independiente de WebSockets (`WebSockerChat`).

## 🛠️ Tecnologías Utilizadas

### Backend (API Rest & WebSockets)
* **Lenguaje:** Java 21
* **Framework:** Spring Boot
* **Base de Datos:** PostgreSQL 
* **ORM:** Spring Data JPA / Hibernate
* **Tiempo Real:** Spring WebSockets
* **Notificaciones:** Spring Boot Mail (SMTP)

### Frontend (Móvil)
* **Plataforma:** Android (Kotlin/Java)

## 📁 Estructura del Proyecto

El backend está dividido lógicamente en los siguientes módulos y paquetes principales:
* `models/`: Entidades de la base de datos (`Usuario`, `Carro`, `Viaje`, `Destino`, `EmailVerification`, etc.).
* `repositories/`: Interfaces de acceso a datos (Spring Data JPA).
* `services/`: Lógica de negocio (`UsuarioServicio`, `ViajeServicio`, `EmailVerificationService`, etc.).
* `validators/`: Validaciones personalizadas de reglas de negocio (`ValidacionViajeImp`).
* `WebSockerChat/`: Módulo encargado de la gestión de la mensajería en tiempo real.
* `WebSockerUbicacion/`: Módulo encargado de la gestión de la ubicacion en tiempo real.

## 🚀 Requisitos Previos

Para ejecutar este proyecto en tu entorno local, necesitas tener instalado:
* **Java Development Kit (JDK) 21**
* **PostgreSQL** (Corriendo en el puerto `5432`)
* **Maven** (Incluido en la mayoría de IDEs como IntelliJ IDEA)
* **IntelliJ IDEA** o cualquier otro IDE compatible con Spring Boot.

