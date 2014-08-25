package metrics.response.time.survey;

public interface QuestionAnswer {
	
	public static abstract class StringValueAnswer implements QuestionAnswer {
		
		public final String value;
		
		public StringValueAnswer(String value) {
			this.value = value;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o.getClass().getName().equals(this.getClass().getName())) {
				return false;
			}
			if (!(o instanceof StringValueAnswer)) {
				return false;
			}
			return ((StringValueAnswer) o).value.equals(value);
		}
	}

	public static class QuestionAnswerPair extends StringValueAnswer {

		public final String question;
		
		public QuestionAnswerPair(String question, String answer) {
			super(answer);
			this.question = question;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof QuestionAnswerPair) {
				QuestionAnswerPair q = (QuestionAnswerPair) o;
				return q.question.equals(question) && q.value.equals(value);
 			} else {
 				return false;
 			}
		}
	}

	public static class SelectedItem extends StringValueAnswer {

		public SelectedItem(String value) {
			super(value);
		}
		
	}
	
	public static class OtherValue extends StringValueAnswer {

		public OtherValue(String value) {
			super(value);
		}
		
	}

	public class SurveyElaborationAnswer extends StringValueAnswer {

		public SurveyElaborationAnswer(String value) {
			super(value);
		}
		
	}
}
