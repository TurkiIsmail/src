package com.example.babelio.network;

import java.util.List;

/**
 * Response model for Open Library book search
 */
public class BookSearchResponse {
    public List<BookResult> docs;
    
    /**
     * Individual book result from Open Library
     */
    public static class BookResult {
        public String key;
        public String title;
        public List<String> author_name;
        public String first_publish_year;
        public List<String> isbn;
        public String publisher;
        public Long cover_i;  // Cover edition ID for better image reliability
        public List<Long> cover_edition_key;  // Alternative cover IDs
        
        /**
         * Get cover image URL with multiple fallback options
         * 1. Try cover_i (most reliable)
         * 2. Try ISBN
         * 3. Use placeholder
         */
        public String getCoverUrl() {
            // Try primary cover ID first
            if (cover_i != null && cover_i > 0) {
                return "https://covers.openlibrary.org/b/id/" + cover_i + "-M.jpg";
            }
            
            // Try ISBN
            if (isbn != null && !isbn.isEmpty()) {
                return "https://covers.openlibrary.org/b/isbn/" + isbn.get(0) + "-M.jpg";
            }
            
            // Try cover edition key if available
            if (cover_edition_key != null && !cover_edition_key.isEmpty()) {
                return "https://covers.openlibrary.org/b/id/" + cover_edition_key.get(0) + "-M.jpg";
            }
            
            // Fallback to placeholder
            return "https://via.placeholder.com/200x300?text=" + (title != null ? title.substring(0, Math.min(title.length(), 10)).replace(" ", "+") : "Book");
        }
        
        /**
         * Get first author name or "Unknown"
         */
        public String getAuthorName() {
            if (author_name != null && !author_name.isEmpty()) {
                return author_name.get(0);
            }
            return "Unknown";
        }
        
        /**
         * Get unique ID from key (e.g., /works/OL45883W -> OL45883W)
         */
        public String getUniqueId() {
            if (key != null) {
                return key.replace("/works/", "").replace("/", "");
            }
            return String.valueOf(title.hashCode());
        }
    }
}
