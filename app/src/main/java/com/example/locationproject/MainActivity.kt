package com.example.locationproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity

import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.example.locationproject.ui.theme.LocationProjectTheme

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LocationProjectTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                       // FullscreenImage(imageRes = R.drawable.background)
                        BottomCard()
                        GetCurrentLocation() //Check above code for this
                    }
                }
            }
        }
    }
}

@Composable
fun BottomCard() {
    // Get screen height
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val offsetY = with(LocalDensity.current) { (screenHeight / 2.8f).toPx().toInt() } // Convert Dp to pixels

    Box(modifier = Modifier.fillMaxSize()) {
        // Card positioned half from the bottom
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset { IntOffset(x = 0, y = offsetY) } // Offset using calculated pixel value
                .fillMaxSize(), // Adjust card width as needed
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        )  {
            // Card content
            Text(
                text = "This is a half-screen rounded card!",
                fontSize = 16.sp,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                color = Color.Black
            )
        }
    }
}

@Composable
fun GetCurrentLocation() {
    val context = LocalContext.current
    var address by remember { mutableStateOf("Fetching address...") }
    var latitude by remember {mutableStateOf("Latitude N/A")}
    var longitude by remember {mutableStateOf("Longitude N/A")}

    // Create a FusedLocationProviderClient
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    // Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            fetchLocation(fusedLocationClient) {
                latitude = "Latitude: ${it.latitude}"
                longitude = "Longitude: ${it.longitude}"
                fetchAddressFromLocation(context,it) {
                    address = it ?: "Unable to fetch address"
                }
            }
        } else {
            address = "Permission denied"
        }
    }

    // Check if permission is granted, otherwise request it
    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> {
                fetchLocation(fusedLocationClient) {
                    latitude = "Latitude: ${it.latitude}"
                    longitude = "Longitude: ${it.longitude}"
                    fetchAddressFromLocation(context, it) {
                        address = it ?: "Unable to fetch address"
                    }

                }
            }
            else -> locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Display the address in a Composable
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(start=16.dp, end = 16.dp, top=64.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)

        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = "Current Location: \n$address",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "$latitude",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    text = "$longitude",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )

                Button(
                    onClick = {
                        fetchLocation(fusedLocationClient) {
                            latitude = "Latitude: ${it.latitude}"
                            longitude = "Longitude: ${it.longitude}"
                            fetchAddressFromLocation(context, it) {
                                address = it ?: "Unable to fetch address"
                            }

                        }
                    },) {
                    Text(text = "Refresh Location")
                }
            }
        }
    }
            }


@SuppressLint("MissingPermission")
private fun fetchLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationFetched: (Location) -> Unit
) {
    val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
        priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        interval = 1000 // Time between updates
        numUpdates = 1  // Only one update
    }

    fusedLocationClient.requestLocationUpdates(
        locationRequest,
        object : com.google.android.gms.location.LocationCallback() {
            override fun onLocationResult(result: com.google.android.gms.location.LocationResult) {
                val location = result.lastLocation
                if (location != null) {
                    onLocationFetched(location)
                }
            }
        },
        null
    )
}



private fun fetchAddressFromLocation(context: Context, location: Location, onAddressFetched: (String?) -> Unit) {
    val geocoder = Geocoder(context, Locale.getDefault())
    val latitude = location.latitude
    val longitude = location.longitude

    try {
        val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
        Log.d("Address", addresses?.get(0)?.toString() ?: "No address object")
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0].getAddressLine(0)
            onAddressFetched(address)
        } else {
            onAddressFetched("No address found")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onAddressFetched("Error fetching address: ${e.message}")
    }
}