package metrics.recipients;

public class RecipientAddressingEvent {
	
	private String name;
	private RecipientAddressingEvent(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}
	
	// Recommendation list generation
	public static final RecipientAddressingEvent SeedTooSmallForListGeneration = new RecipientAddressingEvent("SeedTooSmallForListGeneration");
	public static final RecipientAddressingEvent EmptyListGenerated = new RecipientAddressingEvent("EmptyListGenerated");
	public static final RecipientAddressingEvent ListWithNoCorrectEntriesGenerated = new RecipientAddressingEvent("ListWithNoCorrectEntriesGenerated");
	public static final RecipientAddressingEvent ListWithCorrectEntriesGenerated = new RecipientAddressingEvent("ListWithCorrectEntriesGenerated");

	// User actions
	public static final RecipientAddressingEvent Scan = new RecipientAddressingEvent("Scan");
	public static final RecipientAddressingEvent TypeSingleRecipient = new RecipientAddressingEvent("TypeSingleRecipient");
	public static final RecipientAddressingEvent SwitchBetweenClickAndType = new RecipientAddressingEvent("SwitchBetweenClickAndType");
	public static final RecipientAddressingEvent AddressingCompleted = new RecipientAddressingEvent("AddressingCompleted");
	public static final RecipientAddressingEvent SelectSingleRecipient = new RecipientAddressingEvent("SelectSingleRecipient");
	
	
	public static final class SelectMultipleRecipientsEvent extends RecipientAddressingEvent {
		public final int numRecipients;
		
		private SelectMultipleRecipientsEvent(int numRecipients) {
			super("SelectMultipleRecipientsEvent(" + numRecipients + ")");
			this.numRecipients = numRecipients;
		}
	}

	public static RecipientAddressingEvent SelectMultipleRecipients(int numRecipients) {
		return new SelectMultipleRecipientsEvent(numRecipients);
	}
}
