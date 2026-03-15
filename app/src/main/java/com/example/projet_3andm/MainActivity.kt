package com.example.projet_3andm
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.projet_3andm.ui.theme.Projet_3ANDMTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.example.projet_3andm.database.DatabaseProvider
import com.example.projet_3andm.database.ItemEntity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.dp
import com.example.projet_3andm.api.RecipeSeeder
import com.example.projet_3andm.database.ItemDao
import kotlinx.coroutines.launch
class MainActivity : ComponentActivity() {
    private lateinit var itemDao: ItemDao
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)
        val db = DatabaseProvider.getDatabase(this)
        itemDao = db.itemDao()
        enableEdgeToEdge()
        setContent {
            Projet_3ANDMTheme {
                var recipeCount by remember { mutableIntStateOf(0) }
                var recipes by remember { androidx.compose.runtime.mutableStateOf<List<ItemEntity>>(emptyList()) }

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        RecipeSeeder.seedDatabase(itemDao)
                        recipeCount = itemDao.countRecipes()
                        recipes = itemDao.getAllRecipes()
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        item {
                            Text(
                                text = "Recettes en base : $recipeCount",
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                        items(recipes) { recipe ->
                            CardItem(
                                title = recipe.title,
                                imageUrl = recipe.image
                            )
                            Text(text = recipe.image)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Projet_3ANDMTheme {
        Greeting("Android")
    }
}