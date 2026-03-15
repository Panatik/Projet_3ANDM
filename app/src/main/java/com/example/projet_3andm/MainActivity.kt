package com.example.projet_3andm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.projet_3andm.api.RecipeSeeder
import com.example.projet_3andm.database.CategoryDao
import com.example.projet_3andm.database.CategoryEntity
import com.example.projet_3andm.database.DatabaseProvider
import com.example.projet_3andm.database.ItemDao
import com.example.projet_3andm.database.ItemEntity
import com.example.projet_3andm.ui.theme.Projet_3ANDMTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var itemDao: ItemDao
    private lateinit var categoryDao: CategoryDao

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        val db = DatabaseProvider.getDatabase(this)
        itemDao = db.itemDao()
        categoryDao = db.categoryDao()

        enableEdgeToEdge()

        setContent {
            Projet_3ANDMTheme {
                var recipeCount by remember { mutableIntStateOf(0) }
                var recipes by remember { mutableStateOf<List<ItemEntity>>(emptyList()) }
                var categories by remember { mutableStateOf<List<CategoryEntity>>(emptyList()) }

                var visibleCount by remember { mutableIntStateOf(10) }
                var currentScreen by remember { mutableStateOf("list") }
                var selectedRecipe by remember { mutableStateOf<ItemEntity?>(null) }

                var isLoadingMore by remember { mutableStateOf(false) }
                var isSearchingApi by remember { mutableStateOf(false) }

                var searchQuery by remember { mutableStateOf("") }
                var selectedCategory by remember { mutableStateOf<String?>(null) }

                var exhaustedCategories by remember { mutableStateOf(setOf<String>()) }
                var noMoreGlobalRecipes by remember { mutableStateOf(false) }

                val isCurrentContextExhausted = if (selectedCategory != null) {
                    exhaustedCategories.contains(selectedCategory)
                } else {
                    noMoreGlobalRecipes
                }

                LaunchedEffect(Unit) {
                    RecipeSeeder.seedCategories(categoryDao)
                    RecipeSeeder.seedDatabase(itemDao)

                    categories = categoryDao.getAllCategories()
                    recipes = itemDao.getAllRecipes()
                    recipeCount = recipes.size
                    visibleCount = 10
                }

                val filteredRecipes = remember(searchQuery, recipes, selectedCategory) {
                    recipes.filter { recipe ->
                        val matchesSearch =
                            searchQuery.isBlank() || recipe.title.contains(searchQuery, ignoreCase = true)

                        val matchesCategory =
                            selectedCategory == null || recipe.category == selectedCategory

                        matchesSearch && matchesCategory
                    }
                }

                val displayList = filteredRecipes.take(visibleCount)

                LaunchedEffect(searchQuery) {
                    if (searchQuery.isBlank()) return@LaunchedEffect

                    delay(500)
                    isSearchingApi = true

                    try {
                        withTimeoutOrNull(5_000) {
                            RecipeSeeder.searchRecipesAndCache(itemDao, searchQuery)
                        }

                        recipes = itemDao.getAllRecipes()
                        recipeCount = recipes.size
                        visibleCount = 10
                    } finally {
                        isSearchingApi = false
                    }
                }

                LaunchedEffect(selectedCategory, searchQuery) {
                    visibleCount = 10
                    if (searchQuery.isNotBlank()) {
                        noMoreGlobalRecipes = false
                    }
                }

                LaunchedEffect(selectedCategory) {
                    if (
                        currentScreen == "list" &&
                        selectedCategory != null &&
                        filteredRecipes.isEmpty() &&
                        !isLoadingMore &&
                        !exhaustedCategories.contains(selectedCategory)
                    ) {
                        isLoadingMore = true
                        try {
                            val addedCount = withTimeoutOrNull(5_000) {
                                RecipeSeeder.loadMoreRecipesByCategory(itemDao, selectedCategory!!, 10)
                            } ?: 0

                            if (addedCount > 0) {
                                recipes = itemDao.getAllRecipes()
                                recipeCount = recipes.size
                            } else {
                                exhaustedCategories = exhaustedCategories + selectedCategory!!
                            }

                            visibleCount = 10
                        } finally {
                            isLoadingMore = false
                        }
                    }
                }

                BackHandler(enabled = currentScreen == "details") {
                    currentScreen = "list"
                    selectedRecipe = null
                }

                val listState = rememberLazyListState()

                val shouldLoadMore by remember(
                    listState,
                    currentScreen,
                    displayList.size,
                    isLoadingMore,
                    isCurrentContextExhausted
                ) {
                    derivedStateOf {
                        if (currentScreen != "list") return@derivedStateOf false
                        if (displayList.isEmpty()) return@derivedStateOf false
                        if (isLoadingMore) return@derivedStateOf false
                        if (isCurrentContextExhausted) return@derivedStateOf false

                        val lastVisibleItemIndex =
                            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        val totalItemsCount = listState.layoutInfo.totalItemsCount

                        lastVisibleItemIndex >= totalItemsCount - 3
                    }
                }

                LaunchedEffect(
                    shouldLoadMore,
                    selectedCategory,
                    searchQuery,
                    filteredRecipes.size,
                    recipes.size
                ) {
                    if (!shouldLoadMore) return@LaunchedEffect

                    isLoadingMore = true

                    try {
                        val result = withTimeoutOrNull(5_000) {
                            delay(1000)

                            if (visibleCount < filteredRecipes.size) {
                                visibleCount = minOf(visibleCount + 10, filteredRecipes.size)
                                "display_more"
                            } else {
                                val addedCount = when {
                                    searchQuery.isNotBlank() -> {
                                        RecipeSeeder.searchRecipesAndCache(itemDao, searchQuery)
                                    }
                                    selectedCategory != null -> {
                                        RecipeSeeder.loadMoreRecipesByCategory(itemDao, selectedCategory!!, 10)
                                    }
                                    else -> {
                                        RecipeSeeder.loadMoreRecipes(itemDao, 10)
                                    }
                                }

                                if (addedCount > 0) {
                                    recipes = itemDao.getAllRecipes()
                                    recipeCount = recipes.size
                                    visibleCount = minOf(visibleCount + 10, recipes.size)
                                    "api_more"
                                } else {
                                    when {
                                        searchQuery.isNotBlank() -> {
                                            noMoreGlobalRecipes = true
                                        }
                                        selectedCategory != null -> {
                                            exhaustedCategories = exhaustedCategories + selectedCategory!!
                                        }
                                        else -> {
                                            noMoreGlobalRecipes = true
                                        }
                                    }
                                    "nothing_more"
                                }
                            }
                        }

                        if (result == null) {
                            when {
                                searchQuery.isNotBlank() -> {
                                    noMoreGlobalRecipes = true
                                }
                                selectedCategory != null -> {
                                    exhaustedCategories = exhaustedCategories + selectedCategory!!
                                }
                                else -> {
                                    noMoreGlobalRecipes = true
                                }
                            }
                        }
                    } finally {
                        isLoadingMore = false
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (currentScreen == "list") {
                            TopAppBar(
                                title = {
                                    TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("Rechercher une recette...") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(end = 16.dp),
                                        leadingIcon = {
                                            Icon(Icons.Default.Search, contentDescription = null)
                                        },
                                        trailingIcon = {
                                            if (searchQuery.isNotEmpty()) {
                                                IconButton(onClick = { searchQuery = "" }) {
                                                    Icon(
                                                        Icons.Default.Clear,
                                                        contentDescription = "Effacer"
                                                    )
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
                            if (isSearchingApi) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = "Recettes disponibles hors connexion : $recipeCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilterChip(
                                        selected = selectedCategory == null,
                                        onClick = { selectedCategory = null },
                                        label = { Text("Toutes") }
                                    )

                                    categories.forEach { category ->
                                        FilterChip(
                                            selected = selectedCategory == category.name,
                                            onClick = { selectedCategory = category.name },
                                            label = { Text(category.name) }
                                        )
                                    }
                                }
                            }

                            item {
                                Text(
                                    text = if (searchQuery.isBlank()) {
                                        "Toutes les recettes"
                                    } else {
                                        "${filteredRecipes.size} résultat(s) pour \"$searchQuery\""
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
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

                            if (isLoadingMore && searchQuery.isBlank()) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(30.dp))
                                    }
                                }
                            }

                            if (isCurrentContextExhausted && searchQuery.isBlank()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "Toutes les recettes disponibles ont été chargées",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
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
                                    CardItem(
                                        title = recipe.title,
                                        imageUrl = recipe.image,
                                        onClick = {}
                                    )
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