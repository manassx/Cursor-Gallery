import sys
import os

# Add backend directory to Python path
backend_path = os.path.join(os.path.dirname(__file__), '..', 'backend')
sys.path.insert(0, backend_path)

print(f"[API WRAPPER] Python path configured: {backend_path}")

try:
    from app import app
    print("[API WRAPPER] ✅ Flask app imported successfully")
except Exception as e:
    print(f"[API WRAPPER] ❌ Import failed: {type(e).__name__}: {str(e)}")
    import traceback
    traceback.print_exc()
    
    # Create fallback app
    from flask import Flask, jsonify
    app = Flask(__name__)
    
    @app.route('/', defaults={'path': ''})
    @app.route('/<path:path>')
    def error_handler(path):
        import traceback
        return jsonify({
            "error": "Failed to import backend",
            "type": type(e).__name__,
            "message": str(e),
            "traceback": traceback.format_exc().split('\n')
        }), 500

# Export for Vercel
handler = app