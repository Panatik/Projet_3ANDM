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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import kotlinx.coroutines.delay
import androidx.compose.runtime.mutableStateOf

@OptIn(ExperimentalMaterial3Api::class)
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
                var visibleCount by remember { mutableIntStateOf(10) }
                var currentScreen by remember { androidx.compose.runtime.mutableStateOf("list") }
                var selectedRecipe by remember { androidx.compose.runtime.mutableStateOf<ItemEntity?>(null) }
                var isLoadingMore by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        RecipeSeeder.seedDatabase(itemDao)
                        recipeCount = itemDao.countRecipes()
                        recipes = itemDao.getAllRecipes()
                        visibleCount = 10
                    }
                }

                BackHandler(enabled = currentScreen == "details") {
                    currentScreen = "list"
                    selectedRecipe = null
                }
                val listState = rememberLazyListState()
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleItemIndex =
                            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisibleItemIndex >= visibleCount - 2
                    }
                }

                LaunchedEffect(shouldLoadMore) {
                    if (currentScreen == "list" && shouldLoadMore && !isLoadingMore) {
                        isLoadingMore = true
                        delay(1000)

                        if (visibleCount < recipes.size) {
                            visibleCount = minOf(visibleCount + 10, recipes.size)
                        } else {
                            val addedCount = RecipeSeeder.loadMoreRecipes(itemDao, 10)
                            if (addedCount > 0) {
                                recipes = itemDao.getAllRecipes()
                                recipeCount = recipes.size
                                visibleCount = minOf(visibleCount + 10, recipes.size)
                            }
                        }

                        isLoadingMore = false
                    }
                }

                val filteredRecipes = remember(searchQuery, recipes) {
                    if (searchQuery.isEmpty()) {
                        recipes
                    } else {
                        recipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // C'est ici qu'on remplace le titre par la barre de recherche
                        TopAppBar(
                            title = {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Rechercher une recette...") },
                                    modifier = Modifier.fillMaxWidth(),
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                    )
                                )
                            }
                        )
                    }
                ) { innerPadding ->
                val listState = rememberLazyListState()
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleItemIndex =
                            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisibleItemIndex >= visibleCount - 2
                    }
                }
                LaunchedEffect(shouldLoadMore) {
                    if (currentScreen == "list" && shouldLoadMore && !isLoadingMore) {
                        isLoadingMore = true
                        delay(1000)

                        if (visibleCount < recipes.size) {
                            visibleCount = minOf(visibleCount + 10, recipes.size)
                        } else {
                            val addedCount = RecipeSeeder.loadMoreRecipes(itemDao, 10)
                            if (addedCount > 0) {
                                recipes = itemDao.getAllRecipes()
                                recipeCount = recipes.size
                                visibleCount = minOf(visibleCount + 10, recipes.size)
                            }
                        }

                        isLoadingMore = false
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (currentScreen == "list") {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            /*item {
                                Text(
                                    text = "Recettes en base : $recipeCount",
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }*/

                            item {
                                Text(
                                    text = "${filteredRecipes.size} recettes trouvées",
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            items(recipes.take(visibleCount)) { recipe ->
                                CardItem(
                                    title = recipe.title,
                                    imageUrl = recipe.image,
                                    onClick = {
                                        selectedRecipe = recipe
                                        currentScreen = "details"
                                    }
                                )
                            }
                            if (isLoadingMore) {
                                item {
                                    Text(
                                        text = "Chargement...",
                                        modifier = Modifier.padding(vertical = 16.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        selectedRecipe?.let { recipe ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        currentScreen = "list"
                                        selectedRecipe = null
                                    }
                                ) {
                                    Text("Retour")
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    Column {
                                        CardItem(
                                            title = recipe.title,
                                            imageUrl = recipe.image,
                                            onClick = {}
                                        )
                                    }
                                }

                                Text(
                                    text = "Catégorie : ${recipe.category}",
                                    modifier = Modifier.padding(top = 12.dp),
                                    fontWeight = FontWeight.SemiBold
                                )

                                Text(
                                    text = "Description",
                                    modifier = Modifier.padding(top = 20.dp),
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = recipe.description,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                                )
                            }
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