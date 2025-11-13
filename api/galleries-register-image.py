import os
import json
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

def get_user_from_token():
    """Get user from Bearer token"""
    auth_header = request.headers.get('Authorization')
    if not auth_header or not auth_header.startswith('Bearer '):
        return None
    
    token = auth_header.replace('Bearer ', '')
    try:
        user = supabase.auth.get_user(token)
        return user.user if user else None
    except Exception as e:
        print(f"[AUTH] Error getting user: {e}")
        return None

@app.route('/', methods=['POST'])
@app.route('/<path:path>', methods=['POST'])
def handler(path=None):
    """
    Register an image that was uploaded directly to Supabase Storage.
    This endpoint only receives metadata (URLs, dimensions), not the actual file.
    Bypasses Vercel's 4.5MB limit since payload is tiny (< 1KB).
    """
    try:
        print(f"[DEBUG] Register image request received")
        
        if not supabase:
            print("[ERROR] Supabase not configured")
            return jsonify({"error": "Supabase not configured"}), 500

        user = get_user_from_token()
        if not user:
            print("[ERROR] Unauthorized - no valid user")
            return jsonify({"error": "Unauthorized"}), 401
        
        print(f"[DEBUG] User authenticated: {user.id}")
        
        # Extract gallery ID from query parameters (Vercel routing)
        gallery_id = request.args.get('gallery_id')
        if not gallery_id:
            print("[ERROR] Missing gallery_id parameter")
            return jsonify({"error": "Gallery ID required"}), 400
        
        print(f"[DEBUG] Gallery ID: {gallery_id}")
        
        # Verify gallery ownership
        gallery_result = supabase.table('galleries').select('*').eq('id', gallery_id).eq('user_id', user.id).execute()
        
        if not gallery_result.data:
            print(f"[ERROR] Gallery not found: {gallery_id}")
            return jsonify({"error": "Gallery not found"}), 404
        
        gallery = gallery_result.data[0]
        print(f"[DEBUG] Gallery found: {gallery['name']}")
        
        # Get metadata from request (tiny JSON payload)
        data = request.get_json()
        if not data:
            print("[ERROR] Missing JSON data")
            return jsonify({"error": "Missing JSON data"}), 400
        
        url = data.get('url')
        storage_key = data.get('storageKey')
        file_name = data.get('fileName')
        file_size = data.get('size')
        width = data.get('width')
        height = data.get('height')
        
        if not url or not storage_key:
            return jsonify({"error": "Missing required fields (url, storageKey)"}), 400
        
        print(f"📝 [Register Image] Gallery: {gallery_id}, File: {file_name}, Size: {file_size} bytes")
        
        # Use same URL for thumbnail (Supabase can do transforms later)
        thumbnail_url = url
        
        # Get current max order index
        max_order_result = supabase.table('images').select('order_index').eq('gallery_id', gallery_id).order('order_index', desc=True).limit(1).execute()
        current_max_order = max_order_result.data[0]['order_index'] if max_order_result.data else -1
        
        # Prepare metadata
        metadata = {
            "width": width,
            "height": height,
            "size": file_size,
            "format": file_name.rsplit('.', 1)[1].lower() if '.' in file_name else 'unknown',
            "storage_key": storage_key
        }
        
        # Save image record to database
        image_data = {
            "gallery_id": gallery_id,
            "url": url,
            "thumbnail_url": thumbnail_url,
            "metadata": metadata,
            "order_index": current_max_order + 1
        }
        
        image_result = supabase.table('images').insert(image_data).execute()
        
        if not image_result.data:
            print("[ERROR] Failed to save image record")
            return jsonify({"error": "Failed to save image record"}), 500
        
        # Update gallery image count
        new_count = gallery['image_count'] + 1
        supabase.table('galleries').update({
            "image_count": new_count,
            "status": "processing" if new_count > 0 else gallery['status']
        }).eq('id', gallery_id).execute()
        
        print(f"✅ [Register Image] Success! Total images in gallery: {new_count}")
        
        return jsonify({
            "success": True,
            "image": image_result.data[0]
        }), 200
    
    except Exception as e:
        print(f"❌ [Register Image] Error: {e}")
        return jsonify({"error": str(e)}), 500