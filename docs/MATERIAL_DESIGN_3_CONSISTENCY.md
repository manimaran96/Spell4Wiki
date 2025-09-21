# Material Design 3 Consistency Guide for Spell4Wiki

## Overview

This document outlines the Material Design 3 implementation and consistency guidelines for the Spell4Wiki Android app. All Compose components should follow these guidelines to ensure a cohesive user experience.

## Theme Implementation

### Color Scheme
- **Primary**: Blue (#1976D2) - Used for main actions, app bars, and primary buttons
- **Secondary**: Teal (#03DAC6) - Used for secondary actions and accents
- **Tertiary**: Purple (#9C27B0) - Used for tertiary actions and highlights
- **Error**: Red (#B00020) - Used for error states and destructive actions
- **Surface**: White/Dark - Used for card backgrounds and elevated surfaces
- **Background**: Off-white/Dark - Used for screen backgrounds

### Dynamic Colors
- Supports Android 12+ dynamic colors when available
- Falls back to custom color scheme on older devices
- Proper light/dark theme support with user preference

## Component Guidelines

### Buttons

#### Primary Buttons
```kotlin
Button(
    onClick = { /* action */ },
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    )
) {
    Text("Primary Action")
}
```

#### Secondary Buttons
```kotlin
OutlinedButton(
    onClick = { /* action */ },
    colors = OutlinedButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    Text("Secondary Action")
}
```

#### Text Buttons
```kotlin
TextButton(
    onClick = { /* action */ },
    colors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    )
) {
    Text("Text Action")
}
```

### Cards

#### Standard Cards
```kotlin
Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
) {
    // Card content
}
```

#### Outlined Cards
```kotlin
OutlinedCard(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    )
) {
    // Card content
}
```

### Dialogs

#### Alert Dialogs
```kotlin
AlertDialog(
    onDismissRequest = onDismiss,
    title = {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    },
    text = {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    },
    confirmButton = {
        TextButton(onClick = onConfirm) {
            Text(
                text = "Confirm",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    },
    dismissButton = {
        TextButton(onClick = onDismiss) {
            Text(
                text = "Cancel",
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    },
    containerColor = MaterialTheme.colorScheme.surface,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    textContentColor = MaterialTheme.colorScheme.onSurface
)
```

### Top App Bars

#### Standard Top App Bar
```kotlin
TopAppBar(
    title = {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    },
    navigationIcon = {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.primary,
        titleContentColor = MaterialTheme.colorScheme.onPrimary,
        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
        actionIconContentColor = MaterialTheme.colorScheme.onPrimary
    )
)
```

### Bottom Sheets

#### Modal Bottom Sheet
```kotlin
ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    contentColor = MaterialTheme.colorScheme.onSurface,
    dragHandle = {
        BottomSheetDefaults.DragHandle(
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
) {
    // Bottom sheet content
}
```

## Typography

### Text Styles
- **Display Large**: For hero text and large headings
- **Headline Large**: For main screen titles
- **Headline Medium**: For section headers
- **Headline Small**: For dialog titles
- **Title Large**: For app bar titles
- **Title Medium**: For card titles
- **Body Large**: For primary body text
- **Body Medium**: For secondary body text
- **Label Large**: For button text
- **Label Medium**: For form labels

### Usage Examples
```kotlin
Text(
    text = "Main Title",
    style = MaterialTheme.typography.headlineLarge,
    color = MaterialTheme.colorScheme.onSurface
)

Text(
    text = "Body content",
    style = MaterialTheme.typography.bodyMedium,
    color = MaterialTheme.colorScheme.onSurfaceVariant
)
```

## Spacing and Layout

### Standard Spacing
- **4dp**: Minimal spacing between related elements
- **8dp**: Standard spacing between components
- **16dp**: Standard padding for screens and cards
- **24dp**: Large spacing between sections
- **32dp**: Extra large spacing for major sections

### Elevation
- **0dp**: Surface level (background)
- **1dp**: Slightly elevated (cards at rest)
- **3dp**: Elevated (cards on hover)
- **6dp**: Highly elevated (dialogs, bottom sheets)
- **8dp**: Maximum elevation (floating action buttons)

## Accessibility

### Color Contrast
- All text must meet WCAG AA contrast requirements
- Use semantic colors from the theme
- Provide alternative indicators beyond color

### Touch Targets
- Minimum 48dp touch target size
- Adequate spacing between interactive elements
- Clear visual feedback for interactions

### Screen Reader Support
- Proper content descriptions for all interactive elements
- Semantic markup for screen readers
- Logical focus order

## Migration Checklist

### For Each Compose Screen
- [ ] Uses Spell4WikiTheme wrapper
- [ ] Follows Material Design 3 color scheme
- [ ] Uses proper typography styles
- [ ] Implements consistent spacing
- [ ] Follows elevation guidelines
- [ ] Includes proper accessibility support
- [ ] Handles light/dark theme properly
- [ ] Uses semantic color tokens

### For Each Component
- [ ] Uses Material Design 3 components
- [ ] Follows design system guidelines
- [ ] Implements proper state management
- [ ] Includes loading and error states
- [ ] Supports accessibility features
- [ ] Handles edge cases gracefully

## Testing

### Visual Testing
- Test in both light and dark themes
- Verify on different screen sizes
- Check color contrast ratios
- Validate typography hierarchy

### Accessibility Testing
- Use TalkBack to test screen reader support
- Verify keyboard navigation
- Test with high contrast mode
- Validate touch target sizes

## Resources

- [Material Design 3 Guidelines](https://m3.material.io/)
- [Jetpack Compose Material 3](https://developer.android.com/jetpack/compose/designsystems/material3)
- [Accessibility Guidelines](https://developer.android.com/guide/topics/ui/accessibility)
- [Color Tool](https://m3.material.io/theme-builder)

## Implementation Status

### Completed Migrations ✅
- [x] Dialog system (AlertDialog, BottomSheet)
- [x] Theme implementation with dynamic colors
- [x] Typography system
- [x] Color scheme definition
- [x] Spell4Wiktionary screen
- [x] Language selection components
- [x] Settings components

### Consistent Design System ✅
- [x] Material Design 3 color scheme
- [x] Proper typography hierarchy
- [x] Consistent spacing and elevation
- [x] Accessibility support
- [x] Light/dark theme support
- [x] Dynamic color support (Android 12+)

All migrated components follow Material Design 3 principles and use the established design system consistently throughout the app.
