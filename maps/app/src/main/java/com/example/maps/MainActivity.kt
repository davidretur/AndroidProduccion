package com.example.maps

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.example.maps.ui.theme.MapsTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import android.Manifest
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import com.google.maps.android.compose.MarkerState


import kotlinx.coroutines.tasks.await // Import correcto de await()
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleMapsComposeApp()
        }
    }
}

@Composable
fun GoogleMapsComposeApp() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            //MapScreen() // mapa completo
            LocationDisplayAndStore()
        }
    }
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val currentLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation.value, 15f)
    }

    // CoroutineScope para manejar operaciones asíncronas
    val coroutineScope = rememberCoroutineScope()

    // Solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, obtener la ubicación en una corrutina
            coroutineScope.launch {
                fetchLocation(
                    fusedLocationClient, currentLocation, cameraPositionState,context
                )
            }
        } else {
            // Permiso denegado
        }
    }

    // Verificar y solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido, obtener la ubicación en una corrutina
                fetchLocation(fusedLocationClient, currentLocation, cameraPositionState, context)
            }
            else -> {
                // Solicitar permiso
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Estado para la dirección
    val addressState = remember { mutableStateOf("Obteniendo dirección...") }

    // Obtener la dirección usando GeocodeListener
    LaunchedEffect(currentLocation.value) {
        val geocoder = Geocoder(context, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Usar GeocodeListener para obtener la dirección
            geocoder.getFromLocation(
                currentLocation.value.latitude,
                currentLocation.value.longitude,
                1,
                object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        addressState.value = if (addresses.isNotEmpty()) {
                            addresses[0].getAddressLine(0)
                        } else {
                            "Dirección no disponible"
                        }
                    }

                    override fun onError(errorMessage: String?) {
                        addressState.value = "Error al obtener la dirección"
                    }
                }
            )
        } else {
            // Compatibilidad con versiones anteriores
            try {
                val addresses = geocoder.getFromLocation(
                    currentLocation.value.latitude,
                    currentLocation.value.longitude,
                    1
                )
                addressState.value = if (!addresses.isNullOrEmpty()) {
                    addresses[0].getAddressLine(0)
                } else {
                    "Dirección no disponible"
                }
            } catch (e: Exception) {
                addressState.value = "Error al obtener la dirección"
            }
        }
    }

    // Estado del marcador
    val markerState = remember { MarkerState(position = currentLocation.value) }

    // Mostrar la ventana de información del marcador automáticamente
    LaunchedEffect(markerState) {
        markerState.showInfoWindow()
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState
    ) {
        Marker(
            state = MarkerState(position = currentLocation.value),
            title = "Ubicación actual",
            snippet = """
                📍:${addressState.value}🌐:Lat: ${currentLocation.value.latitude} Lng: ${currentLocation.value.longitude}
            """.trimIndent()
        )
    }
}


private suspend fun fetchLocation(
    fusedLocationClient: FusedLocationProviderClient,
    currentLocation: MutableState<LatLng>,
    cameraPositionState: CameraPositionState,
    context: Context
) {
    // Verificar si el permiso de ubicación está concedido
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        try {
            // Obtener la última ubicación conocida
            val location = fusedLocationClient.lastLocation.await() // Usamos await de kotlinx-coroutines-play-services
            location?.let {
                // Actualizar el estado de la ubicación actual
                currentLocation.value = LatLng(it.latitude, it.longitude)
                // Actualizar la posición de la cámara
                cameraPositionState.position = CameraPosition.fromLatLngZoom(currentLocation.value, 15f)
            } ?: throw Exception("No se pudo obtener la ubicación actual")
        } catch (e: Exception) {
            // Manejar errores al obtener la ubicación
            e.printStackTrace()
            throw e
        }
    } else {
        // Si el permiso no está concedido, lanzar excepción o manejarlo
        throw SecurityException("Permiso de ubicación no concedido")
    }
}

@Composable
fun LocationDisplayAndStore() {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val currentLocation = remember { mutableStateOf(LatLng(0.0, 0.0)) }
    val addressState = remember { mutableStateOf("Obteniendo dirección...") }

    // CoroutineScope para manejar operaciones asíncronas
    val coroutineScope = rememberCoroutineScope()

    // Solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permiso concedido, obtener la ubicación en una corrutina
            coroutineScope.launch {
                fetchLocation(fusedLocationClient, currentLocation, context)
            }
        } else {
            // Permiso denegado
            addressState.value = "Permiso de ubicación denegado"
        }
    }

    // Verificar y solicitar permisos al iniciar
    LaunchedEffect(Unit) {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permiso ya concedido, obtener la ubicación en una corrutina
                fetchLocation(fusedLocationClient, currentLocation, context)
            }
            else -> {
                // Solicitar permiso
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    // Obtener la dirección usando Geocoder
    LaunchedEffect(currentLocation.value) {
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(
                currentLocation.value.latitude,
                currentLocation.value.longitude,
                1
            )
            addressState.value = if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0)
            } else {
                "Dirección no disponible"
            }
        } catch (e: Exception) {
            addressState.value = "Error al obtener la dirección"
        }
    }

    // Mostrar la dirección, latitud y longitud en un formato de texto flotante
    Text(
        text = "Dirección: ${addressState.value}\nLatitud: ${currentLocation.value.latitude}\nLongitud: ${currentLocation.value.longitude}",
        modifier = Modifier.padding(16.dp)
    )

    // Botón para almacenar la ubicación en la base de datos
   /* Button(
        onClick = {
            coroutineScope.launch {
                storeLocationInDatabase(
                    addressState.value,
                    currentLocation.value.latitude,
                    currentLocation.value.longitude
                )
            }
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text("Almacenar ubicación")
    }

    */
}

private suspend fun fetchLocation(
    fusedLocationClient: FusedLocationProviderClient,
    currentLocation: MutableState<LatLng>,
    context: Context
) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let {
                currentLocation.value = LatLng(it.latitude, it.longitude)
            } ?: throw Exception("No se pudo obtener la ubicación actual")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    } else {
        throw SecurityException("Permiso de ubicación no concedido")
    }
}

private suspend fun storeLocationInDatabase(address: String, latitude: Double, longitude: Double) {
    // Aquí iría la lógica para almacenar la ubicación en la base de datos
    // Por ejemplo, usando Room o cualquier otro método de almacenamiento
    // Esto es solo un ejemplo, debes implementar la lógica real según tu base de datos
    // database.locationDao().insert(LocationEntity(address, latitude, longitude))
}
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MapsTheme {
        MapScreen()
    }
}
