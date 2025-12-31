package io.github.cookieysun.otoshidamaroulette.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
        settingsViewModel: SettingsViewModel,
        onBackClick: () -> Unit,
        modifier: Modifier = Modifier
) {
    var showColorPicker by remember { mutableStateOf(false) }
    var selectedItemIndex by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState()

    val amounts by settingsViewModel.amounts.collectAsState()
    val colorIndices by settingsViewModel.colorIndices.collectAsState()

    Scaffold(
            topBar = {
                TopAppBar(
                        title = { Text("設定") },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "戻る"
                                )
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFFD32F2F),
                                        titleContentColor = Color.White,
                                        navigationIconContentColor = Color.White
                                )
                )
            },
            modifier = modifier
    ) { innerPadding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .background(Color(0xFFFFF8E1))
                                .padding(innerPadding)
                                .padding(16.dp)
                                .verticalScroll(rememberScrollState())
        ) {
            Text(
                    text = "金額設定",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "各項目の金額を入力してください（${amounts.size}項目）", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            amounts.forEachIndexed { index, amount ->
                AmountEditRow(
                        index = index,
                        amount = amount,
                        color = settingsViewModel.getColorForIndex(index),
                        canDelete = amounts.size > 2,
                        canMoveUp = index > 0,
                        canMoveDown = index < amounts.size - 1,
                        onAmountChange = { newAmount ->
                            settingsViewModel.updateAmount(index, newAmount)
                        },
                        onColorClick = {
                            selectedItemIndex = index
                            showColorPicker = true
                        },
                        onDeleteClick = { settingsViewModel.removeItem(index) },
                        onMoveUpClick = { settingsViewModel.moveItem(index, index - 1) },
                        onMoveDownClick = { settingsViewModel.moveItem(index, index + 1) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 項目追加ボタン
            Button(
                    onClick = { settingsViewModel.addItem() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("項目を追加")
            }
        }

        // カラーピッカーボトムシート
        if (showColorPicker) {
            ModalBottomSheet(
                    onDismissRequest = { showColorPicker = false },
                    sheetState = sheetState,
                    containerColor = Color.White,
                    scrimColor = Color.Black.copy(alpha = 0.3f)
            ) {
                ColorPickerContent(
                        availableColors = settingsViewModel.availableColors,
                        selectedColorIndex = colorIndices[selectedItemIndex],
                        onColorSelected = { colorIndex ->
                            settingsViewModel.updateColor(selectedItemIndex, colorIndex)
                            showColorPicker = false
                        }
                )
            }
        }
    }
}

@Composable
private fun ColorPickerContent(
        availableColors: List<Color>,
        selectedColorIndex: Int,
        onColorSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(
                text = "色を選択",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            availableColors.forEachIndexed { index, color ->
                Box(
                        modifier =
                                Modifier.size(48.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                                width =
                                                        if (index == selectedColorIndex) 3.dp
                                                        else 0.dp,
                                                color =
                                                        if (index == selectedColorIndex)
                                                                Color(0xFF333333)
                                                        else Color.Transparent,
                                                shape = CircleShape
                                        )
                                        .clickable { onColorSelected(index) }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AmountEditRow(
        index: Int,
        amount: Int,
        color: Color,
        canDelete: Boolean,
        canMoveUp: Boolean,
        canMoveDown: Boolean,
        onAmountChange: (Int) -> Unit,
        onColorClick: () -> Unit,
        onDeleteClick: () -> Unit,
        onMoveUpClick: () -> Unit,
        onMoveDownClick: () -> Unit
) {
    Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // 上下移動ボタン
        Column {
            IconButton(
                    onClick = onMoveUpClick,
                    enabled = canMoveUp,
                    modifier = Modifier.size(24.dp)
            ) {
                Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "上に移動",
                        tint = if (canMoveUp) Color(0xFF666666) else Color.LightGray
                )
            }
            IconButton(
                    onClick = onMoveDownClick,
                    enabled = canMoveDown,
                    modifier = Modifier.size(24.dp)
            ) {
                Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "下に移動",
                        tint = if (canMoveDown) Color(0xFF666666) else Color.LightGray
                )
            }
        }

        // 色インジケーター（クリック可能）
        Box(
                modifier =
                        Modifier.size(32.dp).clip(CircleShape).background(color).clickable {
                            onColorClick()
                        }
        )

        // 番号
        Text(
                text = "${index + 1}.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333),
                modifier = Modifier.width(24.dp)
        )

        // 金額入力フィールド
        OutlinedTextField(
                value = amount.toString(),
                onValueChange = { newValue ->
                    onAmountChange(
                            when {
                                newValue.isEmpty() -> {
                                    0
                                }
                                amount == 0 -> {
                                    newValue.replace(Regex("0"), "").toIntOrNull()
                                }
                                else -> {
                                    newValue.toIntOrNull()
                                }
                            }
                                    ?: 0
                    )
                },
                label = { Text("金額") },
                suffix = { Text("円") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle =
                        androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF333333),
                                fontSize = 16.sp
                        ),
                modifier = Modifier.weight(1f)
        )

        // 削除ボタン
        IconButton(onClick = onDeleteClick, enabled = canDelete) {
            Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "削除",
                    tint = if (canDelete) Color(0xFFF44336) else Color.Gray
            )
        }
    }
}
