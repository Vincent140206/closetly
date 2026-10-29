package vin.pam.closetly.core.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import vin.pam.closetly.core.model.ClosetlyEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClosetlyScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    floatingActionLabel: String? = null,
    onFloatingAction: (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (onBack != null) TextButton(onClick = onBack) { Text("Back") }
                },
                actions = {
                    if (actionLabel != null && onAction != null) {
                        TextButton(onClick = onAction) { Text(actionLabel) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        floatingActionButton = {
            if (floatingActionLabel != null && onFloatingAction != null) {
                FloatingActionButton(onClick = onFloatingAction) { Text(floatingActionLabel) }
            }
        },
        content = content,
    )
}

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ErrorState(message: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun ConfirmDeleteDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete item?") },
        text = { Text("This removes the item from the current dummy repository.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun <T : ClosetlyEntity> FeatureListContent(
    items: List<T>,
    useGrid: Boolean,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (useGrid) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(156.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item -> FeatureItemCard(item, onItemClick) }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(items, key = { it.id }) { item -> FeatureItemCard(item, onItemClick) }
        }
    }
}

@Composable
private fun FeatureItemCard(item: ClosetlyEntity, onItemClick: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onItemClick(item.id) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        if (item.imageUrl != null) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.title,
                modifier = Modifier.fillMaxWidth().height(142.dp),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                Modifier.fillMaxWidth().height(94.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center,
            ) { Text(item.title.take(1), style = MaterialTheme.typography.headlineLarge) }
        }
        Column(Modifier.padding(14.dp)) {
            Text(item.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(
                item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun FeatureDetailContent(
    item: ClosetlyEntity,
    details: List<Pair<String, String>>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDelete by rememberSaveable { mutableStateOf(false) }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            if (item.imageUrl != null) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxWidth().height(260.dp).clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        item { Text(item.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
        item { Text(item.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        items(details) { (label, value) ->
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, fontWeight = FontWeight.Medium)
                }
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onEdit, modifier = Modifier.weight(1f)) { Text("Edit") }
                OutlinedButton(onClick = { showDelete = true }, modifier = Modifier.weight(1f)) { Text("Delete") }
            }
        }
    }
    if (showDelete) {
        ConfirmDeleteDialog(onConfirm = { showDelete = false; onDelete() }, onDismiss = { showDelete = false })
    }
}

@Composable
fun FeatureFormContent(
    initialTitle: String,
    initialDescription: String,
    initialImageUrl: String?,
    supportsImage: Boolean,
    isSubmitting: Boolean,
    onSave: (title: String, description: String, imageUrl: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable(initialTitle) { mutableStateOf(initialTitle) }
    var description by rememberSaveable(initialDescription) { mutableStateOf(initialDescription) }
    var imageUrl by rememberSaveable(initialImageUrl) { mutableStateOf(initialImageUrl) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) imageUrl = uri.toString()
    }
    Column(
        modifier = modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (supportsImage) {
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Selected image preview",
                    modifier = Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            OutlinedButton(
                onClick = {
                    photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (imageUrl == null) "Choose image" else "Change image") }
            Text(
                "Local preview only. Supabase Storage upload is enabled when a cloud repository is added.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / notes") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onSave(title.trim(), description.trim(), imageUrl) },
            enabled = title.isNotBlank() && !isSubmitting,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text(if (isSubmitting) "Saving..." else "Save") }
    }
}
