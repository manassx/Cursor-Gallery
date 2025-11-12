import os
import hashlib
from flask import Flask, request, jsonify
from flask_cors import CORS
from supabase import create_client

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

# Initialize Supabase
SUPABASE_URL = os.environ.get("SUPABASE_URL")
SUPABASE_KEY = os.environ.get("SUPABASE_KEY")

if not SUPABASE_URL or not SUPABASE_KEY:
    print("[ERROR] Missing SUPABASE_URL or SUPABASE_KEY")

supabase = create_client(SUPABASE_URL, SUPABASE_KEY) if SUPABASE_URL and SUPABASE_KEY else None

@app.route('/', methods=['POST'])
@app.route('/<path:path>', methods=['POST'])
def handler(path=None):
    """Handle Google Sign-In authentication"""
    try:
        if not supabase:
            return jsonify({"error": "Supabase not configured"}), 500
            
        data = request.get_json()
        if not data:
            return jsonify({"error": "Missing JSON data"}), 400

        id_token = data.get("idToken")
        email = data.get("email")
        name = data.get("name", "User")

        if not id_token or not email:
            return jsonify({"error": "Missing required fields (idToken, email)"}), 400

        # Generate deterministic password
        secret_salt = os.environ.get("GOOGLE_AUTH_SALT", "cursor-gallery-google-auth-2024")
        password_hash = hashlib.sha256(f"{email}{secret_salt}".encode()).hexdigest()
        google_password = password_hash[:32]

        try:
            # Try to sign in existing user
            res = supabase.auth.sign_in_with_password({
                "email": email,
                "password": google_password
            })
            
            user = res.user
            session = res.session

            return jsonify({
                "user": {
                    "id": user.id,
                    "email": user.email,
                    "name": user.user_metadata.get("full_name", name),
                    "createdAt": user.created_at
                },
                "token": session.access_token
            }), 200

        except Exception:
            # User doesn't exist, create new account
            try:
                signup_res = supabase.auth.sign_up({
                    "email": email,
                    "password": google_password,
                    "options": {
                        "data": {
                            "full_name": name,
                            "auth_provider": "google"
                        }
                    }
                })

                user = signup_res.user
                session = signup_res.session

                if user and session:
                    # Create user settings
                    try:
                        supabase.table('user_settings').insert({
                            "user_id": user.id,
                            "profile": {"bio": "", "website": "", "location": ""},
                            "preferences": {
                                "emailNotifications": True,
                                "defaultGalleryVisibility": "private",
                                "compressImages": True,
                                "defaultThreshold": 80
                            }
                        }).execute()
                    except:
                        pass  # Non-critical

                    return jsonify({
                        "user": {
                            "id": user.id,
                            "email": user.email,
                            "name": user.user_metadata.get("full_name", name),
                            "createdAt": user.created_at
                        },
                        "token": session.access_token
                    }), 201
                else:
                    return jsonify({"error": "Signup succeeded but no session returned"}), 500
                    
            except Exception as signup_error:
                return jsonify({"error": f"Signup failed: {str(signup_error)}"}), 500

    except Exception as e:
        return jsonify({"error": f"Server error: {str(e)}"}), 500