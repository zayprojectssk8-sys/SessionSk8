/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.sk8.appwatch.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Text
import com.sk8.appwatch.presentation.screen.skate_session.SkateSessionScreen
import com.sk8.appwatch.presentation.utils.sendConnectionHandshakeToPhone


@Composable
fun WatchHomeScreen() {
    // 1. Obtener el contexto y el scope de corrutinas
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.sendConnectionHandshakeToPhone()
    }


    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(
            onClick = {}
        ) {
            Text("Iniciar Timer")
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SkateSessionScreen()
        }
    }
}


/*
1. Rendimiento e Intensidad (Cardio)

    Frecuencia Cardíaca en Tiempo Real y Picos (Heart Rate):

        Por qué sirve: Permite ver los picos de adrenalina/esfuerzo al intentar un truco o hacer una rutina (line).

        Uso en la app: Mostrar la frecuencia cardíaca promedio y máxima durante la sesión, identificando las zonas de esfuerzo (ej. cuánto tiempo estuviste en zona anaeróbica).

    Calorías Activas (Active Calories):

        Por qué sirve: El skate quema bastantes calorías, pero la mitad del tiempo se pasa parado en el spot. Medir únicamente las calorías activas da una cifra real del gasto físico del entrenamiento.

2. Movimiento, Velocidad y Desplazamiento (GPS y Acelerómetro)

    Velocidad Máxima y Promedio (Speed):

        Por qué sirve: Ideal para medir qué tan rápido ibas al bajar una rampa, hacer un gap o durante una sesión de street/downhill.

    Distancia Recorrida y Ruta (Distance & GPS Route):

        Por qué sirve: Si el usuario patina de un spot a otro (street skating), mapear la ruta y la distancia total recorrida agrega un valor tipo Strava para skaters.

    Cadencia y Zancadas / Empujes (Pushes):

        Por qué sirve: Usando los sensores de movimiento (acelerómetro/giroscopio), se puede analizar la frecuencia de empujes (pateo) antes de tomar velocidad.

3. Tiempos de Sesión y Ritmo de Entrenamiento

    Tiempo Activo vs. Tiempo en Reposo:

        Por qué sirve: En una sesión de 2 horas en el skatepark, es común que solo 40 minutos sean de patinaje real. Separar el tiempo en movimiento del tiempo de descanso ayuda a evaluar la productividad de la sesión.

4. Seguridad e Impactos (Acelerómetro / Giroscopio)

    Detección de Impactos FUERTES / Caídas (Fall Detection / Heavy Landing):

        Por mecatrónica del reloj: Los picos bruscos en el acelerómetro (medidos en fuerzas G) indican un aterrizaje o una caída fuerte.

        Uso en la app:

            Lanzar una alerta de emergencia o confirmación ("¿Estás bien?").

            Contabilizar los "impactos" o intentos de un truco.

5. Recuperación Post-Sesión

    Carga de Entrenamiento y Frecuencia Cardíaca en Reposo:

        Por qué sirve: Ayuda a saber qué tan fatigado quedó el cuerpo después de una sesión intensa para sugerir tiempos de descanso antes de volver a patinar.
 */