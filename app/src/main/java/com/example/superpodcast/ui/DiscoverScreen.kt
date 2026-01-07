package com.example.superpodcast.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.superpodcast.ui.theme.*

data class PodcastCategoryUi(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val accent: Color
)

@Composable
fun DiscoverScreen(
    onCategoryClick: (PodcastCategoryUi) -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    val categories = listOf(
        PodcastCategoryUi("Motivation", "Daily energy", "⚡", Amber),
        PodcastCategoryUi("Business", "Growth & money", "💼", Mint),
        PodcastCategoryUi("Mind", "Psychology", "🧠", Violet),
        PodcastCategoryUi("Fitness", "Strong body", "🏋️", Orange),
        PodcastCategoryUi("Kids", "Family time", "🧸", Color(0xFF80DEEA)),
        PodcastCategoryUi("Tech", "AI & coding", "🤖", Color(0xFF90CAF9)),
    )

    Surface(modifier = Modifier.fillMaxSize(), color = Espresso) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SuperPodcast", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                        Text(
                            "Discover categories & trending shows",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search",
                            tint = TextPrimary
                        )
                    }
                }
            }

            item {
                Text("Podcast Category", style = MaterialTheme.typography.titleMedium, color = TextPrimary)
                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(330.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 4.dp)
                    ) {
                        items(categories) { cat ->
                            CategoryCard(category = cat, onClick = { onCategoryClick(cat) })
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Trending")
                TrendingRowItem(title = "The Daily Mindset", author = "Mind Studio", tag = "Mind")
                TrendingRowItem(title = "Startup Stories", author = "Business Hub", tag = "Business")
                TrendingRowItem(title = "Gym Power Mix", author = "Fit Cast", tag = "Fitness")
            }
        }
    }
}

@Composable
private fun CategoryCard(category: PodcastCategoryUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Cocoa),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(category.accent.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category.iconEmoji, style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    category.title,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    category.subtitle,
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text("See all", style = MaterialTheme.typography.bodySmall, color = Amber)
    }
}

@Composable
private fun TrendingRowItem(title: String, author: String, tag: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Cocoa),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Mocha),
                contentAlignment = Alignment.Center
            ) {
                Text("🎧")
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(author, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }

            Surface(
                color = Latte,
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = tag,
                    color = TextPrimary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

        }
    }
}
