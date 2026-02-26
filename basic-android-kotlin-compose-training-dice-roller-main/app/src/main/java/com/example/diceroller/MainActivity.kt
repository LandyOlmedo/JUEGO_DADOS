package com.example.diceroller

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diceroller.ui.theme.DiceRollerTheme
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiceRollerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiceRollerApp()
                }
            }
        }
    }
}

// Enum para controlar qué pantalla se muestra
enum class DiceScreen {
    Start,
    Game,
    Instructions
}

// Data class para el puntaje enviado al servidor
data class ScoreData(val score: Int)

// Data class para leer el JSON de instrucciones desde GitHub
data class InstructionItem(val mano: String, val puntos: Int)

// Interfaz para el servidor de puntajes
interface ScoreApiService {
    @POST("scores")
    suspend fun sendScore(@Body score: ScoreData)
}


interface GitHubApiService {
    // Aquí está el link exacto de donde extraemos los datos
    @GET("LandyOlmedo/instrucciones/main/manos_poker.json")
    suspend fun getInstructions(): List<InstructionItem>
}


object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val apiService: ScoreApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScoreApiService::class.java)
    }
}

// Cliente Retrofit para GitHub
object GitHubRetrofitClient {
    private const val BASE_URL = "https://raw.githubusercontent.com/"

    val apiService: GitHubApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApiService::class.java)
    }
}

@Preview
@Composable
fun DiceRollerApp() {
    var currentScreen by remember { mutableStateOf(DiceScreen.Start) }

    when (currentScreen) {
        DiceScreen.Start -> StartScreen(
            onPlayClick = { currentScreen = DiceScreen.Game },
            onInstructionsClick = { currentScreen = DiceScreen.Instructions }
        )
        DiceScreen.Instructions -> InstructionsScreen(
            onBackClick = { currentScreen = DiceScreen.Start }
        )
        DiceScreen.Game -> DiceWithButtonAndImage(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center),
            onBackClick = { currentScreen = DiceScreen.Start }
        )
    }
}

@Composable
fun StartScreen(onPlayClick: () -> Unit, onInstructionsClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Dice Roller", fontSize = 40.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onPlayClick, modifier = Modifier.width(200.dp)) {
            Text(text = "Jugar", fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onInstructionsClick, modifier = Modifier.width(200.dp)) {
            Text(text = "Instrucciones", fontSize = 20.sp)
        }
    }
}

@Composable
fun InstructionsScreen(onBackClick: () -> Unit) {
    // Estado para guardar las instrucciones descargadas
    var instructions by remember { mutableStateOf(emptyList<InstructionItem>()) }
    // Estado para guardar mensajes de error
    var errorMessage by remember { mutableStateOf("") }

    // Descargar las instrucciones desde GitHub al iniciar la pantalla
    LaunchedEffect(Unit) {
        try {
            // Llamada a la API de GitHub
            instructions = GitHubRetrofitClient.apiService.getInstructions()
        } catch (e: Exception) {
            errorMessage = "Error de conexión: ${e.localizedMessage}"
            Log.e("DiceRoller", "Error al descargar instrucciones", e)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título actualizado
        Text(text = "Puntaje", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (errorMessage.isNotEmpty()) {
            // Mostrar error si falla la conexión
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error, fontSize = 16.sp, modifier = Modifier.padding(16.dp))
        } else if (instructions.isEmpty()) {
            // Mostrar cargando mientras se descarga
            Text(text = "Cargando instrucciones...", fontSize = 18.sp)
        } else {
            // Mostrar la lista cuando ya hay datos
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(instructions) { item ->
                    Text(
                        text = "${item.mano}: ${item.puntos} ptos",
                        fontSize = 18.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBackClick) {
            Text(text = "Regresar")
        }
    }
}

@Composable
fun DiceWithButtonAndImage(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    var result1 by remember { mutableStateOf(1) }
    var result2 by remember { mutableStateOf(1) }
    var result3 by remember { mutableStateOf(1) }
    var result4 by remember { mutableStateOf(1) }
    var result5 by remember { mutableStateOf(1) }
    var result6 by remember { mutableStateOf(1) }

    var score by remember { mutableStateOf(0) }
    var attempts by remember { mutableStateOf(0) }
    var playResult by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    val imageResource1 = when (result1) { 1 -> R.drawable.dice_1; 2 -> R.drawable.dice_2; 3 -> R.drawable.dice_3; 4 -> R.drawable.dice_4; 5 -> R.drawable.dice_5; else -> R.drawable.dice_6 }
    val imageResource2 = when (result2) { 1 -> R.drawable.dice_1; 2 -> R.drawable.dice_2; 3 -> R.drawable.dice_3; 4 -> R.drawable.dice_4; 5 -> R.drawable.dice_5; else -> R.drawable.dice_6 }
    val imageResource3 = when (result3) { 1 -> R.drawable.dice_1; 2 -> R.drawable.dice_2; 3 -> R.drawable.dice_3; 4 -> R.drawable.dice_4; 5 -> R.drawable.dice_5; else -> R.drawable.dice_6 }
    val imageResource4 = when (result4) { 1 -> R.drawable.dice_1; 2 -> R.drawable.dice_2; 3 -> R.drawable.dice_3; 4 -> R.drawable.dice_4; 5 -> R.drawable.dice_5; else -> R.drawable.dice_6 }
    val imageResource5 = when (result5) { 1 -> R.drawable.dice_1; 2 -> R.drawable.dice_2; 3 -> R.drawable.dice_3; 4 -> R.drawable.dice_4; 5 -> R.drawable.dice_5; else -> R.drawable.dice_6 }
    val imageResource6 = when (result6) { 1 -> R.drawable.dice_1; 2 -> R.drawable.dice_2; 3 -> R.drawable.dice_3; 4 -> R.drawable.dice_4; 5 -> R.drawable.dice_5; else -> R.drawable.dice_6 }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {

        Text(text = "Intentos: $attempts / 10", fontSize = 20.sp)
        Text(text = "Puntaje: $score", fontSize = 20.sp)

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = playResult,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        val diceSize = Modifier.size(100.dp)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(imageResource1), contentDescription = result1.toString(), modifier = diceSize)
                Spacer(modifier = Modifier.height(8.dp))
                Image(painter = painterResource(imageResource2), contentDescription = result2.toString(), modifier = diceSize)
                Spacer(modifier = Modifier.height(8.dp))
                Image(painter = painterResource(imageResource3), contentDescription = result3.toString(), modifier = diceSize)
            }
            Spacer(modifier = Modifier.width(24.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painter = painterResource(imageResource4), contentDescription = result4.toString(), modifier = diceSize)
                Spacer(modifier = Modifier.height(8.dp))
                Image(painter = painterResource(imageResource5), contentDescription = result5.toString(), modifier = diceSize)
                Spacer(modifier = Modifier.height(8.dp))
                Image(painter = painterResource(imageResource6), contentDescription = result6.toString(), modifier = diceSize)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (attempts < 10) {
                    result1 = (1..6).random()
                    result2 = (1..6).random()
                    result3 = (1..6).random()
                    result4 = (1..6).random()
                    result5 = (1..6).random()
                    result6 = (1..6).random()

                    attempts++

                    val resultsList = listOf(result1, result2, result3, result4, result5, result6)
                    val (rollResult, rollScore) = evaluateRoll(resultsList)
                    playResult = rollResult
                    score += rollScore
                    
                    if (attempts == 10) {
                         scope.launch {
                            try {
                                RetrofitClient.apiService.sendScore(ScoreData(score))
                                Log.d("DiceRoller", "Puntaje enviado con éxito: $score")
                            } catch (e: Exception) {
                                Log.e("DiceRoller", "Error al enviar puntaje", e)
                            }
                        }
                    }
                }
            },
            enabled = attempts < 10
        ) {
            if (attempts < 10) {
                Text(text = stringResource(R.string.roll), fontSize = 24.sp)
            } else {
                Text(text = "Fin del juego", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = {
                    score = 0
                    attempts = 0
                    playResult = ""
                    result1 = 1
                    result2 = 1
                    result3 = 1
                    result4 = 1
                    result5 = 1
                    result6 = 1
                }
            ) {
                Text(text = stringResource(R.string.reset), fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onBackClick) {
                Text(text = "Menu", fontSize = 18.sp)
            }
        }
    }
}

private fun evaluateRoll(results: List<Int>): Pair<String, Int> {
    val counts = results.groupingBy { it }.eachCount()
    val isStraight = counts.size == 6
    val fourOfAKind = counts.values.count { it == 4 }
    val threeOfAKind = counts.values.count { it == 3 }
    val pairs = counts.values.count { it == 2 }

    return when {
        isStraight -> "Escalera" to 7
        counts.containsValue(6) -> "Sextilla" to 6
        counts.containsValue(5) -> "Quintilla" to 5
        fourOfAKind == 1 && pairs == 1 -> "Poker y Par" to 5
        fourOfAKind == 1 -> "Poker" to 4
        threeOfAKind == 2 -> "Dos Tercias" to 4
        threeOfAKind == 1 && pairs == 1 -> "Full House (Tercia y Par)" to 3
        threeOfAKind == 1 -> "Tercia" to 2
        pairs == 3 -> "Tres Pares" to 3
        pairs == 2 -> "Dos Pares" to 2
        pairs == 1 -> "Un Par" to 1
        else -> "Nada" to 0
    }
}
