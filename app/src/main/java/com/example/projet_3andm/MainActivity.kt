package com.example.projet_3andm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.projet_3andm.api.RecipeSeeder
import com.example.projet_3andm.database.DatabaseProvider
import com.example.projet_3andm.database.ItemDao
import com.example.projet_3andm.database.ItemEntity
import com.example.projet_3andm.ui.theme.Projet_3ANDMTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler

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
                var recipes by remember { mutableStateOf<List<ItemEntity>>(emptyList()) }
                var visibleCount by remember { mutableIntStateOf(10) }
                var currentScreen by remember { mutableStateOf("list") }
                var selectedRecipe by remember { mutableStateOf<ItemEntity?>(null) }
                var isLoadingMore by remember { mutableStateOf(false) }
                var searchQuery by remember { mutableStateOf("") }

                LaunchedEffect(Unit) {
                    lifecycleScope.launch {
                        RecipeSeeder.seedDatabase(itemDao)
                        recipes = itemDao.getAllRecipes()
                        recipeCount = recipes.size
                        visibleCount = 10
                    }
                }

                val filteredRecipes = remember(searchQuery, recipes) {
                    if (searchQuery.isEmpty()) {
                        recipes
                    } else {
                        recipes.filter { it.title.contains(searchQuery, ignoreCase = true) }
                    }
                }

                BackHandler(enabled = currentScreen == "details") {
                    currentScreen = "list"
                    selectedRecipe = null
                }

                val listState = rememberLazyListState()
                val shouldLoadMore by remember {
                    derivedStateOf {
                        val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisibleItemIndex >= visibleCount - 2 && searchQuery.isEmpty()
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

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if(currentScreen == "list"){
                            // La TopAppBar avec le TextField de recherche
                            TopAppBar(
                                title = {
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Rechercher une recette...") },
                                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Effacer")
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                                            unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                                        )
                                    )
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    if (currentScreen == "list") {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                Text(
                                    text = if (searchQuery.isEmpty()) "Toutes les recettes" else "${filteredRecipes.size} résultat(s) pour \"$searchQuery\"",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Utilisation de filteredRecipes au lieu de recipes
                            // On applique .take(visibleCount) seulement si on ne recherche rien
                            val displayList = if (searchQuery.isEmpty()) {
                                filteredRecipes.take(visibleCount)
                            } else {
                                filteredRecipes
                            }

                            items(displayList) { recipe ->
                                CardItem(
                                    title = recipe.title,
                                    imageUrl = recipe.image,
                                    onClick = {
                                        selectedRecipe = recipe
                                        currentScreen = "details"
                                    }
                                )
                            }

                            if (isLoadingMore && searchQuery.isEmpty()) {
                                item {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                                        CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                    }
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
                                Button(onClick = { currentScreen = "list"; selectedRecipe = null }) {
                                    Text("Retour")
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                                ) {
                                    CardItem(title = recipe.title, imageUrl = recipe.image, onClick = {})
                                }

                                Text("Catégorie : ${recipe.category}", modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.SemiBold)
                                Text("Description", modifier = Modifier.padding(top = 20.dp), fontWeight = FontWeight.Bold)
                                Text(text = recipe.description, modifier = Modifier.padding(top = 8.dp, bottom = 24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}