package com.example.babelio.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.babelio.models.Book;
import com.example.babelio.models.Comment;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Shelfy.db";
    private static final int DATABASE_VERSION = 2;

    // Table Names
    public static final String TABLE_BOOKS = "books";
    public static final String TABLE_FAVORITES = "favorites";
    public static final String TABLE_COMMENTS = "comments";

    // Common columns
    public static final String COLUMN_ID = "id";

    // Books Table columns
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_IMAGE_URL = "imageUrl";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_REVIEW_COUNT = "reviewCount";
    public static final String COLUMN_GENRE = "genre";
    public static final String COLUMN_YEAR = "yearPublished";

    // Comments Table columns (for Table TABLE_COMMENTS)
    public static final String COLUMN_BOOK_ID = "bookId";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_USER_NAME = "userName";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_COMMENT_RATING = "commentRating";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    // Table Create Statements
    private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TITLE + " TEXT,"
            + COLUMN_AUTHOR + " TEXT,"
            + COLUMN_DESCRIPTION + " TEXT,"
            + COLUMN_IMAGE_URL + " TEXT,"
            + COLUMN_RATING + " REAL,"
            + COLUMN_REVIEW_COUNT + " INTEGER,"
            + COLUMN_GENRE + " TEXT,"
            + COLUMN_YEAR + " INTEGER"
            + ")";

    private static final String CREATE_TABLE_FAVORITES = "CREATE TABLE " + TABLE_FAVORITES + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "bookId TEXT,"
            + "userId TEXT"
            + ")";

    private static final String CREATE_TABLE_COMMENTS = "CREATE TABLE " + TABLE_COMMENTS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_BOOK_ID + " INTEGER,"
            + COLUMN_USER_ID + " TEXT,"
            + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_TEXT + " TEXT,"
            + COLUMN_COMMENT_RATING + " REAL,"
            + COLUMN_TIMESTAMP + " INTEGER"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_BOOKS);
        db.execSQL(CREATE_TABLE_FAVORITES);
        db.execSQL(CREATE_TABLE_COMMENTS);
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMMENTS);
        onCreate(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        String[] titles = {
            "The Great Gatsby", "To Kill a Mockingbird", "1984", "Pride and Prejudice", "The Hobbit",
            "Moby Dick", "The Catcher in the Rye", "Dune", "The Alchemist", "Harry Potter and the Sorcerer's Stone",
            "The Da Vinci Code", "The Hunger Games", "The Martian", "Gone Girl", "Becoming",
            "Sapiens", "The Silent Patient", "Where the Crawdads Sing", "Project Hail Mary", "It Ends With Us"
        };

        String[] authors = {
            "F. Scott Fitzgerald", "Harper Lee", "George Orwell", "Jane Austen", "J.R.R. Tolkien",
            "Herman Melville", "J.D. Salinger", "Frank Herbert", "Paulo Coelho", "J.K. Rowling",
            "Dan Brown", "Suzanne Collins", "Andy Weir", "Gillian Flynn", "Michelle Obama",
            "Yuval Noah Harari", "Alex Michaelides", "Delia Owens", "Andy Weir", "Colleen Hoover"
        };

        String[] descriptions = {
            "A tragic story of love, wealth, and obsession in the Jazz Age.",
            "A gripping tale of racial injustice and childhood innocence in the American South.",
            "A dystopian vision of a totalitarian future where Big Brother watches all.",
            "A witty romantic drama about love, class, and social expectations.",
            "A fantasy adventure of Bilbo Baggins and his journey to the Lonely Mountain.",
            "An obsessive captain hunts a giant white whale across the seas.",
            "A rebellious teenager navigates alienation and loss in New York.",
            "Epic sci-fi saga of politics, religion, and desert planets.",
            "A shepherd boy follows his dreams across the Egyptian desert.",
            "A boy discovers magic, friendship, and a dark enemy at Hogwarts.",
            "A symbologist unravels a religious mystery hidden in art.",
            "Teens fight to the death in a brutal televised arena.",
            "An astronaut is left stranded on Mars and must survive.",
            "A twisty psychological thriller about a missing wife.",
            "Memoir of the former First Lady, from childhood to the White House.",
            "A brief history of humankind, from stone age to modern day.",
            "A woman shoots her husband and never speaks again — a shocking twist awaits.",
            "A young girl grows up alone in marshlands and becomes a murder suspect.",
            "A lone astronaut must save humanity with science and grit.",
            "A poignant romance about love, strength, and hard choices."
        };

        String[] imageUrls = {
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553273/the_great_gatsby_krbora.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553279/to_kill_a_mockingbird_yrivmb.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553278/1984_mkpjjc.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553273/pride_and_prejudice_zu8ith.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553285/the_hobbit_mnemq6.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553267/moby_dick_xqe7cx.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553275/catcher_in_the_rye_zel0ne.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553271/dune_dxitxw.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777552826/the_great_gatsby_cypcit.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553268/harry_potter_1_mizqhn.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553271/da_vinci_code_u8mzan.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553268/hunger_games_hppa4d.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553281/the_martian_bpjc2j.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553270/gone_girl_lf9xxu.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553276/becoming_mzraos.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553265/sapiens_yzbeyn.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553264/silent_patient_cszjjk.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553274/crawdads_sing_t3helw.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553265/project_hail_mary_rereav.jpg",
            "https://res.cloudinary.com/deptrtc9n/image/upload/v1777553267/it_ends_with_us_bswku2.jpg"
        };

        double[] ratings = {4.2, 4.8, 4.7, 4.6, 4.9, 3.9, 4.1, 4.8, 4.3, 4.9, 3.8, 4.5, 4.7, 4.2, 4.8, 4.6, 4.4, 4.7, 4.9, 4.5};
        int[] reviewCounts = {1240, 2100, 1980, 1750, 2600, 980, 1500, 1900, 3100, 5000, 2300, 2900, 1850, 2100, 1400, 1700, 2200, 2500, 1650, 3800};
        String[] genres = {"Classic", "Fiction", "Dystopian", "Romance", "Fantasy", "Adventure", "Coming-of-age", "Science Fiction", "Philosophical", "Fantasy", "Thriller", "Dystopian", "Science Fiction", "Thriller", "Biography", "History", "Psychological Thriller", "Literary Fiction", "Science Fiction", "Romance"};
        int[] years = {1925, 1960, 1949, 1813, 1937, 1851, 1951, 1965, 1988, 1997, 2003, 2008, 2011, 2012, 2018, 2011, 2019, 2018, 2021, 2016};

        for (int i = 0; i < titles.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TITLE, titles[i]);
            values.put(COLUMN_AUTHOR, authors[i]);
            values.put(COLUMN_DESCRIPTION, descriptions[i]);
            values.put(COLUMN_IMAGE_URL, imageUrls[i]);
            values.put(COLUMN_RATING, ratings[i]);
            values.put(COLUMN_REVIEW_COUNT, reviewCounts[i]);
            values.put(COLUMN_GENRE, genres[i]);
            values.put(COLUMN_YEAR, years[i]);
            db.insert(TABLE_BOOKS, null, values);
        }
    }

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BOOKS, null);

        if (cursor.moveToFirst()) {
            do {
                Book book = new Book(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                        (float) cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REVIEW_COUNT))
                );
                // Genre and Year are in Book model? Let's check Book.java
                book.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                books.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return books;
    }

    public void addToFavorites(String bookId, String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("bookId", bookId);
        values.put("userId", userId);
        db.insert(TABLE_FAVORITES, null, values);
        db.close();
    }

    public void removeFromFavorites(String bookId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES, "bookId = ?", new String[]{bookId});
        db.close();
    }

    public boolean isFavorite(String bookId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null, "bookId = ?", new String[]{bookId}, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public List<Book> getFavoriteBooks() {
        List<Book> books = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT b.* FROM " + TABLE_BOOKS + " b " +
                "INNER JOIN " + TABLE_FAVORITES + " f ON b." + COLUMN_ID + " = f.bookId";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                Book book = new Book(
                        String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUTHOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                        (float) cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_RATING)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REVIEW_COUNT))
                );
                book.setGenre(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GENRE)));
                books.add(book);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return books;
    }

    // Comment Methods
    public void addComment(Comment comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BOOK_ID, comment.getBookId());
        values.put(COLUMN_USER_ID, comment.getUserId());
        values.put(COLUMN_USER_NAME, comment.getUserName());
        values.put(COLUMN_TEXT, comment.getText());
        values.put(COLUMN_COMMENT_RATING, comment.getRating());
        values.put(COLUMN_TIMESTAMP, comment.getTimestamp());

        db.insert(TABLE_COMMENTS, null, values);
        db.close();
    }

    public List<Comment> getCommentsForBook(String bookId) {
        List<Comment> comments = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMMENTS, null, COLUMN_BOOK_ID + " = ?", new String[]{bookId}, null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Comment comment = new Comment();
                comment.setCommentId(String.valueOf(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))));
                comment.setBookId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOOK_ID)));
                comment.setUserId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ID)));
                comment.setUserName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME)));
                comment.setText(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TEXT)));
                comment.setRating(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_COMMENT_RATING)));
                comment.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                comments.add(comment);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return comments;
    }
}
