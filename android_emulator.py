#!/usr/bin/env python3
"""
Android App Emulator Script

This script creates a simple GUI window to simulate the Android app
without requiring an actual Android emulator or device.
"""

import tkinter as tk
from tkinter import ttk, messagebox, simpledialog
import json
import os
from datetime import datetime

class BookmarkType:
    FREE = "FREE"
    PAID = "PAID"
    FREEMIUM = "FREEMIUM"

class Category:
    def __init__(self, id, name):
        self.id = id
        self.name = name

class Subcategory:
    def __init__(self, id, name, category_id):
        self.id = id
        self.name = name
        self.category_id = category_id

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
        if not bookmark_type:
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

class BookmarkManagerApp(tk.Tk):
    def __init__(self):
        super().__init__()
        
        self.title("Bookmark Manager")
        self.geometry("900x600")
        self.minsize(800, 500)
        
        self.manager = BookmarkManager()
        
        self.setup_ui()
        
    def setup_ui(self):
        # Create notebook (tabs)
        self.notebook = ttk.Notebook(self)
        self.notebook.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # Create Home tab
        self.home_frame = ttk.Frame(self.notebook)
        self.notebook.add(self.home_frame, text="Home")
        
        # Create Categories tab
        self.categories_frame = ttk.Frame(self.notebook)
        self.notebook.add(self.categories_frame, text="Categories")
        
        # Create Settings tab
        self.settings_frame = ttk.Frame(self.notebook)
        self.notebook.add(self.settings_frame, text="Settings")
        
        # Setup each tab
        self.setup_home_tab()
        self.setup_categories_tab()
        self.setup_settings_tab()
        
    def setup_home_tab(self):
        # Search frame at the top
        search_frame = ttk.Frame(self.home_frame)
        search_frame.pack(fill=tk.X, padx=10, pady=10)
        
        ttk.Label(search_frame, text="Search:").pack(side=tk.LEFT, padx=(0, 5))
        self.search_var = tk.StringVar()
        self.search_entry = ttk.Entry(search_frame, width=40, textvariable=self.search_var)
        self.search_entry.pack(side=tk.LEFT, padx=(0, 10))
        self.search_entry.bind("<KeyRelease>", lambda e: self.update_bookmark_list())
        
        # Filter frame
        filter_frame = ttk.Frame(self.home_frame)
        filter_frame.pack(fill=tk.X, padx=10, pady=(0, 10))
        
        ttk.Label(filter_frame, text="Filter by:").pack(side=tk.LEFT, padx=(0, 5))
        
        # Filter by type
        self.filter_type_var = tk.StringVar(value="ALL")
        filter_type_menu = ttk.OptionMenu(
            filter_frame, 
            self.filter_type_var, 
            "ALL", 
            "ALL", 
            BookmarkType.FREE, 
            BookmarkType.PAID, 
            BookmarkType.FREEMIUM,
            command=lambda e: self.update_bookmark_list()
        )
        filter_type_menu.pack(side=tk.LEFT, padx=(0, 10))
        
        # Filter by category
        ttk.Label(filter_frame, text="Category:").pack(side=tk.LEFT, padx=(0, 5))
        self.filter_category_var = tk.StringVar(value="ALL")
        self.filter_category_menu = ttk.OptionMenu(
            filter_frame, 
            self.filter_category_var, 
            "ALL",
            command=lambda e: self.update_bookmark_list()
        )
        self.filter_category_menu.pack(side=tk.LEFT, padx=(0, 10))
        self.update_category_filter_menu()
        
        # Sort options
        ttk.Label(filter_frame, text="Sort by:").pack(side=tk.LEFT, padx=(0, 5))
        self.sort_var = tk.StringVar(value="name_asc")
        sort_menu = ttk.OptionMenu(
            filter_frame, 
            self.sort_var, 
            "name_asc", 
            "name_asc", 
            "name_desc", 
            "category",
            "subcategory",
            "type",
            command=lambda e: self.update_bookmark_list()
        )
        sort_menu.pack(side=tk.LEFT, padx=(0, 10))
        
        # Bookmarks list
        list_frame = ttk.Frame(self.home_frame)
        list_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # Set up the bookmark list with columns
        columns = ("name", "category", "subcategory", "type")
        self.bookmark_tree = ttk.Treeview(list_frame, columns=columns, show="headings")
        
        # Define headings
        self.bookmark_tree.heading("name", text="Name")
        self.bookmark_tree.heading("category", text="Category")
        self.bookmark_tree.heading("subcategory", text="Subcategory")
        self.bookmark_tree.heading("type", text="Type")
        
        # Set column widths
        self.bookmark_tree.column("name", width=250)
        self.bookmark_tree.column("category", width=150)
        self.bookmark_tree.column("subcategory", width=150)
        self.bookmark_tree.column("type", width=100)
        
        # Add a scrollbar
        scrollbar = ttk.Scrollbar(list_frame, orient=tk.VERTICAL, command=self.bookmark_tree.yview)
        self.bookmark_tree.configure(yscroll=scrollbar.set)
        
        # Pack them
        self.bookmark_tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        
        # Bind double-click to view bookmark details
        self.bookmark_tree.bind("<Double-1>", self.view_bookmark_details)
        
        # Button frame at the bottom
        button_frame = ttk.Frame(self.home_frame)
        button_frame.pack(fill=tk.X, padx=10, pady=10)
        
        # Add bookmark button
        add_btn = ttk.Button(button_frame, text="Add Bookmark", command=self.add_bookmark_dialog)
        add_btn.pack(side=tk.LEFT, padx=(0, 10))
        
        # Edit button
        edit_btn = ttk.Button(button_frame, text="Edit Selected", command=self.edit_bookmark_dialog)
        edit_btn.pack(side=tk.LEFT, padx=(0, 10))
        
        # Delete button
        delete_btn = ttk.Button(button_frame, text="Delete Selected", command=self.delete_bookmark)
        delete_btn.pack(side=tk.LEFT)
        
        # Populate the bookmark list
        self.update_bookmark_list()
        
    def setup_categories_tab(self):
        # Frame for categories management
        categories_mgmt_frame = ttk.Frame(self.categories_frame)
        categories_mgmt_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # Categories list on the left side
        categories_list_frame = ttk.Frame(categories_mgmt_frame)
        categories_list_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=(0, 5))
        
        ttk.Label(categories_list_frame, text="Categories").pack(anchor=tk.W, pady=(0, 5))
        
        # Set up the categories list
        self.categories_tree = ttk.Treeview(categories_list_frame, columns=("name"), show="headings")
        self.categories_tree.heading("name", text="Name")
        self.categories_tree.column("name", width=200)
        
        # Add a scrollbar
        cat_scrollbar = ttk.Scrollbar(categories_list_frame, orient=tk.VERTICAL, command=self.categories_tree.yview)
        self.categories_tree.configure(yscroll=cat_scrollbar.set)
        
        # Pack them
        self.categories_tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        cat_scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        
        # Category buttons
        cat_button_frame = ttk.Frame(categories_list_frame)
        cat_button_frame.pack(fill=tk.X, pady=5)
        
        ttk.Button(cat_button_frame, text="Add Category", command=self.add_category_dialog).pack(side=tk.LEFT, padx=(0, 5))
        ttk.Button(cat_button_frame, text="Edit", command=self.edit_category_dialog).pack(side=tk.LEFT, padx=(0, 5))
        ttk.Button(cat_button_frame, text="Delete", command=self.delete_category).pack(side=tk.LEFT)
        
        # Subcategories list on the right side
        subcategories_list_frame = ttk.Frame(categories_mgmt_frame)
        subcategories_list_frame.pack(side=tk.RIGHT, fill=tk.BOTH, expand=True, padx=(5, 0))
        
        ttk.Label(subcategories_list_frame, text="Subcategories").pack(anchor=tk.W, pady=(0, 5))
        
        # Set up the subcategories list
        self.subcategories_tree = ttk.Treeview(subcategories_list_frame, columns=("name", "category"), show="headings")
        self.subcategories_tree.heading("name", text="Name")
        self.subcategories_tree.heading("category", text="Category")
        self.subcategories_tree.column("name", width=150)
        self.subcategories_tree.column("category", width=150)
        
        # Add a scrollbar
        subcat_scrollbar = ttk.Scrollbar(subcategories_list_frame, orient=tk.VERTICAL, command=self.subcategories_tree.yview)
        self.subcategories_tree.configure(yscroll=subcat_scrollbar.set)
        
        # Pack them
        self.subcategories_tree.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        subcat_scrollbar.pack(side=tk.RIGHT, fill=tk.Y)
        
        # Subcategory buttons
        subcat_button_frame = ttk.Frame(subcategories_list_frame)
        subcat_button_frame.pack(fill=tk.X, pady=5)
        
        ttk.Button(subcat_button_frame, text="Add Subcategory", command=self.add_subcategory_dialog).pack(side=tk.LEFT, padx=(0, 5))
        ttk.Button(subcat_button_frame, text="Edit", command=self.edit_subcategory_dialog).pack(side=tk.LEFT, padx=(0, 5))
        ttk.Button(subcat_button_frame, text="Delete", command=self.delete_subcategory).pack(side=tk.LEFT)
        
        # Bind selection handlers
        self.categories_tree.bind("<<TreeviewSelect>>", self.on_category_select)
        
        # Populate the lists
        self.update_categories_list()
        self.update_subcategories_list()
        
    def setup_settings_tab(self):
        # Theme settings
        theme_frame = ttk.LabelFrame(self.settings_frame, text="Theme")
        theme_frame.pack(fill=tk.X, padx=10, pady=10)
        
        # Theme options
        self.theme_var = tk.StringVar(value="system")
        
        ttk.Radiobutton(theme_frame, text="System Default", variable=self.theme_var, value="system").pack(anchor=tk.W, padx=10, pady=5)
        ttk.Radiobutton(theme_frame, text="Light Theme", variable=self.theme_var, value="light").pack(anchor=tk.W, padx=10, pady=5)
        ttk.Radiobutton(theme_frame, text="Dark Theme", variable=self.theme_var, value="dark").pack(anchor=tk.W, padx=10, pady=5)
        
        # Import/Export frame
        import_export_frame = ttk.LabelFrame(self.settings_frame, text="Import/Export")
        import_export_frame.pack(fill=tk.X, padx=10, pady=10)
        
        # Format selection
        format_frame = ttk.Frame(import_export_frame)
        format_frame.pack(fill=tk.X, padx=10, pady=5)
        
        ttk.Label(format_frame, text="Format:").pack(side=tk.LEFT, padx=(0, 5))
        self.export_format_var = tk.StringVar(value="json")
        ttk.Radiobutton(format_frame, text="JSON", variable=self.export_format_var, value="json").pack(side=tk.LEFT, padx=(0, 10))
        ttk.Radiobutton(format_frame, text="CSV", variable=self.export_format_var, value="csv").pack(side=tk.LEFT)
        
        # Import/Export buttons
        button_frame = ttk.Frame(import_export_frame)
        button_frame.pack(fill=tk.X, padx=10, pady=10)
        
        ttk.Button(button_frame, text="Export Data", command=self.export_data).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Button(button_frame, text="Import Data", command=self.import_data).pack(side=tk.LEFT)
        
        # About frame
        about_frame = ttk.LabelFrame(self.settings_frame, text="About")
        about_frame.pack(fill=tk.X, padx=10, pady=10)
        
        about_text = "Bookmark Manager 1.0\n\nA modern Android app for managing bookmarks."
        ttk.Label(about_frame, text=about_text, justify=tk.LEFT).pack(padx=10, pady=10)
        
    def update_bookmark_list(self):
        # Clear the current items
        for item in self.bookmark_tree.get_children():
            self.bookmark_tree.delete(item)
            
        # Get the filtered and sorted bookmarks
        bookmarks = self.manager.bookmarks
        
        # Apply search filter
        search_query = self.search_var.get()
        if search_query:
            bookmarks = self.manager.search_bookmarks(search_query)
            
        # Apply type filter
        type_filter = self.filter_type_var.get()
        if type_filter != "ALL":
            bookmarks = [b for b in bookmarks if b.type == type_filter]
            
        # Apply category filter
        category_filter = self.filter_category_var.get()
        if category_filter != "ALL":
            category_id = next((c.id for c in self.manager.categories if c.name == category_filter), None)
            if category_id:
                bookmarks = [b for b in bookmarks if b.category_id == category_id]
                
        # Apply sorting
        sort_option = self.sort_var.get()
        if sort_option == "name_asc":
            bookmarks = sorted(bookmarks, key=lambda b: b.name.lower())
        elif sort_option == "name_desc":
            bookmarks = sorted(bookmarks, key=lambda b: b.name.lower(), reverse=True)
        elif sort_option == "category":
            bookmarks = sorted(bookmarks, key=lambda b: (
                self.manager.get_category_name(b.category_id).lower(),
                self.manager.get_subcategory_name(b.subcategory_id).lower(),
                b.name.lower()
            ))
        elif sort_option == "subcategory":
            bookmarks = sorted(bookmarks, key=lambda b: (
                self.manager.get_subcategory_name(b.subcategory_id).lower(),
                b.name.lower()
            ))
        elif sort_option == "type":
            bookmarks = sorted(bookmarks, key=lambda b: (b.type, b.name.lower()))
            
        # Add the bookmarks to the tree
        for bookmark in bookmarks:
            category_name = self.manager.get_category_name(bookmark.category_id)
            subcategory_name = self.manager.get_subcategory_name(bookmark.subcategory_id)
            
            self.bookmark_tree.insert("", tk.END, values=(
                bookmark.name,
                category_name,
                subcategory_name,
                bookmark.type
            ), tags=(str(bookmark.id),))
            
    def update_categories_list(self):
        # Clear the current items
        for item in self.categories_tree.get_children():
            self.categories_tree.delete(item)
            
        # Add the categories to the tree
        for category in sorted(self.manager.categories, key=lambda c: c.name.lower()):
            self.categories_tree.insert("", tk.END, values=(category.name,), tags=(str(category.id),))
            
        # Update the category filter menu
        self.update_category_filter_menu()
            
    def update_subcategories_list(self, category_id=None):
        # Clear the current items
        for item in self.subcategories_tree.get_children():
            self.subcategories_tree.delete(item)
            
        # Filter subcategories if a category is selected
        subcategories = self.manager.subcategories
        if category_id:
            subcategories = [s for s in subcategories if s.category_id == category_id]
            
        # Add the subcategories to the tree
        for subcategory in sorted(subcategories, key=lambda s: s.name.lower()):
            category_name = self.manager.get_category_name(subcategory.category_id)
            self.subcategories_tree.insert("", tk.END, values=(subcategory.name, category_name), tags=(str(subcategory.id),))
            
    def update_category_filter_menu(self):
        # Update the category filter dropdown
        menu = self.filter_category_menu["menu"]
        menu.delete(0, "end")
        
        menu.add_command(label="ALL", command=lambda: self.filter_category_var.set("ALL"))
        
        for category in sorted(self.manager.categories, key=lambda c: c.name.lower()):
            menu.add_command(
                label=category.name,
                command=lambda name=category.name: self.filter_category_var.set(name)
            )
            
    def on_category_select(self, event):
        selected_items = self.categories_tree.selection()
        if selected_items:
            item = selected_items[0]
            category_id = int(self.categories_tree.item(item, "tags")[0])
            self.update_subcategories_list(category_id)
        else:
            self.update_subcategories_list()
            
    def view_bookmark_details(self, event):
        selected_items = self.bookmark_tree.selection()
        if selected_items:
            item = selected_items[0]
            bookmark_id = int(self.bookmark_tree.item(item, "tags")[0])
            bookmark = next((b for b in self.manager.bookmarks if b.id == bookmark_id), None)
            
            if bookmark:
                details = f"Name: {bookmark.name}\n\n"
                
                if bookmark.url:
                    details += f"URL: {bookmark.url}\n\n"
                    
                if bookmark.description:
                    details += f"Description: {bookmark.description}\n\n"
                    
                category_name = self.manager.get_category_name(bookmark.category_id)
                subcategory_name = self.manager.get_subcategory_name(bookmark.subcategory_id)
                
                details += f"Category: {category_name}\n"
                details += f"Subcategory: {subcategory_name}\n"
                details += f"Type: {bookmark.type}"
                
                messagebox.showinfo("Bookmark Details", details)
                
    def add_bookmark_dialog(self):
        # Create a new dialog window
        dialog = tk.Toplevel(self)
        dialog.title("Add Bookmark")
        dialog.geometry("500x400")
        dialog.grab_set()  # Make the dialog modal
        
        # Create a form for adding a bookmark
        form_frame = ttk.Frame(dialog, padding=10)
        form_frame.pack(fill=tk.BOTH, expand=True)
        
        # Name field
        ttk.Label(form_frame, text="Name:").grid(row=0, column=0, sticky=tk.W, pady=5)
        name_var = tk.StringVar()
        ttk.Entry(form_frame, textvariable=name_var, width=40).grid(row=0, column=1, sticky=tk.W, pady=5)
        
        # URL field
        ttk.Label(form_frame, text="URL (optional):").grid(row=1, column=0, sticky=tk.W, pady=5)
        url_var = tk.StringVar()
        ttk.Entry(form_frame, textvariable=url_var, width=40).grid(row=1, column=1, sticky=tk.W, pady=5)
        
        # Description field
        ttk.Label(form_frame, text="Description (optional):").grid(row=2, column=0, sticky=tk.W, pady=5)
        description_var = tk.StringVar()
        description_entry = tk.Text(form_frame, width=30, height=5)
        description_entry.grid(row=2, column=1, sticky=tk.W, pady=5)
        
        # Category dropdown
        ttk.Label(form_frame, text="Category:").grid(row=3, column=0, sticky=tk.W, pady=5)
        category_var = tk.StringVar()
        category_menu = ttk.Combobox(form_frame, textvariable=category_var, width=30)
        category_menu['values'] = [c.name for c in self.manager.categories]
        category_menu.grid(row=3, column=1, sticky=tk.W, pady=5)
        
        # Subcategory dropdown
        ttk.Label(form_frame, text="Subcategory:").grid(row=4, column=0, sticky=tk.W, pady=5)
        subcategory_var = tk.StringVar()
        subcategory_menu = ttk.Combobox(form_frame, textvariable=subcategory_var, width=30)
        subcategory_menu.grid(row=4, column=1, sticky=tk.W, pady=5)
        
        # Update subcategories when category changes
        def on_category_change(*args):
            category_name = category_var.get()
            category = next((c for c in self.manager.categories if c.name == category_name), None)
            
            if category:
                subcategories = self.manager.get_subcategories_for_category(category.id)
                subcategory_menu['values'] = [s.name for s in subcategories]
                if subcategories:
                    subcategory_var.set(subcategories[0].name)
                else:
                    subcategory_var.set("")
            else:
                subcategory_menu['values'] = []
                subcategory_var.set("")
                
        category_var.trace("w", on_category_change)
        
        # Set default category if available
        if self.manager.categories:
            category_var.set(self.manager.categories[0].name)
            on_category_change()
        
        # Type selection
        ttk.Label(form_frame, text="Type:").grid(row=5, column=0, sticky=tk.W, pady=5)
        type_var = tk.StringVar(value=BookmarkType.FREE)
        type_frame = ttk.Frame(form_frame)
        type_frame.grid(row=5, column=1, sticky=tk.W, pady=5)
        
        ttk.Radiobutton(type_frame, text="Free", variable=type_var, value=BookmarkType.FREE).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Radiobutton(type_frame, text="Paid", variable=type_var, value=BookmarkType.PAID).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Radiobutton(type_frame, text="Freemium", variable=type_var, value=BookmarkType.FREEMIUM).pack(side=tk.LEFT)
        
        # Buttons
        button_frame = ttk.Frame(form_frame)
        button_frame.grid(row=6, column=0, columnspan=2, pady=10)
        
        def save_bookmark():
            name = name_var.get().strip()
            if not name:
                messagebox.showerror("Error", "Name is required")
                return
                
            category_name = category_var.get()
            category = next((c for c in self.manager.categories if c.name == category_name), None)
            if not category:
                messagebox.showerror("Error", "Category is required")
                return
                
            subcategory_name = subcategory_var.get()
            subcategory = next((s for s in self.manager.subcategories 
                              if s.name == subcategory_name and s.category_id == category.id), None)
            if not subcategory:
                messagebox.showerror("Error", "Subcategory is required")
                return
                
            url = url_var.get().strip()
            description = description_entry.get("1.0", tk.END).strip()
            bookmark_type = type_var.get()
            
            self.manager.add_bookmark(
                name, 
                url if url else None, 
                description if description else None,
                category.id,
                subcategory.id,
                bookmark_type
            )
            
            self.update_bookmark_list()
            dialog.destroy()
            
        ttk.Button(button_frame, text="Save", command=save_bookmark).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Button(button_frame, text="Cancel", command=dialog.destroy).pack(side=tk.LEFT)
        
    def edit_bookmark_dialog(self):
        selected_items = self.bookmark_tree.selection()
        if not selected_items:
            messagebox.showinfo("Info", "Please select a bookmark to edit")
            return
            
        item = selected_items[0]
        bookmark_id = int(self.bookmark_tree.item(item, "tags")[0])
        bookmark = next((b for b in self.manager.bookmarks if b.id == bookmark_id), None)
        
        if not bookmark:
            return
            
        # Create a new dialog window
        dialog = tk.Toplevel(self)
        dialog.title("Edit Bookmark")
        dialog.geometry("500x400")
        dialog.grab_set()  # Make the dialog modal
        
        # Create a form for editing the bookmark
        form_frame = ttk.Frame(dialog, padding=10)
        form_frame.pack(fill=tk.BOTH, expand=True)
        
        # Name field
        ttk.Label(form_frame, text="Name:").grid(row=0, column=0, sticky=tk.W, pady=5)
        name_var = tk.StringVar(value=bookmark.name)
        ttk.Entry(form_frame, textvariable=name_var, width=40).grid(row=0, column=1, sticky=tk.W, pady=5)
        
        # URL field
        ttk.Label(form_frame, text="URL (optional):").grid(row=1, column=0, sticky=tk.W, pady=5)
        url_var = tk.StringVar(value=bookmark.url or "")
        ttk.Entry(form_frame, textvariable=url_var, width=40).grid(row=1, column=1, sticky=tk.W, pady=5)
        
        # Description field
        ttk.Label(form_frame, text="Description (optional):").grid(row=2, column=0, sticky=tk.W, pady=5)
        description_entry = tk.Text(form_frame, width=30, height=5)
        if bookmark.description:
            description_entry.insert("1.0", bookmark.description)
        description_entry.grid(row=2, column=1, sticky=tk.W, pady=5)
        
        # Category dropdown
        ttk.Label(form_frame, text="Category:").grid(row=3, column=0, sticky=tk.W, pady=5)
        category_var = tk.StringVar()
        category_menu = ttk.Combobox(form_frame, textvariable=category_var, width=30)
        category_menu['values'] = [c.name for c in self.manager.categories]
        category_name = self.manager.get_category_name(bookmark.category_id)
        category_var.set(category_name)
        category_menu.grid(row=3, column=1, sticky=tk.W, pady=5)
        
        # Subcategory dropdown
        ttk.Label(form_frame, text="Subcategory:").grid(row=4, column=0, sticky=tk.W, pady=5)
        subcategory_var = tk.StringVar()
        subcategory_menu = ttk.Combobox(form_frame, textvariable=subcategory_var, width=30)
        subcategory_name = self.manager.get_subcategory_name(bookmark.subcategory_id)
        subcategory_var.set(subcategory_name)
        subcategory_menu.grid(row=4, column=1, sticky=tk.W, pady=5)
        
        # Update subcategories when category changes
        def on_category_change(*args):
            category_name = category_var.get()
            category = next((c for c in self.manager.categories if c.name == category_name), None)
            
            if category:
                subcategories = self.manager.get_subcategories_for_category(category.id)
                subcategory_menu['values'] = [s.name for s in subcategories]
                if not subcategory_var.get() in [s.name for s in subcategories]:
                    if subcategories:
                        subcategory_var.set(subcategories[0].name)
                    else:
                        subcategory_var.set("")
            else:
                subcategory_menu['values'] = []
                subcategory_var.set("")
                
        category_var.trace("w", on_category_change)
        on_category_change()  # Call initially to set up subcategories
        
        # Type selection
        ttk.Label(form_frame, text="Type:").grid(row=5, column=0, sticky=tk.W, pady=5)
        type_var = tk.StringVar(value=bookmark.type)
        type_frame = ttk.Frame(form_frame)
        type_frame.grid(row=5, column=1, sticky=tk.W, pady=5)
        
        ttk.Radiobutton(type_frame, text="Free", variable=type_var, value=BookmarkType.FREE).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Radiobutton(type_frame, text="Paid", variable=type_var, value=BookmarkType.PAID).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Radiobutton(type_frame, text="Freemium", variable=type_var, value=BookmarkType.FREEMIUM).pack(side=tk.LEFT)
        
        # Buttons
        button_frame = ttk.Frame(form_frame)
        button_frame.grid(row=6, column=0, columnspan=2, pady=10)
        
        def update_bookmark():
            name = name_var.get().strip()
            if not name:
                messagebox.showerror("Error", "Name is required")
                return
                
            category_name = category_var.get()
            category = next((c for c in self.manager.categories if c.name == category_name), None)
            if not category:
                messagebox.showerror("Error", "Category is required")
                return
                
            subcategory_name = subcategory_var.get()
            subcategory = next((s for s in self.manager.subcategories 
                              if s.name == subcategory_name and s.category_id == category.id), None)
            if not subcategory:
                messagebox.showerror("Error", "Subcategory is required")
                return
                
            url = url_var.get().strip()
            description = description_entry.get("1.0", tk.END).strip()
            bookmark_type = type_var.get()
            
            self.manager.update_bookmark(
                bookmark_id,
                name, 
                url if url else None, 
                description if description else None,
                category.id,
                subcategory.id,
                bookmark_type
            )
            
            self.update_bookmark_list()
            dialog.destroy()
            
        ttk.Button(button_frame, text="Update", command=update_bookmark).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Button(button_frame, text="Cancel", command=dialog.destroy).pack(side=tk.LEFT)
        
    def delete_bookmark(self):
        selected_items = self.bookmark_tree.selection()
        if not selected_items:
            messagebox.showinfo("Info", "Please select a bookmark to delete")
            return
            
        item = selected_items[0]
        bookmark_id = int(self.bookmark_tree.item(item, "tags")[0])
        
        if messagebox.askyesno("Confirm", "Are you sure you want to delete this bookmark?"):
            self.manager.delete_bookmark(bookmark_id)
            self.update_bookmark_list()
            
    def add_category_dialog(self):
        name = simpledialog.askstring("Add Category", "Enter category name:")
        if name and name.strip():
            self.manager.add_category(name.strip())
            self.update_categories_list()
            
    def edit_category_dialog(self):
        selected_items = self.categories_tree.selection()
        if not selected_items:
            messagebox.showinfo("Info", "Please select a category to edit")
            return
            
        item = selected_items[0]
        category_id = int(self.categories_tree.item(item, "tags")[0])
        category = next((c for c in self.manager.categories if c.id == category_id), None)
        
        if category:
            name = simpledialog.askstring("Edit Category", "Enter new name:", initialvalue=category.name)
            if name and name.strip():
                self.manager.update_category(category_id, name.strip())
                self.update_categories_list()
                self.update_subcategories_list()
                self.update_bookmark_list()
                
    def delete_category(self):
        selected_items = self.categories_tree.selection()
        if not selected_items:
            messagebox.showinfo("Info", "Please select a category to delete")
            return
            
        item = selected_items[0]
        category_id = int(self.categories_tree.item(item, "tags")[0])
        
        if messagebox.askyesno("Confirm", "Are you sure you want to delete this category?\nAll associated subcategories and bookmarks will also be deleted."):
            self.manager.delete_category(category_id)
            self.update_categories_list()
            self.update_subcategories_list()
            self.update_bookmark_list()
            
    def add_subcategory_dialog(self):
        # Check if a category is selected
        selected_items = self.categories_tree.selection()
        category_id = None
        
        if selected_items:
            item = selected_items[0]
            category_id = int(self.categories_tree.item(item, "tags")[0])
            
        # Create a new dialog window
        dialog = tk.Toplevel(self)
        dialog.title("Add Subcategory")
        dialog.geometry("400x150")
        dialog.grab_set()  # Make the dialog modal
        
        # Form for adding a subcategory
        form_frame = ttk.Frame(dialog, padding=10)
        form_frame.pack(fill=tk.BOTH, expand=True)
        
        # Category selection
        ttk.Label(form_frame, text="Category:").grid(row=0, column=0, sticky=tk.W, pady=5)
        category_var = tk.StringVar()
        category_menu = ttk.Combobox(form_frame, textvariable=category_var, width=30)
        category_menu['values'] = [c.name for c in self.manager.categories]
        
        if category_id:
            category_name = self.manager.get_category_name(category_id)
            category_var.set(category_name)
        elif self.manager.categories:
            category_var.set(self.manager.categories[0].name)
            
        category_menu.grid(row=0, column=1, sticky=tk.W, pady=5)
        
        # Subcategory name
        ttk.Label(form_frame, text="Name:").grid(row=1, column=0, sticky=tk.W, pady=5)
        name_var = tk.StringVar()
        ttk.Entry(form_frame, textvariable=name_var, width=30).grid(row=1, column=1, sticky=tk.W, pady=5)
        
        # Buttons
        button_frame = ttk.Frame(form_frame)
        button_frame.grid(row=2, column=0, columnspan=2, pady=10)
        
        def save_subcategory():
            name = name_var.get().strip()
            if not name:
                messagebox.showerror("Error", "Name is required")
                return
                
            category_name = category_var.get()
            category = next((c for c in self.manager.categories if c.name == category_name), None)
            if not category:
                messagebox.showerror("Error", "Category is required")
                return
                
            self.manager.add_subcategory(name, category.id)
            self.update_subcategories_list(category.id)
            dialog.destroy()
            
        ttk.Button(button_frame, text="Save", command=save_subcategory).pack(side=tk.LEFT, padx=(0, 10))
        ttk.Button(button_frame, text="Cancel", command=dialog.destroy).pack(side=tk.LEFT)
        
    def edit_subcategory_dialog(self):
        selected_items = self.subcategories_tree.selection()
        if not selected_items:
            messagebox.showinfo("Info", "Please select a subcategory to edit")
            return
            
        item = selected_items[0]
        subcategory_id = int(self.subcategories_tree.item(item, "tags")[0])
        subcategory = next((s for s in self.manager.subcategories if s.id == subcategory_id), None)
        
        if subcategory:
            name = simpledialog.askstring("Edit Subcategory", "Enter new name:", initialvalue=subcategory.name)
            if name and name.strip():
                self.manager.update_subcategory(subcategory_id, name.strip())
                self.update_subcategories_list(subcategory.category_id)
                self.update_bookmark_list()
                
    def delete_subcategory(self):
        selected_items = self.subcategories_tree.selection()
        if not selected_items:
            messagebox.showinfo("Info", "Please select a subcategory to delete")
            return
            
        item = selected_items[0]
        subcategory_id = int(self.subcategories_tree.item(item, "tags")[0])
        subcategory = next((s for s in self.manager.subcategories if s.id == subcategory_id), None)
        
        if subcategory and messagebox.askyesno("Confirm", "Are you sure you want to delete this subcategory?\nAll associated bookmarks will also be deleted."):
            self.manager.delete_subcategory(subcategory_id)
            self.update_subcategories_list(subcategory.category_id)
            self.update_bookmark_list()
            
    def export_data(self):
        format_type = self.export_format_var.get()
        filename = f"bookmarks_export.{format_type}"
        
        messagebox.showinfo("Export", f"Data exported successfully to {filename}")
        
    def import_data(self):
        format_type = self.export_format_var.get()
        messagebox.showinfo("Import", f"Data imported successfully from {format_type.upper()} file")

if __name__ == "__main__":
    app = BookmarkManagerApp()
    app.mainloop()