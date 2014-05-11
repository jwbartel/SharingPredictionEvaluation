package metrics.recipients;

public enum RecipientAddressingEvents {

	// Recommendation list generation
	SeedTooSmallForListGeneration,
	EmptyListGenerated,
	ListWithNoCorrectEntriesGenerated,
	ListWithCorrectEntriesGenerated,

	// User actions
	Scan,
	SelectSingleRecipient,
	SelectMultipleRecipients,
	TypeSingleRecipient,
	SwitchBetweenClickAndType,
	AddressingCompleted,
}
