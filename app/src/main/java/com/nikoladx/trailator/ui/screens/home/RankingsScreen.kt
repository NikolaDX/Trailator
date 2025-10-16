package com.nikoladx.trailator.ui.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nikoladx.trailator.ui.screens.home.components.RankingHeader
import com.nikoladx.trailator.ui.screens.home.components.RankingItem
import com.nikoladx.trailator.ui.screens.home.viewmodels.RankingsViewModel

@Composable
fun RankingsScreen(
    viewModel: RankingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Leaderboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        HorizontalDivider()

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(32.dp))
        } else if (uiState.error != null) {
            Text(
                "Error loading rankings: ${uiState.error}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else if (uiState.rankedUsers.isEmpty()) {
            Text("No users found.", modifier = Modifier.padding(16.dp))
        } else {
            RankingHeader()

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(uiState.rankedUsers) { index, user ->
                    RankingItem(user = user, rank = index + 1)
                }
            }
        }
    }
}