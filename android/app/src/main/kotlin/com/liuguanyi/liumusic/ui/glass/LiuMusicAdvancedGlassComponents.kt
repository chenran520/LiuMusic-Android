package com.liuguanyi.liumusic.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LiuMusicGlassSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    steps: Int = 0,
    valueLabel: (Float) -> String = { "%.0f".format(it) },
) {
    Column(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title, style = LiuMusicTypography.subheadline)
            Text(valueLabel(value), style = LiuMusicTypography.subheadline, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .55f))
        }
        LiuMusicLiquidSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            stepSize = if (steps > 0) (valueRange.endInclusive - valueRange.start) / (steps + 1) else 0f,
            visibilityThreshold = 0.001f,
            modifier = Modifier.fillMaxWidth().semantics { contentDescription = "$title，${valueLabel(value)}" },
        )
    }
}

@Composable
fun LiuMusicGlassStepper(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().heightIn(min = 52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, modifier = Modifier.weight(1f), style = LiuMusicTypography.body)
        Text(value.toString(), modifier = Modifier.width(38.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f))
        LiuMusicGlassIconButton(
            symbol = LiuMusicSymbol.Xmark,
            enabled = value > range.first,
            contentDescription = "减少$title",
            onClick = { onValueChange((value - 1).coerceIn(range)) },
        )
        Spacer(Modifier.width(6.dp))
        LiuMusicGlassIconButton(
            symbol = LiuMusicSymbol.Plus,
            enabled = value < range.last,
            contentDescription = "增加$title",
            onClick = { onValueChange((value + 1).coerceIn(range)) },
        )
    }
}

@Composable
fun LiuMusicBottomSearchToolbar(
    value: String,
    onValueChange: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .liuMusicGlassSurface(LiuMusicShapes.capsule, material = LiuMusicGlassMaterial.Regular)
            .padding(start = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiuMusicSymbolIcon(LiuMusicSymbol.Search, Modifier.size(22.dp), MaterialTheme.colorScheme.onSurface.copy(alpha = .56f))
        LiuMusicGlassTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurface.copy(alpha = .42f)) },
        )
        LiuMusicGlassIconButton(LiuMusicSymbol.Xmark, onClose, contentDescription = "关闭搜索")
    }
}

data class LiuMusicContextAction(
    val title: String,
    val symbol: LiuMusicSymbol,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun LiuMusicGlassContextMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    actions: List<LiuMusicContextAction>,
    anchor: @Composable () -> Unit,
) {
    Box {
        anchor()
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            modifier = Modifier.background(MaterialTheme.colorScheme.surface, LiuMusicShapes.compact),
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.title, color = if (action.destructive) LiuMusicSystemColors.Red else MaterialTheme.colorScheme.onSurface) },
                    leadingIcon = { LiuMusicSymbolIcon(action.symbol, Modifier.size(21.dp), if (action.destructive) LiuMusicSystemColors.Red else MaterialTheme.colorScheme.onSurface) },
                    onClick = { onDismiss(); action.onClick() },
                )
            }
        }
    }
}

@Composable
fun LiuMusicGlassActionSheet(
    visible: Boolean,
    title: String,
    actions: List<LiuMusicContextAction>,
    onDismiss: () -> Unit,
) {
    LiuMusicGlassSheet(visible = visible, onDismiss = onDismiss) {
        Text(title, modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp), style = LiuMusicTypography.title2)
        actions.forEach { action ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { onDismiss(); action.onClick() }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LiuMusicSymbolIcon(action.symbol, Modifier.size(23.dp), if (action.destructive) LiuMusicSystemColors.Red else MaterialTheme.colorScheme.onSurface)
                Text(action.title, modifier = Modifier.padding(start = 12.dp), color = if (action.destructive) LiuMusicSystemColors.Red else MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun LiuMusicGlassAlert(
    visible: Boolean,
    title: String,
    message: String,
    confirmTitle: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    fieldValue: String? = null,
    onFieldValueChange: ((String) -> Unit)? = null,
) {
    LiuMusicGlassDialog(visible = visible, onDismiss = onDismiss) {
        Text(title, style = LiuMusicTypography.title2, fontWeight = FontWeight.Bold)
        Text(message, modifier = Modifier.padding(top = 6.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = .58f))
        if (fieldValue != null && onFieldValueChange != null) {
            LiuMusicGlassTextField(fieldValue, onFieldValueChange, Modifier.fillMaxWidth().padding(top = 14.dp))
        }
        Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            LiuMusicGlassButton(onDismiss, Modifier.weight(1f), contentPadding = PaddingValues(vertical = 11.dp)) { Text("取消") }
            LiuMusicGlassButton(onConfirm, Modifier.weight(1f), style = LiuMusicGlassButtonStyle.BorderedProminent, contentPadding = PaddingValues(vertical = 11.dp)) { Text(confirmTitle) }
        }
    }
}

@Composable
fun LiuMusicGlassModalSurface(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .liuMusicGlassSurface(LiuMusicShapes.sheet, material = LiuMusicGlassMaterial.Regular)
            .padding(18.dp),
        content = content,
    )
}

@Composable
fun LiuMusicGlassColorPicker(
    colors: List<Color>,
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        colors.forEachIndexed { index, color ->
            val selected = color == selectedColor
            Box(
                Modifier
                    .size(if (selected) 38.dp else 34.dp)
                    .background(color, CircleShape)
                    .semantics {
                        role = Role.RadioButton
                        this.selected = selected
                        contentDescription = "颜色 ${index + 1}"
                    }
                    .clickable { onColorSelected(color) },
            )
        }
    }
}
