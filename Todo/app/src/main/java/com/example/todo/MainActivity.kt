package com.example.todo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.todo.ui.theme.TodoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val todoViewModel: TodoViewModel by viewModels {
        TodoViewModelFactory((application as TodoApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TodoTheme {
                TodoApp(todoViewModel = todoViewModel)
            }
        }
    }
}


@Composable
fun TodoApp(todoViewModel: TodoViewModel) {
    val todos by todoViewModel.allTodos.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Text("+")
            }
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                Column(modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)) {
                    LazyColumn {
                        items(todos) { todo ->
                            TodoItem(todo, todoViewModel)
                        }
                    }
                }

                if (showDialog) {
                    TodoDialog(onDismiss = { showDialog = false }) { title, description ->
                        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(
                            Date()
                        )
                        todoViewModel.insert(Todo(title = title, description = description, date = currentDate))
                        showDialog = false
                    }
                }
            }
        }
    )
}

@Composable
fun TodoItem(todo: Todo, todoViewModel: TodoViewModel) {
    var showEditDialog by remember { mutableStateOf(false) }

    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(todo.title, style = MaterialTheme.typography.headlineSmall)
            Text(todo.description, style = MaterialTheme.typography.bodySmall)
            Text(todo.date, style = MaterialTheme.typography.bodySmall)

            Row {
                IconButton(onClick = { todoViewModel.delete(todo) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
                IconButton(onClick = { showEditDialog = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }

            if (showEditDialog) {
                TodoDialog(
                    onDismiss = { showEditDialog = false },
                    initialTitle = todo.title,
                    initialDescription = todo.description
                ) { title, description ->
                    val updatedTodo = todo.copy(title = title, description = description)
                    todoViewModel.update(updatedTodo)
                    showEditDialog = false
                }
            }
        }
    }
}

@Composable
fun TodoDialog(
    onDismiss: () -> Unit,
    initialTitle: String = "",
    initialDescription: String = "",
    onConfirm: (String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add/Edit Todo") },
        text = {
            Column {
                TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(title, description) }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}