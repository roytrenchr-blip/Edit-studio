package com.editstudio.app.ui.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.editstudio.app.store.StoreItem
import com.editstudio.app.ui.theme.NavyAmber
import com.editstudio.app.ui.theme.NavyBg
import com.editstudio.app.ui.theme.NavyBorder
import com.editstudio.app.ui.theme.NavyPrimary
import com.editstudio.app.ui.theme.NavySurface
import com.editstudio.app.ui.theme.NavyTextPrimary
import com.editstudio.app.ui.theme.NavyTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    viewModel: StoreViewModel,
    onUseTemplate: (StoreItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val downloadStates by viewModel.downloadStates.collectAsStateWithLifecycle()

    val filteredItems = state.items.filter { item ->
        val matchesCategory = state.selectedCategory == "All" || item.category == state.selectedCategory
        val matchesQuery = state.searchQuery.isBlank() ||
            item.name.contains(state.searchQuery, ignoreCase = true) ||
            item.description.contains(state.searchQuery, ignoreCase = true)
        matchesCategory && matchesQuery
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Edit Studio Store",
                    style = MaterialTheme.typography.titleLarge,
                    color = NavyTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Motion Packs, LUTs, Templates & Effects",
                    style = MaterialTheme.typography.bodySmall,
                    color = NavyPrimary
                )
            }
            IconButton(onClick = { viewModel.loadManifest() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh Karein", tint = NavyTextPrimary)
            }
        }

        if (state.isOffline) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyAmber.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudOff, contentDescription = null, tint = NavyAmber)
                Text(
                    text = "Aap offline hain! Cached starter library dikhayi ja rahi hai.",
                    style = MaterialTheme.typography.labelSmall,
                    color = NavyAmber,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Store mein khojein...", color = NavyTextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NavyTextSecondary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NavyPrimary,
                unfocusedBorderColor = NavyBorder
            )
        )

        val selectedIndex = viewModel.categories.indexOf(state.selectedCategory).coerceAtLeast(0)
        ScrollableTabRow(
            selectedTabIndex = selectedIndex,
            containerColor = NavySurface,
            contentColor = NavyPrimary,
            edgePadding = 16.dp
        ) {
            viewModel.categories.forEachIndexed { index, cat ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { viewModel.selectCategory(cat) },
                    text = {
                        Text(
                            text = cat,
                            fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedIndex == index) NavyPrimary else NavyTextSecondary
                        )
                    }
                )
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NavyPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    StoreItemCard(
                        item = item,
                        downloadStatus = downloadStates[item.id],
                        onDownload = { viewModel.downloadItem(item) },
                        onUseTemplate = { onUseTemplate(item) }
                    )
                }
            }
        }
    }
}
