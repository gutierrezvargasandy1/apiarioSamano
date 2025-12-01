@echo off
setlocal enabledelayedexpansion

echo =====================================================
echo 🚀 INICIANDO PROCESO DE REINSTALACIÓN Y CREACIÓN DE IMÁGENES DOCKER
echo =====================================================

cd /d "C:\Users\andre\OneDrive\Escritorio\apiarioSamano\ApiarioSamanoBackEnd"

:: =============================
:: VERIFICAR SI DOCKER ESTÁ EJECUTÁNDOSE
:: =============================
echo 🔍 Verificando estado de Docker...
docker version >nul 2>&1
if !errorlevel! neq 0 (
    echo ❌ ERROR: Docker Desktop no está ejecutándose o no está disponible
    echo.
    echo 💡 Soluciones:
    echo    1. Abre Docker Desktop y espera a que esté completamente inicializado
    echo    2. Verifica que Docker esté instalado correctamente
    echo    3. Ejecuta Docker Desktop como administrador si es necesario
    echo.
    pause
    exit /b 1
)
echo ✅ Docker está funcionando correctamente

:: =============================
:: VERIFICAR ESPACIO EN DISCO Y LIMPIAR CACHE
:: =============================
echo 🔍 Limpiando cache de Docker...
docker system prune -f >nul 2>&1
echo ✅ Cache limpiado

:: =============================
:: SOLO PROCESAR UN MICROSERVICIO PARA PRUEBAS
:: =============================
set servicios= MicroServiceAlmacen:microservicealmacen  MicroServiceApiarios:microserviceapiarios MicroServiceProduccion:microserviceproduccion MicroServiceProveedores:microserviceproveedores MicroServiceAuth:microserviceauth MicroServiceGestorDeArchivos:microservicegestordearchivos MicroServiceUsuario:microserviceusuario MicroServiceNotificacionesGmail:microservicenotificacionesgmail MicroServiceGeneradorCodigo:microservicegeneradorcodigo

set reintentos=1  :: Solo un intento para ver el error real

:: =============================
:: PROCESAR CADA MICROSERVICIO
:: =============================
for %%s in (%servicios%) do (
    for /f "tokens=1,2 delims=:" %%a in ("%%s") do (
        set carpeta=%%a
        set imagen=%%b
        
        echo.
        echo =====================================================
        echo 📦 PROCESANDO: !carpeta!
        echo =====================================================
        
        :: VERIFICAR QUE LA CARPETA EXISTA
        if not exist "!carpeta!" (
            echo ❌ Error: La carpeta !carpeta! no existe
            goto :error
        )
        
        :: VERIFICAR DOCKERFILE
        if not exist "!carpeta!\Dockerfile" (
            echo ❌ Error: No existe Dockerfile en !carpeta!\
            goto :error
        )
        echo ✅ Dockerfile encontrado
        
        :: 1. ELIMINAR CARPETA TARGET
        if exist "!carpeta!\target" (
            echo 🗑 Eliminando carpeta target de !carpeta!...
            rmdir /s /q "!carpeta!\target" >nul 2>&1
        )
        
        :: 2. COMPILAR Y GENERAR TARGET
        echo 🔧 Compilando !carpeta!...
        cd "!carpeta!"
        echo [MVN] Ejecutando: mvn clean package -DskipTests
        call mvn clean package -DskipTests
        if !errorlevel! neq 0 (
            echo ❌ Error compilando !carpeta!
            cd ..
            goto :error
        )
        
        :: VERIFICAR QUE EL JAR SE GENERÓ CORRECTAMENTE
        dir target\*.jar >nul 2>&1
        if !errorlevel! neq 0 (
            echo ❌ Error: No se generó el archivo JAR en !carpeta!\target\
            cd ..
            goto :error
        )
        echo ✅ JAR generado correctamente
        
        :: MOSTRAR INFORMACIÓN DEL JAR
        for %%f in (target\*.jar) do (
            echo 📁 Archivo JAR: %%f (Tamaño: %%~zf bytes)
        )
        
        cd ..
        
        :: 3. CREAR IMAGEN DOCKER (con logs visibles)
        echo 🐳 Creando imagen Docker !imagen!:1.0...
        cd "!carpeta!"
        echo [DOCKER] Ejecutando: docker build -t !imagen!:1.0 .
        docker build -t !imagen!:1.0 .
        if !errorlevel! neq 0 (
            echo ❌ Error creando imagen !imagen!:1.0
            cd ..
            goto :error
        )
        cd ..
        
        echo ✅ !carpeta! completado exitosamente
    )
)

echo.
echo =====================================================
echo ✅ TODAS LAS IMÁGENES DOCKER HAN SIDO CREADAS
echo =====================================================
pause
exit /b 0

:error
echo.
echo ❌ PROCESO INTERRUMPIDO POR ERROR
echo =====================================================
echo.
echo 🔎 Diagnóstico del error en: !carpeta!
echo.
echo 💡 Pasos para solucionar:
echo    1. Verifica el Dockerfile en !carpeta!\Dockerfile
echo    2. Ejecuta manualmente en !carpeta!\: docker build -t !imagen!:1.0 .
echo    3. Verifica que el JAR existe: dir !carpeta!\target\*.jar
echo    4. Prueba construir sin cache: docker build --no-cache -t !imagen!:1.0 .
echo.
echo 📋 Comandos para diagnóstico:
echo    cd !carpeta!
echo    docker build -t !imagen!:1.0 .
echo    dir target
echo.
pause
exit /b 1