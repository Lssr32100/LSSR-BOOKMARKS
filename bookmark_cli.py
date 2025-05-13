#!/usr/bin/env python3
"""
Bookmark Manager CLI

This script provides a command-line interface for the Bookmark Manager application.
"""

import os
import json
import sys
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

class BookmarkCLI:
    def __init__(self):
        self.manager = BookmarkManager()
        self.running = True
        self.current_menu = "main"
        
    def clear_screen(self):
        os.system('cls' if os.name == 'nt' else 'clear')
        
    def print_header(self, title):
        self.clear_screen()
        print("=" * 60)
        print(f"{title:^60}")
        print("=" * 60)
        print()
        
    def wait_for_key(self):
        input("\nPress Enter to continue...")
        
    def show_main_menu(self):
        self.print_header("BOOKMARK MANAGER")
        print("1. View All Bookmarks")
        print("2. Search Bookmarks")
        print("3. Add New Bookmark")
        print("4. Manage Categories")
        print("5. Manage Subcategories")
        print("6. Import/Export")
        print("7. Exit")
        
        choice = input("\nEnter your choice (1-7): ")
        
        if choice == "1":
            self.view_all_bookmarks()
        elif choice == "2":
            self.search_bookmarks()
        elif choice == "3":
            self.add_bookmark()
        elif choice == "4":
            self.manage_categories()
        elif choice == "5":
            self.manage_subcategories()
        elif choice == "6":
            self.import_export_menu()
        elif choice == "7":
            self.running = False
        else:
            print("Invalid choice. Please try again.")
            self.wait_for_key()
            
    def view_all_bookmarks(self):
        self.print_header("ALL BOOKMARKS")
        
        # Sort options
        print("Sort by:")
        print("1. Name (A-Z)")
        print("2. Name (Z-A)")
        print("3. Category")
        print("4. Type")
        print("5. Back to Main Menu")
        
        choice = input("\nEnter sorting choice (1-5): ")
        
        if choice == "5":
            return
            
        bookmarks = self.manager.bookmarks
        
        if choice == "1":
            bookmarks = sorted(bookmarks, key=lambda b: b.name.lower())
        elif choice == "2":
            bookmarks = sorted(bookmarks, key=lambda b: b.name.lower(), reverse=True)
        elif choice == "3":
            bookmarks = sorted(bookmarks, key=lambda b: self.manager.get_category_name(b.category_id).lower())
        elif choice == "4":
            bookmarks = sorted(bookmarks, key=lambda b: b.type)
            
        self.display_bookmarks(bookmarks)
        
    def display_bookmarks(self, bookmarks):
        self.print_header("BOOKMARK LIST")
        
        if not bookmarks:
            print("No bookmarks found.")
            self.wait_for_key()
            return
            
        # Print table header
        print(f"{'ID':<4} {'Name':<30} {'Category':<15} {'Type':<10}")
        print("-" * 60)
        
        for bookmark in bookmarks:
            category_name = self.manager.get_category_name(bookmark.category_id)
            print(f"{bookmark.id:<4} {bookmark.name[:28]:<30} {category_name[:13]:<15} {bookmark.type:<10}")
            
        print("\nOptions:")
        print("1. View Bookmark Details")
        print("2. Edit Bookmark")
        print("3. Delete Bookmark")
        print("4. Back to Main Menu")
        
        choice = input("\nEnter choice (1-4): ")
        
        if choice == "1":
            self.view_bookmark_details(bookmarks)
        elif choice == "2":
            self.edit_bookmark(bookmarks)
        elif choice == "3":
            self.delete_bookmark(bookmarks)
        elif choice == "4":
            return
        else:
            print("Invalid choice.")
            self.wait_for_key()
            
    def view_bookmark_details(self, bookmarks):
        bookmark_id = input("Enter the ID of the bookmark to view: ")
        try:
            bookmark_id = int(bookmark_id)
            bookmark = next((b for b in bookmarks if b.id == bookmark_id), None)
            
            if bookmark:
                self.print_header(f"BOOKMARK DETAILS: {bookmark.name}")
                
                print(f"Name: {bookmark.name}")
                if bookmark.url:
                    print(f"URL: {bookmark.url}")
                if bookmark.description:
                    print(f"Description: {bookmark.description}")
                    
                category_name = self.manager.get_category_name(bookmark.category_id)
                subcategory_name = self.manager.get_subcategory_name(bookmark.subcategory_id)
                
                print(f"Category: {category_name}")
                print(f"Subcategory: {subcategory_name}")
                print(f"Type: {bookmark.type}")
                
                self.wait_for_key()
            else:
                print("Bookmark not found.")
                self.wait_for_key()
        except ValueError:
            print("Invalid ID. Please enter a number.")
            self.wait_for_key()
            
    def search_bookmarks(self):
        self.print_header("SEARCH BOOKMARKS")
        
        query = input("Enter search term: ")
        results = self.manager.search_bookmarks(query)
        
        self.display_bookmarks(results)
        
    def add_bookmark(self):
        self.print_header("ADD NEW BOOKMARK")
        
        name = input("Enter bookmark name: ")
        if not name.strip():
            print("Name cannot be empty.")
            self.wait_for_key()
            return
            
        url = input("Enter URL (optional): ")
        description = input("Enter description (optional): ")
        
        # Select category
        self.print_header("SELECT CATEGORY")
        
        if not self.manager.categories:
            print("No categories available. Please create a category first.")
            self.wait_for_key()
            return
            
        print("Available categories:")
        for category in self.manager.categories:
            print(f"{category.id}. {category.name}")
            
        category_id = input("\nEnter category ID: ")
        try:
            category_id = int(category_id)
            category = next((c for c in self.manager.categories if c.id == category_id), None)
            
            if not category:
                print("Invalid category ID.")
                self.wait_for_key()
                return
                
            # Select subcategory
            subcategories = self.manager.get_subcategories_for_category(category_id)
            
            if not subcategories:
                print("No subcategories available for this category. Please create a subcategory first.")
                self.wait_for_key()
                return
                
            self.print_header("SELECT SUBCATEGORY")
            print("Available subcategories:")
            
            for subcategory in subcategories:
                print(f"{subcategory.id}. {subcategory.name}")
                
            subcategory_id = input("\nEnter subcategory ID: ")
            try:
                subcategory_id = int(subcategory_id)
                subcategory = next((s for s in subcategories if s.id == subcategory_id), None)
                
                if not subcategory:
                    print("Invalid subcategory ID.")
                    self.wait_for_key()
                    return
                    
                # Select type
                self.print_header("SELECT TYPE")
                print("Available types:")
                print(f"1. {BookmarkType.FREE}")
                print(f"2. {BookmarkType.PAID}")
                print(f"3. {BookmarkType.FREEMIUM}")
                
                type_choice = input("\nEnter type (1-3): ")
                
                if type_choice == "1":
                    bookmark_type = BookmarkType.FREE
                elif type_choice == "2":
                    bookmark_type = BookmarkType.PAID
                elif type_choice == "3":
                    bookmark_type = BookmarkType.FREEMIUM
                else:
                    print("Invalid type choice.")
                    self.wait_for_key()
                    return
                    
                # Create the bookmark
                self.manager.add_bookmark(
                    name, 
                    url if url.strip() else None, 
                    description if description.strip() else None,
                    category_id,
                    subcategory_id,
                    bookmark_type
                )
                
                print("\nBookmark added successfully!")
                self.wait_for_key()
                
            except ValueError:
                print("Invalid subcategory ID. Please enter a number.")
                self.wait_for_key()
                
        except ValueError:
            print("Invalid category ID. Please enter a number.")
            self.wait_for_key()
            
    def edit_bookmark(self, bookmarks):
        bookmark_id = input("Enter the ID of the bookmark to edit: ")
        try:
            bookmark_id = int(bookmark_id)
            bookmark = next((b for b in bookmarks if b.id == bookmark_id), None)
            
            if bookmark:
                self.print_header(f"EDIT BOOKMARK: {bookmark.name}")
                
                name = input(f"Enter new name [{bookmark.name}]: ")
                name = name if name.strip() else bookmark.name
                
                url = input(f"Enter new URL [{bookmark.url or ''}]: ")
                url = url if url.strip() else bookmark.url
                
                description = input(f"Enter new description [{bookmark.description or ''}]: ")
                description = description if description.strip() else bookmark.description
                
                # Keep current category and subcategory by default
                category_id = bookmark.category_id
                subcategory_id = bookmark.subcategory_id
                
                # Ask if user wants to change category
                change_category = input("Change category? (y/n): ").lower() == 'y'
                
                if change_category:
                    # Select new category
                    print("\nAvailable categories:")
                    for category in self.manager.categories:
                        print(f"{category.id}. {category.name}")
                        
                    category_input = input("\nEnter category ID: ")
                    try:
                        category_id = int(category_input)
                        category = next((c for c in self.manager.categories if c.id == category_id), None)
                        
                        if not category:
                            print("Invalid category ID. Keeping current category.")
                            category_id = bookmark.category_id
                        else:
                            # Select new subcategory
                            subcategories = self.manager.get_subcategories_for_category(category_id)
                            
                            if not subcategories:
                                print("No subcategories available for this category. Keeping current subcategory.")
                                subcategory_id = bookmark.subcategory_id
                            else:
                                print("\nAvailable subcategories:")
                                for subcategory in subcategories:
                                    print(f"{subcategory.id}. {subcategory.name}")
                                    
                                subcategory_input = input("\nEnter subcategory ID: ")
                                try:
                                    subcategory_id = int(subcategory_input)
                                    subcategory = next((s for s in subcategories if s.id == subcategory_id), None)
                                    
                                    if not subcategory:
                                        print("Invalid subcategory ID. Keeping current subcategory.")
                                        subcategory_id = bookmark.subcategory_id
                                except ValueError:
                                    print("Invalid subcategory ID. Keeping current subcategory.")
                                    subcategory_id = bookmark.subcategory_id
                    except ValueError:
                        print("Invalid category ID. Keeping current category.")
                        category_id = bookmark.category_id
                        subcategory_id = bookmark.subcategory_id
                
                # Ask if user wants to change type
                current_type = bookmark.type
                change_type = input(f"Change type? Current: {current_type} (y/n): ").lower() == 'y'
                
                if change_type:
                    print("\nAvailable types:")
                    print(f"1. {BookmarkType.FREE}")
                    print(f"2. {BookmarkType.PAID}")
                    print(f"3. {BookmarkType.FREEMIUM}")
                    
                    type_choice = input("\nEnter type (1-3): ")
                    
                    if type_choice == "1":
                        bookmark_type = BookmarkType.FREE
                    elif type_choice == "2":
                        bookmark_type = BookmarkType.PAID
                    elif type_choice == "3":
                        bookmark_type = BookmarkType.FREEMIUM
                    else:
                        print("Invalid type choice. Keeping current type.")
                        bookmark_type = current_type
                else:
                    bookmark_type = current_type
                
                # Update the bookmark
                self.manager.update_bookmark(
                    bookmark_id,
                    name, 
                    url, 
                    description,
                    category_id,
                    subcategory_id,
                    bookmark_type
                )
                
                print("\nBookmark updated successfully!")
                self.wait_for_key()
            else:
                print("Bookmark not found.")
                self.wait_for_key()
        except ValueError:
            print("Invalid ID. Please enter a number.")
            self.wait_for_key()
            
    def delete_bookmark(self, bookmarks):
        bookmark_id = input("Enter the ID of the bookmark to delete: ")
        try:
            bookmark_id = int(bookmark_id)
            bookmark = next((b for b in bookmarks if b.id == bookmark_id), None)
            
            if bookmark:
                confirm = input(f"Are you sure you want to delete '{bookmark.name}'? (y/n): ").lower()
                
                if confirm == 'y':
                    self.manager.delete_bookmark(bookmark_id)
                    print("\nBookmark deleted successfully!")
                else:
                    print("\nDeletion cancelled.")
                    
                self.wait_for_key()
            else:
                print("Bookmark not found.")
                self.wait_for_key()
        except ValueError:
            print("Invalid ID. Please enter a number.")
            self.wait_for_key()
            
    def manage_categories(self):
        while True:
            self.print_header("MANAGE CATEGORIES")
            
            # Display all categories
            if not self.manager.categories:
                print("No categories available.")
            else:
                print("Available categories:")
                print(f"{'ID':<4} {'Name':<30}")
                print("-" * 35)
                
                for category in self.manager.categories:
                    print(f"{category.id:<4} {category.name:<30}")
            
            print("\nOptions:")
            print("1. Add Category")
            print("2. Edit Category")
            print("3. Delete Category")
            print("4. Back to Main Menu")
            
            choice = input("\nEnter choice (1-4): ")
            
            if choice == "1":
                self.add_category()
            elif choice == "2":
                self.edit_category()
            elif choice == "3":
                self.delete_category()
            elif choice == "4":
                break
            else:
                print("Invalid choice.")
                self.wait_for_key()
                
    def add_category(self):
        self.print_header("ADD CATEGORY")
        
        name = input("Enter category name: ")
        if not name.strip():
            print("Name cannot be empty.")
            self.wait_for_key()
            return
            
        category = self.manager.add_category(name.strip())
        print(f"\nCategory '{category.name}' added successfully!")
        self.wait_for_key()
        
    def edit_category(self):
        if not self.manager.categories:
            print("No categories available.")
            self.wait_for_key()
            return
            
        category_id = input("Enter the ID of the category to edit: ")
        try:
            category_id = int(category_id)
            category = next((c for c in self.manager.categories if c.id == category_id), None)
            
            if category:
                name = input(f"Enter new name [{category.name}]: ")
                if name.strip():
                    self.manager.update_category(category_id, name.strip())
                    print(f"\nCategory updated successfully!")
                else:
                    print("\nCategory name unchanged.")
                    
                self.wait_for_key()
            else:
                print("Category not found.")
                self.wait_for_key()
        except ValueError:
            print("Invalid ID. Please enter a number.")
            self.wait_for_key()
            
    def delete_category(self):
        if not self.manager.categories:
            print("No categories available.")
            self.wait_for_key()
            return
            
        category_id = input("Enter the ID of the category to delete: ")
        try:
            category_id = int(category_id)
            category = next((c for c in self.manager.categories if c.id == category_id), None)
            
            if category:
                confirm = input(f"Are you sure you want to delete '{category.name}'? All associated subcategories and bookmarks will also be deleted. (y/n): ").lower()
                
                if confirm == 'y':
                    self.manager.delete_category(category_id)
                    print("\nCategory deleted successfully!")
                else:
                    print("\nDeletion cancelled.")
                    
                self.wait_for_key()
            else:
                print("Category not found.")
                self.wait_for_key()
        except ValueError:
            print("Invalid ID. Please enter a number.")
            self.wait_for_key()
            
    def manage_subcategories(self):
        while True:
            self.print_header("MANAGE SUBCATEGORIES")
            
            # Display all categories for selection
            if not self.manager.categories:
                print("No categories available. Please create a category first.")
                self.wait_for_key()
                return
                
            print("Select a category to manage its subcategories:")
            print(f"{'ID':<4} {'Name':<30}")
            print("-" * 35)
            
            for category in self.manager.categories:
                print(f"{category.id:<4} {category.name:<30}")
                
            print("\n0. Back to Main Menu")
            
            category_id = input("\nEnter category ID (or 0 to go back): ")
            
            if category_id == "0":
                break
                
            try:
                category_id = int(category_id)
                category = next((c for c in self.manager.categories if c.id == category_id), None)
                
                if category:
                    self.manage_subcategories_for_category(category)
                else:
                    print("Category not found.")
                    self.wait_for_key()
            except ValueError:
                print("Invalid ID. Please enter a number.")
                self.wait_for_key()
                
    def manage_subcategories_for_category(self, category):
        while True:
            self.print_header(f"SUBCATEGORIES FOR: {category.name}")
            
            # Display all subcategories for this category
            subcategories = self.manager.get_subcategories_for_category(category.id)
            
            if not subcategories:
                print("No subcategories available for this category.")
            else:
                print(f"{'ID':<4} {'Name':<30}")
                print("-" * 35)
                
                for subcategory in subcategories:
                    print(f"{subcategory.id:<4} {subcategory.name:<30}")
            
            print("\nOptions:")
            print("1. Add Subcategory")
            print("2. Edit Subcategory")
            print("3. Delete Subcategory")
            print("4. Back to Categories")
            
            choice = input("\nEnter choice (1-4): ")
            
            if choice == "1":
                self.add_subcategory(category)
            elif choice == "2":
                self.edit_subcategory(subcategories)
            elif choice == "3":
                self.delete_subcategory(subcategories)
            elif choice == "4":
                break
            else:
                print("Invalid choice.")
                self.wait_for_key()
                
    def add_subcategory(self, category):
        self.print_header(f"ADD SUBCATEGORY TO: {category.name}")
        
        name = input("Enter subcategory name: ")
        if not name.strip():
            print("Name cannot be empty.")
            self.wait_for_key()
            return
            
        subcategory = self.manager.add_subcategory(name.strip(), category.id)
        print(f"\nSubcategory '{subcategory.name}' added successfully!")
        self.wait_for_key()
        
    def edit_subcategory(self, subcategories):
        if not subcategories:
            print("No subcategories available.")
            self.wait_for_key()
            return
            
        subcategory_id = input("Enter the ID of the subcategory to edit: ")
        try:
            subcategory_id = int(subcategory_id)
            subcategory = next((s for s in subcategories if s.id == subcategory_id), None)
            
            if subcategory:
                name = input(f"Enter new name [{subcategory.name}]: ")
                if name.strip():
                    self.manager.update_subcategory(subcategory_id, name.strip())
                    print(f"\nSubcategory updated successfully!")
                else:
                    print("\nSubcategory name unchanged.")
                    
                self.wait_for_key()
            else:
                print("Subcategory not found.")
                self.wait_for_key()
        except ValueError:
            print("Invalid ID. Please enter a number.")
            self.wait_for_key()
            
    def delete_subcategory(self, subcategories):
        if not subcategories:
            print("No subcategories available.")
            self.wait_for_key()
            return
            
        subcategory_id = input("Enter the ID of the subcategory to delete: ")
        try:
            subcategory_id = int(subcategory_id)
            subcategory = next((s for s in subcategories if s.id == subcategory_id), None)
            
            if subcategory:
                confirm = input(f"Are you sure you want to delete '{subcategory.name}'? All associated bookmarks will also be deleted. (y/n): ").lower()
                
                if confirm == 'y':
                    self.manager.delete_subcategory(subcategory_id)
                    print("\nSubcategory deleted successfully!")
                else:
                    print("\nDeletion cancelled.")
                    
                self.wait_for_key()
            else:
                print("Subcategory not found.")
                self.wait_for_key()
        except ValueError:
            print("Invalid ID. Please enter a number.")
            self.wait_for_key()
            
    def import_export_menu(self):
        while True:
            self.print_header("IMPORT/EXPORT DATA")
            
            print("Options:")
            print("1. Export Data (JSON)")
            print("2. Import Data (JSON)")
            print("3. Back to Main Menu")
            
            choice = input("\nEnter choice (1-3): ")
            
            if choice == "1":
                # Export to JSON
                print("\nExporting data...")
                # We're already saving to bookmark_data.json
                print("Data exported successfully to 'bookmark_data.json'!")
                self.wait_for_key()
            elif choice == "2":
                # Import from JSON
                confirm = input("This will replace all current data. Continue? (y/n): ").lower()
                
                if confirm == 'y':
                    print("\nImporting data...")
                    try:
                        # We're reloading the same data for this example
                        self.manager.load_data()
                        print("Data imported successfully!")
                    except Exception as e:
                        print(f"Error importing data: {e}")
                    self.wait_for_key()
                else:
                    print("\nImport cancelled.")
                    self.wait_for_key()
            elif choice == "3":
                break
            else:
                print("Invalid choice.")
                self.wait_for_key()
                
    def run(self):
        while self.running:
            self.show_main_menu()
        
        print("\nThank you for using Bookmark Manager!")

if __name__ == "__main__":
    cli = BookmarkCLI()
    cli.run()