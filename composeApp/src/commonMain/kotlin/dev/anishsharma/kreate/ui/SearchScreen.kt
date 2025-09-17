SingleChoiceSegmentedButtonRow {
    SegmentedButton(
        selected = selectionState == ProviderSelection.BOTH,
        onClick = { onSelect(ProviderSelection.BOTH) },
        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
    ) { Text("Both") }
    SegmentedButton(
        selected = selectionState == ProviderSelection.YOUTUBE_ONLY,
        onClick = { onSelect(ProviderSelection.YOUTUBE_ONLY) },
        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
    ) { Text("YouTube") }
    SegmentedButton(
        selected = selectionState == ProviderSelection.SAAVN_ONLY,
        onClick = { onSelect(ProviderSelection.SAAVN_ONLY) },
        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
    ) { Text("JioSaavn") }
}
