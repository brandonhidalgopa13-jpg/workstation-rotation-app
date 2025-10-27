# 🔥 Configuración Completa de Firebase para Sincronización en la Nube

## 🎯 **¿Por qué Firebase?**

Firebase te permite:
- **☁️ Sincronización automática** entre dispositivos
- **🔒 Respaldos seguros** en la nube de Google
- **👥 Colaboración en tiempo real** entre usuarios
- **📱 Acceso desde cualquier lugar** con internet
- **🔄 Sincronización offline** cuando no hay conexión

## 📋 **Pasos Detallados para Configurar Firebase**

### 1. Crear Proyecto en Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Haz clic en "Crear un proyecto"
3. Nombre del proyecto: `workstation-rotation-app`
4. Habilita Google Analytics (opcional)
5. Selecciona tu cuenta de Analytics

### 2. Agregar App Android

1. En la consola del proyecto, haz clic en el ícono de Android
2. Package name: `com.workstation.rotation`
3. App nickname: `Workstation Rotation`
4. SHA-1: Obtén con `./gradlew signingReport`

### 3. Descargar google-services.json

1. Descarga el archivo `google-services.json`
2. Colócalo en `app/google-services.json`
3. **NO** subas este archivo a control de versiones

### 4. Habilitar Servicios de Firebase

#### 4.1 Authentication
1. Ve a Authentication > Sign-in method
2. Habilita "Google" como proveedor
3. Configura el email de soporte del proyecto

#### 4.2 Firestore Database
1. Ve a Firestore Database
2. Haz clic en "Crear base de datos"
3. Selecciona "Comenzar en modo de prueba"
4. Elige una ubicación cercana

#### 4.3 Storage (Opcional)
1. Ve a Storage
2. Haz clic en "Comenzar"
3. Acepta las reglas de seguridad por defecto

### 5. Configurar Reglas de Seguridad

#### Firestore Rules (`firestore.rules`)
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Usuarios solo pueden acceder a sus propios datos
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Respaldos solo para usuarios autenticados
    match /backups/{backupId} {
      allow read, write: if request.auth != null && 
        request.auth.uid == resource.data.userId;
    }
  }
}
```

#### Storage Rules (`storage.rules`)
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 6. Configurar Web Client ID

1. Ve a Authentication > Sign-in method > Google
2. Copia el "Web client ID"
3. Actualiza `CloudAuthManager.kt`:
   ```kotlin
   private const val WEB_CLIENT_ID = "tu-web-client-id-aqui"
   ```

### 7. Estructura de Datos en Firestore

```
/users/{userId}/
  └── workspaces/
      └── default/
          ├── workers/
          │   └── {workerId}
          ├── workstations/
          │   └── {workstationId}
          └── worker_workstations/
              └── {workerId}_{workstationId}

/backups/
  └── {backupId}
```

## 🔒 Seguridad y Privacidad

### Datos Encriptados
- Todos los datos se transmiten por HTTPS
- Firebase maneja la encriptación automáticamente
- Los datos están aislados por usuario

### Permisos Granulares
- Cada usuario solo puede acceder a sus propios datos
- Las reglas de Firestore previenen acceso no autorizado
- Autenticación requerida para todas las operaciones

### Respaldos Seguros
- Los respaldos incluyen metadatos del dispositivo
- Versionado automático de datos
- Eliminación segura de cuentas

## 🚀 Funcionalidades Implementadas

### ✅ Autenticación
- [x] Inicio de sesión con Google
- [x] Inicio de sesión anónimo
- [x] Gestión de sesiones
- [x] Eliminación de cuentas

### ✅ Sincronización
- [x] Subida de datos locales
- [x] Descarga de datos remotos
- [x] Sincronización bidireccional
- [x] Resolución de conflictos

### ✅ Respaldos
- [x] Respaldos automáticos en la nube
- [x] Restauración desde respaldos
- [x] Lista de respaldos disponibles
- [x] Metadatos de respaldos

### ✅ Tiempo Real
- [x] Escucha de cambios en tiempo real
- [x] Notificaciones de cambios
- [x] Sincronización entre dispositivos

## 🛠️ Configuración de Desarrollo

### Variables de Entorno
Crea un archivo `local.properties` con:
```properties
# Firebase
firebase.project_id=workstation-rotation-app
firebase.web_client_id=tu-web-client-id

# Debug
debug.firebase_emulator=false
debug.offline_mode=false
```

### Modo Debug
Para desarrollo local, puedes usar el emulador de Firebase:
```bash
firebase emulators:start --only firestore,auth
```

## 📱 Uso en la App

### Configuraciones
1. Ve a Configuraciones > Respaldo y Sincronización
2. Toca "🔄 Sincronizar" para opciones de nube
3. Inicia sesión con tu cuenta de Google
4. ¡Disfruta de la sincronización automática!

### Estados de Sincronización
- **🔄 Sincronizando**: Datos en proceso
- **✅ Sincronizado**: Datos actualizados
- **❌ Error**: Problema de conexión
- **⚠️ Conflicto**: Requiere resolución manual

## 🆘 Solución de Problemas

### Error: "SDK location not found"
- Asegúrate de tener Android SDK instalado
- Configura `ANDROID_HOME` en variables de entorno

### Error: "google-services.json not found"
- Descarga el archivo desde Firebase Console
- Colócalo en `app/google-services.json`

### Error de autenticación
- Verifica que el SHA-1 esté configurado correctamente
- Asegúrate de que el Web Client ID sea correcto

### Problemas de red
- Verifica conexión a internet
- Revisa las reglas de Firestore
- Comprueba permisos de la app

## 📞 Soporte

Si tienes problemas con la configuración:
1. Revisa la documentación de Firebase
2. Verifica los logs de la aplicación
3. Consulta la consola de Firebase para errores

---

**Desarrollado por**: Brandon Josué Hidalgo Paz  
**Versión**: Sistema de Rotación Inteligente v2.1.0  
**Fecha**: Octubre 2024