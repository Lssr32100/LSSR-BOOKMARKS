from flask import Flask, render_template, request, jsonify, redirect, url_for
from flask_cors import CORS
import json
import os
from datetime import datetime

app = Flask(__name__)
CORS(app)

class BookmarkType:
    FREE = "FREE"
    PAID = "PAID"
    FREEMIUM = "FREEMIUM"

class Category:
    def __init__(self, id, name):
        self.id = id
        self.name = name

    def to_dict(self):
        return {
            "id": self.id,
            "name": self.name
        }

class Subcategory:
    def __init__(self, id, name, category_id):
        self.id = id
        self.name = name
        self.category_id = category_id

    def to_dict(self):
        return {
            "id": self.id,
            "name": self.name,
            "category_id": self.category_id
        }

class Bookmark:
    def __init__(self, id, name, url, description, category_id, subcategory_id, bookmark_type):
        self.id = id
        self.name = name
        self.url = url
        self.description = description
        self.category_id = category_id
        self.subcategory_id = subcategory_id
        self.type = bookmark_type
        self.created_at = datetime.now().timestamp()
        self.updated_at = self.created_at

    def to_dict(self):
        return {
            "id": self.id,
            "name": self.name,
            "url": self.url,
            "description": self.description,
            "category_id": self.category_id,
            "subcategory_id": self.subcategory_id,
            "type": self.type,
            "created_at": self.created_at,
            "updated_at": self.updated_at
        }

class BookmarkManager:
    def __init__(self):
        self.categories = []
        self.subcategories = []
        self.bookmarks = []
        self.next_category_id = 1
        self.next_subcategory_id = 1
        self.next_bookmark_id = 1
        self.load_data()

    def load_data(self):
        # Create default data if not exists
        if not os.path.exists("bookmark_data.json"):
            self.create_default_data()
        else:
            try:
                with open("bookmark_data.json", "r") as f:
                    data = json.load(f)
                    
                    self.categories = [Category(c["id"], c["name"]) for c in data.get("categories", [])]
                    self.subcategories = [Subcategory(s["id"], s["name"], s["category_id"]) 
                                         for s in data.get("subcategories", [])]
                    self.bookmarks = [Bookmark(
                        b["id"], b["name"], b.get("url"), b.get("description"),
                        b["category_id"], b["subcategory_id"], b["type"]
                    ) for b in data.get("bookmarks", [])]
                    
                    # Set next IDs
                    if self.categories:
                        self.next_category_id = max(c.id for c in self.categories) + 1
                    if self.subcategories:
                        self.next_subcategory_id = max(s.id for s in self.subcategories) + 1
                    if self.bookmarks:
                        self.next_bookmark_id = max(b.id for b in self.bookmarks) + 1
            except Exception as e:
                print(f"Error loading data: {e}")
                self.create_default_data()

    def save_data(self):
        data = {
            "categories": [{"id": c.id, "name": c.name} for c in self.categories],
            "subcategories": [{"id": s.id, "name": s.name, "category_id": s.category_id} 
                             for s in self.subcategories],
            "bookmarks": [{"id": b.id, "name": b.name, "url": b.url, 
                          "description": b.description, "category_id": b.category_id, 
                          "subcategory_id": b.subcategory_id, "type": b.type,
                          "created_at": b.created_at, "updated_at": b.updated_at} 
                         for b in self.bookmarks]
        }
        
        with open("bookmark_data.json", "w") as f:
            json.dump(data, f, indent=2)

    def create_default_data(self):
        # Create default categories
        websites = self.add_category("Websites")
        apps = self.add_category("Apps")
        tools = self.add_category("Tools")
        
        # Create default subcategories
        social_media = self.add_subcategory("Social Media", websites.id)
        news = self.add_subcategory("News", websites.id)
        
        productivity = self.add_subcategory("Productivity", apps.id)
        entertainment = self.add_subcategory("Entertainment", apps.id)
        
        dev_tools = self.add_subcategory("Development", tools.id)
        design_tools = self.add_subcategory("Design", tools.id)
        
        # Create default bookmarks
        self.add_bookmark("Twitter", "https://twitter.com", "Social media platform", 
                         websites.id, social_media.id, BookmarkType.FREE)
        self.add_bookmark("CNN", "https://cnn.com", "News website", 
                         websites.id, news.id, BookmarkType.FREE)
        
        self.add_bookmark("Microsoft Office", "https://office.com", "Office suite", 
                         apps.id, productivity.id, BookmarkType.PAID)
        self.add_bookmark("Spotify", "https://spotify.com", "Music streaming", 
                         apps.id, entertainment.id, BookmarkType.FREEMIUM)
        
        self.add_bookmark("Visual Studio Code", "https://code.visualstudio.com", 
                         "Code editor", tools.id, dev_tools.id, BookmarkType.FREE)
        self.add_bookmark("Adobe Photoshop", "https://adobe.com/photoshop", 
                         "Image editing software", tools.id, design_tools.id, BookmarkType.PAID)
        
        self.save_data()

    def add_category(self, name):
        category = Category(self.next_category_id, name)
        self.categories.append(category)
        self.next_category_id += 1
        self.save_data()
        return category
        
    def update_category(self, category_id, name):
        for category in self.categories:
            if category.id == category_id:
                category.name = name
                self.save_data()
                return True
        return False
        
    def delete_category(self, category_id):
        # Delete associated subcategories and bookmarks
        self.subcategories = [s for s in self.subcategories if s.category_id != category_id]
        self.bookmarks = [b for b in self.bookmarks if b.category_id != category_id]
        
        # Delete the category
        self.categories = [c for c in self.categories if c.id != category_id]
        self.save_data()
        
    def add_subcategory(self, name, category_id):
        subcategory = Subcategory(self.next_subcategory_id, name, category_id)
        self.subcategories.append(subcategory)
        self.next_subcategory_id += 1
        self.save_data()
        return subcategory
        
    def update_subcategory(self, subcategory_id, name):
        for subcategory in self.subcategories:
            if subcategory.id == subcategory_id:
                subcategory.name = name
                self.save_data()
                return True
        return False
        
    def delete_subcategory(self, subcategory_id):
        # Delete associated bookmarks
        self.bookmarks = [b for b in self.bookmarks if b.subcategory_id != subcategory_id]
        
        # Delete the subcategory
        self.subcategories = [s for s in self.subcategories if s.id != subcategory_id]
        self.save_data()
        
    def add_bookmark(self, name, url, description, category_id, subcategory_id, bookmark_type):
        bookmark = Bookmark(self.next_bookmark_id, name, url, description, 
                           category_id, subcategory_id, bookmark_type)
        self.bookmarks.append(bookmark)
        self.next_bookmark_id += 1
        self.save_data()
        return bookmark
        
    def update_bookmark(self, bookmark_id, name, url, description, category_id, subcategory_id, bookmark_type):
        for bookmark in self.bookmarks:
            if bookmark.id == bookmark_id:
                bookmark.name = name
                bookmark.url = url
                bookmark.description = description
                bookmark.category_id = category_id
                bookmark.subcategory_id = subcategory_id
                bookmark.type = bookmark_type
                bookmark.updated_at = datetime.now().timestamp()
                self.save_data()
                return True
        return False
        
    def delete_bookmark(self, bookmark_id):
        self.bookmarks = [b for b in self.bookmarks if b.id != bookmark_id]
        self.save_data()
        
    def get_category_name(self, category_id):
        for category in self.categories:
            if category.id == category_id:
                return category.name
        return ""
        
    def get_subcategory_name(self, subcategory_id):
        for subcategory in self.subcategories:
            if subcategory.id == subcategory_id:
                return subcategory.name
        return ""
        
    def get_subcategories_for_category(self, category_id):
        return [s for s in self.subcategories if s.category_id == category_id]
        
    def search_bookmarks(self, query):
        if not query:
            return self.bookmarks
            
        query = query.lower()
        results = []
        
        for bookmark in self.bookmarks:
            if (query in bookmark.name.lower() or 
                (bookmark.description and query in bookmark.description.lower()) or
                query in self.get_category_name(bookmark.category_id).lower() or
                query in self.get_subcategory_name(bookmark.subcategory_id).lower()):
                results.append(bookmark)
                
        return results
        
    def filter_bookmarks_by_type(self, bookmark_type):
        if not bookmark_type or bookmark_type == "ALL":
            return self.bookmarks
        return [b for b in self.bookmarks if b.type == bookmark_type]
        
    def filter_bookmarks_by_category(self, category_id):
        if not category_id:
            return self.bookmarks
        return [b for b in self.bookmarks if b.category_id == category_id]
        
    def filter_bookmarks_by_subcategory(self, subcategory_id):
        if not subcategory_id:
            return self.bookmarks
        return [b for b in self.bookmarks if b.subcategory_id == subcategory_id]
        
    def get_bookmark_with_details(self, bookmark_id):
        bookmark = next((b for b in self.bookmarks if b.id == bookmark_id), None)
        if not bookmark:
            return None
            
        category_name = self.get_category_name(bookmark.category_id)
        subcategory_name = self.get_subcategory_name(bookmark.subcategory_id)
        
        bookmark_dict = bookmark.to_dict()
        bookmark_dict["category_name"] = category_name
        bookmark_dict["subcategory_name"] = subcategory_name
        
        return bookmark_dict
        
    def get_all_bookmarks_with_details(self):
        result = []
        for bookmark in self.bookmarks:
            category_name = self.get_category_name(bookmark.category_id)
            subcategory_name = self.get_subcategory_name(bookmark.subcategory_id)
            
            bookmark_dict = bookmark.to_dict()
            bookmark_dict["category_name"] = category_name
            bookmark_dict["subcategory_name"] = subcategory_name
            
            result.append(bookmark_dict)
            
        return result

# Create a global instance of the BookmarkManager
bookmark_manager = BookmarkManager()

@app.route('/')
def home():
    return render_template('index.html')

@app.route('/api/bookmarks', methods=['GET'])
def get_bookmarks():
    sort_by = request.args.get('sort', 'name_asc')
    category_id = request.args.get('category')
    type_filter = request.args.get('type')
    search_query = request.args.get('search')
    
    bookmarks = bookmark_manager.bookmarks.copy()
    
    # Apply search filter
    if search_query:
        bookmarks = bookmark_manager.search_bookmarks(search_query)
    
    # Apply category filter
    if category_id and category_id != "ALL":
        try:
            category_id = int(category_id)
            bookmarks = [b for b in bookmarks if b.category_id == category_id]
        except ValueError:
            pass
    
    # Apply type filter
    if type_filter and type_filter != "ALL":
        bookmarks = [b for b in bookmarks if b.type == type_filter]
    
    # Apply sorting
    if sort_by == 'name_asc':
        bookmarks.sort(key=lambda b: b.name.lower())
    elif sort_by == 'name_desc':
        bookmarks.sort(key=lambda b: b.name.lower(), reverse=True)
    elif sort_by == 'category':
        bookmarks.sort(key=lambda b: (bookmark_manager.get_category_name(b.category_id).lower(), b.name.lower()))
    elif sort_by == 'type':
        bookmarks.sort(key=lambda b: (b.type, b.name.lower()))
    
    # Convert to dictionaries with category and subcategory names
    result = []
    for bookmark in bookmarks:
        bookmark_dict = bookmark.to_dict()
        bookmark_dict['category_name'] = bookmark_manager.get_category_name(bookmark.category_id)
        bookmark_dict['subcategory_name'] = bookmark_manager.get_subcategory_name(bookmark.subcategory_id)
        result.append(bookmark_dict)
    
    return jsonify(result)

@app.route('/api/bookmarks/<int:bookmark_id>', methods=['GET'])
def get_bookmark(bookmark_id):
    bookmark = bookmark_manager.get_bookmark_with_details(bookmark_id)
    if bookmark:
        return jsonify(bookmark)
    return jsonify({"error": "Bookmark not found"}), 404

@app.route('/api/bookmarks', methods=['POST'])
def add_bookmark():
    data = request.json
    
    name = data.get('name', '').strip()
    url = data.get('url', '').strip()
    description = data.get('description', '').strip()
    
    try:
        category_id = int(data.get('category_id'))
        subcategory_id = int(data.get('subcategory_id'))
    except (ValueError, TypeError):
        return jsonify({"error": "Invalid category or subcategory ID"}), 400
    
    bookmark_type = data.get('type', BookmarkType.FREE)
    
    if not name:
        return jsonify({"error": "Name is required"}), 400
    
    # Validate category and subcategory
    category = next((c for c in bookmark_manager.categories if c.id == category_id), None)
    if not category:
        return jsonify({"error": "Category not found"}), 400
    
    subcategory = next((s for s in bookmark_manager.subcategories if s.id == subcategory_id and s.category_id == category_id), None)
    if not subcategory:
        return jsonify({"error": "Subcategory not found or doesn't belong to the selected category"}), 400
    
    bookmark = bookmark_manager.add_bookmark(
        name, 
        url if url else None, 
        description if description else None, 
        category_id, 
        subcategory_id, 
        bookmark_type
    )
    
    result = bookmark.to_dict()
    result['category_name'] = bookmark_manager.get_category_name(bookmark.category_id)
    result['subcategory_name'] = bookmark_manager.get_subcategory_name(bookmark.subcategory_id)
    
    return jsonify(result), 201

@app.route('/api/bookmarks/<int:bookmark_id>', methods=['PUT'])
def update_bookmark(bookmark_id):
    data = request.json
    
    name = data.get('name', '').strip()
    url = data.get('url', '').strip()
    description = data.get('description', '').strip()
    
    try:
        category_id = int(data.get('category_id'))
        subcategory_id = int(data.get('subcategory_id'))
    except (ValueError, TypeError):
        return jsonify({"error": "Invalid category or subcategory ID"}), 400
    
    bookmark_type = data.get('type', BookmarkType.FREE)
    
    if not name:
        return jsonify({"error": "Name is required"}), 400
    
    # Validate category and subcategory
    category = next((c for c in bookmark_manager.categories if c.id == category_id), None)
    if not category:
        return jsonify({"error": "Category not found"}), 400
    
    subcategory = next((s for s in bookmark_manager.subcategories if s.id == subcategory_id and s.category_id == category_id), None)
    if not subcategory:
        return jsonify({"error": "Subcategory not found or doesn't belong to the selected category"}), 400
    
    success = bookmark_manager.update_bookmark(
        bookmark_id,
        name, 
        url if url else None, 
        description if description else None, 
        category_id, 
        subcategory_id, 
        bookmark_type
    )
    
    if success:
        bookmark = bookmark_manager.get_bookmark_with_details(bookmark_id)
        return jsonify(bookmark)
    
    return jsonify({"error": "Bookmark not found"}), 404

@app.route('/api/bookmarks/<int:bookmark_id>', methods=['DELETE'])
def delete_bookmark(bookmark_id):
    bookmark = next((b for b in bookmark_manager.bookmarks if b.id == bookmark_id), None)
    if not bookmark:
        return jsonify({"error": "Bookmark not found"}), 404
    
    bookmark_manager.delete_bookmark(bookmark_id)
    return jsonify({"success": True})

@app.route('/api/categories', methods=['GET'])
def get_categories():
    categories = [c.to_dict() for c in bookmark_manager.categories]
    return jsonify(categories)

@app.route('/api/categories', methods=['POST'])
def add_category():
    data = request.json
    name = data.get('name', '').strip()
    
    if not name:
        return jsonify({"error": "Name is required"}), 400
    
    category = bookmark_manager.add_category(name)
    return jsonify(category.to_dict()), 201

@app.route('/api/categories/<int:category_id>', methods=['PUT'])
def update_category(category_id):
    data = request.json
    name = data.get('name', '').strip()
    
    if not name:
        return jsonify({"error": "Name is required"}), 400
    
    success = bookmark_manager.update_category(category_id, name)
    if success:
        category = next((c for c in bookmark_manager.categories if c.id == category_id), None)
        return jsonify(category.to_dict())
    
    return jsonify({"error": "Category not found"}), 404

@app.route('/api/categories/<int:category_id>', methods=['DELETE'])
def delete_category(category_id):
    category = next((c for c in bookmark_manager.categories if c.id == category_id), None)
    if not category:
        return jsonify({"error": "Category not found"}), 404
    
    bookmark_manager.delete_category(category_id)
    return jsonify({"success": True})

@app.route('/api/subcategories', methods=['GET'])
def get_subcategories():
    category_id = request.args.get('category_id')
    
    subcategories = bookmark_manager.subcategories
    
    if category_id:
        try:
            category_id = int(category_id)
            subcategories = [s for s in subcategories if s.category_id == category_id]
        except ValueError:
            pass
    
    result = []
    for subcategory in subcategories:
        subcategory_dict = subcategory.to_dict()
        subcategory_dict['category_name'] = bookmark_manager.get_category_name(subcategory.category_id)
        result.append(subcategory_dict)
    
    return jsonify(result)

@app.route('/api/subcategories', methods=['POST'])
def add_subcategory():
    data = request.json
    name = data.get('name', '').strip()
    
    try:
        category_id = int(data.get('category_id'))
    except (ValueError, TypeError):
        return jsonify({"error": "Invalid category ID"}), 400
    
    if not name:
        return jsonify({"error": "Name is required"}), 400
    
    # Validate category
    category = next((c for c in bookmark_manager.categories if c.id == category_id), None)
    if not category:
        return jsonify({"error": "Category not found"}), 400
    
    subcategory = bookmark_manager.add_subcategory(name, category_id)
    
    result = subcategory.to_dict()
    result['category_name'] = bookmark_manager.get_category_name(subcategory.category_id)
    
    return jsonify(result), 201

@app.route('/api/subcategories/<int:subcategory_id>', methods=['PUT'])
def update_subcategory(subcategory_id):
    data = request.json
    name = data.get('name', '').strip()
    
    if not name:
        return jsonify({"error": "Name is required"}), 400
    
    success = bookmark_manager.update_subcategory(subcategory_id, name)
    if success:
        subcategory = next((s for s in bookmark_manager.subcategories if s.id == subcategory_id), None)
        result = subcategory.to_dict()
        result['category_name'] = bookmark_manager.get_category_name(subcategory.category_id)
        return jsonify(result)
    
    return jsonify({"error": "Subcategory not found"}), 404

@app.route('/api/subcategories/<int:subcategory_id>', methods=['DELETE'])
def delete_subcategory(subcategory_id):
    subcategory = next((s for s in bookmark_manager.subcategories if s.id == subcategory_id), None)
    if not subcategory:
        return jsonify({"error": "Subcategory not found"}), 404
    
    bookmark_manager.delete_subcategory(subcategory_id)
    return jsonify({"success": True})

@app.route('/api/export', methods=['GET'])
def export_data():
    data = {
        "categories": [c.to_dict() for c in bookmark_manager.categories],
        "subcategories": [s.to_dict() for s in bookmark_manager.subcategories],
        "bookmarks": [b.to_dict() for b in bookmark_manager.bookmarks]
    }
    return jsonify(data)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)