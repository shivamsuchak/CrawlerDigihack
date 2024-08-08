import json
from flask import Flask, request, jsonify
from transformers import pipeline, AutoTokenizer, AutoModelForSequenceClassification

class NaceCodePrediction:
    """
    This class is designed to predict NACE (Nomenclature of Economic Activities) codes
    using a fine-tuned XLM-RoBERTa model. It utilizes the transformers library to load the model
    and tokenizer, and provides a method to get the top 5 predictions for a given input text.
    """

    def __init__(self):
        """
        Constructor for the NaceCodePrediction class.
        It initializes the model and tokenizer using the 'erst/xlm-roberta-base-finetuned-nace' pre-trained model.
        A pipeline for 'sentiment-analysis' is created which is actually used here for NACE code prediction.
        """
        tokenizer = AutoTokenizer.from_pretrained("erst/xlm-roberta-base-finetuned-nace")
        model = AutoModelForSequenceClassification.from_pretrained("erst/xlm-roberta-base-finetuned-nace")
        self.pipeline = pipeline(
            "sentiment-analysis",
            model=model,
            tokenizer=tokenizer,
            top_k=5
        )

    def predict(self, text):
        """
        Predicts the top 5 NACE codes for the given input text.

        Args:
            text (str): The input text for which NACE code predictions are to be made.

        Returns:
            list: A list of dictionaries containing the top 5 predictions. Each dictionary
                has two keys: 'label' for the predicted NACE code and 'score' for the associated confidence score.
        """
        results = self.pipeline(text)
        predictions = results[0]
        # Sort the predictions based on score and get the top 5
        sorted_predictions = sorted(predictions, key=lambda x: x['score'], reverse=True)[:5]
        return sorted_predictions


# Initialize Flask application
app = Flask(__name__)

# Initialize the NaceCodePrediction class
predictor = NaceCodePrediction()

@app.route('/predict', methods=['POST'])
def get_prediction():
    """
    Flask route to get predictions.
    Expects a JSON payload with a 'text' field.
    """
    data = request.get_json()
    text = data.get('text', '')

    if not text:
        return jsonify({'error': 'No text provided'}), 400

    predictions = predictor.predict(text)
    return jsonify({'predictions': predictions})

if __name__ == '__main__':
    app.run(debug=True)
