# Taller Ebbenezer — Backend

API REST en Spring Boot para gestionar inventario, ventas y fiados del taller.

## Requisitos
- Java 17+
- Maven 3.9+
- Una base de datos PostgreSQL gratuita (recomendado: [Neon.tech](https://neon.tech))

## Configuración

1. Crea una cuenta gratis en https://neon.tech y crea un proyecto. Copia la cadena de conexión.
2. Define las variables de entorno (o edítalas directamente en `src/main/resources/application.yml`):

```bash
export DB_URL="jdbc:postgresql://TU-HOST.neon.tech/tallerdb?sslmode=require"
export DB_USER="tu_usuario"
export DB_PASSWORD="tu_password"
export JWT_SECRET="una-clave-larga-y-secreta"
```

3. Ejecuta el proyecto:

```bash
mvn spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

## Endpoints principales

| Método | Ruta                          | Descripción                              |
|--------|-------------------------------|-------------------------------------------|
| GET    | /api/productos                | Listar inventario                         |
| GET    | /api/productos/stock-bajo     | Productos por debajo del stock mínimo     |
| POST   | /api/productos                | Crear producto                            |
| POST   | /api/productos/{id}/entrada   | Registrar entrada de stock                |
| GET    | /api/clientes                 | Listar clientes                           |
| POST   | /api/clientes                 | Crear cliente                             |
| GET    | /api/clientes/{id}/deuda      | Deuda total pendiente del cliente         |
| POST   | /api/ventas                   | Registrar venta (normal o fiada)          |
| GET    | /api/ventas/pendientes        | Ventas con saldo pendiente                |
| POST   | /api/ventas/abonos            | Registrar abono a una venta fiada         |
| GET    | /api/ordenes-trabajo          | Listar órdenes de reparación/servicio     |
| POST   | /api/ordenes-trabajo          | Crear orden de trabajo                    |
| PATCH  | /api/ordenes-trabajo/{id}/estado | Cambiar estado de una orden            |

## Ejemplo: registrar una venta fiada

```json
POST /api/ventas
{
  "clienteId": "uuid-del-cliente",
  "esFiado": true,
  "items": [
    { "productoId": "uuid-del-producto", "cantidad": 2 }
  ]
}
```

## Ejemplo: registrar un abono

```json
POST /api/ventas/abonos
{
  "ventaId": "uuid-de-la-venta",
  "monto": 50.00
}
```

Cuando el abono cubre el saldo total, la venta pasa a `PAGADA` y el `scoreConfianza`
del cliente sube automáticamente.

## Despliegue gratis

- **Render.com**: crea un "Web Service", conecta tu repo de GitHub, build command
  `mvn clean package -DskipTests`, start command `java -jar target/*.jar`.
- **Google Cloud Run**: `gcloud run deploy` con un Dockerfile (te lo genero si quieres).

## Pendiente / próximos pasos

- [ ] Autenticación JWT real (login con email + password, roles DUENO/EMPLEADO)
- [ ] Endpoint de reportes (ventas por día, productos más vendidos)
- [ ] Generación de recibos PDF
- [ ] Notificaciones/recordatorios de cobro
