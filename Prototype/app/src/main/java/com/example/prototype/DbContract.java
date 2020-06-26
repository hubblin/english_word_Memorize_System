package com.example.prototype;



public final class DbContract {
    private DbContract(){

    }

    public static class DbEntry{
        public static final String _ID = "_id";
        public static final String _COUNT = "_count";
        public static final String TABLE_NAME = "wordbook";
        public static final String WORDBOOK_NAME_TITLE = "title";
        public static final String WORDBOOK_NAME_SUBTITLE = "subtitle";
        public static final String PROBLEM_COUNT = "problem_count";
    }

    public static class DbEntry2{
        public static final String _ID = "_id";
        public static final String TABLE_NAME = "word";
        public static final String WORD_SPELL = "spell";
        public static final String WORD_MEAN1 = "mean1";
        public static final String WORD_MEAN2 = "mean2";
        public static final String WORD_MEAN3 = "mean3";
        public static final String WORD_MEAN4 = "mean4";
        public static final String WORD_MEAN5 = "mean5";
        public static final String WORDBOOK_ID = "wordbook_id";
        public static final String DATE = "date";
        public static final String CORRECT_ANSWER = "correct_answer";
    }


}
