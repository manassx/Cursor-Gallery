from flask import Flask, jsonify

app = Flask(__name__)

@app.route('/', defaults={'path': ''})
@app.route('/<path:path>')
def handler(path):
    return jsonify({
        "status": "ok",
        "message": "CursorGallery API is running!",
        "version": "1.0.0"
    }), 200