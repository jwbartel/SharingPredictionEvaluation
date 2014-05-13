package metrics.recipients;

public class RecipientAddressingEvent {
	
	private RecipientAddressingEvent() {
		// Empty;
	}
	
	// Recommendation list generation
	public static final RecipientAddressingEvent SeedTooSmallForListGeneration = new RecipientAddressingEvent();
	public static final RecipientAddressingEvent EmptyListGenerated = new RecipientAddressingEvent();
	public static final RecipientAddressingEvent ListWithNoCorrectEntriesGenerated = new RecipientAddressingEvent();
	public static final RecipientAddressingEvent ListWithCorrectEntriesGenerated = new RecipientAddressingEvent();

	// User actions
	public static final RecipientAddressingEvent Scan = new RecipientAddressingEvent();
	public static final RecipientAddressingEvent TypeSingleRecipient = new RecipientAddressingEvent();
	public static final RecipientAddressingEvent SwitchBetweenClickAndType = new RecipientAddressingEvent();
	public static final RecipientAddressingEvent AddressingCompleted = new RecipientAddressingEvent();
	public static final RecipientAddressingEvent SelectSingleRecipient = new RecipientAddressingEvent();
	
	
	public static final class SelectMultipleRecipientsEvent extends RecipientAddressingEvent {
		public final int numRecipients;
		
		private SelectMultipleRecipientsEvent(int numRecipients) {
			this.numRecipients = numRecipients;
		}
	}

	public static RecipientAddressingEvent SelectMultipleRecipients(int numRecipients) {
		return new SelectMultipleRecipientsEvent(numRecipients);
	}
}
